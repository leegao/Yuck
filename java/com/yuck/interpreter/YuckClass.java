package com.yuck.interpreter;

public class YuckClass extends YuckObject {
  protected YuckClass(InterpreterContext context) {
    super(context);
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.OBJECT;
  }

  @Override
  public boolean equals(Object other) {
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }


}
