package com.yuck.ast;

import com.yuck.grammar.Token;

public class StringLiteral extends Expression {
  public final String text;
  public StringLiteral(Token token) {
    super(token.startLine, token.startColumn, token.endLine, token.endColumn);
    text = token.text;
  }
}
