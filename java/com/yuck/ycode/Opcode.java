package com.yuck.ycode;

import com.google.common.base.Preconditions;

public enum Opcode {
  LOAD_CONST, NIL, LOAD_LOCAL, STORE_LOCAL, LOAD_UP, STORE_UP, POP, DUP, CLOSURE, CALL,
  TABLE_LOAD, TABLE_STORE, NEW, RETURN, GET_FIELD, PUT_FIELD, LT, LE, EQ, JUMPZ, TO_RANGE,
  ADD, MUL, SUB, DIV, MOD, NEG, NOT, POW, LIST, TABLE, GOTO, NOP,;

  public Opcode plus(int n) {
    Preconditions.checkArgument(ordinal() + n < values().length);
    return Opcode.values()[this.ordinal() + n];
  }
}
