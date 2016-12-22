package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
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
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    left.compile(function, scope);
    index.compile(function, scope);
    return function.emit(Opcode.TABLE_LOAD);
  }
}
