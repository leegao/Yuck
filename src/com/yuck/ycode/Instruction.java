package com.yuck.ycode;

import com.google.common.base.Preconditions;

public class Instruction {
  public final Opcode opcode;
  private int argument;
  public final YCodeContext context;

  private Instruction(Opcode opcode, int argument, YCodeContext context) {
    this.opcode = opcode;
    this.argument = argument;
    this.context = context;
  }

  public static <T> Instruction make(YCodeContext context, Opcode opcode, T data) {
    switch (opcode) {
      case POP:
      case ROT2:
      case CLOSURE:
      case CALL:
      case TABLE_LOAD:
      case TABLE_STORE:
      case TABLE:
      case LIST:
      case RETURN:
      case LT:
      case LE:
      case EQ:
      case TO_RANGE:
      case ADD:
      case MUL:
      case SUB:
      case DIV:
      case MOD:
      case NOT:
      case NEG:
      case NOP:
      case AND:
      case OR:
      case POW:
        Preconditions.checkArgument(data.equals(0));
        return new Instruction(opcode, 0, context);
      case LOAD_CONST:
        return new Instruction(opcode, context.constant(data), context);
      case LOAD_LOCAL:
      case STORE_LOCAL:
        Preconditions.checkArgument(data instanceof String);
        return new Instruction(opcode, context.local((String) data), context);
      case LOAD_UP:
      case STORE_UP:
        Preconditions.checkArgument(data instanceof String);
        String upvalue = (String) data;
        Preconditions.checkArgument(context.upValues.containsKey(upvalue));
        return new Instruction(opcode, context.upValues.get(upvalue), context);
      default:
        throw new IllegalStateException();
    }
  }

  public static Instruction make(YCodeContext context, Opcode opcode) {
    return make(context, opcode, 0);
  }

  public int getArgument() {
    return argument;
  }
}
