package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionExpression extends Expression {
  private final Token start;
  public final ImmutableList<String> parameters;
  public final ImmutableList<Statement> statements;

  public FunctionExpression(Token left, List<Var> parameters, List<Statement> statements, Token right) {
    super(left.startLine, left.startColumn, right.endLine, right.endColumn);
    start = left;
    this.parameters = ImmutableList.copyOf(
        parameters.stream().map(var -> var.id).collect(Collectors.toList()));
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    YCodeCompilationContext nestedContext = new YCodeCompilationContext(
        statements,
        context.name + String.format(".(anonymous function at line %s:%s)", start.startLine, start.startColumn),
        parameters);
    return function.emit(Opcode.CLOSURE, nestedContext.compile().emit(Opcode.NIL).emit(Opcode.RETURN));
  }
}
