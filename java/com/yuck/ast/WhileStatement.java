package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
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
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    // while (condition) { ... } => start; condition; jumpz fallthrough; ...; goto start; fallthrough
    String headLabel = context.flx("head");
    String fallthroughLabel = context.flx("fallthrough");
    context.emit(Opcode.NOP, headLabel);
    condition.compile(context).emit(Opcode.JUMPZ, fallthroughLabel);
    statements.forEach(statement -> statement.compile(context));
    return context.emit(Opcode.GOTO, headLabel).emit(Opcode.NOP, fallthroughLabel);
  }
}
