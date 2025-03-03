import java.util.*;

public class Parser {
    private String source;
    private Map<String, Token> symbolTable = new HashMap<>();

    public Parser(String source) {
        this.source = source;
    }

    public void parse() {
        Lexer lexer = new Lexer(source);
        Token nextToken = null;

        while (nextToken == null || nextToken.token != TokenType.EOF) {
            try {
                nextToken = lexer.getNextToken();
                System.out.println(nextToken.toString());
                if (nextToken.token == TokenType.ID) {
                    symbolTable.put(nextToken.lexeme, nextToken);
                }
            } catch (ScannerError e) {
                Main.handleError(e);
            }

        }

        System.out.println("\n|| Symbol table || \n");
        for (Map.Entry<String, Token> entry : symbolTable.entrySet()) {
            System.out.println(entry);
        }
    }

    public void parseDelayed(int mili) throws InterruptedException {
        Lexer lexer = new Lexer(source);
        Token nextToken = null;
        while (nextToken == null || nextToken.token != TokenType.EOF) {
            try {
                nextToken = lexer.getNextToken();
                Thread.sleep(mili);
                System.out.println(nextToken.toString());
                if (nextToken.token == TokenType.ID) {
                    symbolTable.put(nextToken.lexeme, nextToken);
                }
            } catch (ScannerError e) {
                Main.handleError(e);
            }
        }
        System.out.println("\n|| Symbol table || \n");
        for (Map.Entry<String, Token> entry : symbolTable.entrySet()) {
            System.out.println(entry);
        }
    }
}
