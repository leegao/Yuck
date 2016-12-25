package com.yuck.ycode;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YCodeFunction {
  public final BiMap<String, Integer> locals = HashBiMap.create();
  public final BiMap<String, Integer> upvalues = HashBiMap.create();
  public final BiMap<Object, Integer> constants = HashBiMap.create();
  public transient final BiMap<String, Integer> labels = HashBiMap.create();
  public final List<Instruction> instructions = new ArrayList<>();
  public transient final List<Integer> labelPositions = new ArrayList<>();
  public final BiMap<Instruction, Integer> instructionPositions = HashBiMap.create();
  public final BiMap<YCodeFunction, Integer> functions = HashBiMap.create();
  public final String name;

  public YCodeFunction(List<String> arguments, String name) {
    for (String argument : arguments) {
      local(argument);
    }
    this.name = name;
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

  public int upvalue(String var) {
    if (upvalues.containsKey(var)) {
      return upvalues.get(var);
    }
    int n = upvalues.size();
    upvalues.put(var, n);
    return n;
  }

  public int label(String label) {
    if (labels.containsKey(label)) {
      return labels.get(label);
    }
    int n = labels.size() + 1;
    labels.put(label, n);
    return n;
  }

  public YCodeFunction emit(Opcode opcode, Object data) {
    Instruction instruction = Instruction.make(this, opcode, data);
    int position = instructions.size();
    instructions.add(instruction);
    if (opcode == Opcode.NOP && instruction.getArgument() != 0) {
      labelPositions.add(position);
    }
    instructionPositions.put(instruction, position);
    return this;
  }

  public YCodeFunction emit(Opcode opcode) {
    return emit(opcode, 0);
  }

  public int position(Instruction instruction) {
    Preconditions.checkArgument(instructionPositions.containsKey(instruction));
    return instructionPositions.get(instruction);
  }

  public List<Instruction> assemble() {
    for (Instruction instruction : instructions) {
      instruction.fixup();
    }
    return instructions;
  }

  public int function(YCodeFunction function) {
    Preconditions.checkArgument(!functions.containsKey(function));
    int n = functions.size();
    functions.put(function, n);
    return n;
  }

  public DataOutputStream write(DataOutputStream buffer) throws IOException {
    // Layout:
    // MAGIC: ycode
    // Locals
    // Upvalues
    // Constants
    // Instructions
    // Functions
    // Classes
    // where each list is headed by the number of items
    buffer.write(new byte[] {0, 'y', 'c', 'o', 'd', 'e'});
    // Name
    Utils.writeString(buffer, name);
    // Locals
    buffer.writeShort(locals.size());
    for (int i = 0; i < locals.size(); i++) {
      Preconditions.checkArgument(locals.inverse().containsKey(i));
      String local = locals.inverse().get(i);
      Utils.writeString(buffer, local);
    }
    // Upvalues
    buffer.writeShort(upvalues.size());
    for (int i = 0; i < upvalues.size(); i++) {
      Preconditions.checkArgument(upvalues.inverse().containsKey(i));
      Utils.writeString(buffer, upvalues.inverse().get(i));
    }
    // Constants
    buffer.writeShort(constants.size());
    for (int i = 0; i < constants.size(); i++) {
      Preconditions.checkArgument(constants.inverse().containsKey(i));
      Utils.writeConstant(buffer, constants.inverse().get(i));
    }
    // Instructions
    buffer.writeShort(instructions.size());
    for (Instruction instruction : assemble()) {
      instruction.write(buffer);
    }
    // Functions
    buffer.writeShort(functions.size());
    for (int i = 0; i < functions.size(); i++) {
      Preconditions.checkArgument(functions.inverse().containsKey(i));
      YCodeFunction function = functions.inverse().get(i);
      function.write(buffer);
    }
    // Classes
    buffer.writeShort(0);
    return buffer;
  }

  public static YCodeFunction read(DataInputStream buffer) throws IOException {
    byte[] header = new byte[6];
    buffer.read(header);
    Preconditions.checkArgument(
        new String(header).equals("\0ycode"),
        String.format("'%s': Header should be '\\0ycode'", new String(header)));
    String name = Utils.readString(buffer);
    YCodeFunction function = new YCodeFunction(new ArrayList<>(), name);
    // Locals
    int numLocals = buffer.readShort();
    for (int i = 0; i < numLocals; i++) {
      function.locals.put(Utils.readString(buffer), i);
    }
    // Upvalues
    int numUpvalues = buffer.readShort();
    for (int i = 0; i < numUpvalues; i++) {
      function.upvalues.put(Utils.readString(buffer), i);
    }
    // Constants
    int numConstants = buffer.readShort();
    for (int i = 0; i < numConstants; i++) {
      function.constants.put(Utils.readConstant(buffer), i);
    }
    // Instructions
    int numInstrunctions = buffer.readShort();
    for (int i = 0; i < numInstrunctions; i++) {
      Instruction instruction = Instruction.read(function, buffer);
      function.instructions.add(instruction);
      function.instructionPositions.put(instruction, i);
    }
    // Functions
    int numFunctions = buffer.readShort();
    for (int i = 0; i < numFunctions; i++) {
      function.functions.put(YCodeFunction.read(buffer), i);
    }
    // Classes
    Preconditions.checkArgument(buffer.readShort() == 0);
    return function;
  }

  public String fvs(String prefix) {
    int n = 0;
    String name = prefix + "$" + n++;
    while (locals.containsKey(name)) {
      name = prefix + "$" + n++;
    }
    return name;
  }

  public String flx(String prefix) {
    int n = 0;
    String name = prefix + "$" + n++;
    while (labels.containsKey(name)) {
      name = prefix + "$" + n++;
    }
    return name;
  }
}
