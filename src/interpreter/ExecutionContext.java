package interpreter;

import java.util.ArrayList;

import lexer.Token;
import utils.Environment;
import utils.FileLoader;

public class ExecutionContext {
  public static class CallStackEntry {
    Token functionCallToken;

    public CallStackEntry(Token functionCallToken) {
      this.functionCallToken = functionCallToken;
    }
  }

  public Environment<LoLangValue> environment = new Environment<LoLangValue>();
  public ArrayList<CallStackEntry> callStack = new ArrayList<>();

  public ExecutionContext() {
  }

  private ExecutionContext(Environment<LoLangValue> parentEnvironment, ArrayList<CallStackEntry> parentCallStack) {
    this.environment = new Environment<LoLangValue>(parentEnvironment);
    this.callStack = parentCallStack;
  }

  @SuppressWarnings("unchecked")
  public ExecutionContext fork(CallStackEntry callStackEntry) {
    ArrayList<CallStackEntry> newCallStack = (ArrayList<CallStackEntry>) this.callStack.clone();
    newCallStack.add(callStackEntry);
    ExecutionContext newContext = new ExecutionContext(environment, newCallStack);
    return newContext;
  }

  public ExecutionContext fork() {
    ExecutionContext newContext = new ExecutionContext(environment, this.callStack);
    return newContext;
  }

  public void printCallStack() {
    System.out.println("Call stack:");

    for (CallStackEntry entry : this.callStack) {
      // Point to the area of the file that caused the error
      String[] lines = FileLoader.loadFile(entry.functionCallToken.sourcePath).split("\n");

      System.out.println(String.format("%s:%d:%d - %s", entry.functionCallToken.sourcePath,
          entry.functionCallToken.line, entry.functionCallToken.column, lines[entry.functionCallToken.line - 1]));
    }

    System.out.println();
  }
}
