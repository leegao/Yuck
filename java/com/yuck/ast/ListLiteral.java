package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

import java.util.List;

public class ListLiteral extends Expression {
  public final ImmutableList<Expression> list;

  public ListLiteral(Token lbracket, List<Expression> list, Token rbracket) {
    super(lbracket.startLine, lbracket.startColumn, rbracket.endLine, rbracket.endColumn);
    this.list = ImmutableList.copyOf(list);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    list.forEach(expression -> expression.compile(function, context));
    return function.emit(Opcode.LIST, list.size());
  }
}
