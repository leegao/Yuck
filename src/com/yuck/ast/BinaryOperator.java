package com.yuck.ast;

public class BinaryOperator extends Expression {
  public final String operator;
  public final Expression left;
  public final Expression right;

  public BinaryOperator(String operator, Expression left, Expression right) {
    super(left.getStartLine(), left.getStartColumn(), right.getEndLine(), right.getEndColumn());
    this.operator = operator;
    this.left = left;
    this.right = right;
  }
}
