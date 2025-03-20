import java.util.*;

public class Parser {
    private String source;
    private Map<String, Token> symbolTable = new HashMap<>();

    public Parser(String source) {
        this.source = source;
    }

    public void parse(int delay_in_ms) {
        Lexer lexer = new Lexer(source);
        Token nextToken = null;

        while (nextToken == null || nextToken.type != TokenType.EOF) {
            try {
                nextToken = lexer.getNextToken();

                if (delay_in_ms > 0) {
                    try {
                        Thread.sleep(delay_in_ms);
                    } catch (Exception e) {
                        System.out.println("Error has occured while sleeping: " + e.getMessage());
                    }
                }

                System.out.println(nextToken.toString());
                if (nextToken.type == TokenType.ID) {
                    if (!symbolTable.containsKey(nextToken.lexeme))
                        symbolTable.put(nextToken.lexeme, nextToken);
                }
            } catch (ScannerError e) {
                System.out.println("Error has occured while scanning token: " + e.getMessage());
            }
        }

        System.out.println("\n|| Symbol table || \n");
        for (Map.Entry<String, Token> entry : symbolTable.entrySet()) {
            System.out.println(entry);
        }
    }
}
