package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.sun.org.apache.bcel.internal.generic.GOTO;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    String labelElse = context.flx("else");
    String labelFallthrough = context.flx("fallthrough");
    condition.compile(context).emit(Opcode.JUMPZ, labelElse);
    statements.forEach(statement -> statement.compile(context));
    if (elseStatements.isPresent())
      context.emit(Opcode.GOTO, labelFallthrough);
    context.emit(Opcode.NOP, labelElse);
    if (elseStatements.isPresent()) {
      elseStatements.get().forEach(elseStatement -> elseStatement.compile(context));
      context.emit(Opcode.NOP, labelFallthrough);
    }
    return context;
  }
}
