package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.YCodeFunctionContext;

public class EmptyStatement extends Statement {
  public EmptyStatement(Token semicolon) {
    super(semicolon.startLine, semicolon.startColumn, semicolon.endLine, semicolon.endColumn);
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    return context;
  }
}
