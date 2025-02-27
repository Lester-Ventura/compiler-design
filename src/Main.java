import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

// Thank you https://craftinginterpreters.com/scanning.html
public class Main {

    static boolean hadError = false;

    public static void main(String[] args) {
        Parser parser = new Parser();
        System.out.println("Work!");

        Map<String, TokenType> reservedWords = new HashMap<>();
        reservedWords.put("faker", TokenType.TRUE);
        reservedWords.put("shaker", TokenType.FALSE);

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

    @Deprecated
    private static void run(Lexer lexer, String source, Map<String, TokenType> reservedWords) {
        lexer.lex(source);
        List<Token> tokens = lexer.getLastAddedTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
