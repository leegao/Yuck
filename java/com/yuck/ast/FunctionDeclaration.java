package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionDeclaration extends Statement {
  public final String id;
  public final ImmutableList<String> parameters;
  public final ImmutableList<Statement> statements;

  public FunctionDeclaration(Token function, Token id, List<Var> parameters, List<Statement> statements, Token right) {
    super(function.startLine, function.startColumn, right.endLine, right.endColumn);
    this.id = id.text;
    this.parameters = ImmutableList.copyOf(
        parameters.stream().map(var -> var.id).collect(Collectors.toList()));
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    YCodeCompilationContext nestedContext = new YCodeCompilationContext(statements, context.name + "." + id, parameters);
    YCodeFunction func = nestedContext.compile().emit(Opcode.NIL).emit(Opcode.RETURN);
    return function.emit(Opcode.CLOSURE, func).emit(Opcode.STORE_LOCAL, context.getScope().addLocal(id));
  }
}
