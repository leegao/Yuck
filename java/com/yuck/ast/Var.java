package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;

public class Var extends Expression {
  public final String id;

  public Var(Token id) {
    super(id.startLine, id.startColumn, id.endLine, id.endColumn);
    this.id = id.text;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    return context.emit(Opcode.LOAD_LOCAL, id);
  }
}
