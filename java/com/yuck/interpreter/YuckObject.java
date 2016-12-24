package com.yuck.interpreter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class YuckObject {
  public abstract YuckObjectKind getKind();

  public static YuckObject translate(Object constant) {
    throw new NotImplementedException();
  }
}
