package lexer;

import java.util.HashSet;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int column;
    public final int line;
    public final String sourcePath;

    public Token(TokenType token, String lexeme, ColumnAndRow info, String sourcePath) {
        this.type = token;
        this.lexeme = lexeme;
        this.column = info.getActualColumn();
        this.line = info.getActualRow();
        this.sourcePath = sourcePath;
    }

    public Token(TokenType token, String lexeme) {
        this(token, lexeme, new ColumnAndRow(0, 0), null);
    }

    @Override
    public String toString() {
        HashSet<TokenType> tokenConstantList = new HashSet<>();
        tokenConstantList.add(TokenType.STRING_LITERAL);
        tokenConstantList.add(TokenType.BOOLEAN_LITERAL);
        tokenConstantList.add(TokenType.NUMBER_LITERAL);
        tokenConstantList.add(TokenType.NULL_LITERAL);

        String tokenName = String.format("%20s", "<" + type.toString() + ">");
        if (this.type == TokenType.IDENTIFIER)
            return String.format("Token: %s | Lexeme: %10s | Line: %3d & Column: %d", tokenName, lexeme, line,
                    column);
        else if (tokenConstantList.contains(type))
            return String.format("Token: %s | Value: %10s | Line: %3d & Column: %d", tokenName, lexeme, line,
                    column);
        else
            return String.format("Token: %s | Line: %3d & Column: %d", tokenName, line,
                    column);
    }
}
