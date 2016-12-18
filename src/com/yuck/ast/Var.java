package com.yuck.ast;

import com.yuck.grammar.Token;

public class Var extends Expression {
  public final String id;

  public Var(Token id) {
    super(id.startLine, id.startColumn, id.endLine, id.endColumn);
    this.id = id.text;
  }
}
