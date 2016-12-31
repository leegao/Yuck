package com.yuck.interpreter;

import java.util.Objects;

public class YuckBoolean extends YuckObject {
  public final boolean bool;

  public YuckBoolean(boolean bool, InterpreterContext context) {
    super(context);
    this.bool = bool;
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.BOOL;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckBoolean that = (YuckBoolean) o;
    return bool == that.bool;
  }

  @Override
  public int hashCode() {
    return Objects.hash(bool);
  }

  @Override
  public boolean isFilled() {
    return bool;
  }

  @Override
  public String toString() {
    return Objects.toString(bool);
  }
}
