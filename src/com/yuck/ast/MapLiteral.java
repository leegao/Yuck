package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import javafx.util.Pair;

import java.util.List;

public class MapLiteral extends Expression {
  public final ImmutableList<Pair<Expression, Expression>> terms;

  public MapLiteral(Token left, List<Pair<Expression,Expression>> terms, Token right) {
    super(left.startLine, left.startColumn, right.endLine, right.endColumn);
    this.terms = ImmutableList.copyOf(terms);
  }
}
