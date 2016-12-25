package com.yuck.interpreter;

import com.yuck.ycode.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Objects;

public class YuckInteger extends YuckObject {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckInteger that = (YuckInteger) o;
    return number == that.number;
  }

  @Override
  public int hashCode() {
    return Objects.hash(number);
  }

  public final int number;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.INT;
  }

  public YuckInteger(int number, InterpreterContext context) {
    super(context);
    this.number = number;
  }

  @Override
  public YuckObject binary(Opcode opcode, YuckObject other) {
    if (other instanceof YuckInteger) {
      YuckObject result;
      int a = number;
      int b = ((YuckInteger) other).number;
      switch (opcode) {
        case ADD:
          result = new YuckInteger(a + b, context);
          break;
        case SUB:
          result = new YuckInteger(a - b, context);
          break;
        case MUL:
          result = new YuckInteger(a * b, context);
          break;
        case DIV:
          result = new YuckInteger(a / b, context);
          break;
        case MOD:
          result = new YuckInteger(a % b, context);
          break;
        case POW:
          result = new YuckFloat((float) Math.pow(a, b), context);
          break;
        case LE:
          result = new YuckBoolean(a <= b, context);
          break;
        case LT:
          result = new YuckBoolean(a < b, context);
          break;
        default:
          throw new IllegalStateException();
      }
      return result;
    } else if (other instanceof YuckFloat) {
      YuckObject result;
      float a = number;
      float b = ((YuckFloat) other).number;
      switch (opcode) {
        case ADD:
          result = new YuckFloat(a + b, context);
          break;
        case SUB:
          result = new YuckFloat(a - b, context);
          break;
        case MUL:
          result = new YuckFloat(a * b, context);
          break;
        case DIV:
          result = new YuckFloat(a / b, context);
          break;
        case MOD:
          result = new YuckFloat(a % b, context);
          break;
        case POW:
          result = new YuckFloat((float) Math.pow(a, b), context);
          break;
        case LE:
          result = new YuckBoolean(a <= b, context);
          break;
        case LT:
          result = new YuckBoolean(a < b, context);
          break;
        default:
          throw new IllegalStateException();
      }
      return result;
    } else {
      throw new NotImplementedException();
    }
  }

  @Override
  public boolean isFilled() {
    return number != 0;
  }
}
