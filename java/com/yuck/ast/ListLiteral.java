package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class ListLiteral extends Expression {
  public final ImmutableList<Expression> list;

  public ListLiteral(Token lbracket, List<Expression> list, Token rbracket) {
    super(lbracket.startLine, lbracket.startColumn, rbracket.endLine, rbracket.endColumn);
    this.list = ImmutableList.copyOf(list);
  }
}
