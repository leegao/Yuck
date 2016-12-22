package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

import java.util.List;

public class Call extends Expression {
  private final Expression left;
  public final ImmutableList<Expression> arguments;

  public Call(Expression left, List<Expression> arguments, Token right) {
    super(left.getStartLine(), left.getStartColumn(), right.endLine, right.endColumn);
    this.left = left;
    this.arguments = ImmutableList.copyOf(arguments);
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext compilationContext) {
    left.compile(function, compilationContext);
    arguments.forEach(expr -> expr.compile(function, compilationContext));
    return function.emit(Opcode.CALL, arguments.size() + 1);
  }
}
