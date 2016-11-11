package com.yuck.auxiliary.descentparsing;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;
import javafx.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GrammarBase<T> {
  // Gives the token label
  public abstract String label(T token);

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
    List<Atom> productions = Lists.newArrayList(Splitter.onPattern("\\s+").splitToList(right).stream().map(
        str -> {
          if (str.startsWith("$")) return new Variable(str.substring(1));
          else if (str.equals("%eps")) return new Epsilon();
          else return new Terminal(str);
        }).iterator());
    return new Pair<>(left, productions);
  }

  private Pair<Atom, T> peek(List<T> stream) {
    if (stream.isEmpty()) {
      return new Pair<>(new EOF(), null);
    }
    T top = stream.get(0);
    return new Pair<>(new Terminal(label(top)), top);
  }

  private T consume(List<T> stream, Atom what) {
    Pair<Atom, T> peek = peek(stream);
    if (!peek.getKey().equals(what)) {
      throw new IllegalStateException("Cannot consume " + what + " at " + stream);
    }
    return stream.remove(0);
  }

  public <R> R parse(List<T> stream) {
    // start with the start symbol
    preprocess();
    List<T> current = Lists.newArrayList(stream);
    return (R) parse(current, mGrammar.mStart);
  }

  private Object parse(List<T> stream, Variable state) {
    Pair<Atom, T> peek = peek(stream);
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

  private Object reduce(List<T> stream, Variable state, List<Atom> sentence) {
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

  public static class RuleGrammar {
    private static final Set<String> SIMPLE_TOKENS = Sets.newHashSet("+", "*", "?", "(", ")", "|", "%eps");
    private static final Set<String> ESCAPES = Sets.newHashSet("%+", "%*", "%?", "%(", "%)", "%|");
    private static final Pattern SIMPLE_PATTERN = Pattern.compile("\\(|\\)|\\*|\\+|\\||\\?");

    /**
     * // E -> %eps | ((term | var | %( $Group %)) post?)+
     * E -> %%eps | $E_group2 $E'
     * E' -> $E | %eps
     * E_group1 -> term | var | %( $Group %)
     * E_group2 -> $E_group1 $E_group2'
     * E_group2' -> %eps | post
     * Group -> $E $Group'
     * Group' -> %eps | $Group
     *
     * Tokens: $var, term, +, *, ?, (, ), |, %eps, [%+, %*, %?, %(, %), %|] <- terms
     */
    class RuleToken {
      final String type;
      final String data;

      RuleToken(String type, String data) {
        this.type = type;
        this.data = data;
      }
    }

    public List<RuleToken> tokenize(String rule) {
      List<String> terms = Splitter.onPattern("\\s+").trimResults().splitToList(rule);
      List<RuleToken> tokens = new ArrayList<>();
      List<String> subterms = new ArrayList<>();
      for (String term : terms) {
        Matcher matcher = SIMPLE_PATTERN.matcher(term);
        int end = 0;
        while (matcher.find(end)) {
          // split
          int start = matcher.start();
          end = matcher.end();
          String prefix = term.substring(0, start);
          if (!prefix.isEmpty()) subterms.add(prefix);
          String middle = term.substring(start, end);
          if (!middle.isEmpty()) subterms.add(middle);
        }
        String postfix = term.substring(end);
        if (!postfix.isEmpty()) subterms.add(postfix);
      }
      System.err.println(subterms);
      return tokens;
    }
  }
}
