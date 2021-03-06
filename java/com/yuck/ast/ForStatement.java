package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

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
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    // for i in list {...} -> var it = list.iterator(); while (it.has_next()) { var i = it.next(); ... }
    try (YCodeCompilationContext.Scope scope = context.push()) {
      expr.compile(function, context).emit(Opcode.GET_FIELD, "@iterator").emit(Opcode.CALL, 1); // TOS is list
      String iter = function.fvs("iter");
      function.local(iter);
      String label = function.flx("iter");
      function.label(label);
      String fallthrough = function.flx("fallthrough");
      function.label(fallthrough);
      function.emit(Opcode.STORE_LOCAL, iter);
      // While loop
      function.emit(Opcode.NOP, label);
      function.emit(Opcode.LOAD_LOCAL, iter);
      function.emit(Opcode.GET_FIELD, "hasNext");
      function.emit(Opcode.CALL, 1);
      function.emit(Opcode.JUMPZ, fallthrough);
      // var i = it.next();
      function.emit(Opcode.LOAD_LOCAL, iter);
      function.emit(Opcode.GET_FIELD, "next");
      function.emit(Opcode.CALL, 1);
      function.emit(Opcode.STORE_LOCAL, id);
      statements.forEach(statement -> statement.compile(function, context));
      function.emit(Opcode.GOTO, label);
      function.emit(Opcode.NOP, fallthrough);
    }

    return function;
  }
}
