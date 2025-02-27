import java.util.*;

public class Lexer {
    private String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private Map<String, TokenType> reservedWords = new HashMap<>();
    private final Set<String> identifiers = new HashSet<>(); // They all have the same type so I figured that they'd be
                                                             // redundant

    private int line = 0;
    private int start = 0;
    private int current = 0;
    private int lastAddedIndex = 0;

    // Might just choose one eventually lol
    // but rn they're flexible
    Lexer() {
    }

    Lexer(String source) {
        this.source = source;
    }

    Lexer(Map<String, TokenType> reservedWords) {
        this.reservedWords = reservedWords;
    }

    Lexer(String source, Map<String, TokenType> reservedWords) {
        this.source = source;
        this.reservedWords = reservedWords;
    }

    public void lex(String source) {
        current = 0;
        this.source = source;
        lex();
    }

    public void lex() {
        line++;
        lastAddedIndex = tokens.size();
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        // let's not add the EOF everytime para repeatable
        // tokens.add(new Token(TokenType.EOF, "", 0, line));
        // return tokens;
    }

    private void scanToken() {
        char c = advance();

        if (whiteSpace(c))
            return;

        if (misc(c))
            return;

        if (operator(c))
            return;

        if (isLetter(c) || c == '_') {
            identifier(c);
            return;
        }

        if (c == '\'' || c == '"') {
            string(c);
            return;
        }

        Main.error(current, "Token cannot be read");
    }

    // opted for functions just so it's easier to segment the code
    // the switch cases are a lot less cleaner than I would have hoped tho
    private boolean whiteSpace(char c) {
        switch (c) {
            case '\n':
                line++;
            case ' ':
            case '\r':
            case '\t':
                return true;
            default:
                return false;
        }
    }

    private boolean misc(char c) {
        return switch (c) {
            case '(' ->
                addToken(TokenType.L_PAREN);
            case ')' ->
                addToken(TokenType.R_PAREN);
            case '[' ->
                addToken(TokenType.L_BRACE);
            case ']' ->
                addToken(TokenType.R_BRACE);
            case '{' ->
                addToken(TokenType.L_CURLY_BRACE);
            case '}' ->
                addToken(TokenType.R_CURLY_BRACE);
            case ':' ->
                addToken(TokenType.COLON);
            case ';' ->
                addToken(TokenType.SEMICOLON);
            case ',' ->
                addToken(TokenType.COMMA);
            case '.' ->
                addToken(TokenType.DOT);
            default -> false;
        };
    }

    private boolean operator(char c) {
        return switch (c) {
            case '+' -> addToken(match('+') ? TokenType.DOUBLE_PLUS : TokenType.PLUS);
            case '-' -> addToken(match('-') ? TokenType.DOUBLE_MINUS : TokenType.MINUS);
            case '*' -> addToken(match('*') ? TokenType.DOUBLE_STAR : TokenType.STAR);
            case '%' -> addToken(TokenType.PERCENT);
            case '^' -> addToken(TokenType.CARAT);
            case '&' -> addToken(match('&') ? TokenType.DOUBLE_AMPERSAND : TokenType.AMPERSAND);
            case '|' -> addToken(match('|') ? TokenType.DOUBLE_PIPE : TokenType.PIPE);
            case '!' -> addToken(match('=') ? TokenType.EXCLAMATION_EQUALS : TokenType.EXCLAMATION);
            case '=' -> addToken(match('=') ? TokenType.DOUBLE_EQUALS : TokenType.EQUALS);
            case '<' -> {
                if (match('<'))
                    yield addToken(TokenType.DOUBLE_L_ANGLE_BAR);
                else if (match('='))
                    yield addToken(TokenType.L_ANGLE_BAR_EQUALS);
                else
                    yield addToken(TokenType.L_ANGLE_BAR);
            }
            case '>' -> {
                if (match('>'))
                    yield addToken(TokenType.DOUBLE_R_ANGLE_BAR);
                else if (match('='))
                    yield addToken(TokenType.R_ANGLE_BAR_EQUALS);
                else
                    yield addToken(TokenType.R_ANGLE_BAR);
            }

            case '/' -> {
                if (match('/'))
                    singleLineComment();
                else if (match('*'))
                    multiLineComment();
                else
                    addToken(TokenType.FORWARD_SLASH);
                yield true;
            }
            default -> false;
        };
    }

    private void identifier(char c) {
        while (!isAtEnd() && (isLetter(c) || isDigit(c) || peek() == '_')) {
            c = advance();
        }
        String lexeme = source.substring(start, current);
        if (reservedWords.containsKey(lexeme)) {
            addToken(reservedWords.get(lexeme));
        } else {
            addToken(TokenType.ID);
        }

    }

    private boolean stringDelimiter(char c) {
        return switch (c) {
            case '\'' -> addToken(TokenType.APOSTROPHE, "'");
            case '"' -> addToken(TokenType.QUOTATION, "\"");
            default -> false;
        };
    }

    private void string(char delimiter) {
        stringDelimiter(delimiter);
        match(delimiter); // clean up

        StringBuilder lexeme = new StringBuilder();
        boolean delimited = false;
        while (!isAtEnd()) {
            char c = advance();
            lexeme.append(c);

            if (match(delimiter)) {
                addToken(TokenType.STRING_LITERAL, lexeme.toString());
                stringDelimiter(delimiter);
                delimited = true;
                break;
            }
        }
        if (!delimited) {
            Main.error(line, "String not closed :: Expecting a closing " + delimiter);
        }

    }

    // Character Checking
    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Comments
    private void singleLineComment() {
        while (peek() != '\n' && !isAtEnd())
            advance();
    }

    private void multiLineComment() {
        // This doesn't throw an error when it reaches the end
        while (!isAtEnd() && !(match('*') && match('/'))) {
            advance();
        }
    }

    // HELPER FUNCTIONS
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * A more direct approach to adding a token into the token list. Used in strings
     * to avoid placing the delimiter
     * 
     * @param type
     * @param lexeme
     * @return
     */
    private boolean addToken(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, current, line));
        return true;
    }

    /**
     * Adds a token to the token list using its type, its lexeme is equivalent to
     * the parsed string.
     * 
     * @param type
     * @return
     */
    private boolean addToken(TokenType type) {
        String lexeme = source.substring(start, current);
        return addToken(type, lexeme);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (peek() != expected)
            return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Getters and Setters
    /**
     * Gets all the identifers present in the given source code
     * 
     * @return
     */
    public Set<String> getIdentifiers() {
        return identifiers;
    }

    /**
     * Adds a reserved word into the reserved word symbol table
     * 
     * @param lexeme
     * @param type
     */
    public void addReservedWord(String lexeme, TokenType type) {
        reservedWords.put(lexeme, type);
    }

    /**
     * Sets the given source code to be parsed
     * 
     * @param source
     */
    public void setSourceCode(String source) {
        this.source = source;
    }

    /**
     * 
     * @return
     */
    public List<Token> getAllTokensWithEOF() {
        tokens.add(new Token(TokenType.EOF, "", current, line));
        return tokens;
    }

    /**
     * Gets a list of the tokens added from the last lex
     * 
     * @return
     */
    public List<Token> getLastAddedTokens() {
        List<Token> lastAddedTokens = new ArrayList<>();
        for (int i = lastAddedIndex; i < tokens.size(); i++) {
            lastAddedTokens.add(tokens.get(i));
        }
        return lastAddedTokens;
    }
}