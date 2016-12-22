package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class Selector extends Expression {
  public final Expression left;
  public final String select;

  public Selector(Expression left, Token select) {
    super(left.getStartLine(), left.getStartColumn(), select.endLine, select.endColumn);
    this.left = left;
    this.select = select.text;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    left.compile(function, context);
    return function.emit(Opcode.GET_FIELD, select);
  }
}
