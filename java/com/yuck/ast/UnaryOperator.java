package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;

public class UnaryOperator extends Expression {
  public final String operator;
  public final Expression expression;
  public UnaryOperator(Token operator, Expression expression) {
    super(operator.startLine, operator.startColumn, expression.getEndLine(), expression.getEndColumn());
    this.operator = operator.text;
    this.expression = expression;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    switch (operator) {
      case "-":
        return expression.compile(function, context).emit(Opcode.NEG);
      case "not":
        return expression.compile(function, context).emit(Opcode.NOT);
    }
    throw new IllegalStateException();
  }
}
