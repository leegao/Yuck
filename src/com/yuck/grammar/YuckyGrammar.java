package com.yuck.grammar;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.yuck.auxiliary.descentparsing.Atom;
import com.yuck.auxiliary.descentparsing.GrammarBase;
import com.yuck.auxiliary.descentparsing.Variable;
import com.yuck.auxiliary.descentparsing.annotations.For;
import com.yuck.auxiliary.descentparsing.annotations.Resolve;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;

import static com.yuck.auxiliary.descentparsing.Grammar.V;

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
  level7 := 'not' |  '-' // unary, pre
  level8 := '^' // right
  level9 := (. term)*
  level10 := E(.term | (...))* // unary, post
  */
  @Start
  @Rule("E -> $level1")
  public Object expression(Object level1) {
    return level1;
  }

  @Rule("level1 -> $level2 ((or : bop) $level2 : FoldOp)*")
  public Object level1(Object left, List<?> ors) {
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
    return ranges.map(objects -> left.toString() + objects).orElse(left.toString());
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
    for (Object ignored : ops) buffer += "(";
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

  @Rule("level8 -> $level10 ((pow : bop) $level8)?")
  public Object level8(Object leaf, Optional<List<?>> pow) {
    return pow.map(objects -> "(" + leaf.toString() + foldOperation(objects.get(0), objects.get(1)) + ")").orElse(leaf.toString());
  }

  @Rule("level10' -> . id")
  public Function<Object, Object> level10_(Token dot, Token id) {
    return leaf -> leaf + "." + id;
  }

  @Rule("level10' -> %( $args %)")
  public Function<Object, Object> level10_(Token left, final List<Object> arguments, Token right) {
    return leaf -> {
      Joiner joiner = Joiner.on(", ");
      return leaf.toString() + "(" + joiner.join(arguments) + ")";
    };
  }

  @Rule("level10 -> $E.leaf $level10'*")
  public Object level10(Object leaf, List<Function<Object, Object>> arguments) {
    if (arguments.isEmpty()) {
      return leaf;
    }
    Object tree = arguments.get(0).apply(leaf);
    for (Function<Object, Object> appliable : arguments.subList(1, arguments.size())) {
      tree = appliable.apply(tree.toString().endsWith(")") ? "(" + tree + ")" : tree);
    }
    return tree;
  }

  @Rule("args -> %eps")
  public List<?> arguments() {
    return new ArrayList<>();
  }

  @Rule("args -> $E (, $E : Second)*")
  public List<?> arguments(Object arg, List<?> rest) {
    List<Object> list = new ArrayList<>();
    list.add(arg);
    list.addAll(rest);
    return list;
  }

  @For("Arguments")
  public List<?> forArguments(Object... terms) {
    Preconditions.checkArgument(terms.length == 3);
    return (List<?>) terms[1];
  }

  @For("bop")
  public Object binaryOperator(Object... tokens) {
    Preconditions.checkArgument(tokens.length == 1);
    return tokens[0];
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

  @Rule("E.leaf -> new (id (. id)* : QualifiedName) %( $args %)")
  public Object expLeaf(Token n, Object name, Token open, List<?> arguments, Token closes) {
    return "new " + name + "(" + Joiner.on(", ").join(arguments) + ")";
  }

  @For("QualifiedName")
  public Object buildQualifiedName(Token head, List<Token> tail) {
    if (tail.isEmpty()) return head;
    return head + "." + Joiner.on(".").join(tail);
  }

  @Rule("E.leaf -> function %( $parameters %) { ($statement)* }")
  public Object expLeaf(
      Token function,
      Token open, List<?> parameters, Token close,
      Token left, List<?> statements, Token right) {
    return "function(" + Joiner.on(", ").join(parameters) + ") {" + Joiner.on("; ").join(statements) + "}";
  }

  @Rule("parameters -> %eps")
  public List<?> parameters() {
    return new ArrayList<>();
  }

  @Rule("parameters -> id (, id : Second)*")
  public List<?> parameters(Token id, List<?> rest) {
    List<Object> parameters = new ArrayList<>();
    parameters.add(id);
    parameters.addAll(rest);
    return parameters;
  }

  @Rule("statement -> $E ;")
  public Object statement(Object expr, Token semi) {
    return expr;
  }

  @Rule("var.decl -> var id (= $E)?")
  public Object vardecl(Token var, Token id, Optional<List<?>> init) {
    if (init.isPresent()) {
      return "var " + id + " = " + init.get().get(1);
    }
    return "var " + id;
  }

  @Rule("statement -> $var.decl ;")
  public Object statementVarDecl(Object vardecl, Token semi) {
    return vardecl;
  }

  @Rule("func.decl -> function id %( $parameters %) { ($statement)* }")
  public Object funcdecl(
      Token function,
      Token id,
      Token open, List<?> parameters, Token close,
      Token left, List<?> statements, Token right) {
    return "function " + id + "(" + Joiner.on(", ").join(parameters) + ") {\n  " + Joiner.on(";\n  ").join(statements) + "\n}";
  }

  @Rule("statement -> $func.decl")
  public Object statementFuncDecl(Object funcdecl) {
    return funcdecl;
  }

  @Resolve(variable = "statement", term = "function")
  public List<Atom> resolveFunction(List<Token> rest, Set<List<Atom>> candidates) {
    Preconditions.checkArgument(rest.size() > 1);
    boolean anonymous = rest.get(1).text.equals("(");
    for (List<Atom> candidate : candidates) {
      if (!candidate.get(0).equals(V("func.decl")) && anonymous) {
        return candidate;
      } else if (candidate.get(0).equals(V("func.decl")) && !anonymous) {
        return candidate;
      }
    }
    throw new IllegalStateException();
  }

  @Rule("statement -> while $E { ($statement)* }")
  public Object statement(Token whil, Object cond, Token open, List<?> statements, Token close) {
    return "while " + cond + " { " + Joiner.on("; ").join(statements) + " }";
  }

  @Rule("statement -> for (id (, id : Second)* : Cat) in $E { ($statement)* }")
  public Object statement(
      Token fo,
      List<Token> ids,
      Token in,
      Object expr,
      Token open, List<?> statements, Token close) {
    return "for " + Joiner.on(", ").join(ids) + " in " + expr + " { " + Joiner.on("; ").join(statements) + " }";
  }

  @Rule("statement -> if $E { ($statement)* } (else ($statement | { ($statement)* } ) : Else)?")
  public Object statement(Token iff, Object cond, Token open, List<?> statements, Token close, Optional<List<?>> el) {
    String top = "if " + cond + " { " + Joiner.on("; ").join(statements) + " }";
    if (el.isPresent()) {
      List<?> elseStatements = el.get();
      return top + " else " + (elseStatements.size() != 1 ? "{ " : "") + Joiner.on("; ").join(elseStatements) + (elseStatements.size() != 1 ? " }" : "");
    }
    return top;
  }

  @Resolve(
      // TODO: make this less eyebleedy
      variable = "statement@($statement|{.$statement@($statement@($statement)@group)@star.})@group",
      term = "{")
  public List<Atom> resolveElse(List<Token> rest, Set<List<Atom>> candidates) {
    // Always shift to the `else { statements }` case if rest starts with {
    boolean brack = rest.get(0).text.equals("{");
    for (List<Atom> candidate : candidates) {
      if (candidate.size() == 1 && brack) return candidate;
      if (candidate.size() != 1 && !brack) return candidate;
    }
    throw new IllegalStateException();
  }

  @Rule("statement -> class id { (($var.decl ; | $func.decl : First) : First)* }")
  public Object statement(Token clazz, Token name, Token open, List<?> statements, Token close) {
    return "class " + name + " { " + Joiner.on("; ").join(statements) + " }";
  }

  @Rule("statement -> ;")
  public Object statement(Token semicolon) {
    return "";
  }

  @For("Else")
  public List<?> elseClause(Token el, List<?> statementish) {
    if (statementish.size() == 1) {
      return Lists.newArrayList(statementish.get(0));
    } else if (statementish.size() == 3){
      return (List<?>) statementish.get(1);
    }
    throw new IllegalStateException();
  }

  @For("Cat")
  public <U> List<U> concat(U head, List<U> tail) {
    List<U> result = new ArrayList<>();
    result.add(head);
    result.addAll(tail);
    return result;
  }

  @For("SingleToken")
  public Token singleToken(Object... tokens) {
    Preconditions.checkArgument(tokens.length == 1);
    return (Token) tokens[0];
  }

  @SuppressWarnings("unchecked")
  @For("Second")
  public <T> T second(Object... terms) {
    Preconditions.checkArgument(terms.length > 1);
    return (T) terms[1];
  }

  @SuppressWarnings("unchecked")
  @For("First")
  public <T> T first(Object... terms) {
    Preconditions.checkArgument(terms.length > 0);
    return (T) terms[0];
  }

  @Override
  protected Set<List<Atom>> handleError(Variable variable, Atom on, List<Token> stream) {
//    if (variable.toString().equals(
//        "$statement@($statement@(else.$statement@($statement|{.$statement@($statement@($statement.;)@group)@star.})@group)@group)@maybe")) {
//      return this.mActionTable.get(new Pair<>(variable, new Terminal(";")));
//    }
    return super.handleError(variable, on, stream);
  }

  @Override
  protected Token handleConsumptionError(
      Variable state,
      Atom next,
      List<Token> stream,
      List<Atom> currentSentence,
      Atom expected) {
    // Try to handle missing semicolons whenever possible.
//    if (expected.toString().equals(";"))
//      return new Token(";", -1, -1, ";");
    return super.handleConsumptionError(state, next, stream, currentSentence, expected);
  }

  public static void main(String[] args) throws IOException {
    YuckyGrammar grammar = new YuckyGrammar();
    grammar.preprocess();

    String code1 = "{(1) : function(x){ foo(); bar(); var x = 3; }, \"1\" : new Baz().jar.poo()(132)} - -3 * 2**3**foo(5.baz, 3**3).lol()";
    String code2 = "function(){" +
        "function foo() {print(\"Hello!\");}" +
        "function() {while (true) {}};" +
        "for i in 1 to 3 {}" +
        "if true {} else if false {}" +
        "x();" +
        "class Bar { var z; var d; }" +
    "}";

    YuckyLexer lexer = new YuckyLexer(new StringReader(code1));
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
