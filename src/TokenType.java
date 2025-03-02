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
    MINUS_R_ANGLE_BAR, EQUALS, BACK_SLASH_N, BACK_SLASH_T, DOUBLE_BACK_SLASH, BACK_SLASH_APOSTROPHE,
    BACK_SLASH_QUOTATION, BACK_SLASH_C, BACK_SLASH_F, DOUBLE_FORWARD_SLASH, FORWARD_SLASH_STAR, STAR_FORWARD_SLASH,
    QUOTATION, APOSTROPHE, COMMA, DOT, L_PAREN, R_PAREN, L_BRACE, R_BRACE, COLON, SEMICOLON, L_CURLY_BRACE,
    R_CURLY_BRACE,
    // Reserved Words
    // Boolean Tokens| faker shaker
    TRUE, FALSE,
    // Declaration Tokens| item rune skill recast build
    VARIABLE, CONSTANT, FUNCTION, RETURN, OBJECT,
    // Conditional Tokens| canwin remake lose channel teleport recall flash cancel
    IF, ELIF, ELSE, SWITCH, CASE, DEFAULT, S_GOTO, S_BREAK, WHILE, FOR, BREAK, CONTINUE, OF,
    // Error Handling | feed support carry
    THROW, TRY, CATCH,
    // Type | stats goat message passive cooldown
    NUMBER, BOOLEAN, STRING, VOID, NULL,
    // I/O Operation | import print input
    IMPORT, PRINT, INPUT,
    // Identifier
    ID,
    // String_Literal
    STRING_LITERAL,
    // Numbers
    DECIMAL_NUMBER, FLOAT_NUMBER, BINARY_NUMBER, OCTAL_NUMBER, HEXADECIMAL_NUMBER,
    // End-of-File
    EOF
}
