package com.yuck.ast;

import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunction;

public class This extends Expression {
  public This(Token thisToken) {
    super(thisToken.startLine, thisToken.startColumn, thisToken.endLine, thisToken.endColumn);
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    return function.emit(Opcode.THIS);
  }
}
