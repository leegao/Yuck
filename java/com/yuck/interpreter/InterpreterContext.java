package com.yuck.interpreter;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;

public class InterpreterContext {
  LinkedList<YuckObject> stack = new LinkedList<>();
  HashMap<Integer, YuckObject> locals = new HashMap<>();

  public void push(YuckObject yuckObject) {
    stack.push(yuckObject);
  }

  public YuckObject pop() {
    return stack.pop();
  }

  public int depth() {
    return stack.size();
  }

  public void add(int local, YuckObject object) {
    locals.put(local, object);
  }

  public YuckObject get(int local) {
    Preconditions.checkArgument(locals.containsKey(local));
    return locals.get(local);
  }
}
