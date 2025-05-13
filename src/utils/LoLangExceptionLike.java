package utils;

import lexer.Token;

public interface LoLangExceptionLike {
  Token getToken();

  String getMessage();
}
