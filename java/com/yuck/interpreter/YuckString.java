package com.yuck.interpreter;

import com.yuck.interpreter.builtins.NativeModule;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
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

  private YuckObject find(InterpreterContext context) {
    List<YuckObject> arguments = getArguments(context);
    if (arguments.size() == 1) {
      return wrap(string.indexOf(unwrap(arguments.get(0))));
    } else {
      return wrap(string.indexOf(this.<String>unwrap(arguments.get(0)), unwrap(arguments.get(1))));
    }
  }

  private YuckObject substring(InterpreterContext context) {
    List<YuckObject> arguments = getArguments(context);
    if (arguments.size() == 1) {
      return wrap(string.substring(unwrap(arguments.get(0))));
    } else {
      return wrap(string.substring(unwrap(arguments.get(0)), unwrap(arguments.get(1))));
    }
  }

  public void registerAll() {
    register("length", c -> wrap(string.length()), context);
    register("replace", c -> wrap(string.replace(unwrap(c.get(0)), unwrap(c.get(1)))), context);
    register("charAt", c -> wrap(String.valueOf(string.charAt(unwrap(c.get(0))))), context);
    register("contains", c -> wrap(string.contains(unwrap(c.get(0)))), context);
    register("startsWith", c -> wrap(string.startsWith(unwrap(c.get(0)))), context);
    register("endsWith", c -> wrap(string.endsWith(unwrap(c.get(0)))), context);
    register("find", this::find, context);
    register("trim", c -> wrap(string.trim()), context);
    register("substring", this::substring, context);
    register("format", c -> wrap(String.format(string, getArguments(c).toArray())), context);
    register(
        "repeat",
        c -> wrap(new String(new char[this.<Integer>unwrap(c.get(1))]).replace("\0", unwrap(c.get(0)))),
        context);
    register("lower", c -> wrap(string.toLowerCase()), context);
    register("upper", c -> wrap(string.toUpperCase()), context);
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
