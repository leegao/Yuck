package com.yuck.ast;

import com.yuck.grammar.Token;

public class Var extends Expression {
  public final String id;

  public Var(Token id) {
    super(id.line, id.column, id.line, id.column);
    this.id = id.text;
  }
}
