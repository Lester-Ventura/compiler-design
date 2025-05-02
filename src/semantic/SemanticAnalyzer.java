package semantic;

import interpreter.Global;
import parser.StatementNode.Program;

public class SemanticAnalyzer {
  Program program;

  public SemanticAnalyzer(Program program) {
    this.program = program;
  }

  public void analyze() {
    SemanticContext context = Global.createGlobalSemanticContext();
    program.semanticAnalysis(context);
  }
}
