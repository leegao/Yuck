package com.yuck.ast;

import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
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
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    Opcode opcode;
    boolean not = false;
    switch (operator) {
      case "add": opcode = Opcode.ADD; break;
      case "*": opcode = Opcode.MUL; break;
      case "or": opcode = Opcode.OR; break;
      case "and": opcode = Opcode.AND; break;
      case ">=": not = true;
      case "<": opcode = Opcode.LT; break;
      case ">": not = true;
      case "<=": opcode = Opcode.LE; break;
      case "!=": not = true;
      case "==": opcode = Opcode.EQ; break;
      case "to": opcode = Opcode.TO_RANGE; break;
      case "-": opcode = Opcode.SUB; break;
      case "/": opcode = Opcode.DIV; break;
      case "mod": opcode = Opcode.MOD; break;
      case "pow": opcode = Opcode.POW; break;
      default:
        throw new NotImplementedException();
    }
    left.compile(function, scope);
    right.compile(function, scope);
    function.emit(opcode);
    if (not) function.emit(Opcode.NOT);
    return function;
  }
}
