package com.yuck.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YuckList extends YuckObject {
  public YuckList(InterpreterContext context) {
    super(context);
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.LIST;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckList yuckList = (YuckList) o;
    return Objects.equals(list, yuckList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  public final List<YuckObject> list = new ArrayList<>();

  public void add(YuckObject yuckObject) {
    list.add(yuckObject);
  }

  @Override
  public YuckObject tableLoad(YuckObject key) {
    if (key instanceof YuckInteger) {
      return list.get(((YuckInteger) key).number);
    }
    return super.tableLoad(key);
  }

  @Override
  public void tableStore(YuckObject key, YuckObject val) {
    if (key instanceof YuckInteger) {
      list.set(((YuckInteger) key).number, val);
      return;
    }
    super.tableStore(key, val);
  }

  @Override
  public boolean isFilled() {
    return list.size() != 0;
  }
}
