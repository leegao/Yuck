package com.yuck.auxiliary.descentparsing.javatest;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.yuck.auxiliary.descentparsing.Atom;
import com.yuck.auxiliary.descentparsing.GrammarBase;
import com.yuck.auxiliary.descentparsing.annotations.For;
import com.yuck.auxiliary.descentparsing.annotations.Resolve;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;

import java.util.List;
import java.util.function.Function;

public class Calculator extends GrammarBase<String> {
  @Start
  @Rule("E -> (n | %( $E %) : X) (op (n | %( $E %) : X))*")
  public int E(int head, List<List<?>> rest) {
    Function<Integer, Integer> tail = x -> x;
    for (List<?> opn : rest) {
      Function<Integer, Integer> oldTail = tail;
      tail = x -> getOperation((String) opn.get(0), (Integer) opn.get(1)).apply(oldTail.apply(x));
    }
    return tail.apply(head);
  }

  @Rule("E -> ! $Meh")
  public int E(String bang, String meh) {
    return meh.length();
  }

  @Rule("Meh -> n")
  public String meh(String n) {
    return n;
  }

  @Rule("Meh -> n n")
  public String meh(String n, String m) {
    return n + m;
  }

  @Resolve(variable = "Meh", term = "n")
  public List<Atom> resolveMeh(
      List<String> rest,
      @For("n") List<Atom> n,
      @For("n n") List<Atom> nn) {
    if (rest.size() > 1 && label(rest.get(1)).equals("n")) {
      return nn;
    }
    return n;
  }

  @For("X")
  public int determine(Object... args) {
    List<?> group = Lists.newArrayList(args);
    if (group.size() > 1) {
      return (Integer) group.get(1);
    }
    return Integer.valueOf((String) group.get(0));
  }

  Function<Integer, Integer> getOperation(String op, int right) {
    switch (op) {
      case "+":
        return n -> n + right;
      case "-":
        return n -> n - right;
      case "*":
        return n -> n * right;
      case "/":
        return n -> n / right;
    }
    throw new IllegalStateException();
  }

  @Override
  public String label(String token) {
    try {
      Integer.valueOf(token);
      return "n";
    } catch (Exception e) {
      switch (token) {
        case "+":
        case "-":
        case "*":
        case "/":
          return "op";
        case "(":
          return "%(";
        case ")":
          return "%)";
        case "!":
          return "!";
      }
      throw new IllegalStateException("'" + token + "'");
    }
  }

  public static void main(String[] args) {
    Calculator calculator = new Calculator();
    List<String> tokenStream = Splitter.on(" ")
        .trimResults()
        .omitEmptyStrings()
        .splitToList("1 +  ( 3 * 2 )  - ( ! 111 22 )");
    int result = calculator.parse(tokenStream);
    System.out.println(result);
  }
}
