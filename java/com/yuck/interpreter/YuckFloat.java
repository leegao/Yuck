package com.yuck.interpreter;

public class YuckFloat extends YuckObject {
  public final float number;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.FLOAT;
  }

  public YuckFloat(float number) {
    this.number = number;
  }
}
