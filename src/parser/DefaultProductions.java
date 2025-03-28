package parser;

import java.util.HashMap;

import lexer.TokenType;

public class DefaultProductions {
  static interface DefaultProduction {
    Node run();
  }

  public static HashMap<String, DefaultProduction> createDefaultProductions() {
    HashMap<String, DefaultProduction> defaultProductions = new HashMap<>();

    defaultProductions.put("STATEMENT", () -> new StatementNode.Block());
    defaultProductions.put("EXPRESSION", () -> new ExpressionNode.Literal("0", TokenType.NUMBER_LITERAL));

    return defaultProductions;
  }
}
