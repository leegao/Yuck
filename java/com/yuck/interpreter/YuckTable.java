package com.yuck.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class YuckTable extends YuckObject {
  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.TABLE;
  }

  public Map<YuckObject, YuckObject> yuckObjectMap = new HashMap<>();

  public YuckTable(InterpreterContext context) {
    super(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckTable yuckTable = (YuckTable) o;
    return Objects.equals(yuckObjectMap, yuckTable.yuckObjectMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(yuckObjectMap);
  }

  @Override
  public YuckObject tableLoad(YuckObject key) {
    return yuckObjectMap.get(key);
  }

  @Override
  public void tableStore(YuckObject key, YuckObject val) {
    yuckObjectMap.put(key, val);
  }

  @Override
  public boolean isFilled() {
    return yuckObjectMap.size() != 0;
  }

  @Override
  public String toString() {
    return Objects.toString(yuckObjectMap);
  }
}
