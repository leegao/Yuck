package com.yuck;

import com.yuck.ast.Statement;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.grammar.YuckyGrammar;
import com.yuck.ycode.Instruction;
import com.yuck.ycode.YCodeFunction;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Yuckc {
  @Option(name = "--human-readable", aliases = {"-H"})
  private boolean human;

  @Option(name = "--output", aliases = {"-o"}, forbids = {"--human-readable"})
  private File output;

  @Argument(required = true)
  private String yuckFile;

  public static void main(String[] args) throws IOException {
    new Yuckc().driver(args);
  }

  private void dumpHuman(YCodeFunction function, String level) {
    System.out.printf("%sFunction %s\n", level, function.name);
    {
      int i = 0;
      for (Instruction instruction : function.instructions) {
        System.out.printf("%s%d:\t%s\n", level, i++, instruction);
      }
    }
    System.out.println();

    for (int i = 0; i < function.functions.size(); i++) {
      dumpHuman(function.functions.inverse().get(i), level + "  ");
    }
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
      if (human) {
        dumpHuman(function, "");
      } else {
        if (output == null) {
          output = new File(yuckFile.replaceFirst("\\.yuck$", ".yc"));
          if (output.toString().equals(yuckFile)) {
            throw new IllegalStateException(String.format("%s does not end in a .yuck extension. Please specify a --output file.", yuckFile));
          }
        }
        try (DataOutputStream writer = new DataOutputStream(new FileOutputStream(output))) {
          function.write(writer);
          writer.flush();
        }
      }
    }
  }
}
