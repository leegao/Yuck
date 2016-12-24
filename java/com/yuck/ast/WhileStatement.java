package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

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
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    // while (condition) { ... } => start; condition; jumpz fallthrough; ...; goto start; fallthrough
    String headLabel = function.flx("head");
    String fallthroughLabel = function.flx("fallthrough");
    function.emit(Opcode.NOP, headLabel);
    condition.compile(function, context).emit(Opcode.JUMPZ, fallthroughLabel);
    try (YCodeCompilationContext.Scope scope = context.push()) {
      statements.forEach(statement -> statement.compile(function, context));
    }
    return function.emit(Opcode.GOTO, headLabel).emit(Opcode.NOP, fallthroughLabel);
  }
}
