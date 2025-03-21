public enum TokenType {
    // Aritmethic Operators + - * / % ** ++ --
    PLUS, MINUS, STAR, FORWARD_SLASH, PERCENT, DOUBLE_STAR, DOUBLE_PLUS, DOUBLE_MINUS,
    // Bitwise Operators >> << | & ^
    DOUBLE_L_ANGLE_BAR, DOUBLE_R_ANGLE_BAR, PIPE, AMPERSAND, CARAT,
    // Relational Operators| < <= > >= != ==
    L_ANGLE_BAR, L_ANGLE_BAR_EQUALS, R_ANGLE_BAR, R_ANGLE_BAR_EQUALS, EXCLAMATION_EQUALS, DOUBLE_EQUALS,
    // Logical Operators| && || !
    DOUBLE_AMPERSAND, DOUBLE_PIPE, EXCLAMATION,
    // Special Symbols|
    MINUS_R_ANGLE_BAR, EQUALS, COMMA, DOT, L_PAREN, R_PAREN, L_BRACE, R_BRACE, COLON, SEMICOLON, L_CURLY_BRACE,
    R_CURLY_BRACE,
    // Reserved Words
    // Declaration Tokens| item rune skill recast build
    VARIABLE, CONSTANT, FUNCTION, RETURN, OBJECT,
    // Conditional Tokens| canwin remake lose channel teleport recall flash cancel
    IF, ELIF, ELSE, SWITCH, CASE, DEFAULT, S_GOTO, S_BREAK, WHILE, FOR, BREAK, CONTINUE, OF,
    // Error Handling | feed support carry
    THROW, TRY, CATCH,
    // Type | stats goat message passive
    NUMBER, BOOLEAN, STRING, VOID,
    // I/O Operation | steal broadcast chat
    IMPORT, PRINT, INPUT, EXIT,
    // null | cooldown
    NULL,
    // Identifier
    ID,
    // Boolean Literals | faker shaker
    TRUE, FALSE,
    // String_Literal
    STRING_LITERAL,
    // Numbers
    DECIMAL_NUMBER, FLOAT_NUMBER, BINARY_NUMBER, OCTAL_NUMBER, HEXADECIMAL_NUMBER,
    // End-of-File
    EOF
}
