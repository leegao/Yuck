package com.yuck.interpreter;

import com.yuck.ycode.YCodeClass;

import java.util.Objects;

public class YuckClass extends YuckObject {
  public final YCodeClass yClass;

  public YuckClass(YCodeClass yClass, InterpreterContext context) {
    super(context);
    this.yClass = yClass;
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.OBJECT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckClass yuckClass = (YuckClass) o;
    return Objects.equals(yClass, yuckClass.yClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(yClass);
  }
}
