package com.yuck.interpreter;

public class YuckNil extends YuckObject {
  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.NIL;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof YuckNil;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
