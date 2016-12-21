package com.yuck.ast;

import com.yuck.grammar.Token;

import java.util.Optional;

public class VariableDeclaration extends Statement {
  public final String id;
  public final Optional<Expression> init;

  public VariableDeclaration(Token var, Token id, Optional<Expression> init, Token semi) {
    super(var.startLine, var.startColumn, semi.endLine, semi.endColumn);
    this.id = id.text;
    this.init = init;
  }
}
