import java.util.HashMap;

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
        else if (englishAlphabetCheck(nextChar) || nextChar == '_')
            return lexIdentifier();
        else if (Character.isDigit(nextChar))
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
        advance(nextChar); // still moves the scanner despite the error to avoid looping infinitely
        throw new ScannerError("Error: Unexpected character '" + nextChar + "' at Line: " + position.getActualRow()
                + ", Column: " + position.getActualColumn());
    }

    public Token peekNextToken() {
        int save = currentCharacterIndex;
        Token token = getNextToken();
        currentCharacterIndex = save;
        return token;
    }

    public Token lexOperator() {
        String lexeme = peek() + "";
        switch (peek()) {
            case '+':
                return doubleCharacterCase(lexeme, '+', TokenType.PLUS, '+', TokenType.DOUBLE_PLUS);
            case '-':
                return twoDoubleCharacterCase(lexeme, '-', TokenType.MINUS, '-', TokenType.DOUBLE_MINUS, '>',
                        TokenType.MINUS_R_ANGLE_BAR);
            case '*':
                return doubleCharacterCase(lexeme, '*', TokenType.STAR, '*', TokenType.DOUBLE_STAR);
            case '/':
                return singleCharacterCase(lexeme, '/', TokenType.FORWARD_SLASH);
            case '%':
                return singleCharacterCase(lexeme, '%', TokenType.PERCENT);
            case '<':
                return twoDoubleCharacterCase(lexeme, '<', TokenType.R_ANGLE_BAR, '<', TokenType.DOUBLE_R_ANGLE_BAR,
                        '=', TokenType.R_ANGLE_BAR_EQUALS);
            case '>':
                return twoDoubleCharacterCase(lexeme, '>', TokenType.L_ANGLE_BAR, '>', TokenType.DOUBLE_L_ANGLE_BAR,
                        '=', TokenType.L_ANGLE_BAR_EQUALS);
            case '|':
                return doubleCharacterCase(lexeme, '|', TokenType.PIPE, '|', TokenType.DOUBLE_PIPE);
            case '&':
                return doubleCharacterCase(lexeme, '&', TokenType.AMPERSAND, '&', TokenType.DOUBLE_AMPERSAND);
            case '!':
                return doubleCharacterCase(lexeme, '!', TokenType.EXCLAMATION, '=', TokenType.EXCLAMATION_EQUALS);
            case '=':
                return doubleCharacterCase(lexeme, '=', TokenType.EQUALS, '=', TokenType.DOUBLE_EQUALS);
            case '^':
                return singleCharacterCase(lexeme, '^', TokenType.CARAT);
            default:
                return null;
        }
    }

    public Token lexSymbols() {
        String lexeme = peek() + "";
        switch (peek()) {
            case '(':
                return singleCharacterCase(lexeme, '(', TokenType.L_PAREN);
            case ')':
                return singleCharacterCase(lexeme, ')', TokenType.R_PAREN);
            case '[':
                return singleCharacterCase(lexeme, '[', TokenType.L_BRACE);
            case ']':
                return singleCharacterCase(lexeme, ']', TokenType.R_BRACE);
            case ':':
                return singleCharacterCase(lexeme, ':', TokenType.COLON);
            case ';':
                return singleCharacterCase(lexeme, ';', TokenType.SEMICOLON);
            case ',':
                return singleCharacterCase(lexeme, ',', TokenType.COMMA);
            case '.':
                return singleCharacterCase(lexeme, '.', TokenType.DOT);
            case '{':
                return singleCharacterCase(lexeme, '{', TokenType.L_CURLY_BRACE);
            case '}':
                return singleCharacterCase(lexeme, '}', TokenType.R_CURLY_BRACE);
            default:
                return null;
        }
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

        return new Token(hasHitDecimal ? TokenType.FLOAT_NUMBER : TokenType.DECIMAL_NUMBER, number,
                ColumnAndRow.calculate(startCharacterIndex, source));
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
                && (englishAlphabetCheck(peek()) || Character.isDigit(peek()) || peek() == '_'));

        if (reservedWords.containsKey(identifier)) {
            return new Token(reservedWords.get(identifier), identifier,
                    ColumnAndRow.calculate(startCharacterIndex, source));
        }

        return new Token(TokenType.ID, identifier, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    public Token lexStringLiteral(char delimiter) {
        advance(delimiter); // consume the opening delimiter
        String str = "";

        char currentCharacter = peek();
        while (currentCharacter != delimiter) {

            if (currentCharacter == '\n')
                throw new ScannerError("Error: Unterminated string literal");
            else if (currentCharacter == '\0')
                throw new ScannerError("Error: Unexpected end of file");

            str += currentCharacter;

            currentCharacterIndex++;
            str += escapeCharacter();
            currentCharacter = peek();
        }

        advance(delimiter); // consume the ending "
        return new Token(TokenType.STRING_LITERAL, str, ColumnAndRow.calculate(startCharacterIndex, source));
    }

    private boolean englishAlphabetCheck(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
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
                    throw new ScannerError("Error: Invalid escape character");
            }
        }

        return "";
    }

    // eats all the comments
    // need to loop due to the possibility of chained comments like this one
    public boolean ignoreComment() {
        if (currentCharacterIndex < source.length() - 2 && peek() == '/') {
            if (peekNext() == '/') {
                advance('/');
                advance('/');
                while (hasNextToken() && peek() != '\0' && peek() != '\n') {
                    currentCharacterIndex++;
                }
                return true;
            } else if (peekNext() == '*') {
                advance('/');
                advance('*');
                while (peek() != '*' && peekNext() != '/') {
                    if (currentCharacterIndex >= source.length() - 2) {
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

    public Token singleCharacterCase(String lexeme, char singleChar, TokenType singleCharacter) {
        advance(singleChar);
        return new Token(singleCharacter, lexeme, defaultColumnAndRow());
    }

    public Token doubleCharacterCase(String lexeme, char singleChar, TokenType singleCharacter, char nextChar,
            TokenType doubleCharacter) {
        advance(singleChar);
        return (match(nextChar) ? new Token(doubleCharacter, lexeme + nextChar, defaultColumnAndRow())
                : new Token(singleCharacter, lexeme, defaultColumnAndRow()));
    }

    public Token twoDoubleCharacterCase(String lexeme, char singleChar, TokenType singleCharacter, char nextChar,
            TokenType doubleCharacter, char alternateNextChar, TokenType doubleCharacterAlternate) {
        advance(singleChar);
        if (match(nextChar))
            return new Token(doubleCharacter, lexeme, defaultColumnAndRow());
        return (match(alternateNextChar)
                ? new Token(doubleCharacterAlternate, lexeme, defaultColumnAndRow())
                : new Token(singleCharacter, lexeme, defaultColumnAndRow()));
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
            throw new ScannerError("Error: Expected " + ch + ", got " + peek());

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
        reservedWords.put("chat", TokenType.INPUT);
        reservedWords.put("broadcast", TokenType.PRINT);
        reservedWords.put("ff", TokenType.EXIT);

        return reservedWords;
    }
}
