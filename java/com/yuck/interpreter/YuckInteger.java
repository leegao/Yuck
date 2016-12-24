package com.yuck.interpreter;

public class YuckInteger extends YuckObject {
  public final int number;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.INT;
  }

  public YuckInteger(int number) {
    this.number = number;
  }
}
