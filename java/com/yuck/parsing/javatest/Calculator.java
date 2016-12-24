package com.yuck.parsing.javatest;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.yuck.parsing.Atom;
import com.yuck.parsing.GrammarBase;
import com.yuck.parsing.Variable;
import com.yuck.parsing.annotations.For;
import com.yuck.parsing.annotations.Rule;
import com.yuck.parsing.annotations.Start;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Calculator extends GrammarBase<String> {
//  @Start
//  @Rule("E -> (n | %( $E %) : X) (op (n | %( $E %) : X))*")
//  public int E(int head, List<List<?>> rest) {
//    Function<Integer, Integer> tail = x -> x;
//    for (List<?> opn : rest) {
//      Function<Integer, Integer> oldTail = tail;
//      tail = x -> getOperation((String) opn.get(0), (Integer) opn.get(1)).apply(oldTail.apply(x));
//    }
//    return tail.apply(head);
//  }
//
//  @Rule("E -> ! $Meh")
//  public int E(String bang, String meh) {
//    return meh.length();
//  }
//
//  @Rule("Meh -> n")
//  public String meh(String n) {
//    return n;
//  }
//
//  @Rule("Meh -> n n")
//  public String meh(String n, String m) {
//    return n + m;
//  }
//
//  @Resolve(variable = "Meh", term = "n")
//  public List<Atom> resolveMeh(
//      List<String> rest,
//      @For("n") List<Atom> n,
//      @For("n n") List<Atom> nn) {
//    if (rest.size() > 1 && label(rest.get(1)).equals("n")) {
//      return nn;
//    }
//    return n;
//  }
//
//  @For("X")
//  public int determine(Object... args) {
//    List<?> group = Lists.newArrayList(args);
//    if (group.size() > 1) {
//      return (Integer) group.get(1);
//    }
//    return Integer.valueOf((String) group.get(0));
//  }

  @Rule("Z -> $Z1 (BAR $Z1 : Second)*")
  public String Z(String left, List<String> rest) {
    return left + " | " + Joiner.on(" | ").join(rest);
  }

  @Rule("Z1 -> $Z2 $Z2*")
  public String Z1(String left, List<String> rest) {
    return left + Joiner.on(" ").join(rest);
  }

  @Rule("Z2 -> x STAR?")
  public String Z2(String x, Optional<?> star) {
    if (star.isPresent()) return "(" + x + "*" + ")";
    return x;
  }

  @Rule("Z2 -> %( $Z %) STAR?")
  public String Z2(String open, String in, String close, Optional<?> star) {
    if (star.isPresent()) return "(" + in + ")*";
    return "(" + in + ")";
  }

  @Start
  @Rule("E -> $Z")
  public int EZ(Object z) {
    // System.err.println(z);
    return 0;
  }

  @For("Second")
  public <T> T second(Object... terms) {
    Preconditions.checkArgument(terms.length > 1);
    return (T) terms[1];
  }

  @For("First")
  public <T> T first(Object... terms) {
    Preconditions.checkArgument(terms.length > 0);
    return (T) terms[0];
  }

  @Override
  protected Set<List<Atom>> handleError(Variable variable, Atom on, List<String> stream) {
    return super.handleError(variable, on, stream);
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
        case "@":
          return "@";
        case "|":
          return "BAR";
        case "^":
          return "STAR";
      }
      return "x";
    }
  }

  public static void main(String[] args) {
    Calculator calculator = new Calculator();
    List<String> tokenStream = Splitter.on(" ")
        .trimResults()
        .omitEmptyStrings()
        .splitToList("( a ^ | n ) ^");
    int result = calculator.parse(tokenStream);
    // System.out.println(result);
    HashMultimap<Pair<Variable, Atom>, List<Atom>> actions = calculator.mGrammar.actions();
    for (Map.Entry<Pair<Variable, Atom>, List<Atom>> action : actions.entries()) {
      //Preconditions.checkArgument(action.getValue().size() == 1);
      System.err.println(action.getKey() + "\n\t" + action.getValue());
      System.err.println();
    }
  }
}
