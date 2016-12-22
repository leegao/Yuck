package com.yuck.ast;

import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunctionContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BinaryOperator extends Expression {
  public final String operator;
  public final Expression left;
  public final Expression right;

  public BinaryOperator(String operator, Expression left, Expression right) {
    super(left.getStartLine(), left.getStartColumn(), right.getEndLine(), right.getEndColumn());
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext context) {
    Opcode opcode;
    switch (operator) {
      case "add": opcode = Opcode.ADD; break;
      case "*": opcode = Opcode.MUL; break;
      // TODO: add the other operators
      default:
        throw new NotImplementedException();
    }
    left.compile(context);
    right.compile(context);
    return context.emit(opcode);
  }
}
