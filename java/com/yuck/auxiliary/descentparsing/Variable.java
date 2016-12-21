package com.yuck.auxiliary.descentparsing;

public class Variable extends Atom {
  public Variable(String label) {
    super("$" + label);
  }
}
