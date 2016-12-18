package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class FunctionDeclaration extends Statement {
  public final Token id;
  public final ImmutableList<Var> parameters;
  public final ImmutableList<Statement> statements;

  public FunctionDeclaration(Token function, Token id, List<Var> parameters, List<Statement> statements, Token right) {
    super(function.startLine, function.startColumn, right.endLine, right.endColumn);
    this.id = id;
    this.parameters = ImmutableList.copyOf(parameters);
    this.statements = ImmutableList.copyOf(statements);
  }
}
