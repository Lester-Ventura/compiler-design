import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

// Thank you https://craftinginterpreters.com/scanning.html
// TODO: test file handling
public class Main {

    static boolean hadError = false;

    public static void main(String[] args) {
        Map<String, TokenType> reservedWords = new HashMap<>();

        // Boolean Tokens
        reservedWords.put("faker", TokenType.TRUE);
        reservedWords.put("shaker", TokenType.FALSE);
        // Declaration Tokens
        reservedWords.put("item", TokenType.VARIABLE);
        reservedWords.put("rune", TokenType.CONSTANT);
        reservedWords.put("skill", TokenType.FUNCTION);
        reservedWords.put("recast", TokenType.RETURN);
        reservedWords.put("build", TokenType.OBJECT);
        // Conditional Statements
        reservedWords.put("canwin", TokenType.IF);
        reservedWords.put("remake", TokenType.ELIF);
        reservedWords.put("lose", TokenType.ELSE);
        reservedWords.put("channel", TokenType.SWITCH);
        reservedWords.put("teleport", TokenType.CASE);
        reservedWords.put("recall", TokenType.DEFAULT);
        reservedWords.put("flash", TokenType.S_GOTO);
        reservedWords.put("cancel", TokenType.S_BREAK);
        // Looping Statements
        reservedWords.put("wave", TokenType.WHILE);
        reservedWords.put("cannon", TokenType.FOR);
        reservedWords.put("clear", TokenType.BREAK);
        reservedWords.put("next", TokenType.CONTINUE);
        reservedWords.put("of", TokenType.OF);
        // Error Handling
        reservedWords.put("feed", TokenType.THROW);
        reservedWords.put("support", TokenType.TRY);
        reservedWords.put("carry", TokenType.CATCH);
        // Type Tokens
        reservedWords.put("stats", TokenType.NUMBER);
        reservedWords.put("goat", TokenType.BOOLEAN);
        reservedWords.put("message", TokenType.STRING);
        reservedWords.put("passive", TokenType.VOID);
        reservedWords.put("build", TokenType.OBJECT);
        reservedWords.put("cooldown", TokenType.NULL);
        // I/O Operations
        reservedWords.put("steal", TokenType.IMPORT);
        reservedWords.put("chat", TokenType.PRINT);
        reservedWords.put("broadcast", TokenType.INPUT);

        // TODO: Move this logic to the parser
        Parser parser = new Parser();
        System.out.println("Work!");

        try {
            runPrompt(reservedWords);
        } catch (IOException e) {
            System.err.println("Error with parsing");
        }

    }

    private static void runPrompt(Map<String, TokenType> reservedWords) throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        Lexer lexter = new Lexer(reservedWords);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(lexter, line, reservedWords);
        }
    }

    private static void runFile(String path, Map<String, TokenType> reservedWords) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()), reservedWords);
        if (hadError)
            System.exit(65);
    }

    // Would probably need to change this implementation, just using this for
    // testing
    @Deprecated
    private static void run(String source, Map<String, TokenType> reservedWords) {
        Lexer lexer = new Lexer(source, reservedWords);
        lexer.lex();
        List<Token> tokens = lexer.getLastAddedTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    private static void run(Lexer lexer, String source, Map<String, TokenType> reservedWords) {
        // String sourceMultiline = """
        // item binary : stats = 0b1001010;
        // item binary: stats = 0b1001010;
        // """;
        // lexer.lex(sourceMultiline);
        lexer.lex(source);
        List<Token> tokens = lexer.getLastAddedTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int line, int column, String message) {
        report(line, column, "", message);
    }

    // TODO: ADD Column
    private static void report(int line, int column, String where,
            String message) {
        String err = String.format("[line %d :: column %d] Error %s : %s", line, column, where, message);
        System.err.println(err);
        hadError = true;
    }
}
