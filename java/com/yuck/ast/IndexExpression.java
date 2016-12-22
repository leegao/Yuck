package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;

public class IndexExpression extends Expression {
  public final Expression left;
  public final Expression index;

  public IndexExpression(Expression left, Expression index, Token right) {
    super(left.getStartLine(), left.getStartColumn(), right.endLine, right.endColumn);
    this.left = left;
    this.index = index;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    left.compile(context);
    index.compile(context);
    return context.emit(Opcode.TABLE_LOAD);
  }
}
