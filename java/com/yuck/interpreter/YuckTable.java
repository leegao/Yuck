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

  public YuckTable() {
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
}
