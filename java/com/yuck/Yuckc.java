package com.yuck;

import com.google.common.base.Preconditions;
import com.yuck.ast.Statement;
import com.yuck.grammar.YuckyGrammar;
import com.yuck.ycode.Instruction;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.ycode.YCodeFunction;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
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
        int i = 0;
        for (Instruction instruction : function.instructions) {
          System.out.printf("%d:\t%s\n", i++, instruction);
        }
      } else {
        if (output == null) {
          throw new NotImplementedException();
        }
        try (DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
          function.write(writer);
          writer.flush();
        }
      }
    }
  }
}
