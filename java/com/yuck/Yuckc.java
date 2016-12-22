package com.yuck;

import com.yuck.ast.Statement;
import com.yuck.grammar.YuckyGrammar;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunctionContext;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Yuckc {
  @Argument
  private String yuckFile;

  public static void main(String[] args) throws IOException {
    new Yuckc().driver(args);
  }

  private void driver(String[] args) throws IOException {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.err.println();
      return;
    }

    try (FileReader reader = new FileReader(yuckFile)) {
      YuckyGrammar grammar = new YuckyGrammar();
      List<Statement> statements = grammar.parseYuckCode(reader);
      YCodeFunctionContext functionContext = new YCodeFunctionContext(new ArrayList<>());
      YCodeCompilationContext compilationContext = new YCodeCompilationContext();
      try (YCodeCompilationContext.Scope scope = compilationContext.push()) {
        statements.forEach(statement -> statement.compile(functionContext, compilationContext));
      }
      int i = 0;
      for (Instruction instruction : functionContext.assemble()) {
        System.out.printf("%d:\t%s\n", i++, instruction);
      }
    }
  }
}
