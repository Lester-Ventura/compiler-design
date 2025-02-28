import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private Map<String, TokenType> reservedWords = new HashMap<>();
    private final Map<String, Token> identifiers = new HashMap<>(); // Initial symbol table implementation

    private int line = 0;
    private int start = 0;
    private int current = 0;
    private int lastAddedIndex = 0;
    private int col = 0;

    // Might just choose one eventually lol
    // but rn they're flexible
    Lexer() {
    }

    Lexer(String source) {
        this.source = source;
    }

    /**
     * The constructor I prefer to use, only takes in the reserved words. The source
     * code is passed in using the setter (could be repeated)
     * 
     * @param reservedWords
     */
    public Lexer(Map<String, TokenType> reservedWords) {
        this.reservedWords = reservedWords;
    }

    Lexer(String source, Map<String, TokenType> reservedWords) {
        this.source = source;
        this.reservedWords = reservedWords;
    }

    public List<Token> lex(String source) {
        current = 0;
        this.source = source;
        return lex();
    }

    public List<Token> lex() {
        lastAddedIndex = tokens.size();
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        // let's not add the EOF everytime para repeatable
        // tokens.add(new Token(TokenType.EOF, "", 0, line));
        // return tokens;
        return getLastAddedTokens();
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
            identifier();
            return;
        }

        if (c == '\'' || c == '"') {
            string(c);
            return;
        }

        if (isDigit(c)) {
            if (c == '0') {
                if (match('x'))
                    basedNumbers("[0-9a-fA-F]", TokenType.HEXADECIMAL_NUMBER);
                else if (match('e'))
                    basedNumbers("[0-7]", TokenType.OCTAL_NUMBER);
                else if (match('b'))
                    basedNumbers("[0|1]", TokenType.BINARY_NUMBER);
                else if (peek() == '.')
                    floatingPoint();
                else
                    decimalFloat();

            } else {
                // number("[0-9]", TokenType.DECIMAL_NUMBER);
                decimalFloat();
            }
            return;
        }

        Main.error(line, current, "Token cannot be read");
    }

    // opted for functions just so it's easier to segment the code
    private boolean whiteSpace(char c) {
        switch (c) {
            case '\n':
                col = 0;
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

    private void identifier() {
        while (!isAtEnd() && (isLetter(peek()) || isDigit(peek()) || peek() == '_')) {
            advance();
        }
        String lexeme = source.substring(start, current);
        // this is just more straightforward than what crafting interpreters did
        if (reservedWords.containsKey(lexeme)) {
            addToken(reservedWords.get(lexeme));
        } else {
            addToken(TokenType.ID);
            identifiers.put(lexeme, getLastToken());
        }
    }

    private void string(char delimiter) {
        // stringDelimiter(delimiter);
        // match(delimiter); // clean up
        while (!isAtEnd() && peek() != delimiter) {
            char c = advance();
            // This would eat the escape character rather than tokenize it inside the string
            if (c == '\\') {
                escapeCharacter(c);
            }
        }

        if (isAtEnd()) {
            Main.error(line, current, "String not closed :: Expecting a closing [ " + delimiter + " ] token");
            return;
        }
        advance();
        String lexeme = source.substring(start + 1, current - 1);

        addToken(TokenType.STRING_LITERAL, lexeme.toString());
    }

    /**
     * Tokenizes a string of digits into either a decimal or a floating point (if it
     * has a . in between).
     * 
     * NOTE: 5. is tokenized as <DECIMAL> <DOT> and is not a valid floating point
     */
    private void decimalFloat() {
        while (isDigit(peek()))
            advance();

        if (peek() == '.' && isDigit(peekNext())) {
            floatingPoint();
        } else {
            addToken(TokenType.DECIMAL_NUMBER);
        }

    }

    /**
     * Abstraction of the floating point scanner, so that it could be reused in the
     * 0 check
     */
    private void floatingPoint() {
        if (peek() == '.') {
            advance();
            while (isDigit(peek()))
                advance();

            addToken(TokenType.FLOAT_NUMBER);
        }

    }

    /**
     * Tokenizes a given string if it's a valid number
     * 
     * @param regexRange : the range of possible values of a given number system in
     *                   regex e.g. for
     *                   octal it's [0-7]
     * @param type       : the token type
     */
    private boolean basedNumbers(String regexRange, TokenType type) {
        Pattern pattern = Pattern.compile(regexRange, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(peek() + "");

        // Just a guard clause so that I could duplicate the regex
        if (!matcher.find()) {
            String message = String.format("Invalid %s", type.toString());
            Main.error(line, current, message);
            return false;
        }

        do {
            advance();
            matcher = pattern.matcher(peek() + "");
        } while (matcher.find());

        // if (peek() == '.') {
        // // consumes the token
        // return floatingPoint(regexRange, type);
        // }
        addToken(type);
        return true;
    }

    // Additional String Stuff

    // This would be handled by the semantic analyzer to avoid unwanted tokens in
    // the lex
    private boolean escapeCharacter(char c) {
        // I could make this a HashSet and just do a fast check there but this is a
        // constant sized array
        final char[] escapeCharacters = { '\\', '\'', '"', 'f', 'n', 't', 'c' };

        if (c == '\\') {
            for (char esc : escapeCharacters) {
                if (match(esc)) {
                    return true;
                }
            }
            Main.error(line, current, "Invalid escape character");
            return false;
        } else {
            // error free
            return false;
        }
    }

    // Character Checking
    /**
     * Checks if a character is a letter a-z or A-Z
     * 
     * @param c
     * @return
     */
    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * Shorthand for checking if the character is a decimal number [0-9]
     * 
     * @param c
     * @return
     */
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
        col++;
        return source.charAt(current++);
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

    /**
     * A more direct approach to adding a token into the token list. Used in strings
     * to avoid placing the delimiter
     * 
     * @param type
     * @param lexeme
     * @return
     */
    private boolean addToken(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, col, line));
        return true;
    }

    /**
     * Lookahead + eat
     * 
     * @param expected
     * @return
     */
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (peek() != expected)
            return false;

        col++;
        current++;
        return true;
    }

    /**
     * 1 character lookahead, checks the next character
     * 
     * @return
     */
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    /**
     * 2 character lookahead, used in floating points similar to what was
     * done in crafting interpreters
     * 
     * @return
     */
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Checks if the scanner is at the end of the given source code
     * 
     * @return true if at the end of the source code
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Getters and Setters

    /**
     * Gets all the identifers present in the given source code
     * 
     * @return
     */
    public Map<String, Token> getIdentifiers() {
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
     * @return gets the entire token list, also appends the EOF token in the end
     */
    public List<Token> getAllTokensWithEOF() {
        tokens.add(new Token(TokenType.EOF, "", current, line));
        return tokens;
    }

    /**
     * @return, Gets a list of the tokens added from the last lex. Does not add an
     * EOF token
     */
    public List<Token> getLastAddedTokens() {
        List<Token> lastAddedTokens = new ArrayList<>();
        for (int i = lastAddedIndex; i < tokens.size(); i++) {
            lastAddedTokens.add(tokens.get(i));
        }
        return lastAddedTokens;
    }

    /**
     * 
     * @return the last token added to the token list
     */
    public Token getLastToken() {
        return tokens.get(tokens.size() - 1);
    }

    // Deprecated stuff

    /**
     * Old floating point implementation, checks if a given number (any base) is a
     * floating point
     * 
     * @param regexRange
     * @param type       : specifies the type of the returned token
     * @return
     */
    @Deprecated
    private boolean floatingPoint(String regexRange, TokenType type) {
        Pattern pattern = Pattern.compile(regexRange, Pattern.CASE_INSENSITIVE);
        Matcher matcher;
        if (peek() == '.') {
            advance();
            matcher = pattern.matcher(peek() + "");
            if (!matcher.find()) {
                Main.error(line, current, "Invalid number");
                return false;
            }

            // if it doesn't match after this then we can assume that it's a different
            // string
            do {
                advance();
                matcher = pattern.matcher(peek() + "");
            } while (matcher.find());

            addToken(type, source.substring(start, current));
            return true;
        }
        return false;

    }

    // been experimenting if we should tokenize but I don't think we should
    @Deprecated
    private boolean stringDelimiter(char c) {
        return switch (c) {
            case '\'' -> addToken(TokenType.APOSTROPHE, "'");
            case '"' -> addToken(TokenType.QUOTATION, "\"");
            default -> false;
        };
    }
}