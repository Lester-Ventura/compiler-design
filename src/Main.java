import java.util.ArrayList;

import interpreter.Global;
import utils.*;
import parser.*;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> argsList = new ArrayList<>();
        for (int i = 0; i < args.length; i++)
            argsList.add(args[i]);

        boolean isInteractive = argsList.contains("--interactive");
        if (argsList.contains("--lenient"))
            Global.isLenient = true;

        // load the grammar file and list of productions
        LR1GrammarParser grammarParser = new LR1GrammarParser(FileLoader.loadFile("grammar.txt"));
        ArrayList<LR1GrammarParser.LR1GrammarProduction> productions = grammarParser.parse();
        LR1TableParser tableParser = new LR1TableParser(FileLoader.loadFile("lr1_table.txt"));
        ArrayList<LR1TableParser.LR1TableState> states = tableParser.parse();

        LR1Parser.parser = new LR1Parser(productions, states);
        CodeReader codeReader = new CodeReader(LR1Parser.parser);
        codeReader.run(isInteractive);
    }
}