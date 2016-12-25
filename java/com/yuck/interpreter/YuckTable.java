package com.yuck.interpreter;

import java.util.HashMap;
import java.util.Map;

public class YuckTable extends YuckObject {
  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.TABLE;
  }

  public Map<YuckObject, YuckObject> yuckObjectMap = new HashMap<>();

  public YuckTable() {
  }
}
