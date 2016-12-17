package com.yuck.ast;

public abstract class Base {
  private int mStartLine, mStartColumn, mEndLine, mEndColumn;

  protected Base(int startLine, int startColumn, int endLine, int endColumn) {
    this.mStartLine = startLine;
    this.mStartColumn = startColumn;
    this.mEndLine = endLine;
    this.mEndColumn = endColumn;
  }

  public int getStartLine() {
    return mStartLine;
  }

  public void setStartLine(int mStartLine) {
    this.mStartLine = mStartLine;
  }

  public int getStartColumn() {
    return mStartColumn;
  }

  public void setStartColumn(int mStartColumn) {
    this.mStartColumn = mStartColumn;
  }

  public int getEndLine() {
    return mEndLine;
  }

  public void setEndLine(int mEndLine) {
    this.mEndLine = mEndLine;
  }

  public int getEndColumn() {
    return mEndColumn;
  }

  public void setEndColumn(int mEndColumn) {
    this.mEndColumn = mEndColumn;
  }
}
