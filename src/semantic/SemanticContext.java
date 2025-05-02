package semantic;

import java.util.ArrayList;
import java.util.Arrays;

import interpreter.Environment;
import lexer.Token;
import lexer.TokenType;
import parser.StatementNode;
import parser.Node.StatementList;

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

  public SemanticContext(Environment<LoLangType> parentTypeEnvironment,
      Environment<LoLangType> parentVariableEnvironment,
      ArrayList<Scope> scopes, LoLangType returnType, ArrayList<GotoLabel> gotoLabels) {
    this.typeEnvironment = new Environment<LoLangType>(parentTypeEnvironment);
    this.variableEnvironment = new Environment<LoLangType>(parentVariableEnvironment);
    this.scopes = (ArrayList<Scope>) scopes.clone();
    this.returnType = returnType;
    this.gotoLabels = (ArrayList<GotoLabel>) gotoLabels.clone();
  }

  public SemanticContext() {
    this.returnType = null;
    this.scopes = new ArrayList<>();
    this.gotoLabels = new ArrayList<>();
  }

  public SemanticContext cleanFunctionFork(LoLangType returnType) {
    return new SemanticContext(typeEnvironment, variableEnvironment,
        new ArrayList<>(Arrays.asList(new Scope[] { Scope.FUNCTION_BODY })), returnType, this.gotoLabels);
  }

  public SemanticContext fork() {
    return new SemanticContext(typeEnvironment, variableEnvironment, scopes, returnType, gotoLabels);
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

  public void loadTypesFromStatementList(StatementList list) {
    for (StatementNode statement : list.statements) {
      if (statement instanceof StatementNode.ObjectTypeDeclaration) {
        StatementNode.ObjectTypeDeclaration declaration = (StatementNode.ObjectTypeDeclaration) statement;
        typeEnvironment.define(declaration.identifier.lexeme, declaration.convertToType(this), true);
      }
    }
  }
}
