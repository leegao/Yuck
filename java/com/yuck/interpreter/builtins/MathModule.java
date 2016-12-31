package com.yuck.interpreter.builtins;

import com.yuck.interpreter.*;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class MathModule extends YuckModule implements NativeModule {
  public MathModule(InterpreterContext context) {
    super(context);
  }

  private YuckObject wrap(Number number) {
    if (number instanceof Float || number instanceof Double){
      return new YuckFloat(number.floatValue(), context);
    } else {
      return new YuckInteger(number.intValue(), context);
    }
  }

  private float unwrap(YuckObject object) {
    if (object instanceof YuckInteger) {
      return (float) ((YuckInteger) object).number;
    } else if (object instanceof YuckFloat) {
      return ((YuckFloat) object).number;
    } else {
      throw new IllegalStateException();
    }
  }

  private int unwrapI(YuckObject object) {
    if (object instanceof YuckInteger) {
      return ((YuckInteger) object).number;
    } else if (object instanceof YuckFloat) {
      return (int) ((YuckFloat) object).number;
    } else {
      throw new IllegalStateException();
    }
  }

  private Function<InterpreterContext, YuckObject> wrapI(Function<Integer, Number> function) {
    return c -> wrap(function.apply(unwrapI(c.get(0))));
  }

  private Function<InterpreterContext, YuckObject> wrapF(Function<Float, Number> function) {
    return c -> wrap(function.apply(unwrap(c.get(0))));
  }


  private Function<InterpreterContext, YuckObject> wrap(Function<Float, Number> function) {
    return wrapF(function);
  }

  private Function<InterpreterContext, YuckObject> wrapFI(BiFunction<Float, Integer, Number> function) {
    return c -> wrap(function.apply(unwrap(c.get(0)), unwrapI(c.get(1))));
  }

  private Function<InterpreterContext, YuckObject> wrapFF(BiFunction<Float, Float, Number> function) {
    return c -> wrap(function.apply(unwrap(c.get(0)), unwrap(c.get(1))));
  }

  private Function<InterpreterContext, YuckObject> wrap(BiFunction<Float, Float, Number> function) {
    return wrapFF(function);
  }

  private Function<InterpreterContext, YuckObject> wrap(Supplier<Double> function) {
    return c -> wrap(function.get());
  }

  @Override
  public void registerAll() {
    register("abs", wrapF(Math::abs), context);
    register("acos", wrap(Math::acos), context);
    register("asin", wrap(Math::asin), context);
    register("atan", wrap(Math::atan), context);
    register("atan2", wrap(Math::atan2), context);
    register("cbrt", wrap(Math::cbrt), context);
    register("ceil", wrap(Math::ceil), context);
    register("floor", wrap(Math::floor), context);
    register("cos", wrap(Math::cos), context);
    register("cosh", wrap(Math::cosh), context);
    register("exp", wrap(Math::exp), context);
    register("expm1", wrap(Math::expm1), context);
    register("log", wrap(Math::log), context);
    register("log1p", wrap(Math::log1p), context);
    register("log10", wrap(Math::log10), context);
    register("cbrt", wrap(Math::cbrt), context);
    register("getExponent", wrapF(Math::getExponent), context);
    register("hypot", wrap(Math::hypot), context);
    register("max", wrap(Math::max), context);
    register("min", wrap(Math::min), context);
    register("pow", wrap(Math::pow), context);
    register("random", wrap(Math::random), context);
    register("round", wrapF(Math::round), context);
    register("scalb", wrapFI(Math::scalb), context);
    register("signum", wrapF(Math::signum), context);
    register("sin", wrap(Math::sin), context);
    register("sinh", wrap(Math::sinh), context);
    register("sqrt", wrap(Math::sqrt), context);
    register("tan", wrap(Math::tan), context);
    register("tanh", wrap(Math::tanh), context);
    register("toDegrees", wrap(Math::toDegrees), context);
    register("toRadians", wrap(Math::toRadians), context);
    register("ulp", wrapF(Math::ulp), context);
  }
}
