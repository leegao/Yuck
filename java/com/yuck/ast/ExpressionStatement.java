package com.yuck.ast;

import com.yuck.grammar.Token;

public class ExpressionStatement extends Statement {
  public final Expression expr;

  public ExpressionStatement(Expression expr, Token semi) {
    super(expr.getStartLine(), expr.getStartColumn(), semi.endLine, semi.endColumn);
    this.expr = expr;
  }
}
