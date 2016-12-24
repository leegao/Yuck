package com.yuck.interpreter;

import java.util.LinkedList;

public class InterpreterContext {
  LinkedList<YuckObject> stack = new LinkedList<>();

  public void push(YuckObject yuckObject) {
    stack.push(yuckObject);
  }
}
