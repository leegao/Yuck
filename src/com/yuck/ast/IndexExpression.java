package com.yuck.ast;

import com.yuck.grammar.Token;

public class IndexExpression extends Expression {
  public final Expression left;
  public final Expression index;

  public IndexExpression(Expression left, Expression index, Token right) {
    super(left.getStartLine(), left.getStartColumn(), right.endLine, right.endColumn);
    this.left = left;
    this.index = index;
  }
}
