package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class IfStatement extends Statement {
  private final Expression condition;
  private final ImmutableList<Statement> statements;
  private Optional<List<Statement>> elseStatements = Optional.empty();

  public IfStatement(Token start, Expression condition, List<Statement> statements) {
    super(start.startLine, start.startColumn, -1, -1);
    this.condition = condition;
    this.statements = ImmutableList.copyOf(statements);
  }

  public IfStatement(Token start, Expression condition, List<Statement> statements, Token close) {
    super(start.startLine, start.startColumn, close.endLine, close.endColumn);
    this.condition = condition;
    this.statements = ImmutableList.copyOf(statements);
  }

  public void addElse(List<Statement> statements) {
    elseStatements = Optional.of(statements);
  }
}
