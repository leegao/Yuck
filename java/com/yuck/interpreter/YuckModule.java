package com.yuck.interpreter;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class YuckModule extends YuckObject{
  public final Map<String, YuckObject> map;

  protected YuckModule(Map<String, YuckObject> map, InterpreterContext context) {
    super(context);
    this.map = map;
  }

  protected YuckModule(InterpreterContext context) {
    super(context);
    this.map = new HashMap<>();
  }


  public static List<YuckObject> getArguments(InterpreterContext context) {
    List<YuckObject> arguments = new ArrayList<>();
    for (int i = 0; i < context.locals.size(); i++) {
      arguments.add(context.locals.get(i));
    }
    return arguments;
  }

  public static YuckModule from(InterpreterContext result, InterpreterContext context) {
    Map<String, YuckObject> map = new HashMap<>();
    for (Map.Entry<String, Integer> entry : result.localNames.entrySet()) {
      map.put(entry.getKey(), result.get(entry.getValue()));
    }
    return new YuckModule(map, context);
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.OBJECT;
  }

  @Override
  public boolean equals(Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  protected void register(String name, Function<InterpreterContext, YuckObject> function, InterpreterContext context) {
    map.put(name, new NativeFunction(function, context));
  }

  protected static void registerLocal(
      String name,
      Function<InterpreterContext, YuckObject> function,
      InterpreterContext context) {
    context.add(context.locals.size(), name, new NativeFunction(function, context));
  }

  @Override
  public YuckObject getField(String field) {
    Preconditions.checkArgument(map.containsKey(field));
    return map.get(field);
  }
}
