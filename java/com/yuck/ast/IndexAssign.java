package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

public class IndexAssign extends Statement {
  public final Expression left;
  public final Expression index;
  public final Expression assignee;

  public IndexAssign(Expression left, Expression index, Expression assignee, Token semi) {
    super(left.getStartLine(), left.getStartColumn(), semi.endLine, semi.endColumn);
    this.left = left;
    this.index = index;
    this.assignee = assignee;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    left.compile(function, scope);
    index.compile(function, scope);
    assignee.compile(function, scope);
    return function.emit(Opcode.TABLE_STORE);
  }
}
