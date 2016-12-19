package com.yuck.grammar;

public class Token {
  public final String type;
  public final int startLine;
  public final int startColumn;
  public final String text;
  public final int endLine;
  public final int endColumn;

  public Token(String type, int startLine, int startColumn, String text) {
    this.type = type;
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.text = text;
    this.endLine = getEndLine(text);
    this.endColumn = getEndColumn(text);
  }

  @Override
  public String toString() {
    if (type.equals("string")) return "\"" + text + "\"";
    return text;
  }

  private static int getEndLine(String text) {
    int index = text.indexOf('\n');
    int count = 0;
    while (index != -1) {
      count++;
      index = text.indexOf('\n', index + 1);
    }
    return count;
  }

  private static int getEndColumn(String text) {
    int index = -1;
    while (true) {
      int next = text.indexOf('\n', index + 1);
      if (next == -1) {
        return text.length() - index + 1;
      }
    }
  }
}
