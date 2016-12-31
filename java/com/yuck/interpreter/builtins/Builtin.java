package com.yuck.interpreter.builtins;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.yuck.Yuckc;
import com.yuck.interpreter.*;
import com.yuck.ycode.YCodeFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Builtin {
  public static YuckObject print(InterpreterContext context) {
    List<YuckObject> arguments = getArguments(context);
    String joint = Joiner.on(" ").join(arguments);
    System.out.println(joint);
    return new YuckNil(context);
  }

  public static YuckObject require(InterpreterContext context) {
    Preconditions.checkArgument(context.locals.size() > 0);
    Preconditions.checkArgument(context.get(0) instanceof YuckString);
    String pkg = ((YuckString) context.get(0)).string;
    // Lookup pkg
    Optional<File> lookupPackage = Interpreter.lookupPackage(pkg);
    if (lookupPackage.isPresent()) {
      File file = lookupPackage.get();
      YCodeFunction function;
      try {
        function = Yuckc.yuckc(file.getAbsolutePath(), null, false);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
      InterpreterContext result = Interpreter.interpret(function, new InterpreterContext(context, null));
      YuckObject value = result.pop();
      if (value instanceof YuckNil) {
        return YuckModule.from(result, context);
      } else {
        return value;
      }
    }
    throw new IllegalStateException();
  }

  public static YuckObject error(InterpreterContext context) {
    throw new IllegalStateException(context.get(0).toString());
  }

  public static YuckObject assert_(InterpreterContext context) {
    List<YuckObject> arguments = getArguments(context);
    Preconditions.checkArgument(arguments.size() > 0, "Assert takes at least one argument.");
    if (arguments.get(0).isFilled()) {
      return arguments.get(0);
    }
    if (arguments.size() > 1) {
      throw new IllegalStateException("Assertion failed: " + arguments.get(1).toString());
    }
    throw new IllegalStateException("Assertion failed.");
  }

  public static YuckObject tostring(InterpreterContext context) {
    Preconditions.checkArgument(context.locals.size() > 0);
    return new YuckString(context.get(0).toString(), context);
  }

  public static YuckObject tonumber(InterpreterContext context) {
    Preconditions.checkArgument(context.locals.size() > 0);
    Preconditions.checkArgument(context.get(0) instanceof YuckString);
    String string = ((YuckString) context.get(0)).string;
    try {
      return new YuckInteger(Integer.valueOf(string), context);
    } catch (NumberFormatException e) {
      return new YuckFloat(Float.valueOf(string), context);
    }
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
    register("require", Builtin::require, context);
    register("error", Builtin::error, context);
    register("assert", Builtin::assert_, context);
    register("tostring", Builtin::tostring, context);
    register("tonumber", Builtin::tonumber, context);
  }
}
