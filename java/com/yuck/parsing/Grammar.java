package com.yuck.parsing;

import com.google.common.collect.*;
import javafx.util.Pair;

import java.util.*;

public class Grammar {
  private final Multimap<Variable, List<Atom>> mRules;
  protected final Variable mStart;
  private final HashMultimap<List<Atom>, Atom> mFirstCache = HashMultimap.create();
  private final Map<List<Atom>, Boolean> mNullableCache = new HashMap<>();
  private final HashMultimap<Variable, Atom> mFollowCache = HashMultimap.create();
  private boolean mPreprocessed = false;
  private HashMultimap<Pair<Variable, Atom>, List<Atom>> mActions = HashMultimap.create();

  public Grammar(Multimap<Variable, List<Atom>> rules, Variable start) {
    mRules = rules;
    mStart = start;
  }

  public Set<Atom> first(List<Atom> sentence) {
    // first(atom :: tl) = prefix(atom) + delta(atom) * first(tl)
    // changing tl -> changing (atom :: tl)
    if (sentence.isEmpty()) return Sets.newHashSet(E());
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
        if (atom instanceof Terminal | atom instanceof Epsilon) {
          prefix.add(atom);
        } else if (atom instanceof Variable) {
          for (List<Atom> subRule : mRules.get((Variable) atom)) {
            prefix.addAll(mFirstCache.get(subRule));
            original.add(subRule);
          }
        }
        if (prefix.contains(E())) {
          List<Atom> tail = rule.subList(1, rule.size());
          if (!tail.isEmpty()) {
            prefix.remove(E());
            original.add(tail);
            prefix.addAll(mFirstCache.get(tail));
          }
        }
        if (prefix.size() != oldRule || original.size() != oldRules) {
          mFirstCache.putAll(rule, prefix);
          changed = true;
        }
      }
    }

    return mFirstCache.get(sentence);
  }

  public Set<Atom> follow(Variable variable) {
    // follow(V) = \cup_{X -> aVb} first(b) + nullable(b) * follow(V)
    if (mFollowCache.containsKey(variable)) {
      return mFollowCache.get(variable);
    }
    Set<Variable> original = new HashSet<>();
    original.add(variable);
    Set<Variable> worklist;
    boolean changed = true;
    while (changed) {
      changed = false;
      worklist = Sets.newHashSet(original);
      int oldOriginal = original.size();
      for (Variable nonterminal : worklist) {
        // look for all rules of the form X -> a V b
        Set<Atom> result = new HashSet<>();
        if (nonterminal.equals(mStart)) {
          result.add(new EOF());
        }
        int oldResult = mFollowCache.get(nonterminal).size();
        for (Map.Entry<Variable, List<Atom>> entry : mRules.entries()) {
          for (int i = 0; i < entry.getValue().size(); i++) {
            if (entry.getValue().get(i).equals(nonterminal)) {
              List<Atom> b = entry.getValue().subList(i + 1, entry.getValue().size());
              Set<Atom> sub = Sets.newHashSet(first(b));
              if (sub.contains(E())) {
                sub.addAll(mFollowCache.get(entry.getKey()));
                original.add(entry.getKey());
              }
              sub.remove(E());
              result.addAll(sub);
            }
          }
        }
        if (oldResult != result.size()) {
          mFollowCache.putAll(nonterminal, result);
          changed = true;
        }
      }
      if (oldOriginal != original.size()) {
        changed = true;
      }
    }
    return mFollowCache.get(variable);
  }

  public HashMultimap<Pair<Variable, Atom>, List<Atom>> actions() {
    if (mPreprocessed) return mActions;
    // Computes the action table for var on term.

    for (Variable variable : mRules.keySet()) {
      Set<Atom> followSet = follow(variable);
      for (List<Atom> sentence : mRules.get(variable)) {
        Set<Atom> firstSet = first(sentence);
        for (Atom atom : firstSet) {
          if (atom instanceof Terminal) {
            mActions.put(new Pair<>(variable, atom), sentence);
          }
        }
        if (firstSet.contains(E())) {
          for (Atom atom : followSet) {
            if (atom instanceof Terminal || atom instanceof EOF) {
              mActions.put(new Pair<>(variable, atom), sentence);
            }
          }
        }
      }
    }
    mPreprocessed = true;
    return mActions;
  }

  static Terminal T(String t) {
    return new Terminal(t);
  }

  public static Variable V(String v) {
    return new Variable(v);
  }

  public static Epsilon E() {
    return new Epsilon();
  }
}
