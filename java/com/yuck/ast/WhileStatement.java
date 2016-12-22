package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

import java.util.List;

public class WhileStatement extends Statement {
  public final Expression condition;
  public final ImmutableList<Statement> statements;

  public WhileStatement(Token start, Expression condition, List<Statement> statements, Token close) {
    super(start.startLine, start.startColumn, close.endLine, close.endColumn);
    this.condition = condition;
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    // while (condition) { ... } => start; condition; jumpz fallthrough; ...; goto start; fallthrough
    String headLabel = function.flx("head");
    String fallthroughLabel = function.flx("fallthrough");
    function.emit(Opcode.NOP, headLabel);
    condition.compile(function, scope).emit(Opcode.JUMPZ, fallthroughLabel);
    statements.forEach(statement -> statement.compile(function, scope));
    return function.emit(Opcode.GOTO, headLabel).emit(Opcode.NOP, fallthroughLabel);
  }
}
