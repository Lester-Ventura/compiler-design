package interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import semantic.LoLangType;
import semantic.SemanticContext;
import utils.InputScanner;

public class Global {
  static public ExecutionContext createGlobalExecutionContext() {
    ExecutionContext context = new ExecutionContext();

    context.environment.define("broadcast", new LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
      System.out.println(arguments[0].toString());
      return new LoLangValue.Null();
    }, 1), true);

    context.environment.define("chat", new LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
      System.out.print(((LoLangValue.String) arguments[0]).value);
      String input = InputScanner.globalScanner.nextLine();

      return new LoLangValue.String(input);
    }, 1), true);

    context.environment.define("ff", new LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
      LoLangValue.Number number = (LoLangValue.Number) arguments[0];
      System.exit(number.value == 15 ? (int) 0 : (int) Math.floor(number.value));
      return new LoLangValue.Null();
    }, 1), true);

    // context.environment.define("testing", new
    // LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
    // LoLangValue.Number number = (LoLangValue.Number) arguments[0];
    // LoLangValue.String string = (LoLangValue.String) arguments[1];
    // System.out.println(number.value);
    // System.out.println(string.value);
    // return new LoLangValue.Null();
    // }, 2), true);

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
        createParameters(new LoLangType[] { new LoLangType.Any() }));
    context.variableEnvironment.define("broadcast", broadcastType, true);

    LoLangType.Lambda chatType = new LoLangType.Lambda(new LoLangType.String(),
        createParameters(new LoLangType[] { new LoLangType.String() }));
    context.variableEnvironment.define("chat", chatType, true);

    LoLangType.Lambda ffType = new LoLangType.Lambda(new LoLangType.Void(),
        createParameters(new LoLangType[] { new LoLangType.Number() }));
    context.variableEnvironment.define("ff", ffType, true);

    // LoLangType.Lambda testingType = new LoLangType.Lambda(new LoLangType.Void(),
    // createParameters(new LoLangType[] { new LoLangType.Number(), new
    // LoLangType.String() }));
    // context.variableEnvironment.define("testing", testingType, true);

    return context;
  }

  public static ArrayList<LoLangType> createParameters(LoLangType[] parameterTypes) {
    ArrayList<LoLangType> returned = new ArrayList<>(Arrays.asList(parameterTypes));
    Collections.reverse(returned);
    return returned;
  }
}
