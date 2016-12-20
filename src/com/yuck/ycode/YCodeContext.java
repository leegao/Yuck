package com.yuck.ycode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.List;

public class YCodeContext {
  public final BiMap<String, Integer> locals = HashBiMap.create();
  public final BiMap<String, Integer> upValues;
  public final BiMap<Object, Integer> constants = HashBiMap.create();
  public final BiMap<String, Integer> labels = HashBiMap.create();
  public final List<Instruction> instructions = new ArrayList<>();

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

  public int label(String label) {
    if (labels.containsKey(label)) {
      return labels.get(label);
    }
    int n = labels.size();
    locals.put(label, n);
    return n;
  }

  public void emit(Opcode opcode, Object data) {
    Instruction instruction = Instruction.make(this, opcode, data);
    this.instructions.add(instruction);
  }

  public void emit(Opcode opcode) {
    emit(opcode, 0);
  }
}
