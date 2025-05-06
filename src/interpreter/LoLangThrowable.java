package interpreter;

import lexer.Token;

public class LoLangThrowable extends Error {
  public static class Return extends LoLangThrowable {
    LoLangValue value;

    public Return(LoLangValue value) {
      super();
      this.value = value;
    }

    public Return() {
      this.value = null;
    }
  }

  public static class Error extends LoLangThrowable {
    public final LoLangValue.String message;

    public Error(LoLangValue.String message) {
      super();
      this.message = message;
    }
  }

  public static class SwitchBreak extends LoLangThrowable {
    public SwitchBreak() {
      super();
    }
  }

  public static class SwitchGoto extends LoLangThrowable {
    public final LoLangValue label;
    public final Token source;

    public SwitchGoto(LoLangValue label, Token source) {
      super();
      this.label = label;
      this.source = source;
    }
  }

  public static class LoopBreak extends LoLangThrowable {
    public LoopBreak() {
      super();
    }
  }

  public static class LoopContinue extends LoLangThrowable {
    public LoopContinue() {
      super();
    }
  }

}
