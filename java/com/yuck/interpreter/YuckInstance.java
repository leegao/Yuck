package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.yuck.ycode.YCodeClass;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class YuckInstance extends YuckObject {
  public final YuckClass clazz;
  public transient final YCodeFunction outer;
  public final Map<String, YuckObject> fields = new HashMap<>();
  public transient final InterpreterContext instanceContext;
  public transient final List<YuckInstance> supers = new ArrayList<>();

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
    for (YCodeClass superClass : clazz.yClass.supers(clazz.function)) {
      supers.add(new YuckInstance(new YuckClass(superClass, context, clazz.function), clazz.function, context));
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

  public Optional<YuckInstance> hasField(String field) {
    if (clazz.yClass.fields.contains(field)) {
      return Optional.of(this);
    }
    for (YuckInstance instance : supers) {
      Optional<YuckInstance> bundle = instance.hasField(field);
      if (bundle.isPresent()) {
        return bundle;
      }
    }
    return Optional.empty();
  }

  @Override
  public YuckObject getField(String field) {
    Optional<YuckInstance> instance = hasField(field);
    if (instance.isPresent()) {
      return instance.get().fields.get(field);
    }
    throw new NotImplementedException();
  }

  @Override
  public YuckObject putField(String field, YuckObject object) {
    Optional<YuckInstance> instance = hasField(field);
    if (instance.isPresent()) {
      return instance.get().fields.put(field, object);
    }
    throw new NotImplementedException();
  }
}
