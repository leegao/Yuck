package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class GroupExpression extends Expression {
  public final Expression expression;

  public GroupExpression(Token lparen, Expression expression, Token rparen) {
    super(lparen.startLine, lparen.startColumn, rparen.endLine, rparen.endColumn);
    this.expression = expression;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    return expression.compile(function, context);
  }
}
