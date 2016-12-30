package com.yuck.ast;

import com.yuck.grammar.Token;

public class InstanceOfExpression extends Expression {
  public final Token name;

  public InstanceOfExpression(Expression left, Token name) {
    super(left.getStartLine(), left.getStartColumn(), name.endLine, name.endColumn);
    this.name = name;
  }
}
