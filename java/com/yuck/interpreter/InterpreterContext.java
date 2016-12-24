package com.yuck.interpreter;

import java.util.LinkedList;

public class InterpreterContext {
  LinkedList<YuckObject> stack = new LinkedList<>();

  public void push(YuckObject yuckObject) {
    stack.push(yuckObject);
  }

  public YuckObject pop() {
    return stack.pop();
  }

  public int depth() {
    return stack.size();
  }
}
