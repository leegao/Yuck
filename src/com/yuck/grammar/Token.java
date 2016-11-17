package com.yuck.grammar;

public class Token {
  public final String type;
  public final int line;
  public final int column;
  public final String text;

  public Token(String type, int line, int column, String text) {
    this.type = type;
    this.line = line;
    this.column = column;
    this.text = text;
  }
}
