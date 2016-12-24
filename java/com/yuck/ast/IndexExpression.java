package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class IndexExpression extends Expression {
  public final Expression left;
  public final Expression index;

  public IndexExpression(Expression left, Expression index, Token right) {
    super(left.getStartLine(), left.getStartColumn(), right.endLine, right.endColumn);
    this.left = left;
    this.index = index;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    left.compile(function, context);
    index.compile(function, context);
    return function.emit(Opcode.TABLE_LOAD);
  }
}
