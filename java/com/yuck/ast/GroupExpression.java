package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.YCodeFunctionContext;

public class GroupExpression extends Expression {
  public final Expression expression;

  public GroupExpression(Token lparen, Expression expression, Token rparen) {
    super(lparen.startLine, lparen.startColumn, rparen.endLine, rparen.endColumn);
    this.expression = expression;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    return expression.compile(context);
  }
}
