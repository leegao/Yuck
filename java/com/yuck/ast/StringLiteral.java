package com.yuck.ast;

import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;

public class StringLiteral extends Expression {
  public final String text;
  public StringLiteral(Token token) {
    super(token.startLine, token.startColumn, token.endLine, token.endColumn);
    text = token.text;
  }

  @Override
  public YCodeFunctionContext compile(YCodeFunctionContext function, YCodeCompilationContext scope) {
    return function.emit(Opcode.LOAD_CONST, text);
  }
}
