package com.yuck.ycode;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class Instruction {
  public final Opcode opcode;
  private int argument;
  public final YCodeFunctionContext context;

  private Instruction(Opcode opcode, int argument, YCodeFunctionContext context) {
    this.opcode = opcode;
    this.argument = argument;
    this.context = context;
  }

  public static <T> Instruction make(YCodeFunctionContext context, Opcode opcode, T data) {
    int mult = 1;
    switch (opcode) {
      case NIL:
      case POP:
      case ROT2:
      case TABLE_LOAD:
      case TABLE_STORE:
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
        return new Instruction(opcode, context.upvalue((String) data), context);
      case NEW:
      case GET_FIELD:
      case PUT_FIELD:
        Preconditions.checkArgument(data instanceof String);
        return new Instruction(opcode, context.constant(data), context);
      case JUMPZ:
      case GOTO:
        Preconditions.checkArgument(data instanceof String);
        int label = context.label((String) data);
        return new Instruction(opcode, label | 0x800000, context);
      case NOP:
        if (data instanceof String) {
          Preconditions.checkArgument(!context.labels.containsKey(data));
          return new Instruction(opcode, context.label((String) data), context);
        } else {
          return new Instruction(opcode, 0, context);
        }
      case CLOSURE:
        Preconditions.checkArgument(data instanceof YCodeFunctionContext);
        int f = context.function((YCodeFunctionContext) data);
        return new Instruction(opcode, f, context);
      case TABLE:
        mult = 2;
      case CALL:
      case LIST:
        Preconditions.checkArgument(data instanceof Integer);
        int numArguments = (Integer) data;
        return new Instruction(opcode, mult * numArguments, context);
      default:
        throw new IllegalStateException();
    }
  }

  public static Instruction make(YCodeFunctionContext context, Opcode opcode) {
    return make(context, opcode, 0);
  }

  public static Opcode variable(YCodeFunctionContext context, String string) {
    return context.locals.containsKey(string) ?
        Opcode.LOAD_LOCAL :
        Opcode.LOAD_UP;
  }

  public int getArgument() {
    return argument;
  }

  public void fixup() {
    switch (opcode) {
      case NOP:
        argument = 0;
        return;
      case GOTO:
      case JUMPZ:
        int cursor = argument & 0x800000;
        Preconditions.checkArgument(cursor < context.labelPositions.size());
        int target = context.labelPositions.get(cursor);
        int current = context.position(this);
        argument = target - current;
    }
  }

  public ByteBuffer write(ByteBuffer buffer) {
    buffer.put((byte) opcode.ordinal());
    buffer.putInt(argument);
    return buffer;
  }

  public static Instruction read(YCodeFunctionContext context, ByteBuffer buffer) {
    int op = buffer.get();
    int argument = buffer.getInt();
    return new Instruction(Opcode.values()[op], argument, context);
  }
}
