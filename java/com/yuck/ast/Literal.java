package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;

public class Literal extends Expression {
  public final String data;
  public final String kind;
  public Literal(Token text) {
    super(text.startLine, text.startColumn, text.endLine, text.endColumn);
    data = text.text;
    kind = text.type;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    switch (kind) {
      case "nil":
        return context.emit(Opcode.NIL);
      case "true":
        return context.emit(Opcode.LOAD_CONST, true);
      case "false":
        return context.emit(Opcode.LOAD_CONST, false);
      case "num":
        try {
          return context.emit(Opcode.LOAD_CONST, Integer.valueOf(data));
        } catch (NumberFormatException e) {
          return context.emit(Opcode.LOAD_CONST, Float.valueOf(data));
        }
    }
    throw new IllegalStateException();
  }
}
