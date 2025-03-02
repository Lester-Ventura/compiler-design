
public class Parser {
    private String source;

    public Parser(String source) {
        this.source = source;
    }

    public void parse() {
        Lexer lexer = new Lexer(source);
        Token nextToken = null;

        while (nextToken == null || nextToken.token != TokenType.EOF) {
            nextToken = lexer.getNextToken();
            System.out.println(nextToken.toString());
        }
    }
}
