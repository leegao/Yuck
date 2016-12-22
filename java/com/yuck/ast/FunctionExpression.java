package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionExpression extends Expression {
  public final ImmutableList<String> parameters;
  public final ImmutableList<Statement> statements;

  public FunctionExpression(Token left, List<Var> parameters, List<Statement> statements, Token right) {
    super(left.startLine, left.startColumn, right.endLine, right.endColumn);

    this.parameters = ImmutableList.copyOf(
        parameters.stream().map(var -> var.id).collect(Collectors.toList()));
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    YCodeFunction func = new YCodeFunction(parameters);
    YCodeCompilationContext newCompilationContext = new YCodeCompilationContext();
    try (YCodeCompilationContext.Scope scope = newCompilationContext.push()) {
      statements.forEach(statement -> statement.compile(func, newCompilationContext));
      func.emit(Opcode.NIL).emit(Opcode.RETURN).assemble();
    }
    return function.emit(Opcode.CLOSURE, func);
  }
}
