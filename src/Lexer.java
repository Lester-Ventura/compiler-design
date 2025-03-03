import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private HashMap<String, TokenType> reservedWords = Table.generateReservedWords();
    private int currentCharacterIndex = 0;
    private int startCharacterIndex = 0;
    private String source;

    public Lexer(String source) {
        this.source = source;
    }

    public Token getNextToken() {

        do {
            ignoreWhitespace();
        } while (ignoreComment());

        startCharacterIndex = currentCharacterIndex;

        char nextChar = peek();
        Token check;
        if (nextChar == '"' || nextChar == '\'')
            return lexStringLiteral(nextChar); // takes either ' or " delimiter
        else if (Character.isAlphabetic(nextChar) || nextChar == '_')
            return lexIdentifier();
        else if (Character.isDigit(nextChar) || nextChar == '_')
            return lexNumerical();
        else if ((check = lexOperator()) != null)
            return check;
        else if ((check = lexSymbols()) != null)
            return check;
        else if (nextChar == '\0')
            return new Token(TokenType.EOF, "", ColumnAndRow.calculate(startCharacterIndex, source));

        // throw new Error("Was unable to process the next token");
        // Edited Error Message
        ColumnAndRow position = ColumnAndRow.calculate(currentCharacterIndex, source);
        throw new Error("Error: Unexpected character '" + nextChar + "' at Line: " + position.getActualRow()
                + ", Column: " + position.getActualColumn());
    }

    public Token lexOperator() {
        String lexeme = peek() + "";
        switch (peek()) {
            case '+':
                // I realized too late that this could all have been solved with an if statement
                advance('+');
                if (match('+'))
                    return new Token(TokenType.DOUBLE_PLUS, lexeme + "+", defaultColumnAndRow());
                else
                    return new Token(TokenType.PLUS, lexeme,
                            defaultColumnAndRow());
            case '-':
                advance('-');
                if (match('-'))
                    return new Token(TokenType.DOUBLE_MINUS, lexeme + '-', defaultColumnAndRow());
                else if (match('>'))
                    return new Token(TokenType.MINUS_R_ANGLE_BAR, lexeme + '>', defaultColumnAndRow());
                else
                    return new Token(TokenType.PLUS, lexeme, defaultColumnAndRow());
            case '*':
                advance('*');
                if (match('*'))
                    return new Token(TokenType.DOUBLE_STAR, lexeme + '*', defaultColumnAndRow());
                else
                    return new Token(TokenType.STAR, lexeme, defaultColumnAndRow());
                // Comments are handled outside this
            case '/':
                advance('/');
                return new Token(TokenType.FORWARD_SLASH, lexeme, defaultColumnAndRow());
            case '%':
                advance('%');
                return new Token(TokenType.PERCENT, lexeme, defaultColumnAndRow());
            case '<':
                advance('<');
                if (match('<'))
                    return new Token(TokenType.DOUBLE_R_ANGLE_BAR, lexeme + '<', defaultColumnAndRow());
                else if (match('='))
                    return new Token(TokenType.R_ANGLE_BAR_EQUALS, lexeme + '=', defaultColumnAndRow());
                else
                    return new Token(TokenType.R_ANGLE_BAR, lexeme, defaultColumnAndRow());
            case '>':
                advance('>');
                if (match('>'))
                    return new Token(TokenType.DOUBLE_L_ANGLE_BAR, lexeme + '>', defaultColumnAndRow());
                else if (match('='))
                    return new Token(TokenType.L_ANGLE_BAR_EQUALS, lexeme + '=', defaultColumnAndRow());
                else
                    return new Token(TokenType.L_ANGLE_BAR, lexeme, defaultColumnAndRow());
            case '|':
                advance('|');
                if (match('|'))
                    return new Token(TokenType.DOUBLE_PIPE, lexeme + '|', defaultColumnAndRow());
                else
                    return new Token(TokenType.PIPE, lexeme, defaultColumnAndRow());
            case '&':
                advance('&');
                if (match('&'))
                    return new Token(TokenType.DOUBLE_AMPERSAND, lexeme + '&', defaultColumnAndRow());
                else
                    return new Token(TokenType.AMPERSAND, lexeme, defaultColumnAndRow());
            case '!':
                advance('!');
                if (match('='))
                    return new Token(TokenType.EXCLAMATION_EQUALS, lexeme + '=', defaultColumnAndRow());
                else
                    return new Token(TokenType.EXCLAMATION, lexeme, defaultColumnAndRow());
            case '=':
                advance('=');
                if (match('='))
                    return new Token(TokenType.DOUBLE_EQUALS, lexeme + '=', defaultColumnAndRow());
                else
                    return new Token(TokenType.EQUALS, lexeme, defaultColumnAndRow());
            case '^':
                advance('^');
                return new Token(TokenType.CARAT, lexeme, defaultColumnAndRow());
            default:
                return null;
        }
    }

    public Token lexSymbols() {
        String lexeme = peek() + "";

        if (match('"'))
            return new Token(TokenType.QUOTATION, lexeme, defaultColumnAndRow());
        else if (match('\''))
            return new Token(TokenType.APOSTROPHE, lexeme, defaultColumnAndRow());
        else if (match('('))
            return new Token(TokenType.L_PAREN, lexeme, defaultColumnAndRow());
        else if (match(')'))
            return new Token(TokenType.R_PAREN, lexeme, defaultColumnAndRow());
        else if (match('['))
            return new Token(TokenType.L_BRACE, lexeme, defaultColumnAndRow());
        else if (match(']'))
            return new Token(TokenType.R_BRACE, lexeme, defaultColumnAndRow());
        else if (match(':'))
            return new Token(TokenType.COLON, lexeme, defaultColumnAndRow());
        else if (match(';'))
            return new Token(TokenType.SEMICOLON, lexeme, defaultColumnAndRow());
        else if (match(','))
            return new Token(TokenType.COLON, lexeme, defaultColumnAndRow());
        else if (match('.'))
            return new Token(TokenType.DOT, lexeme, defaultColumnAndRow());
        else if (match('{'))
            return new Token(TokenType.L_CURLY_BRACE, lexeme, defaultColumnAndRow());
        else if (match('}'))
            return new Token(TokenType.R_CURLY_BRACE, lexeme, defaultColumnAndRow());
        else
            return null;
    }

    public Token lexNumerical() {
        char digit = peek();

        if (digit != '0')
            return parseDecimal();
        else if (peekNext() == 'x')
            return parseHexadecimal();
        else if (peekNext() == 'e')
            return parseOctal();
        else if (peekNext() == 'b')
            return parseBinary();
        else
            return parseDecimal();
    }

    public Token parseDecimal() {
        boolean hasHitDecimal = false;
        String number = "";

        do {
            char currentCharacter = peek();

            if (currentCharacter == '.') {
                hasHitDecimal = true;
            }

            number += currentCharacter;
            currentCharacterIndex++;
        } while (Character.isDigit(peek()) || (peek() == '.' && !hasHitDecimal));

        return new Token(TokenType.DECIMAL_NUMBER, number, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    public Token parseHexadecimal() {
        String number = "0x";
        currentCharacterIndex += 2; // skip over the 0x

        while (peek() != '\0' && isHexDigit(peek())) {
            number += peek();
            currentCharacterIndex++;
        }

        return new Token(TokenType.HEXADECIMAL_NUMBER, number, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    public boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    public Token parseOctal() {
        String number = "0e";
        currentCharacterIndex += 2; // skip over the 0x

        while (peek() != '\0' && isOctalDigit(peek())) {
            number += peek();
            currentCharacterIndex++;
        }

        return new Token(TokenType.OCTAL_NUMBER, number, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    public boolean isOctalDigit(char c) {
        return (c >= '0' && c <= '7');
    }

    public Token parseBinary() {
        String number = "0b";
        currentCharacterIndex += 2;

        while (peek() != '\0' && isBinaryDigit(peek())) {
            number += peek();
            currentCharacterIndex++;
        }

        return new Token(TokenType.BINARY_NUMBER, number, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    public boolean isBinaryDigit(char c) {
        return (c == '0' || c == '1');
    }

    public Token lexIdentifier() {
        String identifier = "";

        do {
            char currentCharacter = peek();
            identifier += currentCharacter;
            currentCharacterIndex++;
        } while (currentCharacterIndex < this.source.length()
                && (Character.isAlphabetic(peek()) || Character.isDigit(peek()) || peek() == '_'));

        if (reservedWords.containsKey(identifier)) {
            return new Token(reservedWords.get(identifier), identifier,
                    ColumnAndRow.calculate(startCharacterIndex, source));
        }

        return new Token(TokenType.ID, identifier, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    /**
     * Eats escape characters in string. Does not tokenize.
     * 
     * @return the escaped character as a string
     */
    private String escapeCharacter() {

        if (match('\\')) {
            char c = peek();
            switch (c) {
                case '\\':
                case 'n':
                case 't':
                case 'c':
                case '\'':
                case '"':
                    advance(peek());
                    return "\\" + c;

                default:
                    throw new Error("Invalid escape character");
            }
        }
        return "";
    }

    public Token lexStringLiteral(char delimiter) {
        advance(delimiter); // consume the opening "
        String str = "";

        char currentCharacter = peek();
        while (currentCharacter != delimiter) {
            if (currentCharacter == '\n')
                throw new Error("Unterminated string literal");

            str += currentCharacter;

            currentCharacterIndex++;
            str += escapeCharacter();
            currentCharacter = peek();
        }

        advance(delimiter); // consume the ending "
        return new Token(TokenType.STRING_LITERAL, str, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    // eats all the comments
    // need to loop due to the possibility of chained comments like this one
    public boolean ignoreComment() {
        char c = peek(); // for debugging
        int length = source.length();
        if (currentCharacterIndex < source.length() - 2 && peek() == '/') {
            if (peekNext() == '/') {
                advance('/');
                advance('/');
                while (hasNextToken() && peek() != '\0' && peek() != '\n') {
                    c = peek();
                    currentCharacterIndex++;
                }
                return true;
            } else if (peekNext() == '*') {
                advance('/');
                advance('*');
                while (peek() != '*' && peekNext() != '/') {
                    if (currentCharacterIndex >= source.length() - 2) {
                        throw new Error("Unterminated multiline comment");
                    }
                    currentCharacterIndex++;
                }
                c = peek();
                match('*');
                c = peek();
                match('/');
                c = peek();
                return true;
            }

        }
        return false;
    }

    // skips over every piece of whitespace
    public void ignoreWhitespace() {
        while (currentCharacterIndex < this.source.length() + 1 && Character.isWhitespace(peek())) {
            currentCharacterIndex++;
        }
    }

    // returns the character stored in currentCharacterIndex
    public char peek() {
        return currentCharacterIndex >= this.source.length() ? '\0' : this.source.charAt(currentCharacterIndex);
    }

    // returns the character stored in currentCharacterIndex + 1
    public char peekNext() {
        return currentCharacterIndex + 1 >= this.source.length() ? '\0' : this.source.charAt(currentCharacterIndex + 1);
    }

    public boolean hasNextToken() {
        return currentCharacterIndex < this.source.length();
    }

    // checks the next character and advances if expedted, otherwise it throws an
    // error
    public boolean advance(char ch) {
        if (peek() != ch)
            throw new Error("Expected " + ch + ", got " + peek());

        currentCharacterIndex++;
        return true;
    }

    // Just a boolean check for the next character
    public boolean match(char c) {
        if (peek() != c)
            return false;
        currentCharacterIndex++;
        return true;
    }

    private ColumnAndRow defaultColumnAndRow() {
        return ColumnAndRow.calculate(startCharacterIndex, source);
    }
}

class Table {
    public static HashMap<String, TokenType> generateReservedWords() {
        HashMap<String, TokenType> reservedWords = new HashMap<>();

        // Boolean Tokens
        reservedWords.put("faker", TokenType.TRUE);
        reservedWords.put("shaker", TokenType.FALSE);

        // Declaration Tokens
        reservedWords.put("item", TokenType.VARIABLE);
        reservedWords.put("rune", TokenType.CONSTANT);
        reservedWords.put("skill", TokenType.FUNCTION);
        reservedWords.put("recast", TokenType.RETURN);
        reservedWords.put("build", TokenType.OBJECT);

        // Conditional Statements
        reservedWords.put("canwin", TokenType.IF);
        reservedWords.put("remake", TokenType.ELIF);
        reservedWords.put("lose", TokenType.ELSE);
        reservedWords.put("channel", TokenType.SWITCH);
        reservedWords.put("teleport", TokenType.CASE);
        reservedWords.put("recall", TokenType.DEFAULT);
        reservedWords.put("flash", TokenType.S_GOTO);
        reservedWords.put("cancel", TokenType.S_BREAK);

        // Looping Statements
        reservedWords.put("wave", TokenType.WHILE);
        reservedWords.put("cannon", TokenType.FOR);
        reservedWords.put("clear", TokenType.BREAK);
        reservedWords.put("next", TokenType.CONTINUE);
        reservedWords.put("of", TokenType.OF);

        // Error Handling
        reservedWords.put("feed", TokenType.THROW);
        reservedWords.put("support", TokenType.TRY);
        reservedWords.put("carry", TokenType.CATCH);

        // Type Tokens
        reservedWords.put("stats", TokenType.NUMBER);
        reservedWords.put("goat", TokenType.BOOLEAN);
        reservedWords.put("message", TokenType.STRING);
        reservedWords.put("passive", TokenType.VOID);
        reservedWords.put("build", TokenType.OBJECT);
        reservedWords.put("cooldown", TokenType.NULL);

        // I/O Operations
        reservedWords.put("steal", TokenType.IMPORT);
        reservedWords.put("chat", TokenType.PRINT);
        reservedWords.put("broadcast", TokenType.INPUT);

        return reservedWords;
    }
}
