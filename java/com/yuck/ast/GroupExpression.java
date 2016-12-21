package com.yuck.ast;

import com.yuck.grammar.Token;

public class GroupExpression extends Expression {
  private final Expression expression;

  public GroupExpression(Token lparen, Expression expression, Token rparen) {
    super(lparen.startLine, lparen.startColumn, rparen.endLine, rparen.endColumn);
    this.expression = expression;
  }
}
