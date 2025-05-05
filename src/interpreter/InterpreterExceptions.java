package interpreter;

import lexer.Token;
import semantic.LoLangType;

public abstract class InterpreterExceptions extends Exception {
  public abstract RuntimeError toRuntimeError(Token token);

  public static abstract class FunctionCallException extends InterpreterExceptions {

  }

  public static class FunctionCallArityException extends FunctionCallException {
    public final int expected;
    public final int received;

    public FunctionCallArityException(int expected, int received) {
      this.expected = expected;
      this.received = received;
    }

    public RuntimeError toRuntimeError(Token token) {
      return new RuntimeError("Incorrect number of arguments passed to function. Expected: "
          + this.expected + " but received: " + this.received, token);
    }
  }

  public static class FunctionCallArgumentMismatchException extends FunctionCallException {
    public final LoLangType expected;
    public final LoLangType received;
    public final int index;

    public FunctionCallArgumentMismatchException(int index, LoLangType expected, LoLangType received) {
      this.index = index;
      this.expected = expected;
      this.received = received;
    }

    public RuntimeError toRuntimeError(Token token) {
      return new RuntimeError(
          String.format("Incorrect parameter type passed to function at index %d. Expected: %s. Received: %s",
              this.index, this.expected.toString(), this.received.toString()),
          token);
    }
  }

  public static class FunctionCallReturnTypeMismatchException extends FunctionCallException {
    public final LoLangType expected;
    public final LoLangType received;

    public FunctionCallReturnTypeMismatchException(LoLangType expected, LoLangType received) {
      this.expected = expected;
      this.received = received;
    }

    public RuntimeError toRuntimeError(Token token) {
      return new RuntimeError(
          String.format("Incorrect return type. Expected: %s. Received: %s", this.expected.toString(),
              this.received.toString()),
          token);
    }
  }

  public static class DotAccessNonExistentException extends InterpreterExceptions {
    public final String identifier;

    public DotAccessNonExistentException(String identifier) {
      this.identifier = identifier;
    }

    public RuntimeError toRuntimeError(Token token) {
      return new RuntimeError("Failed to access property \"" + this.identifier + "\" on object", token);
    }
  }

  public static class IndexAccessOutOfBoundsException extends InterpreterExceptions {
    public final int index;

    public IndexAccessOutOfBoundsException(int index) {
      this.index = index;
    }

    public RuntimeError toRuntimeError(Token token) {
      return new RuntimeError(String.format("Index %d out of bounds", this.index), token);
    }
  }

  public static class RedeclaredVariableException extends InterpreterExceptions {
    public final String identifier;

    public RedeclaredVariableException(String identifier) {
      this.identifier = identifier;
    }

    public RuntimeError toRuntimeError(Token token) {
      return new RuntimeError("Cannot redeclare variable \"" + this.identifier + "\"", token);
    }
  }
}
