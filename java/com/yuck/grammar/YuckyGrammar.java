package com.yuck.grammar;

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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yuck.auxiliary.descentparsing.Grammar.V;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class YuckyGrammar extends GrammarBase<Token> {

  @Override
  public String label(Token token) {
    return token.type;
  }

  // Precedence chart
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

  @Rule("level10' -> [ $E ]")
  public Function<Expression, Expression> level10_(Token left, final Expression expression, Token right) {
    return leaf -> new IndexExpression(leaf, expression, right);
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
    switch (token.type) {
      case "id":
        return new Var(token);
      case "string":
        return new StringLiteral(token);
      default:
        return new Literal(token);
    }
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

  @Rule("E.leaf -> function %( $parameters %) { ($statement : First)* }")
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

  @Rule("E.leaf -> nil")
  public Expression expLeafNil(Token nil) {
    return new Literal(nil);
  }

  @Rule("statement -> $E (= $E : Assignment)?;")
  public Statement statement(Expression expr, Optional<Expression> assignment, Token semi) {
    if (assignment.isPresent()) {
      // check the type of expr to disambiguate
      Expression assignee = assignment.get();
      if (expr instanceof Var) {
        return new AssignmentStatement(((Var) expr).token, assignee, semi);
      } else if (expr instanceof IndexExpression){
        return new IndexAssign(((IndexExpression) expr).left, ((IndexExpression) expr).index, assignee, semi);
      } else {
        throw new NotImplementedException();
      }
    }
    return new ExpressionStatement(expr, semi);
  }

  @For("Assignment")
  public Expression assignment(Token eq, Expression assignment) {
    return assignment;
  }

  @Rule("var.decl -> var id (= $E : Second)?")
  public Function<Token, Statement> vardecl(Token var, Token id, Optional<Expression> init) {
    return semi -> new VariableDeclaration(var, id, init, semi);
  }

  @Rule("statement -> $var.decl ;")
  public Statement statementVarDecl(Function<Token, Statement> vardecl, Token semi) {
    return vardecl.apply(semi);
  }

  @Rule("func.decl -> function id %( $parameters %) { ($statement : First)* }")
  public Statement funcdecl(
      Token function,
      Token id,
      Token open, List<Var> parameters, Token close,
      Token left, List<Statement> statements, Token right) {
    return new FunctionDeclaration(function, id, parameters, statements, right);
  }

  @Rule("statement -> $func.decl")
  public Statement statementFuncDecl(Statement funcdecl) {
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

  @Rule("statement -> while $E { ($statement : First)* }")
  public Statement statement(Token whil, Expression cond, Token open, List<Statement> statements, Token close) {
    return new WhileStatement(whil, cond, statements, close);
  }

  @Rule("statement -> for id in $E { ($statement : First)* }")
  public Statement statement(
      Token fo,
      Token id,
      Token in,
      Expression expr,
      Token open, List<Statement> statements, Token close) {
    return new ForStatement(fo, id, expr, statements, close);
  }

  @Rule("statement -> if $E { ($statement : First)* } (else ($statement | { ($statement : First)* } ) : Else)?")
  public Statement statement(Token iff, Expression cond, Token open, List<Statement> statements, Token close, Optional<Function<IfStatement, IfStatement>> elseTransform) {
    return elseTransform.map(transform -> transform.apply(new IfStatement(iff, cond, statements))).orElse(new IfStatement(iff, cond, statements, close));
  }

  @Resolve(
      // TODO: make this less eyebleedy
      variable = "statement@($statement|{.$statement@($statement@($statement)@group)@star.})@group",
      term = "{")
  public List<Atom> resolveElse(List<Token> rest, Set<List<Atom>> candidates) {
    // Always shift to the `else { statements }` case if rest starts with {
    boolean brack = rest.get(0).text.equals("{");
    for (List<Atom> candidate : candidates) {
      if (candidate.size() != 1 && brack) return candidate;
      if (candidate.size() == 1 && !brack) return candidate;
    }
    throw new IllegalStateException();
  }

  @SuppressWarnings("unchecked")
  @Rule("statement -> class id { ($var.decl ; | $func.decl)* }")
  public Statement statement(Token clazz, Token name, Token open, List<List<?>> declarations, Token close) {
    List<VariableDeclaration> variableDeclarations = new ArrayList<>();
    List<FunctionDeclaration> methodDeclarations = new ArrayList<>();
    for (List<?> production : declarations) {
      if (production.size() == 1) {
        methodDeclarations.add((FunctionDeclaration) production.get(0));
      } else {
        Function<Token, VariableDeclaration> varDecl = (Function<Token, VariableDeclaration>) production.get(0);
        Token semicolon = (Token) production.get(1);
        variableDeclarations.add(varDecl.apply(semicolon));
      }
    }
    return new ClassStatement(clazz, name, variableDeclarations, methodDeclarations, close);
  }

  @Rule("statement -> ;")
  public Statement statement(Token semicolon) {
    return new EmptyStatement(semicolon);
  }

  @Rule("statement -> return $E? ;")
  public Statement statement(Token ret, Optional<Expression> expression, Token semicolon) {
    return new ReturnStatement(ret, expression.orElse(new Literal(new Token("nil", -1, -1, "nil"))), semicolon);
  }

  @SuppressWarnings("unchecked")
  @For("Else")
  public Function<IfStatement, IfStatement> elseClause(Token el, List<?> statementish) {
    final int endLine;
    final int endColumn;
    final List<Statement> statements;
    if (statementish.size() == 1) {
      Statement statement = (Statement) statementish.get(0);
      statements = Lists.newArrayList(statement);
      endLine = statement.getEndLine();
      endColumn = statement.getEndColumn();
    } else if (statementish.size() == 3){
      statements = (List<Statement>) statementish.get(1);
      Token close = (Token) statementish.get(2);
      endLine = close.endLine;
      endColumn = close.endColumn;
    } else {
      throw new IllegalStateException();
    }
    return ifStatement -> {
      ifStatement.setEndColumn(endColumn);
      ifStatement.setEndLine(endLine);
      ifStatement.addElse(statements);
      return ifStatement;
    };
  }

  @Start
  @Rule("root -> $statement*")
  public List<Statement> statements(List<Statement> statements) {
    return statements;
  }

  @For("Cat")
  public <U> List<U> concat(U head, List<U> tail) {
    List<U> result = new ArrayList<>();
    result.add(head);
    result.addAll(tail);
    return result;
  }

  @For("SingleToken")
  public Token singleToken(Token token) {
    return token;
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
    if (expected.toString().equals(";"))
      throw new IllegalStateException("Missing a semicolon.");
    return super.handleConsumptionError(state, next, stream, currentSentence, expected);
  }

  @Argument
  private String file;

  public void driver(String[] args) throws IOException {


    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.err.println();
      return;
    }

    try (FileReader reader = new FileReader(file)) {
      System.out.println(parseYuckCode(reader));
    }
  }

  public static void main(String[] args) throws IOException {
    YuckyGrammar grammar = new YuckyGrammar();
    grammar.driver(args);
  }

  public List<Statement> parseYuckCode(Reader reader) throws IOException {
    YuckyLexer lexer = new YuckyLexer(reader);
    List<Token> tokens = new ArrayList<>();
    Token token = lexer.yylex();
    while (token != null) {
      tokens.add(token);
      token = lexer.yylex();
    }
    return parse(tokens);
  }

  public List<Statement> parseYuckCode(String code) throws IOException {
    StringReader reader = new StringReader(code);
    return parseYuckCode(reader);
  }
}
