package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Interpreter {
  public InterpreterContext interpret(YCodeFunction function, InterpreterContext context) {
    int pc = 0;
    while (true) {
      Preconditions.checkArgument(pc >= 0);
      if (pc >= function.instructions.size()) {
        // PUSH a nil for falling off of the function
        return context;
      }
      Instruction instruction = function.instructions.get(pc);
      int next = pc + 1;
      switch (instruction.opcode) {
        case RETURN:
          Preconditions.checkArgument(context.stack.size() == 1);
          return context;
        case LOAD_CONST:
          Object constant = function.constants.inverse().get(instruction.getArgument());
          YuckObject yuckConstant = YuckObject.translate(constant);
          context.push(yuckConstant);
          break;
        default:
          throw new NotImplementedException();
      }
      pc = next;
    }
  }
}
