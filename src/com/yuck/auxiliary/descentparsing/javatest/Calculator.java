package com.yuck.auxiliary.descentparsing.javatest;

import com.google.common.base.Splitter;
import com.yuck.auxiliary.descentparsing.GrammarBase;
import com.yuck.auxiliary.descentparsing.RuleGrammar;
import com.yuck.auxiliary.descentparsing.Variable;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;

import java.util.Optional;
import java.util.function.Function;

public class Calculator extends GrammarBase<String> {
  @Rule("E -> n n?")
  @Start
  public int E(String n, Optional<String> op) {
    return Integer.valueOf(n) + (op.isPresent() ? Integer.valueOf(op.get()) : 0);
  }

//  @Rule("E' -> %eps")
//  public Function<Integer, Integer> E_() {
//    return n -> n;
//  }
//
//  @Rule("E' -> op $E")
//  public Function<Integer, Integer> E_(String op, int right) {
//    return getOperation(op, right);
//  }

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
      }
      throw new IllegalStateException();
    }
  }

  public static void main(String[] args) {
    Calculator calculator = new Calculator();

    // Note that our grammar is right-associative, so this is (1 + (3 * 4))
    int result = calculator.parse(Splitter.on(" ").splitToList("1 5"));
    System.out.println(result);

    RuleGrammar ruleGrammar = new RuleGrammar(new Variable("E"));
    System.err.println(ruleGrammar.tokenize("(a | b)? $E"));
    System.err.println(ruleGrammar.parse(ruleGrammar.tokenize("(a | b)? $E")).toString());
  }
}
