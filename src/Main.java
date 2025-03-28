import java.util.ArrayList;

import utils.*;
import parser.*;

public class Main {
    public static void main(String[] args) {
        boolean isInteractive = args.length <= 0 || !args[0].equals("--no-interactive");

        // load the grammar file and list of productions
        LR1GrammarParser grammarParser = new LR1GrammarParser(FileLoader.loadFile("grammar.txt"));
        ArrayList<LR1GrammarParser.SLR1GrammarProduction> productions = grammarParser.parse();
        LR1TableParser tableParser = new LR1TableParser(FileLoader.loadFile("slr1_table.txt"));
        ArrayList<LR1TableParser.SLR1TableState> states = tableParser.parse();

        CreateParser parserGenerator = (String input) -> new LR1Parser(input, productions, states);
        CodeReader codeReader = new CodeReader(parserGenerator);
        codeReader.run(isInteractive);

        // String sourceCode = FileLoader.loadFile("./ExampleCodes/CustomExample.lol");
        // Node node = parser.parse();
        // System.out.println(node);

        // CodeReader reader = new CodeReader();
        // reader.run();
    }
}