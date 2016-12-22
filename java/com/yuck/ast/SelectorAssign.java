package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

public class SelectorAssign extends Statement {
  public final Expression left;
  public final String select;
  public final Expression assignee;

  public SelectorAssign(Expression left, String select, Expression assignee, Token semi) {
    super(left.getStartLine(), left.getStartColumn(), semi.endLine, semi.endColumn);
    this.left = left;
    this.select = select;
    this.assignee = assignee;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext compilationContext) {
    left.compile(function, compilationContext);
    assignee.compile(function, compilationContext);
    return function.emit(Opcode.PUT_FIELD, select);
  }
}
