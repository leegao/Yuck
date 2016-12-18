package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class FunctionExpression extends Expression {
  public final ImmutableList<Var> parameters;
  public final ImmutableList<Statement> statements;

  public FunctionExpression(Token left, List<Var> parameters, List<Statement> statements, Token right) {
    super(left.startLine, left.startColumn, right.endLine, right.endColumn);

    this.parameters = ImmutableList.copyOf(parameters);
    this.statements = ImmutableList.copyOf(statements);
  }
}
