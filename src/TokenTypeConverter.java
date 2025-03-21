import java.util.HashMap;

public class TokenTypeConverter {
  public static HashMap<TokenType, String> generate() {
    // this hashmap must map all tokens in TokenType to their string representation
    // in the table. NOTE: there are some missing on purpose
    HashMap<TokenType, String> map = new HashMap<>();

    // arithmetic operators
    map.put(TokenType.PLUS, "plus");
    map.put(TokenType.MINUS, "minus");
    map.put(TokenType.STAR, "star");
    map.put(TokenType.FORWARD_SLASH, "forward_slash");
    map.put(TokenType.PERCENT, "percent");
    map.put(TokenType.DOUBLE_STAR, "double_star");
    map.put(TokenType.DOUBLE_PLUS, "double_plus");
    map.put(TokenType.DOUBLE_MINUS, "double_minus");

    // bitwise operators
    map.put(TokenType.DOUBLE_L_ANGLE_BAR, "double_l_angle_bar");
    map.put(TokenType.DOUBLE_R_ANGLE_BAR, "double_r_angle_bar");
    map.put(TokenType.PIPE, "pipe");
    map.put(TokenType.AMPERSAND, "ampersand");
    map.put(TokenType.CARAT, "carat");

    // relational operators
    map.put(TokenType.L_ANGLE_BAR, "l_angle_bar");
    map.put(TokenType.L_ANGLE_BAR_EQUALS, "l_angle_bar_equals");
    map.put(TokenType.R_ANGLE_BAR, "r_angle_bar");
    map.put(TokenType.R_ANGLE_BAR_EQUALS, "r_angle_bar_equals");
    map.put(TokenType.EXCLAMATION_EQUALS, "exclamation_equals");
    map.put(TokenType.DOUBLE_EQUALS, "double_equals");

    // logical operators
    map.put(TokenType.DOUBLE_AMPERSAND, "double_ampersand");
    map.put(TokenType.DOUBLE_PIPE, "double_pipe");
    map.put(TokenType.EXCLAMATION, "exclamation");

    // special symbols
    map.put(TokenType.MINUS_R_ANGLE_BAR, "minus_r_angle_bar");
    map.put(TokenType.EQUALS, "equals");
    map.put(TokenType.COMMA, "comma");
    map.put(TokenType.DOT, "dot");
    map.put(TokenType.L_PAREN, "l_paren");
    map.put(TokenType.R_PAREN, "r_paren");
    map.put(TokenType.L_BRACE, "l_brace");
    map.put(TokenType.R_BRACE, "r_brace");
    map.put(TokenType.L_CURLY_BRACE, "l_curly_brace");
    map.put(TokenType.R_CURLY_BRACE, "r_curly_brace");
    map.put(TokenType.COLON, "colon");
    map.put(TokenType.SEMICOLON, "semicolon");

    // reserved words
    map.put(TokenType.VARIABLE, "var");
    map.put(TokenType.CONSTANT, "const");
    map.put(TokenType.FUNCTION, "function");
    map.put(TokenType.RETURN, "return");
    map.put(TokenType.OBJECT, "object");

    // conditional tokens
    map.put(TokenType.IF, "if");
    map.put(TokenType.ELIF, "elif");
    map.put(TokenType.ELSE, "else");
    map.put(TokenType.SWITCH, "switch");
    map.put(TokenType.CASE, "case");
    map.put(TokenType.DEFAULT, "default");
    map.put(TokenType.S_GOTO, "goto");
    map.put(TokenType.S_BREAK, "switch_break");
    map.put(TokenType.WHILE, "while");
    map.put(TokenType.FOR, "for");
    map.put(TokenType.BREAK, "loop_break");
    map.put(TokenType.CONTINUE, "continue");
    map.put(TokenType.OF, "of");

    // error handling
    map.put(TokenType.THROW, "throw");
    map.put(TokenType.TRY, "try");
    map.put(TokenType.CATCH, "catch");

    // types
    map.put(TokenType.NUMBER, "number_type");
    map.put(TokenType.BOOLEAN, "boolean_type");
    map.put(TokenType.STRING, "string_type");
    map.put(TokenType.VOID, "void");

    // literals
    map.put(TokenType.NULL, "null_literal");
    map.put(TokenType.TRUE, "boolean_literal");
    map.put(TokenType.FALSE, "boolean_literal");
    map.put(TokenType.ID, "identifier");
    map.put(TokenType.STRING_LITERAL, "string_literal");
    map.put(TokenType.DECIMAL_NUMBER, "number_literal");
    map.put(TokenType.FLOAT_NUMBER, "number_literal");
    map.put(TokenType.BINARY_NUMBER, "number_literal");
    map.put(TokenType.OCTAL_NUMBER, "number_literal");
    map.put(TokenType.HEXADECIMAL_NUMBER, "number_literal");
    map.put(TokenType.EOF, "eof");

    return map;
  }
}