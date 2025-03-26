import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        boolean isInteractive = args.length <= 0 || !args[0].equals("--no-interactive");

        // load the grammar file and list of productions
        SLR1GrammarParser grammarParser = new SLR1GrammarParser(FileLoader.loadFile("grammar.txt"));
        ArrayList<SLR1GrammarParser.SLR1GrammarProduction> productions = grammarParser.parse();
        SLR1TableParser tableParser = new SLR1TableParser(FileLoader.loadFile("slr1_table.txt"));
        ArrayList<SLR1TableParser.SLR1TableState> states = tableParser.parse();

        SLR1Parser parser = new SLR1Parser(productions, states);
        CodeReader codeReader = new CodeReader(parser);
        codeReader.run(isInteractive);

        // String sourceCode = FileLoader.loadFile("./ExampleCodes/CustomExample.lol");
        // Node node = parser.parse();
        // System.out.println(node);

        // CodeReader reader = new CodeReader();
        // reader.run();
    }
}