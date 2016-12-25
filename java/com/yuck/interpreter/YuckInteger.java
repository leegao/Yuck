package com.yuck.interpreter;

import java.util.Objects;

public class YuckInteger extends YuckObject {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckInteger that = (YuckInteger) o;
    return number == that.number;
  }

  @Override
  public int hashCode() {
    return Objects.hash(number);
  }

  public final int number;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.INT;
  }

  public YuckInteger(int number) {
    this.number = number;
  }
}
