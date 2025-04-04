package parser;

import java.util.ArrayList;

public class LR1GrammarParser {
  static enum LR1GrammarTokenType {
    VARIABLE, TERMINAL, COLON, SEMICOLON, EOF
  }

  static class LR1GrammarToken {
    LR1GrammarTokenType type;
    String lexeme = "";

    LR1GrammarToken(LR1GrammarTokenType type, String lexeme) {
      this.type = type;
      this.lexeme = lexeme;
    }

    LR1GrammarToken(LR1GrammarTokenType type) {
      this.type = type;
      this.lexeme = "";
    }
  }

  static class LR1GrammarLexer {
    String input;
    int currentCharacterIndex = 0;
    ArrayList<LR1GrammarToken> tokens = new ArrayList<>();

    LR1GrammarLexer(String input) {
      this.input = input;
    }

    void expect(char ch) {
      char currentCharacter = this.input.charAt(this.currentCharacterIndex);
      if (currentCharacter != ch) {
        throw new RuntimeException("Unexpected character: " + currentCharacter);
      } else
        currentCharacterIndex++;
    }

    void lexVariable() {
      currentCharacterIndex++; // consume the '<'
      String lexeme = "";

      while (this.currentCharacterIndex < this.input.length() && this.input.charAt(this.currentCharacterIndex) != '>') {
        char currentCharacter = this.input.charAt(this.currentCharacterIndex);
        lexeme += currentCharacter;
        this.currentCharacterIndex++;
      }

      expect('>');
      this.tokens.add(new LR1GrammarToken(LR1GrammarTokenType.VARIABLE, lexeme));
    }

    void lexTerminal() {
      currentCharacterIndex++; // consume the '<'
      String lexeme = "";

      while (this.currentCharacterIndex < this.input.length() && this.input.charAt(this.currentCharacterIndex) != ']') {
        char currentCharacter = this.input.charAt(this.currentCharacterIndex);
        lexeme += currentCharacter;
        this.currentCharacterIndex++;
      }

      expect(']');
      this.tokens.add(new LR1GrammarToken(LR1GrammarTokenType.VARIABLE, lexeme));
    }

    ArrayList<LR1GrammarToken> lex() {
      while (this.currentCharacterIndex < this.input.length()) {
        char currentCharacter = this.input.charAt(this.currentCharacterIndex);

        switch_case: switch (currentCharacter) {
          case '<':
            lexVariable();
            break switch_case;
          case '[':
            lexTerminal();
            break switch_case;
          case ':':
            this.tokens.add(new LR1GrammarToken(LR1GrammarTokenType.COLON));
            currentCharacterIndex++;
            break switch_case;
          case ';':
            this.tokens.add(new LR1GrammarToken(LR1GrammarTokenType.SEMICOLON));
            currentCharacterIndex++;
            break switch_case;
          case ' ':
          case '\r':
          case '\n':
            currentCharacterIndex++;
            break switch_case;
          default:
            throw new RuntimeException("Unexpected character: " + currentCharacter);
        }
      }

      return this.tokens;
    }
  }

  String input;

  public LR1GrammarParser(String input) {
    this.input = input;
  }

  public static class LR1GrammarProduction {
    static enum LR1GrammarProductionRHSSymbolType {
      TERMINAL, VARIABLE
    }

    static class LR1GrammarProductionRHSSymbol {
      LR1GrammarProductionRHSSymbolType type;
      String lexeme;

      LR1GrammarProductionRHSSymbol(LR1GrammarProductionRHSSymbolType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
      }
    }

    String lhs;
    ArrayList<LR1GrammarProductionRHSSymbol> rhs;

    LR1GrammarProduction(String lhs, ArrayList<LR1GrammarProductionRHSSymbol> rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  int currentTokenIndex = 0;
  ArrayList<LR1GrammarToken> tokens;
  ArrayList<LR1GrammarProduction> productions = new ArrayList<>();

  void expectToken(LR1GrammarTokenType type) {
    LR1GrammarToken token = tokens.get(currentTokenIndex);
    if (token.type != type) {
      throw new RuntimeException("Unexpected token: " + token.type);
    }
    currentTokenIndex++;
  }

  void parseProduction() {
    LR1GrammarToken lhs = tokens.get(currentTokenIndex);
    currentTokenIndex++;
    expectToken(LR1GrammarTokenType.COLON);

    ArrayList<LR1GrammarProduction.LR1GrammarProductionRHSSymbol> symbols = new ArrayList<LR1GrammarProduction.LR1GrammarProductionRHSSymbol>();

    LR1GrammarToken currentToken = tokens.get(currentTokenIndex);
    while (currentToken.type != LR1GrammarTokenType.SEMICOLON) {
      if (currentToken.type == LR1GrammarTokenType.TERMINAL) {
        symbols.add(new LR1GrammarProduction.LR1GrammarProductionRHSSymbol(
            LR1GrammarProduction.LR1GrammarProductionRHSSymbolType.TERMINAL, currentToken.lexeme));
      } else if (currentToken.type == LR1GrammarTokenType.VARIABLE) {
        symbols.add(new LR1GrammarProduction.LR1GrammarProductionRHSSymbol(
            LR1GrammarProduction.LR1GrammarProductionRHSSymbolType.VARIABLE, currentToken.lexeme));
      }

      currentTokenIndex++;
      currentToken = tokens.get(currentTokenIndex);
    }

    expectToken(LR1GrammarTokenType.SEMICOLON);
    productions.add(new LR1GrammarProduction(lhs.lexeme, symbols));
  }

  /**
   * Generates the list of productions
   */
  public ArrayList<LR1GrammarProduction> parse() {
    LR1GrammarLexer lexer = new LR1GrammarLexer(input);
    this.tokens = lexer.lex();

    while (currentTokenIndex < tokens.size())
      this.parseProduction();

    return this.productions;
  }
}
