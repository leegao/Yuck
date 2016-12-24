package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.Opcode;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class Var extends Expression {
  public final String id;
  public Token token;

  public Var(Token id) {
    super(id.startLine, id.startColumn, id.endLine, id.endColumn);
    this.id = id.text;
    this.token = id;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    String variable = context.lookup(id).orElse(id);
    Opcode opcode = Instruction.variable(function, variable);
    return function.emit(opcode, variable);
  }
}
