package com.yuck.auxiliary.descentparsing;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.yuck.auxiliary.descentparsing.Grammar.E;
import static com.yuck.auxiliary.descentparsing.Grammar.T;
import static com.yuck.auxiliary.descentparsing.Grammar.V;

/**
 * E -> eps | $E_group2 $E'
 * E' -> $E | %eps
 * E_group1 -> term | var | %( $Group $Handler %)
 * E_group2 -> $E_group1 $E_group2'
 * E_group2' -> %eps | $post
 * Group -> $E $Group'
 * Group' -> %eps | '|' $Group
 * Post -> + | * | ?
 * Handler -> %eps | ':' term
 *
 * Tokens: $var, term, +, *, ?, (, ), |, %eps, :, [%+, %*, %?, %(, %), %|] <- terms
 */
public class RuleGrammar extends GrammarBase<RuleGrammar.RuleToken> {
  private static final Set<String> SIMPLE_TOKENS = Sets.newHashSet("+", "*", "?", "(", ")", "|", ":");
  private static final Set<String> ESCAPES = Sets.newHashSet("%+", "%*", "%?", "%(", "%)", "%|", "%:");
  private static final Pattern SIMPLE_PATTERN = Pattern.compile("\\(|\\)|\\*|\\+|\\||\\?|:");
  private static final Map<String, Method> mRegistrar = new HashMap<>();
  private static final Grammar sRuleGrammar;
  private static final Map<Pair<Variable, List<Atom>>, Method> sRuleRegistry = new HashMap<>();
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
    rules.put(V("E_group1"), newArrayList(T("("), V("Group"), V("Handler"), T(")")));
    rules.put(V("E_group2"), newArrayList(V("E_group1"), V("E_group2'")));
    rules.put(V("E_group2'"), newArrayList(E()));
    rules.put(V("E_group2'"), newArrayList(V("Post")));
    rules.put(V("Group"), newArrayList(V("E"), V("Group'")));
    rules.put(V("Group'"), newArrayList(E()));
    rules.put(V("Group'"), newArrayList(T("|"), V("Group")));
    rules.put(V("Post"), newArrayList(T("*")));
    rules.put(V("Post"), newArrayList(T("+")));
    rules.put(V("Post"), newArrayList(T("?")));
    rules.put(V("Handler"), newArrayList(E()));
    rules.put(V("Handler"), newArrayList(T(":"), T("term")));
    sRuleGrammar = new Grammar(rules, V("E"));

    register(V("E"), newArrayList(T("eps")), "E#1");
    register(V("E"), newArrayList(V("E_group2"), V("E'")), "E#2");
    register(V("E'"), newArrayList(V("E")), "E'#2");
    register(V("E'"), newArrayList(E()), "E'#1");
    register(V("E_group1"), newArrayList(T("term")), "E_group1#term");
    register(V("E_group1"), newArrayList(T("var")), "E_group1#term");
    register(V("E_group1"), newArrayList(T("("), V("Group"), V("Handler"), T(")")), "E_group1#group");
    register(V("E_group2"), newArrayList(V("E_group1"), V("E_group2'")), "E_group2");
    register(V("E_group2'"), newArrayList(E()), "E_group2'#1");
    register(V("E_group2'"), newArrayList(V("Post")), "E_group2'#2");
    register(V("Group"), newArrayList(V("E"), V("Group'")), "Group");
    register(V("Group'"), newArrayList(E()), "Group'#1");
    register(V("Group'"), newArrayList(T("|"), V("Group")), "Group'#2");
    register(V("Post"), newArrayList(T("*")), "Post");
    register(V("Post"), newArrayList(T("+")), "Post");
    register(V("Post"), newArrayList(T("?")), "Post");
    register(V("Handler"), newArrayList(E()), "Handler#1");
    register(V("Handler"), newArrayList(T(":"), T("term")), "Handler#2");
  }

  private final Variable mParent;
  protected final Map<Variable, String> mGroupHandlers = new HashMap<>();

  public RuleGrammar(Variable parent) {
    mParent = parent;
  }

  static void register(Variable variable, List<Atom> sentence, String target) {
    Method method = Preconditions.checkNotNull(mRegistrar.get(target));
    sRuleRegistry.put(new Pair<>(variable, sentence), method);
  }

  @Override
  public String label(RuleToken token) {
    return token.type;
  }

  @Override
  protected Grammar preprocess() {
    if (mPreprocessed) {
      return mGrammar;
    }

    mGrammar = sRuleGrammar;
    mMethodMap = sRuleRegistry;
    mActionTable = mGrammar.actions();
    mPreprocessed = true;
    return mGrammar;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Register {
    String value();
  }

  @Register("E#1") // E -> eps
  public Bundle E1(RuleToken epsToken) {
    return Bundle.of(HashMultimap.create(), newArrayList(E()));
  }

  @Register("E#2") // E -> $E_group $E'
  public Bundle E1(Bundle group, Optional<Bundle> e_) {
    if (e_.isPresent()) {
      return Bundle.merge(group, e_.get());
    }
    return group;
  }

  @Register("E'#1") // -> %eps
  public Optional<Bundle> E_() {
    return Optional.empty();
  }

  @Register("E'#2") // -> $E
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

  @Register("Post") // -> + | * | ?
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
      case "var": return Bundle.of(HashMultimap.create(), newArrayList(V(term.data.substring(1))));
    }
    throw new IllegalStateException();
  }

  @Register("E_group1#group") // -> %( $Group $Handler %)
  public Bundle E_group1(RuleToken lparen, List<Bundle> group, Optional<String> handler, RuleToken rparen) {
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

    if (handler.isPresent()) {
      mGroupHandlers.put(fvs, handler.get());
    }
    return Bundle.of(intermediates, newArrayList(fvs));
  }

  @Register("Handler#1") // -> %eps
  public Optional<String> Handler() {
    return Optional.empty();
  }

  @Register("Handler#2") // -> ':' term
  public Optional<String> Handler(RuleToken colon, RuleToken term) {
    return Optional.of(term.data);
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
          Variable star = fresh(sentence, "star");
          List<Atom> newSentence = new ArrayList<>();
          newSentence.addAll(group.head);
          newSentence.add(star);
          intermediates.put(star, newArrayList(E()));
          intermediates.put(star, newSentence);
          intermediates.put(fvs, newSentence);
          return Bundle.of(intermediates, newArrayList(fvs));
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
  public List<Bundle> group_() {
    return new ArrayList<>();
  }

  @Register("Group'#2") // -> '|' $Group
  public List<Bundle> group_(RuleToken bar, List<Bundle> bundles) {
    return bundles;
  }

  public Variable fresh(List<List<Atom>> rule, String type) {
    String mid = Joiner.on("|").join(rule.stream().map(x -> Joiner.on(".").join(x)).collect(Collectors.toList()));
    return V(mParent.mLabel.substring(1) + "@(" + mid + ")@" + type);
  }

  enum Postfix {
    PLUS, STAR, MAYBE
  }

  static class Bundle {
    public final HashMultimap<Variable, List<Atom>> intermediates;
    public final List<Atom> head;

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

    @Override
    public String toString() {
      return head + " :\t" + intermediates;
    }
  }

  class RuleToken {
    final String type;
    final String data;

    RuleToken(String type, String data) {
      this.type = type;
      this.data = data;
    }

    @Override
    public String toString() {
      return data;
    }
  }

  public List<RuleToken> tokenize(String rule) {
    List<String> terms = Splitter.onPattern("\\s+").trimResults().splitToList(rule);
    List<RuleToken> tokens = new ArrayList<>();
    List<String> subterms = new ArrayList<>();
    for (String term : terms) {
      if (ESCAPES.contains(term)) {
        subterms.add(term);
        continue;
      }
      Matcher matcher = SIMPLE_PATTERN.matcher(term);
      int last = 0;
      while (matcher.find(last)) {
        // split
        int start = matcher.start();
        int end = matcher.end();
        String prefix = term.substring(last, start);
        if (!prefix.isEmpty()) subterms.add(prefix);
        String middle = term.substring(start, end);
        if (!middle.isEmpty()) subterms.add(middle);
        last = end;
      }
      String postfix = term.substring(last);
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
