package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class NewExpression extends Expression {
  public final QualifiedName name;
  public final ImmutableList<Expression> arguments;

  public NewExpression(Token start, QualifiedName name, List<Expression> arguments, Token close) {
    super(start.line, start.column, close.line, close.column);
    this.name = name;
    this.arguments = ImmutableList.copyOf(arguments);
  }
}
