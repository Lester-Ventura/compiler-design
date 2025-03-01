
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
        String tokenName = String.format("%20s", "<" + token.toString() + ">");
        return String.format("Token: %s | Lexeme: %10s | Line: %3d & Column: %d", tokenName, lexeme, line,
                column);
    }
}
