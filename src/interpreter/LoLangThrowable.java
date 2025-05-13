package interpreter;

import lexer.Token;

public abstract class LoLangThrowable extends Error {
  public abstract RuntimeError toRuntimeError();

  public static class Return extends LoLangThrowable {
    LoLangValue value;
    public final Token token;

    public Return(LoLangValue value, Token token) {
      super();
      this.value = value;
      this.token = token;
    }

    public Return(Token token) {
      this.token = token;
      this.value = null;
    }

    public RuntimeError toRuntimeError() {
      return new RuntimeError("Return statement is used outside of function body", token);
    }
  }

  public static class Error extends LoLangThrowable {
    public final Token token;

    public Error(Token token) {
      super();
      this.token = token;
    }

    public RuntimeError toRuntimeError() {
      return new RuntimeError(this.token.lexeme, this.token);
    }
  }

  public static class SwitchBreak extends LoLangThrowable {
    public final Token token;

    public SwitchBreak(Token token) {
      super();
      this.token = token;
    }

    public RuntimeError toRuntimeError() {
      return new RuntimeError("Switch break statement is used outside of state body", token);
    }
  }

  public static class SwitchGoto extends LoLangThrowable {
    public final LoLangValue label;
    public final Token source;
    public final Token gotoToken;

    public SwitchGoto(LoLangValue label, Token source, Token gotoToken) {
      super();
      this.label = label;
      this.source = source;
      this.gotoToken = gotoToken;
    }

    public RuntimeError toRuntimeError() {
      return new RuntimeError("Switch goto statement is used outside of state body", gotoToken);
    }
  }

  public static class LoopBreak extends LoLangThrowable {
    public final Token token;

    public LoopBreak(Token token) {
      super();
      this.token = token;
    }

    public RuntimeError toRuntimeError() {
      return new RuntimeError("Loop break statement is used outside of state body", token);
    }
  }

  public static class LoopContinue extends LoLangThrowable {
    public final Token token;

    public LoopContinue(Token token) {
      super();
      this.token = token;
    }

    public RuntimeError toRuntimeError() {
      return new RuntimeError("Loop break statement is used outside of state body", token);
    }
  }
}
