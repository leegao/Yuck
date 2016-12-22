package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

import java.util.List;

public class DoStatement extends Statement {
  public final ImmutableList<Statement> statements;

  public DoStatement(Token doToken, List<Statement> statements, Token close) {
    super(doToken.startLine, doToken.startColumn, close.endLine, close.endColumn);
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext compilationContext) {
    try (YCodeCompilationContext.Scope scope = compilationContext.push()) {
      statements.forEach(statement -> statement.compile(function, compilationContext));
    }
    return function;
  }
}
