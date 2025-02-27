
public class Token {
    final TokenType token;
    final String lexeme;
    final int column;
    final int line;

    Token(TokenType token, String lexeme, int column, int line) {
        this.token = token;
        this.lexeme = lexeme;
        this.column = column;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format("Token: <%s> | Lexeme: %s | Line: %d & Column: %d", token.toString(), lexeme, line,
                column);
    }
}
