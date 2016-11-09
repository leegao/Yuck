package com.yuck;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.ArrayList;
import java.util.List;

public class Yuckc {
  @Argument
  private List<String> arguments = new ArrayList<>();

  public static void main(String[] args) {
    new Yuckc().driver(args);
  }

  private void driver(String[] args) {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.err.println();
      return;
    }

    System.out.println(arguments);
  }
}
