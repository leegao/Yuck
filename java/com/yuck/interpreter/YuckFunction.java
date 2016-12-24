package com.yuck.interpreter;

import com.yuck.ycode.YCodeFunction;

public class YuckFunction extends YuckObject {
  public final YCodeFunction function;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.FUNCTION;
  }

  public YuckFunction(YCodeFunction function, InterpreterContext context) {
    this.function = function;
  }
}
