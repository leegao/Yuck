package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

public class Selector extends Expression {
  public final Expression left;
  public final String select;

  public Selector(Expression left, Token select) {
    super(left.getStartLine(), left.getStartColumn(), select.endLine, select.endColumn);
    this.left = left;
    this.select = select.text;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    left.compile(function, scope);
    return function.emit(Opcode.GET_FIELD, select);
  }
}
