package parser;

import java.util.HashMap;

import lexer.ColumnAndRow;
import lexer.Token;
import lexer.TokenType;

public class DefaultProductions {
  static interface DefaultProduction {
    Node run();
  }

  public static HashMap<String, DefaultProduction> createDefaultProductions() {
    HashMap<String, DefaultProduction> defaultProductions = new HashMap<>();

    defaultProductions.put("STATEMENT", () -> new StatementNode.Block());
    defaultProductions.put("EXPRESSION",
        () -> new ExpressionNode.Literal(new Token(TokenType.NUMBER_LITERAL, "0", new ColumnAndRow(0, 0))));

    return defaultProductions;
  }
}
