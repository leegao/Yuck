package com.yuck.interpreter;

public class YuckNil extends YuckObject {
  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.NIL;
  }
}
