package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.yuck.ycode.YCodeClass;
import com.yuck.ycode.YCodeFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YuckClass extends YuckObject {
  public final YCodeClass yClass;
  public final YCodeFunction function;
  public List<YuckClass> extensions = new ArrayList<>();

  public YuckClass(YCodeClass yClass, InterpreterContext context, YCodeFunction function) {
    super(context);
    this.yClass = yClass;
    this.function = function;
    for (String extension : yClass.extensions) {
      if (yClass.localExtension.containsKey(extension)) {
        YuckObject yuckObject = context.get(yClass.localExtension.get(extension));
        Preconditions.checkArgument(yuckObject instanceof YuckClass);
        extensions.add((YuckClass) yuckObject);
      } else {
        int up = yClass.upvalueExtension.get(extension);
        YuckObject yuckObject = context.lookup(function.upvalues.inverse().get(up));
        Preconditions.checkArgument(yuckObject instanceof YuckClass);
        extensions.add((YuckClass) yuckObject);
      }
    }
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
