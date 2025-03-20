import java.util.ArrayList;

public class SLR1GrammarParser {
  static enum SLR1GrammarTokenType {
    VARIABLE, TERMINAL, COLON, SEMICOLON, EOF
  }

  static class SLR1GrammarToken {
    SLR1GrammarTokenType type;
    String lexeme = "";

    SLR1GrammarToken(SLR1GrammarTokenType type, String lexeme) {
      this.type = type;
      this.lexeme = lexeme;
    }

    SLR1GrammarToken(SLR1GrammarTokenType type) {
      this.type = type;
      this.lexeme = "";
    }
  }

  static class SLR1GrammarLexer {
    String input;
    int currentCharacterIndex = 0;
    ArrayList<SLR1GrammarToken> tokens = new ArrayList<>();

    SLR1GrammarLexer(String input) {
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
      this.tokens.add(new SLR1GrammarToken(SLR1GrammarTokenType.VARIABLE, lexeme));
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
      this.tokens.add(new SLR1GrammarToken(SLR1GrammarTokenType.VARIABLE, lexeme));
    }

    ArrayList<SLR1GrammarToken> lex() {
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
            this.tokens.add(new SLR1GrammarToken(SLR1GrammarTokenType.COLON));
            currentCharacterIndex++;
            break switch_case;
          case ';':
            this.tokens.add(new SLR1GrammarToken(SLR1GrammarTokenType.SEMICOLON));
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

  SLR1GrammarParser(String input) {
    this.input = input;
  }

  static class SLR1GrammarProduction {
    static enum SLR1GrammarProductionRHSSymbolType {
      TERMINAL, VARIABLE
    }

    static class SLR1GrammarProductionRHSSymbol {
      SLR1GrammarProductionRHSSymbolType type;
      String lexeme;

      SLR1GrammarProductionRHSSymbol(SLR1GrammarProductionRHSSymbolType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
      }
    }

    String lhs;
    ArrayList<SLR1GrammarProductionRHSSymbol> rhs;

    SLR1GrammarProduction(String lhs, ArrayList<SLR1GrammarProductionRHSSymbol> rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  int currentTokenIndex = 0;
  ArrayList<SLR1GrammarToken> tokens;
  ArrayList<SLR1GrammarProduction> productions = new ArrayList<>();

  void expectToken(SLR1GrammarTokenType type) {
    SLR1GrammarToken token = tokens.get(currentTokenIndex);
    if (token.type != type) {
      throw new RuntimeException("Unexpected token: " + token.type);
    }
    currentTokenIndex++;
  }

  void parseProduction() {
    SLR1GrammarToken lhs = tokens.get(currentTokenIndex);
    currentTokenIndex++;
    expectToken(SLR1GrammarTokenType.COLON);

    ArrayList<SLR1GrammarProduction.SLR1GrammarProductionRHSSymbol> symbols = new ArrayList<SLR1GrammarProduction.SLR1GrammarProductionRHSSymbol>();

    SLR1GrammarToken currentToken = tokens.get(currentTokenIndex);
    while (currentToken.type != SLR1GrammarTokenType.SEMICOLON) {
      if (currentToken.type == SLR1GrammarTokenType.TERMINAL) {
        symbols.add(new SLR1GrammarProduction.SLR1GrammarProductionRHSSymbol(
            SLR1GrammarProduction.SLR1GrammarProductionRHSSymbolType.TERMINAL, currentToken.lexeme));
      } else if (currentToken.type == SLR1GrammarTokenType.VARIABLE) {
        symbols.add(new SLR1GrammarProduction.SLR1GrammarProductionRHSSymbol(
            SLR1GrammarProduction.SLR1GrammarProductionRHSSymbolType.VARIABLE, currentToken.lexeme));
      }

      currentTokenIndex++;
      currentToken = tokens.get(currentTokenIndex);
    }

    expectToken(SLR1GrammarTokenType.SEMICOLON);
    productions.add(new SLR1GrammarProduction(lhs.lexeme, symbols));
  }

  /**
   * Generates the list of productions
   */
  ArrayList<SLR1GrammarProduction> parse() {
    SLR1GrammarLexer lexer = new SLR1GrammarLexer(input);
    this.tokens = lexer.lex();

    while (currentTokenIndex < tokens.size())
      this.parseProduction();

    return this.productions;
  }
}
