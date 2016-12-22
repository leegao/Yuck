package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

public class ReturnStatement extends Statement {
  public final Expression expression;

  public ReturnStatement(Token ret, Expression expression, Token semicolon) {
    super(ret.startLine, ret.startColumn, semicolon.endLine, semicolon.endColumn);
    this.expression = expression;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext compilationContext) {
    return expression.compile(function, compilationContext).emit(Opcode.RETURN);
  }
}
