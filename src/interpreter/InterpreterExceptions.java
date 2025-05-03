package interpreter;

public class InterpreterExceptions {
  public static class FunctionCallArityException extends Exception {
    public FunctionCallArityException() {
    }
  }

  public static class DotAccessNonExistentException extends Exception {
    public DotAccessNonExistentException() {
    }
  }

  public static class IndexAccessOutOfBoundsException extends Exception {
    public IndexAccessOutOfBoundsException() {
    }
  }
}
