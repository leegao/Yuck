package com.yuck.interpreter;

import java.util.Objects;

public class YuckFloat extends YuckObject {
  public final float number;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.FLOAT;
  }

  public YuckFloat(float number, InterpreterContext context) {
    super(context);
    this.number = number;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckFloat yuckFloat = (YuckFloat) o;
    return Float.compare(yuckFloat.number, number) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(number);
  }
}
