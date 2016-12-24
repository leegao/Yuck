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
        case ADD:
        case SUB:
        case MUL:
        case DIV:
        case MOD:
        case LE:
        case LT:
        case EQ:
        case POW: {
          Preconditions.checkArgument(
              context.depth() >= 2,
              "Stack is not deep enough at " + instruction);
          YuckObject right = context.pop();
          YuckObject left = context.pop();
          // For now, just worry about if they are both integers or both floats
          if (left instanceof YuckInteger && right instanceof YuckInteger) {
            YuckObject result;
            int a = ((YuckInteger) left).number;
            int b = ((YuckInteger) right).number;
            switch (instruction.opcode) {
              case ADD:
                result = new YuckInteger(a + b);
                break;
              case SUB:
                result = new YuckInteger(a - b);
                break;
              case MUL:
                result = new YuckInteger(a * b);
                break;
              case DIV:
                result = new YuckInteger(a / b);
                break;
              case MOD:
                result = new YuckInteger(a % b);
                break;
              case POW:
                result = new YuckFloat((float) Math.pow(a, b));
                break;
              case LE:
                result = new YuckBoolean(a <= b);
                break;
              case LT:
                result = new YuckBoolean(a < b);
                break;
              case EQ:
                result = new YuckBoolean(a == b);
                break;
              default:
                throw new IllegalStateException();
            }
            context.push(result);
          } else if (left instanceof YuckFloat && right instanceof YuckInteger) {
            YuckObject result;
            float a = ((YuckFloat) left).number;
            int b = ((YuckInteger) right).number;
            switch (instruction.opcode) {
              case ADD:
                result = new YuckFloat(a + b);
                break;
              case SUB:
                result = new YuckFloat(a - b);
                break;
              case MUL:
                result = new YuckFloat(a * b);
                break;
              case DIV:
                result = new YuckFloat(a / b);
                break;
              case MOD:
                result = new YuckFloat(a % b);
                break;
              case POW:
                result = new YuckFloat((float) Math.pow(a, b));
                break;
              case LE:
                result = new YuckBoolean(a <= b);
                break;
              case LT:
                result = new YuckBoolean(a < b);
                break;
              case EQ:
                result = new YuckBoolean(a == b);
                break;
              default:
                throw new IllegalStateException();
            }
            context.push(result);
          } else if (left instanceof YuckFloat && right instanceof YuckFloat) {
            YuckObject result;
            float a = ((YuckFloat) left).number;
            float b = ((YuckFloat) right).number;
            switch (instruction.opcode) {
              case ADD:
                result = new YuckFloat(a + b);
                break;
              case SUB:
                result = new YuckFloat(a - b);
                break;
              case MUL:
                result = new YuckFloat(a * b);
                break;
              case DIV:
                result = new YuckFloat(a / b);
                break;
              case MOD:
                result = new YuckFloat(a % b);
                break;
              case POW:
                result = new YuckFloat((float) Math.pow(a, b));
                break;
              case LE:
                result = new YuckBoolean(a <= b);
                break;
              case LT:
                result = new YuckBoolean(a < b);
                break;
              case EQ:
                result = new YuckBoolean(a == b);
                break;
              default:
                throw new IllegalStateException();
            }
            context.push(result);
          } else if (left instanceof YuckInteger && right instanceof YuckFloat) {
            YuckObject result;
            float a = ((YuckInteger) left).number;
            float b = ((YuckFloat) right).number;
            switch (instruction.opcode) {
              case ADD:
                result = new YuckFloat(a + b);
                break;
              case SUB:
                result = new YuckFloat(a - b);
                break;
              case MUL:
                result = new YuckFloat(a * b);
                break;
              case DIV:
                result = new YuckFloat(a / b);
                break;
              case MOD:
                result = new YuckFloat(a % b);
                break;
              case POW:
                result = new YuckFloat((float) Math.pow(a, b));
                break;
              case LE:
                result = new YuckBoolean(a <= b);
                break;
              case LT:
                result = new YuckBoolean(a < b);
                break;
              case EQ:
                result = new YuckBoolean(a == b);
                break;
              default:
                throw new IllegalStateException();
            }
            context.push(result);
          } else {
            throw new NotImplementedException();
          }
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
          YuckObject yuckConstant = YuckObject.translate(constant);
          context.push(yuckConstant);
          break;
        case NOP:
          break;
        case STORE_LOCAL:
          context.add(instruction.getArgument(), context.pop());
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
            InterpreterContext nextContext = new InterpreterContext();
            YuckFunction closure = (YuckFunction) callable;
            int local = 0;
            for (YuckObject argument : arguments) {
              nextContext.add(local++, argument);
            }
            // TODO: upvalues
            InterpreterContext result = interpret(closure.function, nextContext);
            context.push(result.pop());
          } else {
            throw new NotImplementedException();
          }
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
