package com.yuck.interpreter;

import com.yuck.ycode.YCodeFunction;

import java.util.Objects;

public class YuckFunction extends YuckObject {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckFunction that = (YuckFunction) o;
    return Objects.equals(function, that.function);
  }

  @Override
  public int hashCode() {
    return Objects.hash(function);
  }

  public final YCodeFunction function;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.FUNCTION;
  }

  public YuckFunction(YCodeFunction function, InterpreterContext context) {
    this.function = function;
  }
}
