package com.yuck.grammar;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.yuck.ast.*;
import com.yuck.auxiliary.descentparsing.Atom;
import com.yuck.auxiliary.descentparsing.GrammarBase;
import com.yuck.auxiliary.descentparsing.Variable;
import com.yuck.auxiliary.descentparsing.annotations.For;
import com.yuck.auxiliary.descentparsing.annotations.Resolve;
import com.yuck.auxiliary.descentparsing.annotations.Rule;
import com.yuck.auxiliary.descentparsing.annotations.Start;
import javafx.util.Pair;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
  public Expression expression(Expression level1) {
    return level1;
  }

  @Rule("level1 -> $level2 ((or : bop) $level2 : FoldOp)*")
  public Expression level1(Expression left, List<Function<Expression, Expression>> ors) {
    return merge(left, ors);
  }

  @Rule("level2 -> $level3 ((and : bop) $level3 : FoldOp)*")
  public Expression level2(Expression left, List<Function<Expression, Expression>> ands) {
    return merge(left, ands);
  }

  @Rule("level3 -> $level4 ((< | > | <= | >= | != | == : bop) $level4 : FoldOp)*")
  public Expression level3(Expression left, List<Function<Expression, Expression>> cmps) {
    return merge(left, cmps);
  }

  @Rule("level4 -> $level5 ((to : bop) $level4 : FoldOp)?")
  public Expression level4(Expression left, Optional<Function<Expression, Expression>> ranges) {
    return ranges.map(expr -> expr.apply(left)).orElse(left);
  }

  @Rule("level5 -> $level6 ((add | - : bop) $level6 : FoldOp)*")
  public Expression level5(Expression left, List<Function<Expression, Expression>> ops) {
    return merge(left, ops);
  }

  @Rule("level6 -> $level7 ((mul | / | mod : bop) $level7 : FoldOp)*")
  public Expression level6(Expression left, List<Function<Expression, Expression>> ops) {
    return merge(left, ops);
  }

  private Expression merge(Expression left, List<Function<Expression, Expression>> ops) {
    for (Function<Expression, Expression> op : ops) left = op.apply(left);
    return left;
  }

  @Rule("level7 -> (- | not : bop) $level7")
  public Expression level7(Token op, Expression right) {
    return new UnaryOperator(op, right);
  }

  @Rule("level7 -> $level8")
  public Expression level7(Expression level8) {
    return level8;
  }

  @Rule("level8 -> $level10 ((pow : bop) $level8 : FoldOp)?")
  public Expression level8(Expression left, Optional<Function<Expression, Expression>> pow) {
    return pow
        .map(expr -> expr.apply(left))
        .orElse(left);
  }

  @For("bop")
  public Token binaryOperator(Token token) {
    return token;
  }

  @For("FoldOp")
  public Function<Expression, Expression> foldOperation(Token op, Expression right) {
    return left -> new BinaryOperator(op.text, left, right);
  }

  @Rule("level10' -> . id")
  public Function<Expression, Expression> level10_(Token dot, Token id) {
    return leaf -> new Selector(leaf, id);
  }

  @Rule("level10' -> %( $args %)")
  public Function<Expression, Expression> level10_(Token left, final List<Expression> arguments, Token right) {
    return leaf -> new Call(leaf, arguments, right);
  }

  @Rule("level10 -> $E.leaf $level10'*")
  public Expression level10(Expression leaf, List<Function<Expression, Expression>> arguments) {
    for (Function<Expression, Expression> appliable : arguments) {
      leaf = appliable.apply(leaf);
    }
    return leaf;
  }

  @Rule("args -> %eps")
  public List<Expression> arguments() {
    return new ArrayList<>();
  }

  @Rule("args -> $E (, $E : Second)*")
  public List<Expression> arguments(Expression left, List<Expression> rest) {
    List<Expression> list = new ArrayList<>();
    list.add(left);
    list.addAll(rest);
    return list;
  }

  @For("Arguments")
  public List<?> forArguments(Object... terms) {
    Preconditions.checkArgument(terms.length == 3);
    return (List<?>) terms[1];
  }

  @Rule("E.leaf -> (num | string | true | false | id : SingleToken)")
  public Expression expLeaf(Token token) {
    // TODO: fixme
    return new Var(token);
  }

  @Rule("E.leaf -> %( $E %)")
  public Expression expLeaf(Token left, Expression group, Token right) {
    return new GroupExpression(left, group, right);
  }

  @Rule("E.leaf -> [ $E (, $E : Second)* ]")
  public Expression expLeaf(Token lbracket, Expression head, List<Expression> expressions, Token rbracket) {
    List<Expression> list = new ArrayList<>();
    list.add(head);
    list.addAll(expressions);
    return new ListLiteral(lbracket, list, rbracket);
  }

  @Rule("E.leaf -> [ ]")
  public Expression expLeaf(Token left, Token right) {
    return new ListLiteral(left, new ArrayList<>(), right);
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
  public Expression expLeaf(Token left, Expression key, Token colon, Expression val, List<List<?>> tail, Token right) {
    List<Pair<Expression, Expression>> terms = new ArrayList<>();
    terms.add(new Pair<>(key, val));
    for (List<?> entry : tail) {
      Preconditions.checkArgument(entry.size() == 4);
      terms.add(new Pair<>((Expression) entry.get(1), (Expression) entry.get(3)));
    }
    return new MapLiteral(left, terms, right);
  }

  @Rule("E.leaf -> { }")
  public Expression expressionMap(Token left, Token right) {
    return new MapLiteral(left, new ArrayList<>(), right);
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
  public Expression expLeaf(Token start, QualifiedName name, Token open, List<Expression> arguments, Token close) {
    return new NewExpression(start, name, arguments, close);
  }

  @For("QualifiedName")
  public QualifiedName buildQualifiedName(Token head, List<Token> tail) {
    List<String> names = new ArrayList<>();
    names.add(head.text);
    names.addAll(tail.stream().map(token -> token.text).collect(Collectors.toList()));
    List<Token> tokens = new ArrayList<>();
    tokens.add(head);
    tokens.addAll(tail);
    return new QualifiedName(head, names, tokens.get(tokens.size() - 1));
  }

  @Rule("E.leaf -> function %( $parameters %) { ($statement)* }")
  public Expression expLeaf(
      Token function,
      Token open, List<Var> parameters, Token close,
      Token left, List<Statement> statements, Token right) {
    return new FunctionExpression(function, parameters, statements, right);
  }

  @Rule("parameters -> %eps")
  public List<Var> parameters() {
    return new ArrayList<>();
  }

  @Rule("parameters -> id (, id : Second)*")
  public List<Var> parameters(Token id, List<Token> rest) {
    List<Var> parameters = new ArrayList<>();
    parameters.add(new Var(id));
    parameters.addAll(rest.stream().map(Var::new).collect(Collectors.toList()));
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

    YuckyLexer lexer = new YuckyLexer(new StringReader(code2));
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
