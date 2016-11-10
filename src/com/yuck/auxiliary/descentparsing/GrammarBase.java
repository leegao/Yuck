package com.yuck.auxiliary.descentparsing;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class GrammarBase<T> {
  // Gives the token label
  public abstract String label(T token);

  protected Pair<Variable, List<Atom>> parseRule(String rule) {
    // id -> ($id | %eps | \S+)+
    List<String> strings = Splitter.on("->").limit(2).trimResults().splitToList(rule);
    assert strings.size() == 2;
    Variable left = new Variable(strings.get(0));
    String right = strings.get(1);
    List<Atom> productions = Lists.newArrayList(Splitter.onPattern("\\s+").splitToList(right).stream().map(
        str -> {
          if (str.startsWith("$")) return new Variable(str.substring(1));
          else if (str.equals("%eps")) return new Epsilon();
          else return new Terminal(str);
        }).iterator());
    return new Pair<>(left, productions);
  }
}
