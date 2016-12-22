package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

import java.util.List;

public class DoStatement extends Statement {
  public final ImmutableList<Statement> statements;

  public DoStatement(Token doToken, List<Statement> statements, Token close) {
    super(doToken.startLine, doToken.startColumn, close.endLine, close.endColumn);
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    try (YCodeCompilationContext.Scope scope = context.push()) {
      statements.forEach(statement -> statement.compile(function, context));
    }
    return function;
  }
}
