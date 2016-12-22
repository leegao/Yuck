package com.yuck.ast;

import com.yuck.grammar.Token;

public class AssignmentStatement extends Statement {
  public final String id;
  public final Expression expr;

  public AssignmentStatement(Token id, Expression expr, Token semi) {
    super(id.startLine, id.startColumn, semi.endLine, semi.endColumn);
    this.id = id.text;
    this.expr = expr;
  }
}
