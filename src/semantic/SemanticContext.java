package semantic;

import java.util.ArrayList;
import java.util.Arrays;

import lexer.Token;
import lexer.TokenType;
import parser.StatementNode;
import parser.Node.StatementList;
import utils.Environment;
import utils.EnvironmentException;
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

  ArrayList<SemanticContext> childrens = new ArrayList<>();

  public SemanticContext cleanFunctionFork(LoLangType returnType) {
    SemanticContext ret = new SemanticContext(typeEnvironment, variableEnvironment,
        new ArrayList<>(Arrays.asList(new Scope[] { Scope.FUNCTION_BODY })), returnType, this.gotoLabels,
        this.exceptions);

    childrens.add(ret);
    return ret;
  }

  public SemanticContext fork() {
    SemanticContext ret = new SemanticContext(typeEnvironment, variableEnvironment, scopes, returnType, gotoLabels,
        exceptions);

    childrens.add(ret);
    return ret;

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

  public void printSymbolTableToParent() {
    System.out.println("Variables defined starting from this context:");
    System.out
        .println("==================================================================================================");
    System.out
        .println("| DEPTH |                 NAME                      |              TYPE               | CONSTANT |");
    System.out
        .println("==================================================================================================");
    printSymbolTableToParentR(0, this.variableEnvironment);
    System.out
        .println(
            "==================================================================================================\n");
  }

  private void printSymbolTableToParentR(int depth, Environment<LoLangType> local) {
    for (String name : local.variables.keySet()) {
      if (name.equals("dump_symbol_table"))
        continue;

      SymbolTableEntry<LoLangType> entry = local.variables.get(name);
      System.out.println(
          String.format("| %-5d | %-41s | %-31s | %-8s |", depth, name, entry.value.toString(),
              entry.constant ? "true" : "false"));
    }

    if (local.parent != null)
      printSymbolTableToParentR(depth + 1, local.parent);
  }

  public void printSymbolTableToChild() {
    System.out.println("Generated contexts starting from the root:");
    printSymbolTableToChild(0);
  }

  private void printSymbolTableToChild(int depth) {
    String spacer = "";
    for (int i = 0; i < depth; i++)
      spacer += "\t";

    System.out
        .println(
            spacer
                + "=====================================================================================================");
    System.out
        .println(
            spacer
                + "| SIBLING |                   NAME                     |              TYPE               | CONSTANT |");
    System.out
        .println(
            spacer
                + "=====================================================================================================");

    if (variableEnvironment.variables.keySet().size() > 0) {
      for (String name : variableEnvironment.variables.keySet()) {
        if (name.equals("dump_symbol_table"))
          continue;

        SymbolTableEntry<LoLangType> entry = variableEnvironment.variables.get(name);
        System.out.println(
            String.format(spacer + "| %-7s | %-42s | %-31s | %-8s |", "x", name, entry.value.toString(),
                entry.constant ? "true" : "false"));
      }
    } else {
      System.out
          .println(
              spacer
                  + "|                              NO VARIABLES LOCALLY DEFINED IN THIS CONTEXT                         |");
    }

    System.out
        .println(
            spacer
                + "=====================================================================================================");

    if (variableEnvironment.siblings.size() > 0) {
      for (int i = 0; i < variableEnvironment.siblings.size(); i++) {
        Environment<LoLangType> sibling = variableEnvironment.siblings.get(i);

        for (String name : sibling.variables.keySet()) {
          if (name.equals("dump_symbol_table"))
            continue;

          SymbolTableEntry<LoLangType> entry = sibling.variables.get(name);
          System.out.println(
              String.format(spacer + "| %-7s | %-42s | %-31s | %-8s |", i + "", name, entry.value.toString(),
                  entry.constant ? "true" : "false"));
        }
      }

      System.out
          .println(
              spacer
                  + "=====================================================================================================");
    }

    System.out.println();

    for (SemanticContext child : childrens) {
      child.printSymbolTableToChild(depth + 1);
    }
  }

  public void loadTypesFromStatementList(StatementList list) {
    for (StatementNode statement : list.statements) {
      if (statement instanceof StatementNode.ObjectTypeDeclaration) {
        StatementNode.ObjectTypeDeclaration declaration = (StatementNode.ObjectTypeDeclaration) statement;
        try {
          typeEnvironment.define(declaration.identifier.lexeme, declaration.convertToType(this), true);
        } catch (EnvironmentException.EnvironmentAlreadyDeclaredException e) {
          this.addException(
              new SemanticAnalyzerException(
                  "Cannot redeclare type \"" + declaration.identifier.lexeme + "\"",
                  declaration.identifier));
        }
      }
    }
  }
}
