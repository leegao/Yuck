package com.yuck.ast;

import com.yuck.grammar.Token;

public class UnaryOperator extends Expression {
  public final String operator;
  public final Expression expression;
  public UnaryOperator(Token operator, Expression expression) {
    super(operator.startLine, operator.startColumn, expression.getEndLine(), expression.getEndColumn());
    this.operator = operator.text;
    this.expression = expression;
  }
}
