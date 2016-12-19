package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class WhileStatement extends Statement {
  public final Expression condition;
  public final ImmutableList<Statement> statements;

  public WhileStatement(Token start, Expression condition, List<Statement> statements, Token close) {
    super(start.startLine, start.startColumn, close.endLine, close.endColumn);
    this.condition = condition;
    this.statements = ImmutableList.copyOf(statements);
  }
}
