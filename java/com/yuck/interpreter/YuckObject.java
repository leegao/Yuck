package com.yuck.interpreter;

import com.google.gson.GsonBuilder;
import com.yuck.ycode.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class YuckObject {
  protected YuckObject(InterpreterContext context) {
    this.context = context;
  }

  public abstract YuckObjectKind getKind();
  private final String type = getClass().getSimpleName();
  protected transient final InterpreterContext context;

  public static YuckObject translate(Object constant, InterpreterContext context) {
    if (constant instanceof Integer) {
      return new YuckInteger((Integer) constant, context);
    } else if (constant instanceof Float) {
      return new YuckFloat((Float) constant, context);
    } else if (constant instanceof Boolean) {
      return new YuckBoolean((Boolean) constant, context);
    } else if (constant instanceof String) {
      return new YuckString((String) constant, context);
    } else {
      throw new NotImplementedException();
    }
  }

  @Override
  public abstract boolean equals(Object other);

  @Override
  public abstract int hashCode();

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }

  public YuckObject tableLoad(YuckObject key) {
    throw new NotImplementedException();
  }

  public void tableStore(YuckObject key, YuckObject val) {
    throw new NotImplementedException();
  }

  public YuckObject binary(Opcode opcode, YuckObject other) {
    throw new NotImplementedException();
  }
}
