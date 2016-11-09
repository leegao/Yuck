package com.yuck.auxiliary.descentparsing;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.List;

public class Grammar {
  private final Multimap<Variable, List<Atom>> mRules;

  public Grammar(Multimap<Variable, List<Atom>> rules) {
    mRules = rules;
  }

  public static void main(String[] args) {
    ImmutableMultimap<Variable, List<Atom>> rules = ImmutableMultimap.<Variable, List<Atom>>builder()
        .put(V("e"), Lists.newArrayList(T("n"), V("e'")))
        .put(V("e'"), Lists.newArrayList(V("op"), V("e")))
        .put(V("e'"), Lists.newArrayList(E()))
        .put(V("op"), Lists.newArrayList(T("+")))
        .put(V("op"), Lists.newArrayList(T("-")))
        .put(V("op"), Lists.newArrayList(T("*")))
        .build();
    Grammar grammar = new Grammar(rules);
    System.err.println(rules);
  }

  private static Terminal T(String t) {
    return new Terminal(t);
  }

  private static Variable V(String v) {
    return new Variable(v);
  }

  private static Epsilon E() {
    return new Epsilon();
  }
}
