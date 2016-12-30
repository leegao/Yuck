package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.sun.istack.internal.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class InterpreterContext {
  protected final LinkedList<YuckObject> stack = new LinkedList<>();
  protected final HashMap<Integer, YuckObject> locals = new HashMap<>();
  protected final HashMap<String, Integer> localNames = new HashMap<>();
  protected final Optional<YuckInstance> currentInstance;
  protected final Optional<InterpreterContext> previous;

  public InterpreterContext() {
    previous = Optional.empty();
    currentInstance = Optional.empty();
  }

  public InterpreterContext(@Nullable InterpreterContext previous, @Nullable YuckInstance currentInstance) {
    this.previous = Optional.ofNullable(previous);
    this.currentInstance = Optional.ofNullable(currentInstance);
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
    if (locals.containsKey(local)) {
      return locals.get(local);
    } else {
      return new YuckNil(this);
    }
  }

  public YuckObject lookup(String name) {
    if (localNames.containsKey(name)) {
      return locals.get(localNames.get(name));
    }
    Preconditions.checkArgument(previous.isPresent(), "Cannot find the variable " + name);
    return previous.get().lookup(name);
  }

  public YuckInstance lookupThis() {
    if (currentInstance.isPresent()) {
      return currentInstance.get();
    }
    Preconditions.checkArgument(previous.isPresent(), "Not inside an instance.");
    return previous.get().lookupThis();
  }

  public void storeup(String name, YuckObject val) {
    if (localNames.containsKey(name)) {
      locals.put(localNames.get(name), val);
      return;
    }
    Preconditions.checkArgument(previous.isPresent(), "Cannot find the variable " + name);
    previous.get().storeup(name, val);
  }
}
