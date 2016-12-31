package com.yuck.interpreter.builtins;

import com.google.common.base.Joiner;
import com.yuck.interpreter.InterpreterContext;
import com.yuck.interpreter.NativeFunction;
import com.yuck.interpreter.YuckNil;
import com.yuck.interpreter.YuckObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Builtin {
  public static YuckObject print(InterpreterContext context) {
    List<YuckObject> arguments = getArguments(context);
    String joint = Joiner.on(" ").join(arguments);
    System.out.println(joint);
    return new YuckNil(context);
  }

  public static void register(
      String name,
      Function<InterpreterContext, YuckObject> function,
      InterpreterContext context) {
    context.add(context.locals.size(), name, new NativeFunction(function, context));
  }

  public static List<YuckObject> getArguments(InterpreterContext context) {
    List<YuckObject> arguments = new ArrayList<>();
    for (int i = 0; i < context.locals.size(); i++) {
      arguments.add(context.locals.get(i));
    }
    return arguments;
  }

  public static void registerAll(InterpreterContext context) {
    register("print", Builtin::print, context);
  }
}
