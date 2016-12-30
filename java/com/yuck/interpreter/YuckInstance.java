package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.yuck.ycode.YCodeFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class YuckInstance extends YuckObject {
  public final YuckClass clazz;
  public transient final YCodeFunction outer;
  public final Map<String, YuckObject> fields = new HashMap<>();
  public transient final InterpreterContext instanceContext;

  public YuckInstance(YuckClass clazz, YCodeFunction outer, InterpreterContext context) {
    super(context);
    instanceContext = new InterpreterContext(context, this);
    this.clazz = clazz;
    this.outer = outer;
    for (String field : clazz.yClass.fields) {
      fields.put(field, new YuckNil(instanceContext));
    }
    for (Map.Entry<String, Integer> method : clazz.yClass.methods.entrySet()) {
      fields.put(method.getKey(), new YuckFunction(outer.functions.inverse().get(method.getValue()), instanceContext));
    }
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.OBJECT;
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz);
  }

  @Override
  public YuckObject getField(String field) {
    Preconditions.checkArgument(clazz.yClass.fields.contains(field));
    return fields.get(field);
  }

  @Override
  public YuckObject putField(String field, YuckObject object) {
    Preconditions.checkArgument(clazz.yClass.fields.contains(field));
    return fields.put(field, object);
  }
}
