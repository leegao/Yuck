package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

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
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext compilationContext) {
    // for i in list {...} -> var it = list.iterator(); while (it.has_next()) { var i = it.next(); ... }
    String iter = function.fvs("iter");
    String label = function.flx("iter");
    String fallthrough = function.flx("iter");

    expr.compile(function, compilationContext); // TOS is list
    function.emit(Opcode.STORE_LOCAL, iter);
    // While loop
    function.emit(Opcode.NOP, label);
    function.emit(Opcode.LOAD_LOCAL, iter);
    function.emit(Opcode.GET_FIELD, "has_next");
    function.emit(Opcode.CALL, 1);
    function.emit(Opcode.JUMPZ, fallthrough);
    // var i = it.next();
    function.emit(Opcode.LOAD_LOCAL, iter);
    function.emit(Opcode.GET_FIELD, "next");
    function.emit(Opcode.CALL, 1);
    function.emit(Opcode.STORE_LOCAL, id);
    statements.forEach(statement -> statement.compile(function, compilationContext));
    function.emit(Opcode.GOTO, label);
    function.emit(Opcode.NOP, fallthrough);

    return function;
  }
}
