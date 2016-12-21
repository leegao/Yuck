package com.yuck.ycode;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class YCodeFunctionContext {
  public final BiMap<String, Integer> locals = HashBiMap.create();
  public final BiMap<String, Integer> upvalues = HashBiMap.create();
  public final BiMap<Object, Integer> constants = HashBiMap.create();
  public final BiMap<String, Integer> labels = HashBiMap.create();
  public final List<Instruction> instructions = new ArrayList<>();
  public final List<Integer> labelPositions = new ArrayList<>();
  public final BiMap<Instruction, Integer> instructionPositions = HashBiMap.create();
  public final BiMap<YCodeFunctionContext, Integer> functions = HashBiMap.create();

  public YCodeFunctionContext(List<String> arguments) {
    for (String argument : arguments) {
      local(argument);
    }
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
    int n = labels.size();
    locals.put(label, n);
    return n;
  }

  public void emit(Opcode opcode, Object data) {
    Instruction instruction = Instruction.make(this, opcode, data);
    int position = instructions.size();
    instructions.add(instruction);
    if (opcode == Opcode.NOP && instruction.getArgument() != 0) {
      labelPositions.add(position);
    }
    instructionPositions.put(instruction, position);
  }

  public void emit(Opcode opcode) {
    emit(opcode, 0);
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

  public int function(YCodeFunctionContext function) {
    Preconditions.checkArgument(functions.containsKey(function));
    int n = functions.size();
    functions.put(function, n);
    return n;
  }

  public ByteBuffer write(ByteBuffer buffer) {
    // Layout:
    // MAGIC: ycode
    // Locals
    // Upvalues
    // Constants
    // Instructions
    // Functions
    // Classes
    // where each list is headed by the number of items
    buffer.putChar('y').putChar('c').putChar('o').putChar('d').putChar('e');
    // Locals
    buffer.putInt(locals.size());
    for (int i = 0; i < locals.size(); i++) {
      Preconditions.checkArgument(locals.inverse().containsKey(i));
      String local = locals.inverse().get(i);
      Utils.putString(buffer, local);
    }
    // Upvalues
    buffer.putInt(upvalues.size());
    for (int i = 0; i < upvalues.size(); i++) {
      Preconditions.checkArgument(upvalues.inverse().containsKey(i));
      Utils.putString(buffer, upvalues.inverse().get(i));
    }
    // Constants
    buffer.putInt(constants.size());
    for (int i = 0; i < constants.size(); i++) {
      Preconditions.checkArgument(constants.inverse().containsKey(i));
      Utils.putConstant(buffer, constants.inverse().get(i));
    }
    // Instructions
    buffer.putInt(instructions.size());
    for (Instruction instruction : assemble()) {
      instruction.write(buffer);
    }
    // Functions
    buffer.putInt(functions.size());
    for (int i = 0; i < functions.size(); i++) {
      Preconditions.checkArgument(functions.inverse().containsKey(i));
      YCodeFunctionContext function = functions.inverse().get(i);
      function.write(buffer);
    }
    // Classes
    buffer.putInt(0);
    return buffer;
  }

  public static YCodeFunctionContext read(ByteBuffer buffer) {
    Preconditions.checkArgument(buffer.getChar() == 'y');
    Preconditions.checkArgument(buffer.getChar() == 'c');
    Preconditions.checkArgument(buffer.getChar() == 'o');
    Preconditions.checkArgument(buffer.getChar() == 'd');
    Preconditions.checkArgument(buffer.getChar() == 'e');
    YCodeFunctionContext context = new YCodeFunctionContext(new ArrayList<>());
    // Locals
    int numLocals = buffer.getInt();
    for (int i = 0; i < numLocals; i++) {
      context.locals.put(Utils.getString(buffer), i);
    }
    // Upvalues
    int numUpvalues = buffer.getInt();
    for (int i = 0; i < numUpvalues; i++) {
      context.upvalues.put(Utils.getString(buffer), i);
    }
    // Constants
    int numConstants = buffer.getInt();
    for (int i = 0; i < numConstants; i++) {
      context.constants.put(Utils.getConstant(buffer), i);
    }
    // Instructions
    int numInstrunctions = buffer.getInt();
    for (int i = 0; i < numInstrunctions; i++) {
      context.instructions.add(Instruction.read(context, buffer));
    }
    // Functions
    int numFunctions = buffer.getInt();
    for (int i = 0; i < numFunctions; i++) {
      context.functions.put(YCodeFunctionContext.read(buffer), i);
    }
    // Classes
    Preconditions.checkArgument(buffer.getInt() == 0);
    return context;
  }
}
