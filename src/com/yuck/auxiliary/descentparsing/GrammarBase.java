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

  public static class RuleGrammar {
    private static final Set<String> SIMPLE_TOKENS = Sets.newHashSet("+", "*", "?", "(", ")", "|");
    private static final Set<String> ESCAPES = Sets.newHashSet("%+", "%*", "%?", "%(", "%)", "%|");
    private static final Pattern SIMPLE_PATTERN = Pattern.compile("\\(|\\)|\\*|\\+|\\||\\?");
    private static final Map<String, Method> mRegistrar = new HashMap<>();
    private static final Grammar mRuleGrammar;
    private static final Map<Pair<Variable, List<Atom>>, Method> mRuleRegistry = new HashMap<>();
    static {
      for (Method method : RuleGrammar.class.getDeclaredMethods()) {
        Register registration = method.getDeclaredAnnotation(Register.class);
        if (registration != null) {
          mRegistrar.put(registration.value(), method);
        }
      }

      HashMultimap<Variable, List<Atom>> rules = HashMultimap.create();
      rules.put(V("E"), newArrayList(T("eps")));
      rules.put(V("E"), newArrayList(V("E_group2"), V("E'")));
      rules.put(V("E'"), newArrayList(V("E")));
      rules.put(V("E'"), newArrayList(E()));
      rules.put(V("E_group1"), newArrayList(T("term")));
      rules.put(V("E_group1"), newArrayList(T("var")));
      rules.put(V("E_group1"), newArrayList(T("("), V("Group"), T(")")));
      rules.put(V("E_group2"), newArrayList(V("E_group1"), V("E_group2'")));
      rules.put(V("E_group2'"), newArrayList(E()));
      rules.put(V("E_group2'"), newArrayList(V("$Post")));
      rules.put(V("Group"), newArrayList(V("E"), V("Group'")));
      rules.put(V("Group'"), newArrayList(E()));
      rules.put(V("Group'"), newArrayList(T("|"), V("Group")));
      rules.put(V("Post"), newArrayList(T("*")));
      rules.put(V("Post"), newArrayList(T("+")));
      rules.put(V("Post"), newArrayList(T("?")));
      mRuleGrammar = new Grammar(rules, V("E"));

      register(V("E"), newArrayList(T("eps")), "E#1");
      register(V("E"), newArrayList(V("E_group2"), V("E'")), "E#2");
      register(V("E'"), newArrayList(V("E")), "E'#1");
      register(V("E'"), newArrayList(E()), "E'#2");
      register(V("E_group1"), newArrayList(T("term")), "E_group1#term");
      register(V("E_group1"), newArrayList(T("var")), "E_group1#term");
      register(V("E_group1"), newArrayList(T("("), V("Group"), T(")")), "E_group1#group");
      register(V("E_group2"), newArrayList(V("E_group1"), V("E_group2'")), "E_group2");
      register(V("E_group2'"), newArrayList(E()), "E_group2'#1");
      register(V("E_group2'"), newArrayList(V("$Post")), "E_group2'#2");
      register(V("Group"), newArrayList(V("E"), V("Group'")), "Group");
      register(V("Group'"), newArrayList(E()), "Group'#1");
      register(V("Group'"), newArrayList(T("|"), V("Group")), "Group'#2");
      register(V("Post"), newArrayList(T("*")), "Post");
      register(V("Post"), newArrayList(T("+")), "Post");
      register(V("Post"), newArrayList(T("?")), "Post");
    }

    private final Variable mParent;
    private final List<String> mScopes = new ArrayList<>();
    private static int sId = 0;

    public RuleGrammar(Variable parent) {
      mParent = parent;
    }

    static void register(Variable variable, List<Atom> sentence, String target) {
      Method method = Preconditions.checkNotNull(mRegistrar.get(target));
      mRuleRegistry.put(new Pair<>(variable, sentence), method);
    }

    static void preprocess() {
      HashMultimap<Pair<Variable, Atom>, List<Atom>> actions = mRuleGrammar.actions();

    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Register {
      String value();
    }

    @Register("E#1") // E -> eps
    public Bundle E1(RuleToken eps) {
      return Bundle.of(HashMultimap.create(), newArrayList(E()));
    }

    @Register("E#2") // E -> $E_group $E'
    public Bundle E1(Bundle group, Optional<Bundle> e_) {
      if (e_.isPresent()) {
        return Bundle.merge(group, e_.get());
      }
      return group;
    }

    @Register("E'#1")
    public Optional<Bundle> E_(RuleToken eps) {
      return Optional.empty();
    }

    @Register("E'#2")
    public Optional<Bundle> E_(Bundle e) {
      return Optional.of(e);
    }

    @Register("E_group2'#1") // E_group22 -> %eps
    public Optional<Postfix> E_group2_() {
      return Optional.empty();
    }

    @Register("E_group2'#2") // E_group2' -> $Post
    public Optional<Postfix> E_group2_(Postfix operator) {
      return Optional.of(operator);
    }

    @Register("Post")
    public Postfix Post(RuleToken token) {
      switch (token.type) {
        case "+": return Postfix.PLUS;
        case "*": return Postfix.STAR;
        case "?": return Postfix.MAYBE;
      }
      throw new IllegalStateException();
    }

    @Register("E_group1#term") // -> term | var
    public Bundle E_group1(RuleToken term) {
      switch (term.type) {
        case "term": return Bundle.of(HashMultimap.create(), newArrayList(T(term.data)));
        case "var": return Bundle.of(HashMultimap.create(), newArrayList(V(term.data)));
      }
      throw new IllegalStateException();
    }

    @Register("E_group1#group") // -> %( $Group %)
    public Bundle E_group1(RuleToken _, List<Bundle> group, RuleToken __) {
      // generates a new variable that gets pointed to e and others; it then gets added to the remaining bundles
      List<List<Atom>> alternates = new ArrayList<>();
      HashMultimap<Variable, List<Atom>> intermediates = HashMultimap.create();
      for (Bundle bundle : group) {
        alternates.add(bundle.head);
        intermediates.putAll(bundle.intermediates);
      }
      Variable fvs = fresh(alternates, "group");
      for (List<Atom> sentence : alternates) {
        intermediates.put(fvs, sentence);
      }
      return Bundle.of(intermediates, newArrayList(fvs));
    }

    @Register("E_group2") // -> $E_group1 $E_group2'
    public Bundle E_group2(Bundle group, Optional<Postfix> rest) {
      if (rest.isPresent()) {
        // X? => fvs -> %eps | X
        // X* => fvs -> %eps | X fvs
        // X+ => X fvs; fvs -> %eps | X fvs
        List<List<Atom>> sentence = new ArrayList<>();
        HashMultimap<Variable, List<Atom>> intermediates = HashMultimap.create();
        intermediates.putAll(group.intermediates);
        sentence.add(group.head);
        switch (rest.get()) {
          case MAYBE: {
            Variable fvs = fresh(sentence, "maybe");
            intermediates.put(fvs, newArrayList(E()));
            intermediates.put(fvs, group.head);
            return Bundle.of(intermediates, newArrayList(fvs));
          }
          case STAR: {
            Variable fvs = fresh(sentence, "star");
            List<Atom> newSentence = new ArrayList<>();
            newSentence.addAll(group.head);
            newSentence.add(fvs);
            intermediates.put(fvs, newArrayList(E()));
            intermediates.put(fvs, newSentence);
            return Bundle.of(intermediates, newArrayList(fvs));
          }
          case PLUS: {
            Variable fvs = fresh(sentence, "plus");
            List<Atom> newSentence = new ArrayList<>();
            newSentence.addAll(group.head);
            newSentence.add(fvs);
            intermediates.put(fvs, newArrayList(E()));
            intermediates.put(fvs, newSentence);
            return Bundle.of(intermediates, newArrayList(newSentence));
          }
        }
        throw new NotImplementedException();
      }
      return group;
    }

    @Register("Group") // -> $E $Group'
    public List<Bundle> group(Bundle e, List<Bundle> others) {
      // generates a new variable that gets pointed to e and others; it then gets added to the remaining bundles
      List<Bundle> all = new ArrayList<>();
      all.add(e);
      all.addAll(others);
      return all;
    }

    @Register("Group'#1") // -> %eps
    public List<Bundle> group_(RuleToken eps) {
      return new ArrayList<>();
    }

    @Register("Group'#2") // -> '|' $Group
    public List<Bundle> group_(RuleToken bar, List<Bundle> bundles) {
      return bundles;
    }

    public Variable fresh(List<List<Atom>> rule, String type) {
      sId += 1;
      return V(mParent.mLabel.substring(1) + "@" + rule + "#" + type + sId);
    }

    enum Postfix {
      PLUS, STAR, MAYBE
    }

    static class Bundle {
      private final HashMultimap<Variable, List<Atom>> intermediates;
      private final List<Atom> head;

      Bundle(HashMultimap<Variable, List<Atom>> intermediates, List<Atom> head) {
        this.intermediates = intermediates;
        this.head = head;
      }

      public static Bundle of(HashMultimap<Variable, List<Atom>> intermediates, List<Atom> head) {
        return new Bundle(intermediates, head);
      }

      public static Bundle merge(Bundle left, Bundle right) {
        // (map1, h1) + (map2, h2) = (map1 \cup map2, h1 ++ h2)
        HashMultimap<Variable, List<Atom>> resultMap = HashMultimap.create();
        resultMap.putAll(left.intermediates);
        resultMap.putAll(right.intermediates);
        List<Atom> resultList = new ArrayList<>();
        resultList.addAll(left.head);
        resultList.addAll(right.head);
        return Bundle.of(resultMap, resultList);
      }
    }

    /**
     * // E -> %eps | ((term | var | %( $Group %)) $post?)+
     * E -> eps | $E_group2 $E'
     * E' -> $E | %eps
     * E_group1 -> term | var | %( $Group %)
     * E_group2 -> $E_group1 $E_group2'
     * E_group2' -> %eps | $post
     * Group -> $E $Group'
     * Group' -> %eps | '|' $Group
     * Post -> + | * | ?
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

      for (String term : subterms) {
        if (SIMPLE_TOKENS.contains(term)) {
          tokens.add(new RuleToken(term, term));
        } else if (term.startsWith("$")) {
          tokens.add(new RuleToken("var", term));
        } else if (term.equals("%eps")) {
          tokens.add(new RuleToken("eps", term));
        } else {
          tokens.add(new RuleToken("term", term));
        }
      }
      return tokens;
    }
  }
}
