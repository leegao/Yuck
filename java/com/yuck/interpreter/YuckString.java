package com.yuck.interpreter;

import com.yuck.interpreter.builtins.NativeModule;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Objects;

public class YuckString extends YuckModule implements NativeModule {
  public final String string;

  public YuckString(String string, InterpreterContext context) {
    super(context);
    this.string = string;
    registerAll();
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

  public void registerAll() {
    register("length", c -> wrap(string.length()), context);
    register("replace", c -> wrap(string.replace(unwrap(c.get(0)), unwrap(c.get(1)))), context);
  }

  private YuckObject wrap(Object object) {
    if (object instanceof Integer) {
      return new YuckInteger((Integer) object, context);
    } else if (object instanceof Float) {
      return new YuckFloat((Float) object, context);
    } else if (object instanceof Double) {
      return new YuckFloat(((Double) object).floatValue(), context);
    } else if (object instanceof String) {
      return new YuckString((String) object, context);
    } else {
      throw new NotImplementedException();
    }
  }

  private <T> T unwrap(YuckObject yuckObject) {
    if (yuckObject instanceof YuckString) {
      return (T) ((YuckString) yuckObject).string;
    } else {
      throw new NotImplementedException();
    }
  }
}
