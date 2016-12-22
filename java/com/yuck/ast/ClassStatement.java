package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.grammar.Token;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class ClassStatement extends Statement {
  public final String name;
  public final ImmutableList<VariableDeclaration> variableDeclarations;
  public final ImmutableList<FunctionDeclaration> methodDeclarations;

  public ClassStatement(
      Token clazz,
      Token name,
      List<VariableDeclaration> variableDeclarations,
      List<FunctionDeclaration> methodDeclarations,
      Token close) {
    super(clazz.startLine, clazz.startColumn, close.endLine, close.endColumn);
    this.name = name.text;
    this.variableDeclarations = ImmutableList.copyOf(variableDeclarations);
    this.methodDeclarations = ImmutableList.copyOf(methodDeclarations);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    throw new NotImplementedException();
  }
}
