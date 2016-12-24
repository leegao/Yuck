package com.yuck.interpreter;

import java.util.ArrayList;
import java.util.List;

public class YuckList extends YuckObject {
  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.LIST;
  }

  public final List<YuckObject> list = new ArrayList<>();

  public void add(YuckObject yuckObject) {
    list.add(yuckObject);
  }
}
