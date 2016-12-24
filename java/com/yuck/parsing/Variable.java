package com.yuck.parsing;

public class Variable extends Atom {
  public Variable(String label) {
    super("$" + label);
  }
}
