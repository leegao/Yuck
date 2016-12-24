package com.yuck.interpreter;

public class YuckBoolean extends YuckObject {
  public final boolean bool;

  public YuckBoolean(boolean bool) {
    this.bool = bool;
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.BOOL;
  }
}
