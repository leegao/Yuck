package com.yuck.interpreter;

public class YuckNil extends YuckObject {
  public YuckNil(InterpreterContext context) {
    super(context);
  }

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

  @Override
  public boolean isFilled() {
    return false;
  }
}
