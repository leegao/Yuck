package com.yuck.auxiliary.descentparsing;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.yuck.auxiliary.descentparsing.annotations.For;
import com.yuck.auxiliary.descentparsing.annotations.Resolve;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;
import javafx.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.yuck.auxiliary.descentparsing.Grammar.V;

@SuppressWarnings({"unused", "unchecked"})
public abstract class GrammarBase<U> {
  private final Map<String, Method> mGroupHandlerRegistry = new HashMap<>();
  private final Map<Pair<Variable, Atom>, Method> mConflictHandlerRegistry = new HashMap<>();

  // Gives the token label
  public abstract String label(U token);

  protected boolean mPreprocessed = false;
  protected Grammar mGrammar;
  protected Map<Pair<Variable, List<Atom>>, Method> mMethodMap = new HashMap<>();
  protected Map<Variable, Class<?>> mTypeMap = new HashMap<>();
  protected HashMultimap<Pair<Variable, Atom>, List<Atom>> mActionTable;
  protected final Map<Variable, String> mGroupHandlers = new HashMap<>();

  protected Pair<Variable, RuleGrammar.Bundle> parseRule(String rule) {
    // id -> ($id | %eps | \S+)+
    List<String> strings = Splitter.on("->").limit(2).trimResults().splitToList(rule);
    if (strings.size() != 2) throw new IllegalStateException();
    Variable left = new Variable(strings.get(0));
    String right = strings.get(1);
    RuleGrammar ruleGrammar = new RuleGrammar(left);
    List<RuleGrammar.RuleToken> ruleTokens = ruleGrammar.tokenize(right);
    RuleGrammar.Bundle bundle = ruleGrammar.parse(ruleTokens);
    mGroupHandlers.putAll(ruleGrammar.mGroupHandlers);
    return new Pair<>(left, bundle);
  }

  private Pair<Atom, U> peek(List<U> stream) {
    if (stream.isEmpty()) {
      return new Pair<>(new EOF(), null);
    }
    U top = stream.get(0);
    return new Pair<>(new Terminal(label(top)), top);
  }

  private U consume(Variable state, List<Atom> sentence, List<U> stream, Atom what) {
    Pair<Atom, U> peek = peek(stream);
    if (!peek.getKey().equals(what)) {
      // throw new IllegalStateException("Cannot consume " + what + " at " + stream);
      return handleConsumptionError(state, peek.getKey(), stream, sentence, what);
    }
    return stream.remove(0);
  }

  protected U handleConsumptionError(
      Variable state,
      Atom next,
      List<U> stream,
      List<Atom> currentSentence,
      Atom expected) {
    throw new IllegalStateException("Cannot consume " + expected + " at " + stream);
  }

  public <R> R parse(List<U> stream) {
    // start with the start symbol
    preprocess();
    List<U> current = newArrayList(stream);
    return (R) parse(current, mGrammar.mStart);
  }

  protected Set<List<Atom>> handleError(Variable variable, Atom on, List<U> stream) {
    Pair<Atom, U> peek = peek(stream);
    throw new IllegalStateException("Error: No action at state " + variable + " on " + peek);
  }

  private Object parse(List<U> stream, Variable state) {
    Pair<Atom, U> peek = peek(stream);
    Pair<Variable, Atom> key = new Pair<>(state, peek.getKey());
    Set<List<Atom>> sentences = mActionTable.get(key);
    if (sentences.isEmpty()) {
      sentences = handleError(state, peek.getKey(), stream);
    }

    if (sentences.size() > 1) {
      if (mConflictHandlerRegistry.containsKey(key)) {
        Method handler = mConflictHandlerRegistry.get(key);
        Resolve resolver = handler.getDeclaredAnnotation(Resolve.class);
        Annotation[][] parameterAnnotations = handler.getParameterAnnotations();

        List<Atom> sentence;
        try {
          if (parameterAnnotations.length > 2) {
            // figure out the right annotations
            List<Object> arguments = new ArrayList<>();
            arguments.add(stream);
            for (int i = 1; i < parameterAnnotations.length; i++) {
              Optional<Annotation> any = newArrayList(parameterAnnotations[i])
                  .stream()
                  .filter(p -> p instanceof For)
                  .findAny();
              // Check that the conflicts are covered
              String production = any.map(x -> (For) x).get().value();
              RuleGrammar ruleGrammar = new RuleGrammar(key.getKey());
              List<RuleGrammar.RuleToken> ruleTokens = ruleGrammar.tokenize(production);
              RuleGrammar.Bundle bundle = ruleGrammar.parse(ruleTokens);
              arguments.add(bundle.head);
            }
            sentence = (List<Atom>) handler.invoke(this, arguments.toArray());
          } else {
            sentence = (List<Atom>) handler.invoke(this, stream, sentences);
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw Throwables.propagate(e);
        }
        return reduce(stream, state, sentence);
      }
      throw new IllegalStateException();
    } else {
      return reduce(stream, state, sentences.iterator().next());
    }
  }

  private Object reduce(List<U> stream, Variable state, List<Atom> sentence) {
    List<Pair<Atom, ?>> arguments = new ArrayList<>();
    for (Atom term : sentence) {
      if (term instanceof Terminal) {
        arguments.add(peek(stream));
        consume(state, sentence, stream, term);
      } else if (term instanceof Variable) {
        Object result = parse(stream, (Variable) term);
        arguments.add(new Pair<>(term, result));
      }
    }
    Method method = mMethodMap.get(new Pair<>(state, sentence));
    try {
      // Differentiate between variadic methods and normal methods
      Object[] args = arguments.stream().map(Pair::getValue).toArray();
      if (method.isVarArgs() && method.getParameterCount() == 1) {
        return method.invoke(this, new Object[] {args});
      } else {
        return method.invoke(this, args);
      }
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

    for (Method method : this.getClass().getMethods()) {
      For handler = method.getDeclaredAnnotation(For.class);
      if (handler != null) {
        mGroupHandlerRegistry.put(handler.value(), method);
      }

      Resolve resolver = method.getDeclaredAnnotation(Resolve.class);
      if (resolver != null) {
        Variable variable = V(resolver.variable());
        Atom term = resolver.term().equals("%eof") ? new EOF() : new Terminal(resolver.term());
        mConflictHandlerRegistry.put(new Pair<>(variable, term), method);
      }
    }

    ImmutableMultimap.Builder<Variable, List<Atom>> rules = ImmutableMultimap.builder();
    for (Method method : this.getClass().getMethods()) {
      Rule rule = method.getDeclaredAnnotation(Rule.class);
      if (rule != null) {
        Pair<Variable, RuleGrammar.Bundle> variableListPair = parseRule(rule.value());
        Variable key = variableListPair.getKey();
        RuleGrammar.Bundle bundle = variableListPair.getValue();
        rules.putAll(bundle.intermediates);
        rules.put(key, bundle.head);
        for (Map.Entry<Variable, List<Atom>> intermediate : bundle.intermediates.entries()) {
          try {
            Method intermediateMethod = handleIntermediateVariable(intermediate.getKey(), intermediate.getValue());
            mMethodMap.put(new Pair<>(intermediate.getKey(), intermediate.getValue()), intermediateMethod);
          } catch (NoSuchMethodException e) {
            throw Throwables.propagate(e);
          }
        }
        mMethodMap.put(new Pair<>(key, bundle.head), method);
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
        if (mConflictHandlerRegistry.containsKey(key)) {
          // either handle(stream, Set) or handle(stream, @For...)
          Method method = mConflictHandlerRegistry.get(key);
          Annotation[][] parameterAnnotations = method.getParameterAnnotations();
          Class<?>[] parameterTypes = method.getParameterTypes();
          if (parameterAnnotations.length == 2) {
            Preconditions.checkArgument(parameterTypes[0].equals(List.class));
            Preconditions.checkArgument(parameterTypes[1].equals(Set.class));
          } else if (parameterAnnotations.length >= 2) {
            Preconditions.checkArgument(parameterTypes[0].equals(List.class));
            Set<List<Atom>> current = new HashSet<>();
            for (int i = 1; i < parameterAnnotations.length; i++) {
              Preconditions.checkArgument(parameterTypes[i].equals(List.class));
              Optional<Annotation> any = newArrayList(parameterAnnotations[i])
                  .stream()
                  .filter(p -> p instanceof For)
                  .findAny();
              Preconditions.checkArgument(any.isPresent());
              // Check that the conflicts are covered
              String production = any.map(x -> (For) x).get().value();
              RuleGrammar ruleGrammar = new RuleGrammar(key.getKey());
              List<RuleGrammar.RuleToken> ruleTokens = ruleGrammar.tokenize(production);
              RuleGrammar.Bundle bundle = ruleGrammar.parse(ruleTokens);
              current.add(bundle.head);
            }
            Preconditions.checkArgument(current.equals(conflicts));
          } else {
            throw new IllegalStateException("Conflict resolvers require at least two arguments.");
          }
          continue;
        }
        throw new IllegalStateException(
            "Conflict at " + key + " over " + conflicts +
                "; you should either refactor or add in a conflict resolver.");
      }
    }

    mActionTable = actionTable;
    mPreprocessed = true;
    return mGrammar;
  }

  public Method handleIntermediateVariable(Variable variable, List<Atom> production) throws NoSuchMethodException {
    // $X@...@type#n
    String name = variable.mLabel.substring(1);
    int first = name.indexOf('@');
    int last = name.lastIndexOf('@');
    Preconditions.checkArgument(first != last);
    String parent = name.substring(0, first);
    String expr = name.substring(first + 1, last);
    String type = name.substring(last + 1);
    // Right now, just switch on the type
    switch (type) {
      case "group": {
        if (mGroupHandlers.containsKey(variable)) {
          Method handler = mGroupHandlerRegistry.get(mGroupHandlers.get(variable));
          if (handler != null) {
            return handler;
          } else {
            return getClass().getDeclaredMethod(mGroupHandlers.get(variable), Object[].class);
          }
        }
        return GrammarBase.class.getDeclaredMethod("group", Object[].class);
      }
      case "maybe": {
        if (production.get(0) instanceof Epsilon) {
          return GrammarBase.class.getDeclaredMethod("maybeEmpty");
        } else {
          return GrammarBase.class.getDeclaredMethod("maybeFull", Object.class);
        }
      }
      case "star": {
        if (production.get(0) instanceof Epsilon) {
          return GrammarBase.class.getDeclaredMethod("starEmpty");
        } else {
          return GrammarBase.class.getDeclaredMethod("starFull", Object.class, List.class);
        }
      }
      case "plus":
        return GrammarBase.class.getDeclaredMethod("starFull", Object.class, List.class);
    }
    throw new IllegalStateException();
  }

  public <R> List<R> group(R... args) {
    return newArrayList(args);
  }

  public <R> List<R> starEmpty() {
    return new ArrayList<>();
  }

  public <R> List<R> starFull(R r, List<R> rest) {
    List<R> result = new ArrayList<>();
    result.add(r);
    result.addAll(rest);
    return result;
  }

  public <R> Optional<R> maybeEmpty() {
    return Optional.empty();
  }

  public <R> Optional<R> maybeFull(R r) {
    return Optional.of(r);
  }
}
