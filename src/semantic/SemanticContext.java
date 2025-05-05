package semantic;

import java.util.ArrayList;
import java.util.Arrays;

import lexer.Token;
import lexer.TokenType;
import parser.StatementNode;
import parser.Node.StatementList;
import utils.Environment;
import utils.Environment.SymbolTableEntry;

public class SemanticContext {
  public enum Scope {
    FUNCTION_BODY, SWITCH_CASE_BODY, LOOP_BODY,
  }

  public static class GotoLabel {
    public String stringLabel = null;
    public int intLabel = -1;

    public GotoLabel(Token token) {
      if (token.type == TokenType.STRING_LITERAL)
        this.stringLabel = token.lexeme;
      else if (token.type == TokenType.NUMBER_LITERAL)
        this.intLabel = Integer.parseInt(token.lexeme);
    }
  }

  public Environment<LoLangType> typeEnvironment = new Environment<LoLangType>();
  public Environment<LoLangType> variableEnvironment = new Environment<LoLangType>();

  public final ArrayList<Scope> scopes;
  public final LoLangType returnType;
  public final ArrayList<GotoLabel> gotoLabels;

  public final ArrayList<SemanticAnalyzerException> exceptions;

  @SuppressWarnings("unchecked")
  public SemanticContext(Environment<LoLangType> parentTypeEnvironment,
      Environment<LoLangType> parentVariableEnvironment, ArrayList<Scope> scopes, LoLangType returnType,
      ArrayList<GotoLabel> gotoLabels, ArrayList<SemanticAnalyzerException> exceptions) {
    this.typeEnvironment = new Environment<LoLangType>(parentTypeEnvironment);
    this.variableEnvironment = new Environment<LoLangType>(parentVariableEnvironment);
    this.scopes = (ArrayList<Scope>) scopes.clone();
    this.returnType = returnType;
    this.gotoLabels = (ArrayList<GotoLabel>) gotoLabels.clone();
    this.exceptions = exceptions;
  }

  public SemanticContext() {
    this.returnType = null;
    this.scopes = new ArrayList<>();
    this.gotoLabels = new ArrayList<>();
    this.exceptions = new ArrayList<>();
  }

  public SemanticContext cleanFunctionFork(LoLangType returnType) {
    return new SemanticContext(typeEnvironment, variableEnvironment,
        new ArrayList<>(Arrays.asList(new Scope[] { Scope.FUNCTION_BODY })), returnType, this.gotoLabels,
        this.exceptions);
  }

  public SemanticContext fork() {
    return new SemanticContext(typeEnvironment, variableEnvironment, scopes, returnType, gotoLabels, exceptions);
  }

  public void pushScope(Scope scope) {
    if (isInScope(scope))
      return;

    this.scopes.add(scope);
  }

  public boolean isInScope(Scope scope) {
    for (Scope s : this.scopes)
      if (s == scope)
        return true;

    return false;
  }

  public void addException(SemanticAnalyzerException exception) {
    if (this.exceptions != null)
      this.exceptions.add(exception);
  }

  public void print() {
    System.out.println("Symbol Table:");
    System.out
        .println("==================================================================================================");
    System.out
        .println("| DEPTH |                 NAME                      |              TYPE               | CONSTANT |");
    System.out
        .println("==================================================================================================");

    printHere(0, this.variableEnvironment);

    System.out
        .println("==================================================================================================");
  }

  private void printHere(int depth, Environment<LoLangType> local) {
    for (String name : local.variables.keySet()) {
      if (name.equals("dump_symbol_table"))
        continue;

      SymbolTableEntry<LoLangType> entry = local.variables.get(name);
      System.out.println(
          String.format("| %-5d | %-41s | %-31s | %-8s |", depth, name, entry.value.toString(),
              entry.constant ? "true" : "false"));
    }

    if (local.parent != null)
      printHere(depth + 1, local.parent);
  }

  public void loadTypesFromStatementList(StatementList list) {
    for (StatementNode statement : list.statements) {
      if (statement instanceof StatementNode.ObjectTypeDeclaration) {
        StatementNode.ObjectTypeDeclaration declaration = (StatementNode.ObjectTypeDeclaration) statement;
        typeEnvironment.define(declaration.identifier.lexeme, declaration.convertToType(this), true);
      }
    }
  }
}
