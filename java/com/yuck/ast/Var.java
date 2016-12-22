package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;

public class Var extends Expression {
  public final String id;
  public Token token;

  public Var(Token id) {
    super(id.startLine, id.startColumn, id.endLine, id.endColumn);
    this.id = id.text;
    this.token = id;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    return context.emit(Opcode.LOAD_LOCAL, id);
  }
}
