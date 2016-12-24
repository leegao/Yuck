package com.yuck.interpreter;

import com.google.gson.GsonBuilder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class YuckObject {
  public abstract YuckObjectKind getKind();
  private String type = getClass().getSimpleName();

  public static YuckObject translate(Object constant) {
    if (constant instanceof Integer) {
      return new YuckInteger((Integer) constant);
    } else if (constant instanceof Float) {
      return new YuckFloat((Float) constant);
    } else if (constant instanceof Boolean) {
      return new YuckBoolean((Boolean) constant);
    } else {
      throw new NotImplementedException();
    }
  }

  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
