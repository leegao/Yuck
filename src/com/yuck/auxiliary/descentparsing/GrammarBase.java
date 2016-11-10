package com.yuck.auxiliary.descentparsing;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;
import javafx.util.Pair;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GrammarBase<T> {
  // Gives the token label
  public abstract String label(T token);

  private boolean mPreprocessed = false;
  private Variable mStart;
  private Grammar mGrammar;
  private Map<Pair<Variable, List<Atom>>, Method> mMethodMap = new HashMap<>();
  private Map<Variable, Class<?>> mTypeMap = new HashMap<>();

  protected static Pair<Variable, List<Atom>> parseRule(String rule) {
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

  protected Grammar preprocess() {
    mPreprocessed = true;
    ImmutableMultimap.Builder<Variable, List<Atom>> rules = ImmutableMultimap.builder();
    for (Method method : this.getClass().getMethods()) {
      Rule rule = method.getDeclaredAnnotation(Rule.class);
      if (rule != null) {
        Pair<Variable, List<Atom>> variableListPair = parseRule(rule.rule());
        Variable key = variableListPair.getKey();
        rules.put(key, variableListPair.getValue());
        mMethodMap.put(variableListPair, method);
        if (method.getDeclaredAnnotation(Start.class) != null) {
          if (mStart != null && !mStart.equals(key))
            throw new IllegalStateException("Cannot have multiple start states");
          mStart = key;
        }
        Class<?> returnType = method.getReturnType();
        if (!mTypeMap.containsKey(key)) {
          mTypeMap.put(key, returnType);
        } else {
          // check compatibility
          Class<?> old = mTypeMap.get(key);
          if (!old.isAssignableFrom(returnType)) {
            if (returnType.isAssignableFrom(old)) {
              mTypeMap.put(key, returnType);
            } else {
              throw new IllegalStateException(
                  "Cannot join types " + old.getTypeName() + " and " + returnType.getTypeName());
            }
          }
        }
      }
    }
    mGrammar =  new Grammar(rules.build(), mStart);
    return mGrammar;
  }
}
