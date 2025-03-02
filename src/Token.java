
public class Token {
    final TokenType token;
    final String lexeme;
    final int column;
    final int line;

    Token(TokenType token, String lexeme, ColumnAndRow info) {
        this.token = token;
        this.lexeme = lexeme;
        this.column = info.getActualColumn();
        this.line = info.getActualRow();
    }

    @Override
    public String toString() {
        String tokenName = String.format("%20s", "<" + token.toString() + ">");
        return String.format("Token: %s | Lexeme: %10s | Line: %3d & Column: %d", tokenName, lexeme, line,
                column);
    }
}
