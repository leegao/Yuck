package com.yuck.ast;

public abstract class Statement extends Base {
  protected Statement(int startLine, int startColumn, int endLine, int endColumn) {
    super(startLine, startColumn, endLine, endColumn);
  }
}
