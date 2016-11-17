package com.yuck.grammar;

import com.yuck.auxiliary.descentparsing.GrammarBase;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class YuckyGrammar extends GrammarBase<Token> {
  @Override
  public String label(Token token) {
    throw new NotImplementedException();
  }
}
