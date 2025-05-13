package interpreter;

import lexer.Token;
import utils.LoLangExceptionLike;

public class RuntimeError extends Error implements LoLangExceptionLike {
	Token token;

	public RuntimeError(String message, Token token) {
		super(message);
		this.token = token;
	}

	public Token getToken() {
		return this.token;
	}
}
