package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

import java.util.List;
import java.util.Optional;

public class IfStatement extends Statement {
  public final Expression condition;
  public final ImmutableList<Statement> statements;
  public Optional<List<Statement>> elseStatements = Optional.empty();

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

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    String labelElse = function.flx("else");
    String labelFallthrough = function.flx("fallthrough");
    condition.compile(function, context).emit(Opcode.JUMPZ, labelElse);
    try (YCodeCompilationContext.Scope scope = context.push()) {
      statements.forEach(statement -> statement.compile(function, context));
    }
    if (elseStatements.isPresent())
      function.emit(Opcode.GOTO, labelFallthrough);
    function.emit(Opcode.NOP, labelElse);
    if (elseStatements.isPresent()) {
      try (YCodeCompilationContext.Scope scope = context.push()) {
        elseStatements.get().forEach(elseStatement -> elseStatement.compile(function, context));
      }
      function.emit(Opcode.NOP, labelFallthrough);
    }
    return function;
  }
}
