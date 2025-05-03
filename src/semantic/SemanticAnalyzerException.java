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
}
