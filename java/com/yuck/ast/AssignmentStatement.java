package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class AssignmentStatement extends Statement {
  public final String id;
  public final Expression expr;

  public AssignmentStatement(Token id, Expression expr, Token semi) {
    super(id.startLine, id.startColumn, semi.endLine, semi.endColumn);
    this.id = id.text;
    this.expr = expr;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    String variable = context.lookup(id).orElse(id);
    Opcode opcode = Instruction.variable(function, variable).plus(1);
    return expr.compile(function, context).emit(opcode, variable);
  }
}
