package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import javafx.util.Pair;

import java.util.List;

public class MapLiteral extends Expression {
  public final ImmutableList<Pair<Expression, Expression>> terms;

  public MapLiteral(Token left, List<Pair<Expression,Expression>> terms, Token right) {
    super(left.line, left.column, right.line, right.column);
    this.terms = ImmutableList.copyOf(terms);
  }
}
