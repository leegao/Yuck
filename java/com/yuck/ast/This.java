package com.yuck.ast;

import com.yuck.grammar.Token;

public class This extends Expression {
  public This(Token thisToken) {
    super(thisToken.startLine, thisToken.startColumn, thisToken.endLine, thisToken.endColumn);
  }
}
