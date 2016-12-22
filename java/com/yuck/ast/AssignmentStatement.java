package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;

public class AssignmentStatement extends Statement {
  public final String id;
  public final Expression expr;

  public AssignmentStatement(Token id, Expression expr, Token semi) {
    super(id.startLine, id.startColumn, semi.endLine, semi.endColumn);
    this.id = id.text;
    this.expr = expr;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    return expr.compile(context).emit(Opcode.STORE_LOCAL, id);
  }
}
