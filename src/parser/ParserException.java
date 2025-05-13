package parser;

import lexer.Token;
import utils.LoLangExceptionLike;

public class ParserException extends Exception implements LoLangExceptionLike {
  public Token token;

  ParserException(String message, Token token) {
    super(message);
    this.token = token;
  }

  public Token getToken() {
    return this.token;
  }
}
