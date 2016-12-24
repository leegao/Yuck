package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Interpreter {
  public InterpreterContext interpret(YCodeFunction function, InterpreterContext context) {
    int pc = 0;
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
        case POW:
          Preconditions.checkArgument(context.depth() >= 2);
          YuckObject right = context.pop();
          YuckObject left = context.pop();
          // For now, just worry about if they are both integers or both floats
          if (left instanceof YuckInteger && right instanceof YuckInteger) {
            YuckObject result;
            int a = ((YuckInteger) left).number;
            int b = ((YuckInteger) right).number;
            switch (instruction.opcode) {
              case ADD: result = new YuckInteger(a + b); break;
              case SUB: result = new YuckInteger(a - b); break;
              case MUL: result = new YuckInteger(a * b); break;
              case DIV: result = new YuckInteger(a / b); break;
              case MOD: result = new YuckInteger(a % b); break;
              case POW: result = new YuckFloat((float) Math.pow(a, b)); break;
              case LE: result = new YuckBoolean(a <= b); break;
              case LT: result = new YuckBoolean(a < b); break;
              case EQ: result = new YuckBoolean(a == b); break;
              default: throw new IllegalStateException();
            }
            context.push(result);
          } else if (left instanceof YuckFloat && right instanceof YuckInteger) {
            YuckObject result;
            float a = ((YuckFloat) left).number;
            int b = ((YuckInteger) right).number;
            switch (instruction.opcode) {
              case ADD: result = new YuckFloat(a + b); break;
              case SUB: result = new YuckFloat(a - b); break;
              case MUL: result = new YuckFloat(a * b); break;
              case DIV: result = new YuckFloat(a / b); break;
              case MOD: result = new YuckFloat(a % b); break;
              case POW: result = new YuckFloat((float) Math.pow(a, b)); break;
              case LE: result = new YuckBoolean(a <= b); break;
              case LT: result = new YuckBoolean(a < b); break;
              case EQ: result = new YuckBoolean(a == b); break;
              default: throw new IllegalStateException();
            }
            context.push(result);
          } else if (left instanceof YuckFloat && right instanceof YuckFloat) {
            YuckObject result;
            float a = ((YuckFloat) left).number;
            float b = ((YuckFloat) right).number;
            switch (instruction.opcode) {
              case ADD: result = new YuckFloat(a + b); break;
              case SUB: result = new YuckFloat(a - b); break;
              case MUL: result = new YuckFloat(a * b); break;
              case DIV: result = new YuckFloat(a / b); break;
              case MOD: result = new YuckFloat(a % b); break;
              case POW: result = new YuckFloat((float) Math.pow(a, b)); break;
              case LE: result = new YuckBoolean(a <= b); break;
              case LT: result = new YuckBoolean(a < b); break;
              case EQ: result = new YuckBoolean(a == b); break;
              default: throw new IllegalStateException();
            }
            context.push(result);
          } else if (left instanceof YuckInteger && right instanceof YuckFloat) {
            YuckObject result;
            float a = ((YuckInteger) left).number;
            float b = ((YuckFloat) right).number;
            switch (instruction.opcode) {
              case ADD: result = new YuckFloat(a + b); break;
              case SUB: result = new YuckFloat(a - b); break;
              case MUL: result = new YuckFloat(a * b); break;
              case DIV: result = new YuckFloat(a / b); break;
              case MOD: result = new YuckFloat(a % b); break;
              case POW: result = new YuckFloat((float) Math.pow(a, b)); break;
              case LE: result = new YuckBoolean(a <= b); break;
              case LT: result = new YuckBoolean(a < b); break;
              case EQ: result = new YuckBoolean(a == b); break;
              default: throw new IllegalStateException();
            }
            context.push(result);
          } else {
            throw new NotImplementedException();
          }
          break;
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
        default:
          System.err.printf("%s not supported.", instruction);
          throw new NotImplementedException();
      }
      pc = next;
    }
  }
}
