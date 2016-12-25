package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {
  public static final boolean DEBUG = false;
  public InterpreterContext interpret(YCodeFunction function, InterpreterContext context) {
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
          if (value instanceof YuckBoolean) {
            if (!((YuckBoolean) value).bool) {
              next = instruction.getArgument();
            }
          } else {
            throw new NotImplementedException();
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
          if (callable instanceof YuckFunction) {
            InterpreterContext nextContext = new InterpreterContext(context);
            YuckFunction closure = (YuckFunction) callable;
            int local = 0;
            for (YuckObject argument : arguments) {
              nextContext.add(local, closure.function.locals.inverse().get(local), argument);
              local++;
            }
            InterpreterContext result = interpret(closure.function, nextContext);
            context.push(result.pop());
          } else {
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
        default:
          System.err.printf("%s not supported.", instruction);
          throw new NotImplementedException();
      }
      pc = next;
    }
  }
}
