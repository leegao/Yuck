package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

import java.util.Optional;

public class VariableDeclaration extends Statement {
  public final String id;
  public final Optional<Expression> init;

  public VariableDeclaration(Token var, Token id, Optional<Expression> init, Token semi) {
    super(var.startLine, var.startColumn, semi.endLine, semi.endColumn);
    this.id = id.text;
    this.init = init;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    if (init.isPresent()) {
      init.get().compile(function, scope);
    } else {
      function.emit(Opcode.NIL);
    }
    return function.emit(Opcode.STORE_LOCAL, id);
  }
}
