package com.yuck.ast;

import com.yuck.grammar.Token;

public class EmptyStatement extends Statement {
  public EmptyStatement(Token semicolon) {
    super(semicolon.startLine, semicolon.startColumn, semicolon.endLine, semicolon.endColumn);
  }
}
