public class Lexer {
    
    Lexer(){

    }
    public Token getNextToken(){
        return new Token(TokenType.TRUE,"faker",0,0);
    }

}