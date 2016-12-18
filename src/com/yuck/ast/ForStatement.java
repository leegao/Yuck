package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class ForStatement extends Statement {
  public final Token id;
  public final Expression expr;
  public final ImmutableList<Statement> statements;

  public ForStatement(Token start, Token id, Expression expr, List<Statement> statements, Token close) {
    super(start.startLine, start.startColumn, close.endLine, close.endColumn);
    this.id = id;
    this.expr = expr;
    this.statements = ImmutableList.copyOf(statements);
  }
}
