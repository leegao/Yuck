package com.yuck.ast;

import com.yuck.grammar.Token;

public class GroupExpression extends Expression {
  private final Expression expression;

  public GroupExpression(Token lparen, Expression expression, Token rparen) {
    super(lparen.line, lparen.column, rparen.line, rparen.column);
    this.expression = expression;
  }
}
