package com.yuck.interpreter;

import com.yuck.Yuck;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class YuckInstance extends YuckObject {
  public final YuckClass clazz;
  public final YCodeFunction outer;
  public final Map<String, YuckObject> fields = new HashMap<>();
  public transient final InterpreterContext instanceContext;
  public final List<YuckInstance> supers = new ArrayList<>();

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
    for (YuckClass superClass : clazz.extensions) {
      supers.add(new YuckInstance(superClass, superClass.function, superClass.context));
    }
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.OBJECT;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof YuckObject) {
      Optional<YuckObject> result = invoke("equals", (YuckObject) o);
      if (result.isPresent()) return result.get().isFilled();
    }
    return this == o;
  }

  @Override
  public int hashCode() {
    Optional<YuckObject> result = invoke("hash");
    if (result.isPresent()) {
      YuckObject yuckObject = result.get();
      if (yuckObject instanceof YuckInteger) {
        return ((YuckInteger) yuckObject).number;
      }
    }
    return clazz.hashCode();
  }

  public Optional<YuckObject> invoke(String name, YuckObject... arguments) {
    Optional<YuckInstance> field = hasField(name);
    if (field.isPresent()) {
      YuckInstance base = field.get();
      YuckObject method = base.getField(name);
      if (method instanceof YuckFunction) {
        InterpreterContext newContext = new InterpreterContext(instanceContext, this);
        for (int i = 0; i < arguments.length; i++) {
          String parameter = ((YuckFunction) method).function.locals.inverse().get(i);
          newContext.add(i, parameter != null ? parameter : "param@" + i, arguments[i]);
        }
        InterpreterContext result = Interpreter.interpret(((YuckFunction) method).function, newContext);
        return Optional.of(result.pop());
      }
    }
    return Optional.empty();
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

  public Optional<YuckInstance> getSuper(String name) {
    if (this.clazz.yClass.name.equals(name)) {
      return Optional.of(this);
    }
    for (YuckInstance superInstance : supers) {
      Optional<YuckInstance> instance = superInstance.getSuper(name);
      if (instance.isPresent()) {
        return instance;
      }
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    Optional<YuckObject> result = invoke("toString");
    if (result.isPresent()) {
      YuckObject yuckObject = result.get();
      if (yuckObject instanceof YuckString) {
        return ((YuckString) yuckObject).string;
      }
    }
    return super.toString();
  }

  @Override
  public YuckObject tableLoad(YuckObject key) {
    return super.tableLoad(key);
  }

  @Override
  public void tableStore(YuckObject key, YuckObject val) {
    super.tableStore(key, val);
  }

  @Override
  public YuckObject binary(Opcode opcode, YuckObject other) {
    return super.binary(opcode, other);
  }

  @Override
  public boolean isFilled() {
    return super.isFilled();
  }
}
