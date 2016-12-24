package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class ReturnStatement extends Statement {
  public final Expression expression;

  public ReturnStatement(Token ret, Expression expression, Token semicolon) {
    super(ret.startLine, ret.startColumn, semicolon.endLine, semicolon.endColumn);
    this.expression = expression;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    return expression.compile(function, context).emit(Opcode.RETURN);
  }
}
