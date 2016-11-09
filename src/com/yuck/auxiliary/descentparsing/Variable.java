package com.yuck.auxiliary.descentparsing;

public class Variable extends Atom {
  protected Variable(String label) {
    super("$" + label);
  }
}
