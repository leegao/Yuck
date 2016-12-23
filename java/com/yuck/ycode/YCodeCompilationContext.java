package com.yuck.ycode;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.yuck.ast.Statement;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YCodeCompilationContext {
  private final List<Statement> statements;
  public final String name;
  private final List<String> parameters;

  public YCodeCompilationContext(List<Statement> statements, String name, List<String> parameters) {
    this.statements = statements;
    this.name = name;
    this.parameters = parameters;
  }

  public class Scope implements Closeable {
    BiMap<String, String> locals = HashBiMap.create();
    private Scope() {}

    @Override
    public void close() {
      Preconditions.checkArgument(scopes.get(scopes.size() - 1) == this);
      scopes.remove(scopes.size() - 1);
    }

    public String addLocal(String var) {
      Preconditions.checkArgument(!locals.containsKey(var), String.format("The variables %s is already declared in this scope.", var));
      String variable = YCodeCompilationContext.this.lookup(var).map(s -> s + "'").orElse(var);
      locals.put(var, variable);
      return variable;
    }

    public Optional<String> lookup(String var) {
      return Optional.ofNullable(locals.get(var));
    }
  }

  List<Scope> scopes = new ArrayList<>();

  public Scope push() {
    Scope scope = new Scope();
    scopes.add(scope);
    return scope;
  }

  public Scope getScope() {
    return scopes.get(scopes.size() - 1);
  }

  public Optional<String> lookup(String var) {
    for (Scope scope : Lists.reverse(scopes)) {
      Optional<String> result = scope.lookup(var);
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }

  public YCodeFunction compile() {
    YCodeFunction function = new YCodeFunction(parameters, name);
    try (Scope topLevel = push()) {
      parameters.forEach(topLevel::addLocal);
      statements.forEach(statement -> statement.compile(function, this));
    }
    function.assemble();
    return function;
  }
}
