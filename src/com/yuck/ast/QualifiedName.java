package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;

import java.util.List;

public class QualifiedName extends Expression {
  public final ImmutableList<String> names;

  public QualifiedName(Token first, List<String> names, Token last) {
    super(first.startLine, first.startColumn, last.endLine, last.endColumn);
    this.names = ImmutableList.copyOf(names);
  }
}
