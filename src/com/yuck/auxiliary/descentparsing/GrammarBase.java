package com.yuck.auxiliary.descentparsing;

import com.google.common.base.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;
import javafx.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static com.yuck.auxiliary.descentparsing.Grammar.E;
import static com.yuck.auxiliary.descentparsing.Grammar.V;
import static com.yuck.auxiliary.descentparsing.Grammar.T;

public abstract class GrammarBase<U> {
  // Gives the token label
  public abstract String label(U token);

  private boolean mPreprocessed = false;
  private Grammar mGrammar;
  private Map<Pair<Variable, List<Atom>>, Method> mMethodMap = new HashMap<>();
  private Map<Variable, Class<?>> mTypeMap = new HashMap<>();
  HashMultimap<Pair<Variable, Atom>, List<Atom>> mActionTable;

  protected static Pair<Variable, List<Atom>> parseRule(String rule) {
    // id -> ($id | %eps | \S+)+
    List<String> strings = Splitter.on("->").limit(2).trimResults().splitToList(rule);
    if (strings.size() != 2) throw new IllegalStateException();
    Variable left = new Variable(strings.get(0));
    String right = strings.get(1);
    List<Atom> productions = newArrayList(Splitter.onPattern("\\s+").splitToList(right).stream().map(
        str -> {
          if (str.startsWith("$")) return new Variable(str.substring(1));
          else if (str.equals("%eps")) return new Epsilon();
          else return new Terminal(str);
        }).iterator());
    return new Pair<>(left, productions);
  }

  private Pair<Atom, U> peek(List<U> stream) {
    if (stream.isEmpty()) {
      return new Pair<>(new EOF(), null);
    }
    U top = stream.get(0);
    return new Pair<>(new Terminal(label(top)), top);
  }

  private U consume(List<U> stream, Atom what) {
    Pair<Atom, U> peek = peek(stream);
    if (!peek.getKey().equals(what)) {
      throw new IllegalStateException("Cannot consume " + what + " at " + stream);
    }
    return stream.remove(0);
  }

  public <R> R parse(List<U> stream) {
    // start with the start symbol
    preprocess();
    List<U> current = newArrayList(stream);
    return (R) parse(current, mGrammar.mStart);
  }

  private Object parse(List<U> stream, Variable state) {
    Pair<Atom, U> peek = peek(stream);
    Set<List<Atom>> sentences = mActionTable.get(new Pair<>(state, peek.getKey()));
    // TODO: Add error handling here.
    if (sentences.isEmpty()) {
      throw new IllegalStateException("Error: No action at state " + state + " on " + peek);
    }

    if (sentences.size() > 1) {
      throw new IllegalStateException(); // Impossible, for now
    } else {
      for (List<Atom> sentence : sentences) {
        return reduce(stream, state, sentence);
      }
    }

    throw new IllegalStateException();
  }

  private Object reduce(List<U> stream, Variable state, List<Atom> sentence) {
    List<Pair<Atom, ?>> arguments = new ArrayList<>();
    for (Atom term : sentence) {
      if (term instanceof Terminal) {
        arguments.add(peek(stream));
        consume(stream, term);
      } else if (term instanceof Variable) {
        Object result = parse(stream, (Variable) term);
        arguments.add(new Pair<>(term, result));
      }
    }
    Method method = mMethodMap.get(new Pair<>(state, sentence));
    try {
      return method.invoke(this, arguments.stream().map(Pair::getValue).toArray());
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw Throwables.propagate(e);
    }
  }

  protected Grammar preprocess() {
    if (mPreprocessed) return mGrammar;

    Variable start = null;
    mGrammar = null;
    mMethodMap.clear();
    mTypeMap.clear();

    ImmutableMultimap.Builder<Variable, List<Atom>> rules = ImmutableMultimap.builder();
    for (Method method : this.getClass().getMethods()) {
      Rule rule = method.getDeclaredAnnotation(Rule.class);
      if (rule != null) {
        Pair<Variable, List<Atom>> variableListPair = parseRule(rule.value());
        Variable key = variableListPair.getKey();
        rules.put(key, variableListPair.getValue());
        mMethodMap.put(variableListPair, method);
        if (method.getDeclaredAnnotation(Start.class) != null) {
          if (start != null && !start.equals(key))
            throw new IllegalStateException("Cannot have multiple start states");
          start = key;
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
    mGrammar =  new Grammar(rules.build(), start);
    HashMultimap<Pair<Variable, Atom>, List<Atom>> actionTable = mGrammar.actions();

    for (Pair<Variable, Atom> key : actionTable.keySet()) {
      Set<List<Atom>> conflicts = actionTable.get(key);
      if (conflicts.size() > 1) {
        // check for conflict
        throw new NotImplementedException();
      }
    }

    mActionTable = actionTable;
    mPreprocessed = true;
    return mGrammar;
  }
}
