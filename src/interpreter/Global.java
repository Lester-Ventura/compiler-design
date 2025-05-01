package interpreter;

public class Global {
  static public ExecutionContext createGlobal() {
    ExecutionContext context = new ExecutionContext();

    context.environment.define("broadcast", new LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
      System.out.println(arguments[0].toString());
      return new LoLangValue.Null();
    }, 1), false);

    return context;
  };
}
