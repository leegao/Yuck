package com.yuck.ast;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;

import java.util.List;

public class NewExpression extends Expression {
  public final QualifiedName name;
  public final ImmutableList<Expression> arguments;

  public NewExpression(Token start, QualifiedName name, List<Expression> arguments, Token close) {
    super(start.startLine, start.startColumn, close.endLine, close.endColumn);
    this.name = name;
    this.arguments = ImmutableList.copyOf(arguments);
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    context.emit(Opcode.NEW, Joiner.on('.').join(name.names))
        .emit(Opcode.DUP)
        .emit(Opcode.GET_FIELD, "init");
    arguments.forEach(expression -> expression.compile(context));
    return context.emit(Opcode.CALL, arguments.size() + 1).emit(Opcode.POP);
  }
}
