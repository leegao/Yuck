package com.yuck.interpreter;

import java.util.Objects;

public class YuckString extends YuckObject {
  public final String string;

  public YuckString(String string, InterpreterContext context) {
    super(context);
    this.string = string;
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.STRING;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckString that = (YuckString) o;
    return Objects.equals(string, that.string);
  }

  @Override
  public int hashCode() {
    return Objects.hash(string);
  }

  @Override
  public boolean isFilled() {
    return string.length() != 0;
  }


  @Override
  public String toString() {
    return string;
  }
}
