import java.util.HashSet;

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
        HashSet<TokenType> tokenConstantList = new HashSet<>();
        tokenConstantList.add(TokenType.STRING_LITERAL);
        tokenConstantList.add(TokenType.TRUE);
        tokenConstantList.add(TokenType.FALSE);
        tokenConstantList.add(TokenType.DECIMAL_NUMBER);
        tokenConstantList.add(TokenType.FLOAT_NUMBER);
        tokenConstantList.add(TokenType.BINARY_NUMBER);
        tokenConstantList.add(TokenType.HEXADECIMAL_NUMBER);
        tokenConstantList.add(TokenType.OCTAL_NUMBER);
        String tokenName = String.format("%20s", "<" + token.toString() + ">");
        if(this.token == TokenType.ID)
            return String.format("Token: %s | Lexeme: %10s | Line: %3d & Column: %d", tokenName, lexeme, line,
                column);
        else if(tokenConstantList.contains(token))
            return String.format("Token: %s | Value: %10s | Line: %3d & Column: %d", tokenName, lexeme, line,
                column);
        else
        return String.format("Token: %s | Line: %3d & Column: %d\"",tokenName, line,
        column);
    }
}
