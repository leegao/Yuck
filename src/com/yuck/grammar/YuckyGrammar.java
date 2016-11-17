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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YuckyGrammar extends GrammarBase<Token> {
  @Override
  public String label(Token token) {
    return token.type;
  }

  @Start
  @Rule("E -> (num | string | true | false | id : SingleToken)")
  public Object expression(Token token) {
    return token;
  }

  @Rule("E -> [ $E (, $E : Second)* ]")
  public Object expression(Token lbracket, Object head, List<?> expressions, Token rbracket) {
    List<Object> list = new ArrayList<>();
    list.add(head);
    list.addAll(expressions);
    return list;
  }

  @Rule("E -> [ ]")
  public Object expression(Token left, Token right) {
    return new ArrayList<>();
  }

  @Resolve(variable = "E", term = "[")
  public List<Atom> resolveList(
      List<Token> rest,
      @For("[ $E (, $E : Second)* ]") List<Atom> filledList,
      @For("[ ]") List<Atom> emptyList) {
    Preconditions.checkArgument(rest.size() > 1);
    return rest.get(1).type.equals("]") ? emptyList : filledList;
  }

  @Rule("E -> { $E %: $E (, $E %: $E)* }")
  public Object expression(Token left, Object key, Token colon, Object val, List<List<?>> tail, Token right) {
    Map<Object, Object> map = new HashMap<>();
    map.put(key, val);
    for (List<?> entry : tail) {
      Preconditions.checkArgument(entry.size() == 4);
      map.put(entry.get(1), entry.get(3));
    }
    return map;
  }

  @Rule("E -> { }")
  public Object expressionMap(Token left, Token right) {
    return new HashMap<>();
  }

  @Resolve(variable = "E", term = "{")
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

    YuckyLexer lexer = new YuckyLexer(new StringReader("{1 : \"1\", \"1\" : 2}"));
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
