package com.yuck.interpreter;

import com.yuck.ycode.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Objects;

public class YuckFloat extends YuckObject {
  public final float number;

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.FLOAT;
  }

  public YuckFloat(float number, InterpreterContext context) {
    super(context);
    this.number = number;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckFloat yuckFloat = (YuckFloat) o;
    return Float.compare(yuckFloat.number, number) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(number);
  }

  @Override
  public YuckObject binary(Opcode opcode, YuckObject other) {
    YuckObject result;
    float a = number;
    float b;
    if (other instanceof YuckInteger) {
      b = ((YuckInteger) other).number;
    } else if (other instanceof YuckFloat) {
      b = ((YuckFloat) other).number;
    } else {
      throw new NotImplementedException();
    }
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
  }
}
