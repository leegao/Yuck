package com.yuck.grammar;

import com.google.common.base.Preconditions;
import com.yuck.auxiliary.descentparsing.Atom;
import com.yuck.auxiliary.descentparsing.GrammarBase;
import com.yuck.auxiliary.descentparsing.RuleGrammar;
import com.yuck.auxiliary.descentparsing.annotations.For;
import com.yuck.auxiliary.descentparsing.annotations.Resolve;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class YuckyGrammar extends GrammarBase<Token> {
  @Override
  public String label(Token token) {
    return token.type;
  }

//  @Start
//  @Rule("E -> $E.leaf")
//  public Object expression(Object leaf) {
//    return leaf;
//  }

  // Precedence chart
  /*
  level1 := 'or' // left
  level2 := 'and' // left
  level3 := '<'  |  '>'  |  '<=' |  '>='  |  '!='  |  '==' // left
  level4 := 'to' // right
  level5 := '+'  |  '-' // left
  level6 := '*'  |  '/'  |  '%' // left
  level7 := 'not' |  '-' // unary
  level8 := '^' // right
  */
  @Start
  @Rule("E -> $level2 ((or : bop) $level2 : FoldOp)*")
  public Object expression(Object left, List<?> ors) {
    return merge(left, ors);
  }

  @Rule("level2 -> $level3 ((and : bop) $level3 : FoldOp)*")
  public Object level2(Object left, List<?> ands) {
    return merge(left, ands);
  }

  @Rule("level3 -> $level4 ((< | > | <= | >= | != | == : bop) $level4 : FoldOp)*")
  public Object level3(Object left, List<?> cmps) {
    return merge(left, cmps);
  }

  @Rule("level4 -> $level5 ((to : bop) $level4)?")
  public Object level4(Object left, Optional<List<?>> ranges) {
    if (ranges.isPresent()) {
      return left.toString() + ranges.get();
    }
    return left;
  }

  @Rule("level5 -> $level6 ((add | - : bop) $level6 : FoldOp)*")
  public Object level5(Object left, List<?> ops) {
    return merge(left, ops);
  }

  @Rule("level6 -> $level7 ((mul | / | mod : bop) $level7 : FoldOp)*")
  public Object level6(Object left, List<?> ops) {
    return merge(left, ops);
  }

  private Object merge(Object left, List<?> ops) {
    String buffer = "";
    for (Object op : ops) buffer += "(";
    buffer += left;
    for (Object op : ops) buffer += op + ")";
    return buffer;
  }

  @Rule("level7 -> (- | not) $level7")
  public Object level7(List<?> op, Object right) {
    return "(" + op.get(0).toString() + right.toString() + ")";
  }

  @Rule("level7 -> $level8")
  public Object level7(Object level8) {
    return level8;
  }

  @Rule("level8 -> $E.leaf ((pow : bop) $level8)?")
  public Object level8(Object leaf, Optional<List<?>> pow) {
    if (pow.isPresent()) {
      return "(" + leaf.toString() + foldOperation(pow.get().get(0), pow.get().get(1)) + ")";
    }
    return leaf;
  }

  @For("bop")
  public Object binaryOperator(Object... tokens) {
    Preconditions.checkArgument(tokens.length == 1);
    return (Object) tokens[0];
  }

  @For("FoldOp")
  public Object foldOperation(Object... terms) {
    Preconditions.checkArgument(terms.length == 2);
    return terms[0].toString() + terms[1].toString();
  }

  @Rule("E.leaf -> (num | string | true | false | id : SingleToken)")
  public Object expLeaf(Token token) {
    return token;
  }

  @Rule("E.leaf -> %( $E %)")
  public Object expLeaf(Token left, Object group, Token right) {
    return group;
  }

  @Rule("E.leaf -> [ $E (, $E : Second)* ]")
  public Object expLeaf(Token lbracket, Object head, List<?> expressions, Token rbracket) {
    List<Object> list = new ArrayList<>();
    list.add(head);
    list.addAll(expressions);
    return list;
  }

  @Rule("E.leaf -> [ ]")
  public Object expLeaf(Token left, Token right) {
    return new ArrayList<>();
  }

  @Resolve(variable = "E.leaf", term = "[")
  public List<Atom> resolveList(
      List<Token> rest,
      @For("[ $E (, $E : Second)* ]") List<Atom> filledList,
      @For("[ ]") List<Atom> emptyList) {
    Preconditions.checkArgument(rest.size() > 1);
    return rest.get(1).type.equals("]") ? emptyList : filledList;
  }

  @Rule("E.leaf -> { $E %: $E (, $E %: $E)* }")
  public Object expLeaf(Token left, Object key, Token colon, Object val, List<List<?>> tail, Token right) {
    Map<Object, Object> map = new HashMap<>();
    map.put(key, val);
    for (List<?> entry : tail) {
      Preconditions.checkArgument(entry.size() == 4);
      map.put(entry.get(1), entry.get(3));
    }
    return map;
  }

  @Rule("E.leaf -> { }")
  public Object expressionMap(Token left, Token right) {
    return new HashMap<>();
  }

  @Resolve(variable = "E.leaf", term = "{")
  public List<Atom> resolveMap(
      List<Token> rest,
      @For("{ $E %: $E (, $E %: $E)* }") List<Atom> filledList,
      @For("{ }") List<Atom> emptyList) {
    Preconditions.checkArgument(rest.size() > 1);
    return rest.get(1).type.equals("}") ? emptyList : filledList;
  }

  @For("SingleToken")
  public Token singleToken(Object... tokens) {
    Preconditions.checkArgument(tokens.length == 1);
    return (Token) tokens[0];
  }

  @For("Second")
  public <T> T second(Object... terms) {
    Preconditions.checkArgument(terms.length > 1);
    return (T) terms[1];
  }

  @For("First")
  public <T> T first(Object... terms) {
    Preconditions.checkArgument(terms.length > 0);
    return (T) terms[0];
  }

  public static void main(String[] args) throws IOException {
    YuckyGrammar grammar = new YuckyGrammar();
    grammar.preprocess();

    YuckyLexer lexer = new YuckyLexer(new StringReader("{(1) : \"1\", \"1\" : 2} - -3 * 2**3**4"));
    List<Token> tokens = new ArrayList<>();
    Token token = lexer.yylex();
    while (token != null) {
      tokens.add(token);
      token = lexer.yylex();
    }
    Object o = grammar.parse(tokens);
    System.err.println(o);
  }
}
