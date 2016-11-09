package com.yuck.auxiliary.descentparsing;

import com.google.common.collect.*;

import java.util.*;

public class Grammar {
  private final Multimap<Variable, List<Atom>> mRules;
  private final HashMultimap<List<Atom>, Atom> mFirstCache = HashMultimap.create();
  private final Map<List<Atom>, Boolean> mNullableCache = new HashMap<>();

  public Grammar(Multimap<Variable, List<Atom>> rules) {
    mRules = rules;
  }

  public Set<Atom> first(List<Atom> sentence) {
    // first(atom :: tl) = prefix(atom) + delta(atom) * first(tl)
    // changing tl -> changing (atom :: tl)
    if (mFirstCache.containsKey(sentence)) {
      return mFirstCache.get(sentence);
    }

    Set<List<Atom>> original = new HashSet<>();
    original.add(sentence);
    Set<List<Atom>> worklist;

    boolean changed = true;
    while (changed) {
      worklist = Sets.newHashSet(original);
      changed = false;
      int oldRules = original.size();
      for (List<Atom> rule : worklist) {
        if (rule.isEmpty()) continue;
        int oldRule = mFirstCache.get(rule).size();
        Atom atom = rule.get(0);
        Set<Atom> prefix = new HashSet<>();
        if (atom instanceof Terminal || atom instanceof Epsilon) {
          prefix.add(atom);
        } else if (atom instanceof Variable) {
          for (List<Atom> subRule : mRules.get((Variable) atom)) {
            prefix.addAll(mFirstCache.get(subRule));
            original.add(subRule);
          }
        }
        if (prefix.contains(E())) {
          List<Atom> tail = rule.subList(1, rule.size());
          original.add(tail);
          prefix.addAll(mFirstCache.get(tail));
        }
        if (prefix.size() != oldRule || original.size() != oldRules) {
          mFirstCache.putAll(rule, prefix);
          changed = true;
        }
      }
    }

    return mFirstCache.get(sentence);
  }

  public boolean nullable(List<Atom> sentence) {
    // nullable(hd :: tl) = eps in first(hd) and nullable(tl)
    if (sentence.isEmpty()) return true;
    if (mNullableCache.containsKey(sentence)) {
      return mNullableCache.get(sentence);
    }
    boolean result = first(sentence).contains(E()) && nullable(sentence.subList(1, sentence.size()));
    mNullableCache.put(sentence, result);
    return result;
  }

  public Set<Atom> follow(Variable variable) {
    // follow(V) = \cup_{X -> aVb} first(b) + nullable(b) * follow(V)
    return null;
  }

  private Set<Atom> prefix(Atom atom) {
    if (atom instanceof Epsilon) {
      return Sets.newHashSet(atom);
    } else if (atom instanceof Terminal) {
      return Sets.newHashSet(atom);
    } else if (atom instanceof Variable) {
      Set<Atom> result = new HashSet<>();
      for (List<Atom> rule : mRules.get((Variable) atom)) {
        result.addAll(first(rule));
      }
      return result;
    }
    throw new IllegalStateException("Cannot prefix EOF.");
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
    for (Map.Entry<Variable, List<Atom>> entry : rules.entries()) {
      System.err.println(entry.getValue() + "  ->  " + grammar.first(entry.getValue()));
    }
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
