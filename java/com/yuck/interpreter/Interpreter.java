package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.YCodeClass;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Interpreter {
  public static final boolean DEBUG = false;
  public static final List<Path> YUCK_PATHS = new ArrayList<>();

  public static void addPath(Path path) {
    YUCK_PATHS.add(path);
  }

  static {
    // Add the current working directory
    addPath(Paths.get(".").toAbsolutePath());
    // Add the environment
    String yuck_paths = System.getenv("YUCK_PATHS");
    if (yuck_paths != null) {
      for (String string : Splitter.on(":").omitEmptyStrings().trimResults().split(yuck_paths)) {
        Path path = Paths.get(string).toAbsolutePath();
        addPath(path);
      }
    }
  }

  public static Optional<File> lookupPackage(String pkg) {
    String file = pkg.replace('.', '/') + ".yuck";
    for (Path path : YUCK_PATHS) {
      Path resolve = path.resolve(file);
      if (Files.exists(resolve)) {
        return Optional.of(resolve.toFile());
      }
    }
    return Optional.empty();
  }

  public static InterpreterContext interpret(YCodeFunction function, InterpreterContext context) {
    int pc = 0;
    if (DEBUG) {
      function.instructions.forEach(System.err::println);
      System.err.println();
    }
    while (true) {
      Preconditions.checkArgument(pc >= 0);
      if (pc >= function.instructions.size()) {
        // PUSH a nil for falling off of the function
        return context;
      }
      Instruction instruction = function.instructions.get(pc);
      int next = pc + 1;
      switch (instruction.opcode) {
        case EQ: {
          Preconditions.checkArgument(
              context.depth() >= 2,
              "Stack is not deep enough at " + instruction);
          YuckObject right = context.pop();
          YuckObject left = context.pop();
          context.push(new YuckBoolean(left.equals(right), context));
          break;
        }
        case ADD:
        case SUB:
        case MUL:
        case DIV:
        case MOD:
        case LE:
        case LT:
        case POW: {
          Preconditions.checkArgument(
              context.depth() >= 2,
              "Stack is not deep enough at " + instruction);
          YuckObject right = context.pop();
          YuckObject left = context.pop();
          context.push(left.binary(instruction.opcode, right));
          break;
        }
        case JUMPZ:
          YuckObject value = context.pop();
          if (!value.isFilled()) {
            next = instruction.getArgument();
          }
          break;
        case GOTO:
          next = instruction.getArgument();
          break;
        case RETURN:
          Preconditions.checkArgument(context.depth() == 1);
          return context;
        case LOAD_CONST:
          Object constant = function.constants.inverse().get(instruction.getArgument());
          YuckObject yuckConstant = YuckObject.translate(constant, context);
          context.push(yuckConstant);
          break;
        case NOP:
          break;
        case STORE_LOCAL:
          context.add(instruction.getArgument(), function.locals.inverse().get(instruction.getArgument()), context.pop());
          break;
        case LOAD_LOCAL:
          context.push(context.get(instruction.getArgument()));
          break;
        case CLOSURE: {
          YCodeFunction closure = function.functions.inverse().get(instruction.getArgument());
          YuckFunction yuckFunction = new YuckFunction(closure, context);
          context.push(yuckFunction);
          break;
        }
        case CALL: {
          List<YuckObject> arguments = new ArrayList<>();
          for (int i = 0; i < instruction.getArgument() - 1; i++) {
            arguments.add(0, context.pop());
          }
          YuckObject callable = context.pop();
          InterpreterContext nextContext = new InterpreterContext(callable.context, null);

          if (callable instanceof YuckFunction) {
            YuckFunction closure = (YuckFunction) callable;
            int local = 0;
            for (YuckObject argument : arguments) {
              nextContext.add(local, closure.function.locals.inverse().get(local), argument);
              local++;
            }
            InterpreterContext result = interpret(closure.function, nextContext);
            context.push(result.pop());
          } else if (callable instanceof NativeFunction) {
            NativeFunction closure = (NativeFunction) callable;
            int local = 0;
            for (YuckObject argument : arguments) {
              nextContext.add(local, "arg" + local, argument);
              local++;
            }
            context.push(closure.function.apply(nextContext));
          } else {
            System.err.println(instruction + " : " + callable);
            throw new NotImplementedException();
          }
          break;
        }
        case LOAD_UP: {
          String upvalue = function.upvalues.inverse().get(instruction.getArgument());
          Preconditions.checkArgument(context.previous.isPresent(), String.format("Cannot get an upvalue (%s) from the root context.", upvalue));
          context.push(context.previous.get().lookup(upvalue));
          break;
        }
        case STORE_UP: {
          String upvalue = function.upvalues.inverse().get(instruction.getArgument());
          Preconditions.checkArgument(context.previous.isPresent(), String.format("Cannot set an upvalue (%s) from the root context.", upvalue));
          YuckObject top = context.pop();
          context.previous.get().storeup(upvalue, top);
          break;
        }
        case DUP: {
          context.push(context.stack.getLast());
          break;
        }
        case NIL: {
          context.push(new YuckNil(context));
          break;
        }
        case TO_RANGE: {
          YuckObject right = context.pop();
          YuckObject left = context.pop();
          YuckList result = new YuckList(context);
          if (left instanceof YuckInteger && right instanceof YuckInteger) {
            for (int i = ((YuckInteger) left).number; i <= ((YuckInteger) right).number; i++) {
              result.add(new YuckInteger(i, context));
            }
          } else {
            throw new NotImplementedException();
          }
          context.push(result);
          break;
        }
        case LIST: {
          YuckList result = new YuckList(context);
          for (int i = 0; i < instruction.getArgument(); i++) {
            result.list.add(0, context.pop());
          }
          context.push(result);
          break;
        }
        case POP: context.pop(); break;
        case TABLE: {
          YuckTable result = new YuckTable(context);
          for (int i = 0; i < instruction.getArgument(); i += 2) {
            YuckObject val = context.pop();
            YuckObject key = context.pop();
            result.yuckObjectMap.put(key, val);
          }
          context.push(result);
          break;
        }
        case NEG: {
          YuckObject top = context.pop();
          YuckObject result;
          if (top instanceof YuckFloat) {
            result = new YuckFloat(-((YuckFloat) top).number, context);
          } else if (top instanceof YuckInteger) {
            result = new YuckInteger(-((YuckInteger) top).number, context);
          } else {
            throw new NotImplementedException();
          }
          context.push(result);
          break;
        }
        case NOT: {
          YuckObject top = context.pop();
          YuckObject result;
          if (top instanceof YuckBoolean) {
            result = new YuckBoolean(!((YuckBoolean) top).bool, context);
          } else {
            throw new NotImplementedException();
          }
          context.push(result);
          break;
        }
        case TABLE_LOAD: {
          YuckObject key = context.pop();
          YuckObject base = context.pop();
          YuckObject result = base.tableLoad(key);
          context.push(result);
          break;
        }
        case TABLE_STORE: {
          YuckObject val = context.pop();
          YuckObject key = context.pop();
          YuckObject base = context.pop();
          base.tableStore(key, val);
          break;
        }
        case GET_FIELD: {
          YuckObject base = context.pop();
          context.push(base.getField((String) function.constants.inverse().get(instruction.getArgument())));
          break;
        }
        case PUT_FIELD: {
          YuckObject val = context.pop();
          YuckObject base = context.pop();
          base.putField((String) function.constants.inverse().get(instruction.getArgument()), val);
          break;
        }
        case CLASS: {
          YCodeClass yClass = function.classes.inverse().get(instruction.getArgument());
          YuckClass yuckClass = new YuckClass(yClass, context, function);
          context.push(yuckClass);
          break;
        }
        case NEW: {
          YuckObject clazz = context.pop();
          Preconditions.checkArgument(clazz instanceof YuckClass);
          YuckInstance instance = new YuckInstance((YuckClass) clazz, function, context);
          context.push(instance);
          break;
        }
        case THIS: {
          YuckInstance instance = context.lookupThis();
          context.push(instance);
          break;
        }
        case SUPER: {
          YuckInstance instance = context.lookupThis();
          String name = (String) function.constants.inverse().get(instruction.getArgument());
          Optional<YuckInstance> superInstance = instance.getSuper(name);
          if (superInstance.isPresent()) {
            context.push(superInstance.get());
          } else {
            throw new IllegalStateException("No super class of " + name);
          }
          break;
        }
        case INSTANCEOF: {
          String name = (String) function.constants.inverse().get(instruction.getArgument());
          YuckObject yuckObject = context.pop();
          Preconditions.checkArgument(yuckObject instanceof YuckInstance);
          YuckInstance instance = (YuckInstance) yuckObject;
          context.push(new YuckBoolean(instance.getSuper(name).isPresent(), context));
          break;
        }
        default:
          System.err.printf("%s not supported.", instruction);
          throw new NotImplementedException();
      }
      pc = next;
    }
  }
}
