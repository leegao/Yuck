package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.grammar.Token;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    if (name.names.size() != 1) {
      throw new NotImplementedException();
    }

    String id = name.names.get(0);
    String variable = context.lookup(id).orElse(id);
    function.emit(Instruction.variable(function, variable), variable)
        .emit(Opcode.NEW)
        .emit(Opcode.DUP)
        .emit(Opcode.GET_FIELD, "init");
    arguments.forEach(expression -> expression.compile(function, context));
    return function.emit(Opcode.CALL, arguments.size() + 1).emit(Opcode.POP);
  }
}
