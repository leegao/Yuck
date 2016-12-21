package com.yuck.ast;

public abstract class Expression extends Base {
  protected Expression(int startLine, int startColumn, int endLine, int endColumn) {
    super(startLine, startColumn, endLine, endColumn);
  }
}
