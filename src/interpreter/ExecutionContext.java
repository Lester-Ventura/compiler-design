package interpreter;

public class ExecutionContext {
  public Environment<LoLangValue> environment = new Environment<LoLangValue>();

  public ExecutionContext(Environment<LoLangValue> parentEnvironment) {
    this.environment = new Environment<LoLangValue>(parentEnvironment);
  }

  public ExecutionContext() {
  }

  public ExecutionContext fork() {
    ExecutionContext newContext = new ExecutionContext(environment);
    return newContext;
  }
}
