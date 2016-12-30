package com.yuck.ast;

import com.google.common.collect.ImmutableList;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeClass;
import com.yuck.ycode.YCodeFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class ClassStatement extends Statement {
  public final String name;
  public final ImmutableList<VariableDeclaration> fieldDeclarations;
  public final ImmutableList<FunctionDeclaration> methodDeclarations;

  public ClassStatement(
      Token clazz,
      Token name,
      List<VariableDeclaration> fieldDeclarations,
      List<FunctionDeclaration> methodDeclarations,
      Token close) {
    super(clazz.startLine, clazz.startColumn, close.endLine, close.endColumn);
    this.name = name.text;
    this.fieldDeclarations = ImmutableList.copyOf(fieldDeclarations);
    this.methodDeclarations = ImmutableList.copyOf(methodDeclarations);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    YCodeClass yClass = new YCodeClass(name);
    try (YCodeCompilationContext.Scope scope = context.push()) {
      for (FunctionDeclaration method : methodDeclarations) {
        yClass.addField(method.id);
        YCodeCompilationContext nestedContext = new YCodeCompilationContext(
            method.statements,
            context.name + "." + name + "." + method.id,
            method.parameters);
        YCodeFunction func = nestedContext.compile().emit(Opcode.NIL).emit(Opcode.RETURN);
        int functionId = function.function(func);
        yClass.addMethod(functionId, method.id);
      }
      for (VariableDeclaration field : fieldDeclarations) {
        yClass.addField(field.id);
      }
    }
    if (!yClass.methods.containsKey("init")) {
      throw new IllegalStateException("A class must have a constructor function init(...).");
    }
    function.emit(Opcode.CLASS, yClass);
    function.emit(Opcode.STORE_LOCAL, name);
    return function;
  }
}
