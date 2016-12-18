package com.yuck.ast;

import com.yuck.grammar.Token;

public class Literal extends Expression {
  public final String data;
  public Literal(Token text) {
    super(text.startLine, text.startColumn, text.endLine, text.endColumn);
    data = text.text;
  }
}
