package interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import semantic.LoLangType;
import semantic.SemanticContext;

public class Global {
  static public ExecutionContext createGlobalExecutionContext() {
    ExecutionContext context = new ExecutionContext();

    context.environment.define("broadcast", new LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
      System.out.println(arguments[0].toString());
      return new LoLangValue.Null();
    }, 1), false);

    context.environment.define("chat", new LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
      Scanner scanner = new Scanner(System.in);
      System.out.print(((LoLangValue.String) arguments[0]).value);
      String input = scanner.nextLine();
      scanner.close();

      return new LoLangValue.String(input);
    }, 1), false);

    return context;
  };

  static public SemanticContext createGlobalSemanticContext() {
    SemanticContext context = new SemanticContext();

    context.typeEnvironment.define("message", new LoLangType.String(), true);
    context.typeEnvironment.define("stats", new LoLangType.Number(), true);
    context.typeEnvironment.define("goat", new LoLangType.Boolean(), true);
    context.typeEnvironment.define("cooldown", new LoLangType.Null(), true);
    context.typeEnvironment.define("passive", new LoLangType.Void(), true);

    LoLangType.Lambda broadcastType = new LoLangType.Lambda(new LoLangType.Void(),
        new ArrayList<LoLangType>(Arrays.asList(new LoLangType[] { new LoLangType.String() })));
    context.variableEnvironment.define("broadcast", broadcastType, true);

    LoLangType.Lambda chatType = new LoLangType.Lambda(new LoLangType.String(),
        new ArrayList<LoLangType>(Arrays.asList(new LoLangType[] { new LoLangType.String() })));
    context.variableEnvironment.define("chat", chatType, true);

    return context;
  }
}
