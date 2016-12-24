package com.yuck.interpreter;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class InterpreterContext {
  protected final LinkedList<YuckObject> stack = new LinkedList<>();
  protected final HashMap<Integer, YuckObject> locals = new HashMap<>();
  protected final HashMap<String, Integer> localNames = new HashMap<>();
  protected final Optional<InterpreterContext> previous;

  public InterpreterContext() {
    previous = Optional.empty();
  }

  public InterpreterContext(InterpreterContext previous) {
    this.previous = Optional.of(previous);
  }

  public void push(YuckObject yuckObject) {
    stack.push(yuckObject);
  }

  public YuckObject pop() {
    return stack.pop();
  }

  public int depth() {
    return stack.size();
  }

  public void add(int local, String name, YuckObject object) {
    locals.put(local, object);
    if (!localNames.containsKey(name)) localNames.put(name, local);
  }

  public YuckObject get(int local) {
    Preconditions.checkArgument(locals.containsKey(local));
    return locals.get(local);
  }

  public YuckObject lookup(String name) {
    if (localNames.containsKey(name)) {
      return locals.get(localNames.get(name));
    }
    Preconditions.checkArgument(previous.isPresent());
    return previous.get().lookup(name);
  }
}
