package parser;

import java.util.ArrayList;
import java.util.HashMap;

public class LR1TableParser {
  static enum LR1TableTokenType {
    VARIABLE, TERMINAL, EQUAL, PROCESS, COMMA, NEWLINE
  }

  static class LR1TableToken {
    LR1TableTokenType type;
    String lexeme = "";

    LR1TableToken(LR1TableTokenType type, String lexeme) {
      this.lexeme = lexeme;
      this.type = type;
    }

    LR1TableToken(LR1TableTokenType type) {
      this.lexeme = "";
      this.type = type;
    }

    public String toString() {
      return this.type + " " + this.lexeme;
    }
  }

  static class LR1TableLexer {
    String input;
    int currentCharacterIndex = 0;
    ArrayList<LR1TableToken> tokens = new ArrayList<LR1TableToken>();

    LR1TableLexer(String input) {
      this.input = input;
    }

    void expectCharacter(char ch) {
      char currentCharacter = input.charAt(currentCharacterIndex);
      if (currentCharacter != ch) {
        throw new RuntimeException("Unexpected character: " + currentCharacter);
      } else
        currentCharacterIndex++;
    }

    void lexTerminal() {
      currentCharacterIndex++; // consume the [
      String lexeme = "";
      while (currentCharacterIndex < input.length() && input.charAt(currentCharacterIndex) != ']')
        lexeme += input.charAt(currentCharacterIndex++);

      expectCharacter(']');
      tokens.add(new LR1TableToken(LR1TableTokenType.TERMINAL, lexeme));
    }

    void lexVariable() {
      currentCharacterIndex++; // consume the [
      String lexeme = "";
      while (currentCharacterIndex < input.length() && input.charAt(currentCharacterIndex) != '>')
        lexeme += input.charAt(currentCharacterIndex++);

      expectCharacter('>');
      tokens.add(new LR1TableToken(LR1TableTokenType.VARIABLE, lexeme));
    }

    void lexAction() {
      char ch = input.charAt(currentCharacterIndex);
      String lexeme = "";

      do {
        lexeme += ch;
        ch = input.charAt(++currentCharacterIndex);
      } while (currentCharacterIndex < input.length() && !(ch == ',' || ch == '\n' || ch == '\r'));

      tokens.add(new LR1TableToken(LR1TableTokenType.PROCESS, lexeme));
    }

    ArrayList<LR1TableToken> lex() {
      while (currentCharacterIndex < input.length()) {
        char c = input.charAt(currentCharacterIndex);

        if (c == ' ') {
          currentCharacterIndex++;
          continue;
        } else if (c == '\n') {
          tokens.add(new LR1TableToken(LR1TableTokenType.NEWLINE));
          currentCharacterIndex++;
          continue;
        } else if (c == '[')
          lexTerminal();
        else if (c == '<')
          lexVariable();
        else if (c == '=') {
          tokens.add(new LR1TableToken(LR1TableTokenType.EQUAL));
          currentCharacterIndex++;
          continue;
        } else if (c == ',') {
          tokens.add(new LR1TableToken(LR1TableTokenType.COMMA));
          currentCharacterIndex++;
          continue;
        } else
          lexAction();
      }

      return this.tokens;
    }
  }

  String input;

  public LR1TableParser(String input) {
    this.input = input;
  }

  static enum LR1TableProcessType {
    SHIFT, REDUCE, GOTO
  }

  static class LR1TableProcess {
    LR1TableProcessType type;
    int value;

    LR1TableProcess(LR1TableProcessType type, int value) {
      this.type = type;
      this.value = value;
    }

    public String toString() {
      return String.format("%s %d", this.type, this.value);
    }
  }

  public static class LR1TableState {
    HashMap<String, LR1TableProcess> actions = new HashMap<>();
    HashMap<String, LR1TableProcess> gotos = new HashMap<>();

    public String toString() {
      return String.format("%s\n%s", this.actions.toString(), this.gotos.toString());
    }
  }

  ArrayList<LR1TableToken> tokens;
  int currentTokenIndex = 0;

  LR1TableToken expectToken(LR1TableTokenType type) {
    LR1TableToken token = tokens.get(currentTokenIndex);
    if (token.type != type) {
      throw new RuntimeException("Unexpected token: " + token.type);
    }
    currentTokenIndex++;
    return token;
  }

  ArrayList<LR1TableState> states = new ArrayList<>();

  LR1TableState parseState() {
    LR1TableState newState = new LR1TableState();
    LR1TableToken currentToken = tokens.get(currentTokenIndex);

    do {
      currentTokenIndex++;
      if (currentToken.type == LR1TableTokenType.TERMINAL) {
        expectToken(LR1TableTokenType.EQUAL);
        LR1TableToken actionToken = expectToken(LR1TableTokenType.PROCESS);
        int value = Integer.parseInt(actionToken.lexeme.substring(1));

        newState.actions.put(currentToken.lexeme, new LR1TableProcess(
            actionToken.lexeme.startsWith("r") ? LR1TableProcessType.REDUCE : LR1TableProcessType.SHIFT, value));
      } else if (currentToken.type == LR1TableTokenType.VARIABLE) {
        expectToken(LR1TableTokenType.EQUAL);
        LR1TableToken gotoToken = expectToken(LR1TableTokenType.PROCESS);

        int value = Integer.parseInt(gotoToken.lexeme);
        newState.gotos.put(currentToken.lexeme, new LR1TableProcess(LR1TableProcessType.GOTO, value));
      }

      currentToken = tokens.get(currentTokenIndex);
      if (currentToken.type == LR1TableTokenType.COMMA) {
        currentTokenIndex++;
        currentToken = tokens.get(currentTokenIndex);
      }

    } while (currentToken.type != LR1TableTokenType.NEWLINE);

    currentTokenIndex++; // consume newline token
    return newState;
  }

  public ArrayList<LR1TableState> parse() {
    LR1TableLexer lexer = new LR1TableLexer(input);
    this.tokens = lexer.lex();

    while (currentTokenIndex < this.tokens.size())
      this.states.add(this.parseState());
    return this.states;
  }
}
