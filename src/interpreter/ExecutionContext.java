package interpreter;

public class ExecutionContext {
  public Environment environment = new Environment();

  public ExecutionContext(Environment parentEnvironment) {
    this.environment = new Environment(parentEnvironment);
  }

  public ExecutionContext() {
  }

  public ExecutionContext fork() {
    ExecutionContext newContext = new ExecutionContext(environment);
    return newContext;
  }
}
