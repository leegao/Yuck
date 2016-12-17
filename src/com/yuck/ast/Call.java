package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class Call extends Expression {
  public final ImmutableList<Expression> arguments;

  public Call(Expression left, List<Expression> arguments, Token right) {
    super(left.getStartLine(), left.getStartColumn(), right.line, right.column);
    this.arguments = ImmutableList.copyOf(arguments);
  }
}
