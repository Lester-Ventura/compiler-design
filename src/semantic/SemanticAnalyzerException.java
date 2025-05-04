package semantic;

import lexer.Token;
import utils.LoLangExceptionLike;

public class SemanticAnalyzerException extends Exception implements LoLangExceptionLike {
  public Token token;

  public SemanticAnalyzerException(String message, Token token) {
    super(message);
    this.token = token;
  }

  public Token getToken() {
    return this.token;
  }

  public abstract static class GenericReturnTypeException extends Exception {
  }

  public static class GenericReturnTypeArityException extends GenericReturnTypeException {
    public final int expected;
    public final int received;

    public GenericReturnTypeArityException(int expected, int received) {
      super();
      this.expected = expected;
      this.received = received;
    }
  }

  public static class GenericReturnTypeParameterMismatchException extends GenericReturnTypeException {
    public final int index;
    public final LoLangType expected;
    public final LoLangType received;

    public GenericReturnTypeParameterMismatchException(int index, LoLangType expected, LoLangType received) {
      super();
      this.index = index;
      this.expected = expected;
      this.received = received;
    }
  }
}
