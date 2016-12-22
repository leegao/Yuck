package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import java.util.List;

public class ForStatement extends Statement {
  public final String id;
  public final Expression expr;
  public final ImmutableList<Statement> statements;

  public ForStatement(Token start, Token id, Expression expr, List<Statement> statements, Token close) {
    super(start.startLine, start.startColumn, close.endLine, close.endColumn);
    this.id = id.text;
    this.expr = expr;
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    // for i in list {...} -> var it = list.iterator(); while (it.has_next()) { var i = it.next(); ... }
    String iter = context.fvs("iter");
    String label = context.flx("iter");
    String fallthrough = context.flx("iter");

    expr.compile(context); // TOS is list
    context.emit(Opcode.STORE_LOCAL, iter);
    // While loop
    context.emit(Opcode.NOP, label);
    context.emit(Opcode.LOAD_LOCAL, iter);
    context.emit(Opcode.GET_FIELD, "has_next");
    context.emit(Opcode.CALL, 1);
    context.emit(Opcode.JUMPZ, fallthrough);
    // var i = it.next();
    context.emit(Opcode.LOAD_LOCAL, iter);
    context.emit(Opcode.GET_FIELD, "next");
    context.emit(Opcode.CALL, 1);
    context.emit(Opcode.STORE_LOCAL, id);
    statements.forEach(statement -> statement.compile(context));
    context.emit(Opcode.GOTO, label);
    context.emit(Opcode.NOP, fallthrough);

    return context;
  }
}
