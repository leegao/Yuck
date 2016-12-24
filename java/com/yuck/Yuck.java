package com.yuck;

import com.yuck.ast.Statement;
import com.yuck.compilation.YCodeCompilationContext;
import com.yuck.grammar.YuckyGrammar;
import com.yuck.interpreter.Interpreter;
import com.yuck.interpreter.InterpreterContext;
import com.yuck.ycode.YCodeFunction;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Yuck {
  @Argument(required = true)
  private String ycode;

  public static void main(String[] args) throws IOException {
    new Yuck().driver(args);
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

    if (ycode.endsWith(".yuck")) {
      try (FileReader reader = new FileReader(ycode)) {
        YuckyGrammar grammar = new YuckyGrammar();
        List<Statement> statements = grammar.parseYuckCode(reader);
        String name = Paths.get(ycode).getFileName().toString();
        YCodeCompilationContext context = new YCodeCompilationContext(
            statements,
            String.format("@(%s)", name),
            new ArrayList<>());
        YCodeFunction function = context.compile();
        File output = new File(ycode.replaceFirst("\\.yuck$", ".yc"));
        try (DataOutputStream writer = new DataOutputStream(new FileOutputStream(output))) {
          function.write(writer);
          writer.flush();
        }
        ycode = output.toString();
      }
    }

    try (DataInputStream reader = new DataInputStream(new FileInputStream(ycode))) {
      YCodeFunction function = YCodeFunction.read(reader);
      Interpreter interpreter = new Interpreter();
      InterpreterContext context = interpreter.interpret(function, new InterpreterContext());
      System.out.println(context.pop());
    }
  }
}
