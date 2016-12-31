package com.yuck.interpreter;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class YuckModule extends YuckObject{
  public final Map<String, YuckObject> map;

  protected YuckModule(Map<String, YuckObject> map, InterpreterContext context) {
    super(context);
    this.map = map;
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

  @Override
  public YuckObject getField(String field) {
    Preconditions.checkArgument(map.containsKey(field));
    return map.get(field);
  }
}
