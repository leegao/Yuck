package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class ExpressionStatement extends Statement {
  public final Expression expr;

  public ExpressionStatement(Expression expr, Token semi) {
    super(expr.getStartLine(), expr.getStartColumn(), semi.endLine, semi.endColumn);
    this.expr = expr;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    // Each expression pushes one item onto the stack
    return expr.compile(function, context).emit(Opcode.POP);
  }
}
