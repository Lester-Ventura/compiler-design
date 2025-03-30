package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexEngine {
  private HashMap<String, RegexNode> environment = new HashMap<>();
  String input;
  int currentCharacterIndex;
  int startCharacterIndex;

  private RegexEngine(String input) {
    this.input = input;
  }

  private char peek() {
    return currentCharacterIndex >= this.input.length() ? '\0' : this.input.charAt(currentCharacterIndex);
  }

  // returns the character stored in currentCharacterIndex + 1
  private char peekNext() {
    return currentCharacterIndex + 1 >= this.input.length() ? '\0' : this.input.charAt(currentCharacterIndex + 1);
  }

  // eats all the comments
  // need to loop due to the possibility of chained comments like this one
  private boolean ignoreComment() {
    if (currentCharacterIndex < input.length() - 2 && peek() == '/') {
      if (peekNext() == '/') {
        currentCharacterIndex += 2;

        while (hasNextToken() && peek() != '\0' && peek() != '\n') {
          currentCharacterIndex++;
        }
        return true;
      } else if (peekNext() == '*') {
        currentCharacterIndex += 2;

        while (peek() != '*' && peekNext() != '/') {
          if (currentCharacterIndex >= input.length() - 2) {
            throw new ScannerError("Error: Unterminated multiline comment");
          }
          currentCharacterIndex++;
        }
        match('*');
        match('/');
        return true;
      }

    }
    return false;
  }

  // Just a boolean check for the next character
  private boolean match(char c) {
    if (peek() != c)
      return false;
    currentCharacterIndex++;
    return true;
  }

  // skips over every piece of whitespace
  private void ignoreWhitespace() {
    while (currentCharacterIndex < this.input.length() + 1 && Character.isWhitespace(peek())) {
      currentCharacterIndex++;
    }
  }

  public void addRule(String name, String expression) {
    addRule(name, expression, null);
  }

  public void addRule(String name, String expression, TokenType emit) {
    // need to lex the expression
    RegexLexer lexer = new RegexLexer(expression);
    ArrayList<RegexToken> tokens = lexer.lex();

    RegexParser parser = new RegexParser(tokens);
    RegexNode root = parser.parse();

    root.setTokenType(emit);

    environment.put(name, root);
  }

  public Token peekNextToken() {
    int save = currentCharacterIndex;
    Token token = getNextToken();
    currentCharacterIndex = save;
    return token;
  }

  public Token getNextToken() {
    do {
      ignoreWhitespace();
    } while (ignoreComment());

    startCharacterIndex = currentCharacterIndex;

    if (hasNextToken() == false)
      return new Token(TokenType.EOF, "", ColumnAndRow.calculate(startCharacterIndex, input));

    RegexEngineParsingResult ret = new RegexEngineParsingResult(false, "", null);
    RegexNode node = null;

    for (Map.Entry<String, RegexNode> entry : environment.entrySet()) {
      RegexNode attemptNode = entry.getValue();

      if (attemptNode.getTokenType() == null)
        continue;

      ArrayList<String> matches = attemptNode.getMatches(input.substring(currentCharacterIndex), environment);

      if (matches.size() > 0) {
        String longest = "";
        for (String match : matches)
          if (match.length() > longest.length())
            longest = match;

        if (!ret.success || ret.lexeme.length() < longest.length()
            || (ret.lexeme.length() == longest.length() && node.getTokenType() == TokenType.IDENTIFIER)) {
          ret = new RegexEngineParsingResult(true, longest, entry.getKey());
          node = attemptNode;
        }
      }
    }

    if (node != null && node.getTokenType() != null) {
      currentCharacterIndex += ret.lexeme.length();
      Token returnedToken = new Token(node.getTokenType(), ret.lexeme,
          ColumnAndRow.calculate(startCharacterIndex, input));
      return returnedToken;
    }

    char nextChar = input.charAt(startCharacterIndex);
    ColumnAndRow position = ColumnAndRow.calculate(startCharacterIndex, input);

    currentCharacterIndex++;
    throw new ScannerError("Error: Unexpected character '" + nextChar + "' at Line: " + position.getActualRow()
        + ", Column: " + position.getActualColumn());
  }

  public boolean hasNextToken() {
    return currentCharacterIndex < input.length();
  }

  public static RegexEngine createRegexEngine(String input) {
    RegexEngine lexer = new RegexEngine(input);

    // lex symbols
    lexer.addRule("plus", "$+", TokenType.PLUS);
    lexer.addRule("minus", "$-", TokenType.MINUS);
    lexer.addRule("star", "$*", TokenType.STAR);
    lexer.addRule("forward_slash", "$/", TokenType.FORWARD_SLASH);
    lexer.addRule("percent", "$%", TokenType.PERCENT);
    lexer.addRule("double_star", "$*$*", TokenType.DOUBLE_STAR);
    lexer.addRule("double_plus", "$+$+", TokenType.DOUBLE_PLUS);
    lexer.addRule("double_minus", "$-$-", TokenType.DOUBLE_MINUS);

    lexer.addRule("double_l_angle_bar", "$>$>", TokenType.DOUBLE_L_ANGLE_BAR);
    lexer.addRule("double_r_angle_bar", "$<$<", TokenType.DOUBLE_R_ANGLE_BAR);
    lexer.addRule("pipe", "$|", TokenType.PIPE);
    lexer.addRule("ampersand", "$&", TokenType.AMPERSAND);
    lexer.addRule("carat", "$^", TokenType.CARAT);

    lexer.addRule("l_angle_bar", "$<", TokenType.L_ANGLE_BAR);
    lexer.addRule("l_angle_bar_equals", "$<$=", TokenType.L_ANGLE_BAR_EQUALS);
    lexer.addRule("r_angle_bar", "$>", TokenType.R_ANGLE_BAR);
    lexer.addRule("r_angle_bar_equals", "$>$=", TokenType.R_ANGLE_BAR_EQUALS);
    lexer.addRule("exclamation_equals", "$!$=", TokenType.EXCLAMATION_EQUALS);
    lexer.addRule("double_equals", "$=$=", TokenType.DOUBLE_EQUALS);

    lexer.addRule("double_ampersand", "$&$&", TokenType.DOUBLE_AMPERSAND);
    lexer.addRule("double_pipe", "$|$|", TokenType.DOUBLE_PIPE);
    lexer.addRule("exclamation", "$!", TokenType.EXCLAMATION);

    lexer.addRule("minus_r_angle_bar", "$-$>", TokenType.MINUS_R_ANGLE_BAR);
    lexer.addRule("equals", "$=", TokenType.EQUALS);
    lexer.addRule("comma", "$,", TokenType.COMMA);
    lexer.addRule("dot", "$.", TokenType.DOT);
    lexer.addRule("colon", "$:", TokenType.COLON);
    lexer.addRule("semicolon", "$;", TokenType.SEMICOLON);

    lexer.addRule("l_paren", "$(", TokenType.L_PAREN);
    lexer.addRule("r_paren", "$)", TokenType.R_PAREN);
    lexer.addRule("l_brace", "$[", TokenType.L_BRACE);
    lexer.addRule("r_brace", "$]", TokenType.R_BRACE);
    lexer.addRule("l_curly_brace", "${", TokenType.L_CURLY_BRACE);
    lexer.addRule("r_curly_brace", "$}", TokenType.R_CURLY_BRACE);

    lexer.addRule("lowercase", "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z");
    lexer.addRule("uppercase", "A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z");
    lexer.addRule("letter", "${lowercase}|${uppercase}");
    lexer.addRule("symbols",
        "$ | $! | $@ | $# | $$ | $% | $^ | $& | $* | $( | $) | ${ | $[ | $} | $] | $; | $: | $< | $, | $. | $> | $? | $/ | $` | $~ | $- | $_ | $+ | $=");
    lexer.addRule("escape_character", "$\\ | $\n | $\t | $\r | $\\$\" | $\\$\'");
    lexer.addRule("digit", "0|1|2|3|4|5|6|7|8|9");
    lexer.addRule("character", "${letter}|${digit}|${symbols}|${escape_character}");

    lexer.addRule("float_number", "(${digit})+$.(${digit})+");
    lexer.addRule("decimal_number", "(${digit})+");
    lexer.addRule("octal_number", "0e(0|1|2|3|4|5|6|7)+");
    lexer.addRule("hexadecimal_number", "0x(${digit}|a|b|c|d|e|f|A|B|C|D|E|F)+");
    lexer.addRule("binary_number", "0b(0|1)+");

    // handle literal tokens
    lexer.addRule("string_literal", "$\"(${character})*$\" | $\'(${character})*$\'", TokenType.STRING_LITERAL);
    lexer.addRule("number_literal",
        "${float_number}|${decimal_number}|${octal_number}|${binary_number}|${hexadecimal_number}",
        TokenType.NUMBER_LITERAL);
    lexer.addRule("boolean_literal", "faker|shaker", TokenType.BOOLEAN_LITERAL);
    lexer.addRule("null_literal", "cooldown", TokenType.NULL_LITERAL);

    // handle identifier and reserved words
    lexer.addRule("item", "item", TokenType.VARIABLE);
    lexer.addRule("rune", "rune", TokenType.CONSTANT);
    lexer.addRule("skill", "skill", TokenType.FUNCTION);
    lexer.addRule("steal", "steal", TokenType.IMPORT);
    lexer.addRule("build", "build", TokenType.OBJECT);
    lexer.addRule("canwin", "canwin", TokenType.IF);
    lexer.addRule("remake", "remake", TokenType.ELIF);
    lexer.addRule("lose", "lose", TokenType.ELSE);
    lexer.addRule("channel", "channel", TokenType.SWITCH);
    lexer.addRule("teleport", "teleport", TokenType.CASE);
    lexer.addRule("recall", "recall", TokenType.DEFAULT);
    lexer.addRule("flash", "flash", TokenType.SWITCH_GOTO);
    lexer.addRule("cancel", "cancel", TokenType.SWITCH_BREAK);
    lexer.addRule("wave", "wave", TokenType.WHILE);
    lexer.addRule("cannon", "cannon", TokenType.FOR);
    lexer.addRule("clear", "clear", TokenType.LOOP_BREAK);
    lexer.addRule("next", "next", TokenType.LOOP_CONTINUE);
    lexer.addRule("of", "of", TokenType.OF);
    lexer.addRule("support", "support", TokenType.TRY);
    lexer.addRule("carry", "carry", TokenType.CATCH);
    lexer.addRule("feed", "feed", TokenType.THROW);
    lexer.addRule("recast", "recast", TokenType.RETURN);

    // handle type tokens
    lexer.addRule("number_type", "message", TokenType.NUMBER_TYPE);
    lexer.addRule("boolean_type", "stats", TokenType.BOOLEAN_TYPE);
    lexer.addRule("string_type", "goat", TokenType.STRING_TYPE);
    lexer.addRule("void_type", "passive", TokenType.VOID_TYPE);
    lexer.addRule("identifier", "(${letter}|$_)(${letter}|${digit}|$_)*", TokenType.IDENTIFIER);

    return lexer;
  }
}

class RegexEngineParsingResult {
  boolean success;
  String lexeme;
  String from;

  public RegexEngineParsingResult(boolean success, String lexeme, String from) {
    this.success = success;
    this.lexeme = lexeme;
    this.from = from;
  }

  public String toString() {
    return success ? "From: " + from + ". Lexeme: " + lexeme : "No token found";
  }
}

abstract class RegexNode {
  private TokenType emit;

  public TokenType getTokenType() {
    return emit;
  }

  public void setTokenType(TokenType emit) {
    this.emit = emit;
  }

  abstract public String toString();

  abstract public ArrayList<String> getMatches(String restString, HashMap<String, RegexNode> environment);
}

class RegexConcatenationNode extends RegexNode {
  ArrayList<RegexNode> nodes;

  public RegexConcatenationNode(ArrayList<RegexNode> nodes) {
    this.nodes = nodes;
  }

  public String toString() {
    String ret = "";
    for (int i = 0; i < nodes.size(); i++) {
      ret += nodes.get(i).toString();
    }
    return ret;
  }

  public ArrayList<String> getMatches(String restString, HashMap<String, RegexNode> environment) {
    ArrayList<String> caches = new ArrayList<>();
    caches.add("");

    for (RegexNode node : nodes) {
      ArrayList<String> nextCaches = new ArrayList<>();

      for (String cache : caches) {
        String rest = restString.replaceFirst(Pattern.quote(cache), "");
        ArrayList<String> nextMatches = node.getMatches(rest, environment);
        nextCaches.addAll(nextMatches.stream().map(m -> cache + m).collect(Collectors.toList()));
      }

      caches = nextCaches;
    }

    return caches;
  }
}

class RegexEitherNode extends RegexNode {
  ArrayList<RegexNode> nodes;

  public RegexEitherNode(ArrayList<RegexNode> nodes) {
    this.nodes = nodes;
  }

  public String toString() {
    String ret = "";
    for (int i = 0; i < nodes.size(); i++) {
      ret += nodes.get(i).toString();
      if (i != nodes.size() - 1)
        ret += "|";
    }
    return ret;
  }

  public ArrayList<String> getMatches(String restString, HashMap<String, RegexNode> environment) {
    ArrayList<String> matches = new ArrayList<>();
    for (RegexNode node : nodes)
      matches.addAll(node.getMatches(restString, environment));
    return matches;
  }
}

class RegexLiteralNode extends RegexNode {
  char ch;

  public RegexLiteralNode(char ch) {
    this.ch = ch;
  }

  public String toString() {
    return "" + ch;
  }

  public ArrayList<String> getMatches(String restString, HashMap<String, RegexNode> environment) {
    ArrayList<String> matches = new ArrayList<>();
    char starting = restString.charAt(0);
    if (starting == ch)
      matches.add("" + starting);
    return matches;
  }
}

class RegexVariableNode extends RegexNode {
  String variableName;

  public RegexVariableNode(String variableName) {
    this.variableName = variableName;
  }

  public String toString() {
    return "<" + variableName + ">";
  }

  public ArrayList<String> getMatches(String restString, HashMap<String, RegexNode> environment) {
    if (!environment.containsKey(this.variableName))
      return new ArrayList<>();

    RegexNode roolNode = environment.get(this.variableName);
    return roolNode.getMatches(restString, environment);
  }
}

enum RegexGroupingNodeModifiers {
  NONE,
  NONE_OR_MORE,
  ONE_OR_MORE,
}

class RegexGroupingNode extends RegexNode {
  RegexNode internalNode;
  RegexGroupingNodeModifiers modifier;

  public RegexGroupingNode(RegexNode internalNode, RegexGroupingNodeModifiers modifier) {
    this.internalNode = internalNode;
    this.modifier = modifier;
  }

  public RegexGroupingNode(RegexNode internalNode) {
    this(internalNode, RegexGroupingNodeModifiers.NONE);
  }

  public String toString() {
    return "(" + internalNode.toString() + ")" + (modifier == RegexGroupingNodeModifiers.ONE_OR_MORE ? "+"
        : modifier == RegexGroupingNodeModifiers.NONE_OR_MORE ? "*"
            : "");
  }

  public ArrayList<String> getMatches(String restString, HashMap<String, RegexNode> environment) {

    ArrayList<String> initialMatches = internalNode.getMatches(restString, environment);

    if (initialMatches.size() == 0) {
      if (modifier == RegexGroupingNodeModifiers.NONE_OR_MORE)
        initialMatches.add("");
      return initialMatches;
    } else if (modifier == RegexGroupingNodeModifiers.NONE)
      return initialMatches;

    ArrayList<String> matches = initialMatches;

    // handle matching for NONE_OR_MORE or ONE_OR_MORE
    while (true) {
      ArrayList<String> nextMatches = new ArrayList<>();

      for (String match : matches) {
        String rest = restString.replaceFirst(Pattern.quote(match), "");
        if (rest.length() == 0)
          continue;

        ArrayList<String> nextMatch = internalNode.getMatches(rest, environment);
        nextMatches.addAll(nextMatch.stream().map(m -> match + m).collect(Collectors.toList()));
      }

      if (nextMatches.size() == 0)
        break;

      matches = nextMatches;
    }

    return matches;
  }
}

class RegexParser {
  ArrayList<RegexToken> tokens;
  int currentTokenIndex = 0;

  RegexParser(ArrayList<RegexToken> tokens) {
    this.tokens = tokens;
  }

  RegexNode parse() { // parse starting from the top
    RegexNode first = this.parseConcatenation();
    ArrayList<RegexNode> possibles = new ArrayList<>();
    possibles.add(first);

    while (currentTokenIndex < tokens.size() &&
        tokens.get(currentTokenIndex).type == RegexTokenType.PIPE) {
      this.currentTokenIndex++; // consume the PIPE token
      RegexNode nextNode = this.parseConcatenation();
      possibles.add(nextNode);
    }

    if (possibles.size() > 1)
      return new RegexEitherNode(possibles);
    else
      return first;
  }

  RegexNode parseConcatenation() {
    RegexNode first = this.parseTerminal();
    ArrayList<RegexNode> nodes = new ArrayList<>();
    nodes.add(first);

    while (currentTokenIndex < tokens.size() &&
        (tokens.get(currentTokenIndex).type != RegexTokenType.PIPE &&
            tokens.get(currentTokenIndex).type != RegexTokenType.RPAREN)) {
      RegexNode nextNode = this.parseTerminal();
      nodes.add(nextNode);
    }

    if (nodes.size() > 1)
      return new RegexConcatenationNode(nodes);
    else
      return first;
  }

  RegexNode parseTerminal() {
    RegexToken currentToken = tokens.get(currentTokenIndex);

    switch (currentToken.type) {
      case LPAREN: {
        currentTokenIndex++; // CONSUME L_PAREN
        RegexNode internalNode = parse();

        expect(RegexTokenType.RPAREN); // next token should be R_PAREN

        RegexGroupingNodeModifiers modifier = RegexGroupingNodeModifiers.NONE;
        if (tokens.get(currentTokenIndex).type == RegexTokenType.ASTERISK) {
          modifier = RegexGroupingNodeModifiers.NONE_OR_MORE;
          currentTokenIndex++;
        } else if (tokens.get(currentTokenIndex).type == RegexTokenType.PLUS) {
          modifier = RegexGroupingNodeModifiers.ONE_OR_MORE;
          currentTokenIndex++;
        }

        return new RegexGroupingNode(internalNode, modifier);
      }

      case LITERAL: {
        currentTokenIndex++;
        return new RegexLiteralNode(currentToken.value.charAt(0));
      }

      case VARIABLE: {
        currentTokenIndex++;
        return new RegexVariableNode(currentToken.value);
      }

      default: {
        throw new Error("Was not able to parse the regex. Token: " + currentToken.toString());
      }
    }
  }

  void expect(RegexTokenType type) {
    RegexToken currentToken = tokens.get(currentTokenIndex);

    if (currentToken.type == type) {
      currentTokenIndex++;
    } else {
      throw new Error("Expected: " + type.toString() + ". Received: " + currentToken.toString());
    }
  }
}

enum RegexTokenType {
  LITERAL,
  PIPE,
  ASTERISK,
  PLUS,
  VARIABLE,
  LPAREN,
  RPAREN,
}

class RegexToken {
  RegexTokenType type;
  String value;

  RegexToken(RegexTokenType type, String value) {
    this.type = type;
    this.value = value;
  }

  RegexToken(RegexTokenType type, char ch) {
    this(type, "" + ch);
  }

  public String toString() {
    return "Type: " + type.toString() + ". Value: " + value;
  }
}

// This class takes a regular expression and breaks it up into tokens
class RegexLexer {
  String expression;
  ArrayList<RegexToken> tokens = new ArrayList<>();
  int index = 0;

  public RegexLexer(String expression) {
    this.expression = expression;
  }

  public ArrayList<RegexToken> lex() {
    while (index < expression.length()) {
      char currentCharacter = expression.charAt(index);

      if (Character.isAlphabetic(currentCharacter) || Character.isDigit(currentCharacter)) {
        tokens.add(new RegexToken(RegexTokenType.LITERAL, currentCharacter));
        index++;
      } else if (currentCharacter == '|') {
        tokens.add(new RegexToken(RegexTokenType.PIPE, currentCharacter));
        index++;
      } else if (currentCharacter == '+') {
        tokens.add(new RegexToken(RegexTokenType.PLUS, currentCharacter));
        index++;
      } else if (currentCharacter == '*') {
        tokens.add(new RegexToken(RegexTokenType.ASTERISK, currentCharacter));
        index++;
      } else if (currentCharacter == '(') {
        tokens.add(new RegexToken(RegexTokenType.LPAREN, currentCharacter));
        index++;
      } else if (currentCharacter == ')') {
        tokens.add(new RegexToken(RegexTokenType.RPAREN, currentCharacter));
        index++;
      }

      else if (currentCharacter == '$') {
        if (expression.length() > index + 1 && expression.charAt(index + 1) != '{'
            || expression.length() == index + 2) {
          // capture whatever the next character is as is
          tokens.add(new RegexToken(RegexTokenType.LITERAL, expression.charAt(index + 1)));
          index += 2;
        } else {
          // we have a regex variable so we need to handle until the matching }
          index += 2; // move index to the start of the variable
          String variableName = "";

          if (index == expression.length()) {
            tokens.add(new RegexToken(RegexTokenType.LITERAL, "}"));
          } else {
            while (expression.length() > index && expression.charAt(index) != '}') {
              variableName += expression.charAt(index);
              index++;
            }

            // consume the ending }
            tokens.add(new RegexToken(RegexTokenType.VARIABLE, variableName));
            index++;
          }
        }
      }

      else if (Character.isWhitespace(currentCharacter))
        index++;

      else {
        System.out.println("Was not able to handle ch: " + currentCharacter + " at index: " + index + " in expression: "
            + expression);
        System.exit(-1);
      }
    }

    return tokens;
  }
}