package com.yuck.ast;

import com.yuck.grammar.Token;

public class Selector extends Expression {
  public final Expression left;
  public final String select;

  public Selector(Expression left, Token select) {
    super(left.getStartLine(), left.getStartColumn(), select.endLine, select.endColumn);
    this.left = left;
    this.select = select.text;
  }
}
