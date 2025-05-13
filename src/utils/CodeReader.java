package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import interpreter.ExecutionContext;
import interpreter.Global;
import interpreter.LoLangThrowable;
import interpreter.RuntimeError;
import parser.ParserResult;
import parser.StatementNode;
import semantic.SemanticContext;
import parser.LR1Parser;
import parser.Node;

public class CodeReader {
  LR1Parser parser;
  static boolean symbolTableEnabled = false,
      parseTreeEnabled = false,
      tokenEnabled = false;

  public CodeReader(LR1Parser parser) {
    this.parser = parser;
  }

  public void run(boolean interactive) {
    while (true) {
      ArrayList<File> files = getFilesRoot();
      File examplesRoot = getExamplesRoot();

      for (int i = 0; i < files.size(); i++)
        System.out.println(String.format("[%d]: %s", i + 1,
            files.get(i).getAbsolutePath().replaceAll(examplesRoot.getAbsolutePath() + "/", "")));

      System.out.println(String.format("[%d]: %s", files.size() + 1, "Exit"));
      printLongLine();
      System.out.print("Enter the file number to read: ");

      int fileNumber = InputScanner.globalScanner.nextInt();
      InputScanner.globalScanner.nextLine(); // consume the newline

      int index = fileNumber - 1;
      if (index == files.size()) {
        System.out.println("Exiting...");
        break;
      } else if (index < 0 || index >= files.size()) {
        System.out.println("Invalid file number, restarting...");
        continue;
      }

      File file = files.get(fileNumber - 1);
      System.out.println("Scanning " + file.getName());

      try {
        parseFile(file);
        if (interactive == false)
          System.exit(0);

        printLongLine();
        System.out.println("Scanning complete! Enter any key to continue...");
        InputScanner.globalScanner.nextLine(); // consume the newline
      } catch (IOException e) {
        System.out.println("Error has occured while scanning file: " + e.getMessage());
      }
    }
  }

  static FilenameFilter filter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.endsWith(".lol");
    }
  };

  static FilenameFilter getDirs = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return dir.isDirectory();
    }
  };

  public static ArrayList<File> recurse(File directory) {
    // get the list of files containing the .lol extension, trying in the current
    // directory first, then in the parent directory
    File[] files = directory.listFiles(filter);
    ArrayList<File> filesList = new ArrayList<>();
    if (files != null)
      for (File file : files)
        filesList.add(file);

    File[] subDirs = directory.listFiles(getDirs);
    if (subDirs != null) {
      for (File subDir : subDirs) {
        filesList.addAll(recurse(subDir));
      }
    }

    return filesList;
  }

  public static File getExamplesRoot() {
    File directory = new File("./ExampleCodes");
    if (directory == null || directory.exists() == false)
      directory = new File("../ExampleCodes");

    return directory;
  }

  public static ArrayList<File> getFilesRoot() {
    return recurse(getExamplesRoot());
  }

  public void parseFile(File file) throws IOException {
    Scanner scanner = new Scanner(file, "UTF-8");

    String source = scanner.hasNext() ? scanner.useDelimiter("\\Z").next() + "\n" : "\n";
    scanner.close();
    ParserResult parsingResult = parser.parse(source, file.getPath());
    if (tokenEnabled) {
      parser.printRegexTokens(source, file.getPath());
      printLongLine();
    }
    if (parsingResult.errors.size() != 0) {
      System.out.println("The following errors were encountered during parsing:\n");
      ErrorWindowBuilder.printErrors(parsingResult.errors);
    }

    if (parsingResult.root == null) {
      System.out.println("Was unable to create a parse tree");
      return;
    }
    if (parseTreeEnabled) {
      System.out.println(parsingResult.root);
    }
    printRoot(parsingResult.root);

    if (parsingResult.errors.size() != 0) {
      System.out.println("Errors were encountered during parsing. Continue?");
      System.out.print("yes/no: ");
      String response = InputScanner.globalScanner.nextLine();
      if (response.equals("yes") == false)
        return;
    }

    if (parsingResult.root != null && (parsingResult.root instanceof StatementNode.Program)) {
      StatementNode.Program program = (StatementNode.Program) parsingResult.root;
      printLongLine();

      System.out.println("Performing semantic analysis...\n");

      SemanticContext context = Global.createGlobalSemanticContext();
      program.semanticAnalysis(context);

      // Print symbol table recursively
      if (symbolTableEnabled) {
        context.printSymbolTableToChild();
      }
      if (context.exceptions.size() != 0) {
        System.out.println("The following errors were encountered during semantic analysis:\n");
        ErrorWindowBuilder.printErrors(context.exceptions);
      }

      System.out.println("Semantic analysis complete!");

      // Begin executing the program
      printLongLine();

      if (context.exceptions.size() != 0) {
        System.out.println("Errors were encountered during semantic analysis. Continue to runtime?");
        System.out.print("yes/no: ");
        String response = InputScanner.globalScanner.nextLine();
        if (response.equals("yes") == false)
          return;
      }

      try {
        ExecutionContext global = Global.createGlobalExecutionContext();
        program.execute(global, global);
      } catch (RuntimeError e) {
        ErrorWindowBuilder.printErrors(e);
      } catch (LoLangThrowable e) {
        ErrorWindowBuilder.printErrors(e.toRuntimeError());
      }
    }
  }

  public static void settings(boolean symbolTable, boolean parseTree, boolean tokens) {
    symbolTableEnabled = symbolTable;
    parseTreeEnabled = parseTree;
    tokenEnabled = tokens;

  }

  static void printRoot(Node node) throws IOException {
    String output = DOTGenerator.generate(node);
    FileWriter writer = new FileWriter("output.dot");

    try {
      writer.write(output);
      System.out.println("DOT file successfully generated to output.dot");
    } catch (IOException e) {
      System.out.println("Error has occured while writing file: " + e.getMessage());
    }

    writer.close();
  }

  static void printLongLine() {
    System.out
        .println("==============================================================================================");
  }
}