
public class Token {
    final TokenType token;
    final String lexeme;
    final int column;
    final int line;

    Token(TokenType token,String lexeme,int column,int line){
        this.token = token;
        this.lexeme = lexeme;
        this.column = column;
        this.line = line;
    }
    @Override
    public String toString(){
        return ("Token: "+token.toString()+" |Lexeme: "+lexeme+" |Line&Column: "+column+"& "+line);
    }
}
