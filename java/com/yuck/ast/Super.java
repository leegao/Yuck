package com.yuck.ast;

import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.grammar.Token;
import com.yuck.ycode.Opcode;
import com.yuck.ycode.YCodeFunction;

public class Super extends Expression {
  public final String id;

  public Super(Token thisToken, Token id) {
    super(thisToken.startLine, thisToken.startColumn, id.endLine, id.endColumn);
    this.id = id.text;
  }

  @Override
  public YCodeFunction compile(YCodeFunction function, YCodeCompilationContext context) {
    return function.emit(Opcode.SUPER, id);
  }
}
