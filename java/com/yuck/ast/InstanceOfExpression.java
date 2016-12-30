package com.yuck.ast;

import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunction;

public class InstanceOfExpression extends Expression {
  public final Expression left;
  public final String name;

  public InstanceOfExpression(Expression left, Token name) {
    super(left.getStartLine(), left.getStartColumn(), name.endLine, name.endColumn);
    this.left = left;
    this.name = name.text;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    return left.compile(function, context).emit(Opcode.INSTANCEOF, name);
  }
}
