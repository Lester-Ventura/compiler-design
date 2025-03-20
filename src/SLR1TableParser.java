import java.util.ArrayList;
import java.util.HashMap;

public class SLR1TableParser {
  static enum SLR1TableTokenType {
    VARIABLE, TERMINAL, EQUAL, PROCESS, COMMA, NEWLINE
  }

  static class SLR1TableToken {
    SLR1TableTokenType type;
    String lexeme = "";

    SLR1TableToken(SLR1TableTokenType type, String lexeme) {
      this.lexeme = lexeme;
      this.type = type;
    }

    SLR1TableToken(SLR1TableTokenType type) {
      this.lexeme = "";
      this.type = type;
    }

    public String toString() {
      return this.type + " " + this.lexeme;
    }
  }

  static class SLR1TableLexer {
    String input;
    int currentCharacterIndex = 0;
    ArrayList<SLR1TableToken> tokens = new ArrayList<SLR1TableToken>();

    SLR1TableLexer(String input) {
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
      tokens.add(new SLR1TableToken(SLR1TableTokenType.TERMINAL, lexeme));
    }

    void lexVariable() {
      currentCharacterIndex++; // consume the [
      String lexeme = "";
      while (currentCharacterIndex < input.length() && input.charAt(currentCharacterIndex) != '>')
        lexeme += input.charAt(currentCharacterIndex++);

      expectCharacter('>');
      tokens.add(new SLR1TableToken(SLR1TableTokenType.VARIABLE, lexeme));
    }

    void lexAction() {
      char ch = input.charAt(currentCharacterIndex);
      String lexeme = "";

      do {
        lexeme += ch;
        ch = input.charAt(++currentCharacterIndex);
      } while (currentCharacterIndex < input.length() && !(ch == ',' || ch == '\n'));

      tokens.add(new SLR1TableToken(SLR1TableTokenType.PROCESS, lexeme));
    }

    ArrayList<SLR1TableToken> lex() {
      while (currentCharacterIndex < input.length()) {
        char c = input.charAt(currentCharacterIndex);

        if (c == ' ') {
          currentCharacterIndex++;
          continue;
        } else if (c == '\n') {
          tokens.add(new SLR1TableToken(SLR1TableTokenType.NEWLINE));
          currentCharacterIndex++;
          continue;
        } else if (c == '[')
          lexTerminal();
        else if (c == '<')
          lexVariable();
        else if (c == '=') {
          tokens.add(new SLR1TableToken(SLR1TableTokenType.EQUAL));
          currentCharacterIndex++;
          continue;
        } else if (c == ',') {
          tokens.add(new SLR1TableToken(SLR1TableTokenType.COMMA));
          currentCharacterIndex++;
          continue;
        } else
          lexAction();
      }

      return this.tokens;
    }
  }

  String input;

  SLR1TableParser(String input) {
    this.input = input;
  }

  static enum SLR1TableProcessType {
    SHIFT, REDUCE, GOTO
  }

  static class SLR1TableProcess {
    SLR1TableProcessType type;
    int value;

    SLR1TableProcess(SLR1TableProcessType type, int value) {
      this.type = type;
      this.value = value;
    }

    public String toString() {
      return String.format("%s %d", this.type, this.value);
    }
  }

  static class SLR1TableState {
    HashMap<String, SLR1TableProcess> actions = new HashMap<>();
    HashMap<String, SLR1TableProcess> gotos = new HashMap<>();

    public String toString() {
      return String.format("%s\n%s", this.actions.toString(), this.gotos.toString());
    }
  }

  ArrayList<SLR1TableToken> tokens;
  int currentTokenIndex = 0;

  SLR1TableToken expectToken(SLR1TableTokenType type) {
    SLR1TableToken token = tokens.get(currentTokenIndex);
    if (token.type != type) {
      throw new RuntimeException("Unexpected token: " + token.type);
    }
    currentTokenIndex++;
    return token;
  }

  ArrayList<SLR1TableState> states = new ArrayList<>();

  SLR1TableState parseState() {
    SLR1TableState newState = new SLR1TableState();
    SLR1TableToken currentToken = tokens.get(currentTokenIndex);

    do {
      currentTokenIndex++;

      if (currentToken.type == SLR1TableTokenType.TERMINAL) {
        expectToken(SLR1TableTokenType.EQUAL);
        SLR1TableToken actionToken = expectToken(SLR1TableTokenType.PROCESS);
        int value = Integer.parseInt(actionToken.lexeme.substring(1));

        newState.actions.put(currentToken.lexeme, new SLR1TableProcess(
            actionToken.lexeme.startsWith("r") ? SLR1TableProcessType.REDUCE : SLR1TableProcessType.SHIFT, value));
      } else if (currentToken.type == SLR1TableTokenType.VARIABLE) {
        expectToken(SLR1TableTokenType.EQUAL);
        SLR1TableToken gotoToken = expectToken(SLR1TableTokenType.PROCESS);
        int value = Integer.parseInt(gotoToken.lexeme);
        newState.gotos.put(currentToken.lexeme, new SLR1TableProcess(SLR1TableProcessType.GOTO, value));
      }

      currentToken = tokens.get(currentTokenIndex);
      if (currentToken.type == SLR1TableTokenType.COMMA) {
        currentTokenIndex++;
        currentToken = tokens.get(currentTokenIndex);
      }

    } while (currentToken.type != SLR1TableTokenType.NEWLINE);

    currentTokenIndex++; // consume newline token
    return newState;
  }

  ArrayList<SLR1TableState> parse() {
    SLR1TableLexer lexer = new SLR1TableLexer(input);
    this.tokens = lexer.lex();

    while (currentTokenIndex < this.tokens.size())
      this.states.add(this.parseState());
    return this.states;
  }
}
