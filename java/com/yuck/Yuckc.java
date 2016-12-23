package com.yuck;

import com.yuck.ast.Statement;
import com.yuck.grammar.YuckyGrammar;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
      String name = Paths.get(yuckFile).getFileName().toString();
      YCodeCompilationContext context = new YCodeCompilationContext(
          statements,
          String.format("@(%s)", name),
          new ArrayList<>());
      YCodeFunction function = context.compile();
      int i = 0;
      for (Instruction instruction : function.instructions) {
        System.out.printf("%d:\t%s\n", i++, instruction);
      }
    }
  }
}
