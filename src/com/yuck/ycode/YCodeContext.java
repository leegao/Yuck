package com.yuck.ycode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class YCodeContext {
  public final BiMap<String, Integer> locals = HashBiMap.create();
  public final BiMap<String, Integer> upValues;
  public final BiMap<Object, Integer> constants = HashBiMap.create();
  public final BiMap<String, Integer> labels = HashBiMap.create();

  public YCodeContext(BiMap<String, Integer> upValues) {
    this.upValues = upValues;
  }

  public int constant(Object o) {
    if (constants.containsKey(o)) {
      return constants.get(o);
    }
    int n = constants.size();
    constants.put(o, n);
    return n;
  }

  public int local(String var) {
    if (locals.containsKey(var)) {
      return locals.get(var);
    }
    int n = locals.size();
    locals.put(var, n);
    return n;
  }
}
