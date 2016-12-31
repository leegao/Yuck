package com.yuck.interpreter;

import java.util.function.Function;

public class NativeFunction extends YuckObject {
  public final Function<InterpreterContext, YuckObject> function;

  public NativeFunction(Function<InterpreterContext, YuckObject> function, InterpreterContext context) {
    super(context);
    this.function = function;
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.FUNCTION;
  }

  @Override
  public boolean equals(Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
