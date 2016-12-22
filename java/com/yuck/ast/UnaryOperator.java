package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

public class UnaryOperator extends Expression {
  public final String operator;
  public final Expression expression;
  public UnaryOperator(Token operator, Expression expression) {
    super(operator.startLine, operator.startColumn, expression.getEndLine(), expression.getEndColumn());
    this.operator = operator.text;
    this.expression = expression;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    switch (operator) {
      case "-":
        return expression.compile(function, scope).emit(Opcode.NEG);
      case "not":
        return expression.compile(function, scope).emit(Opcode.NOT);
    }
    throw new IllegalStateException();
  }
}
