package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

import interpreter.ExecutionContext;
import interpreter.Global;
import interpreter.RuntimeError;
import interpreter.LoLangThrowable;
import interpreter.LoLangValue;
import lexer.Token;
import lexer.TokenType;
import parser.Node.SwitchCaseList.SwitchCase;
import semantic.LoLangType;
import semantic.SemanticAnalyzerException;
import semantic.SemanticContext;
import semantic.SemanticContext.Scope;
import utils.Caster;
import utils.DOTGenerator;
import utils.EnvironmentException.EnvironmentAlreadyDeclaredException;
import utils.ErrorWindowBuilder;

public abstract class StatementNode extends Node {
  public abstract void execute(ExecutionContext context, ExecutionContext dynamicContext);

  abstract void semanticAnalysis(SemanticContext context);

  public static class Program extends StatementNode {
    StatementList statements;

    Program(StatementList statements) {
      this.statements = statements;
    }

    Program() {
      this.statements = new StatementList();
    }

    public String toString() {
      String statementsString = String.join("\n", this.statements.toString());
      return String.format("[Program: %s]", statementsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Program");
      this.statements.toDot(builder);
      builder.addEdge(this.hashCode(), this.statements.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      for (StatementNode statement : this.statements.statements)
        statement.execute(context, dynamicContext);
    }

    public void semanticAnalysis(SemanticContext context) {
      context.loadTypesFromStatementList(this.statements);

      for (StatementNode statement : this.statements.statements)
        statement.semanticAnalysis(context);
    }
  }

  public static class Import extends StatementNode {
    public static abstract class ImportException extends Exception {
      public abstract RuntimeError toRuntimeError(Token token, Path path);

      public abstract SemanticAnalyzerException toSemanticAnalyzerException(Token token, Path path);
    }

    public static class ImportNonExistentException extends ImportException {
      public RuntimeError toRuntimeError(Token token, Path path) {
        return new RuntimeError(String.format("Cannot find file \"%s\"", path.toAbsolutePath()), token);
      }

      public SemanticAnalyzerException toSemanticAnalyzerException(Token token, Path path) {
        return new SemanticAnalyzerException(String.format("Cannot find file \"%s\"", path.toAbsolutePath()),
            token);
      }
    }

    public static class ImportParsingFailedException extends ImportException {
      ArrayList<ParserException> errors;

      public ImportParsingFailedException(ArrayList<ParserException> errors) {
        super();
        this.errors = errors;
      }

      public RuntimeError toRuntimeError(Token token, Path path) {
        return new RuntimeError(String.format("Failed to parse \"%s\"", path.toAbsolutePath()), token);
      }

      public SemanticAnalyzerException toSemanticAnalyzerException(Token token, Path path) {
        return new SemanticAnalyzerException(String.format("Failed to parse \"%s\"", path.toAbsolutePath()),
            token);
      }
    }

    Token token;

    Import(Token token) {
      this.token = token;
    }

    public String toString() {
      return String.format("[Import: %s]", this.token.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Import [lexeme=" + this.token.lexeme.replace("\"", "\'") + "]");
    }

    private Path getPath() {
      // get directory of this.token.sourcePath
      return Paths.get(Paths.get(this.token.sourcePath).getParent().toString(), this.token.lexeme);
    }

    private ParserResult getSyntaxTree() throws ImportException {
      File file = getPath().toFile();

      if (!file.exists())
        throw new ImportNonExistentException();

      try {
        Scanner scanner = new Scanner(file);
        String source = scanner.hasNext() ? scanner.useDelimiter("\\Z").next() + "\n" : "\n";
        scanner.close();

        ParserResult result = LR1Parser.parser.parse(source, file.getPath());
        if (result.root == null)
          throw new ImportParsingFailedException(result.errors);

        return result;
      } catch (FileNotFoundException e) {
        throw new ImportNonExistentException();
      }
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      Path path = getPath();

      try {
        ParserResult result = getSyntaxTree();

        if (result.errors.size() != 0) {
          System.out.println(
              String.format("The following errors were encountered during parsing %s:\n", path.toAbsolutePath()));
          ErrorWindowBuilder.printErrors(result.errors);
        }

        StatementNode.Program program = (StatementNode.Program) result.root;
        ExecutionContext newContext = Global.createGlobalExecutionContext();
        program.execute(newContext, dynamicContext);
        context.environment.siblings.add(newContext.environment);
      } catch (ImportException e) {
        if (e instanceof ImportParsingFailedException) {
          System.out.println("Failed to parse file " + path.toAbsolutePath());
          ArrayList<ParserException> errors = ((ImportParsingFailedException) e).errors;

          if (errors.size() != 0) {
            System.out.println(
                String.format("The following errors were encountered during parsing %s:\n", path.toAbsolutePath()));
            ErrorWindowBuilder.printErrors(errors);
          }
        }

        throw e.toRuntimeError(token, path);
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      Path path = getPath();

      try {
        ParserResult result = getSyntaxTree();

        if (result.errors.size() != 0) {
          System.out.println(
              String.format("The following errors were encountered during parsing %s:\n", path.toAbsolutePath()));
          ErrorWindowBuilder.printErrors(result.errors);
        }

        StatementNode.Program program = (StatementNode.Program) result.root;
        SemanticContext newContext = Global.createGlobalSemanticContext();
        program.semanticAnalysis(newContext);

        context.typeEnvironment.siblings.add(newContext.typeEnvironment);
        context.variableEnvironment.siblings.add(newContext.variableEnvironment);
      } catch (ImportException e) {
        if (e instanceof ImportParsingFailedException) {
          System.out.println("Failed to parse file " + path.toAbsolutePath());
          ArrayList<ParserException> errors = ((ImportParsingFailedException) e).errors;

          if (errors.size() != 0) {
            System.out.println(
                String.format("The following errors were encountered during parsing %s:\n", path.toAbsolutePath()));
            ErrorWindowBuilder.printErrors(errors);
          }
        }

        context.exceptions.add(e.toSemanticAnalyzerException(this.token, path));
      }
    }
  }

  public static class If extends StatementNode {
    Node.IfStatementBranches branches;
    StatementNode elseBody;

    If(Node.IfStatementBranches branches, StatementNode elseBody) {
      this.elseBody = elseBody;
      this.branches = branches;
    }

    If(Node.IfStatementBranches branches) {
      this.branches = branches;
      this.elseBody = null;
    }

    public String toString() {
      if (this.elseBody == null) {
        return String.format("[If: %s]", this.branches.toString());
      }

      return String.format("[If: %s %s]", this.branches.toString(),
          this.elseBody.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "If");
      this.branches.toDot(builder);
      builder.addEdge(this.hashCode(), this.branches.hashCode());

      if (this.elseBody != null) {
        this.elseBody.toDot(builder);
        builder.addEdge(this.hashCode(), this.elseBody.hashCode());
      }
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      ExecutionContext forkedContext = context.fork();

      for (IfStatementBranch branch : this.branches.clauses) {
        // Evaluate the condition inside the branch
        LoLangValue condition = branch.condition.evaluate(context, dynamicContext);
        boolean conditionRaw = false;

        try {
          conditionRaw = Caster.toBoolean(condition);
        } catch (Caster.CastingException err) {
          throw new RuntimeError("If condition should be a boolean", branch.conditionToken);
        }

        if (!conditionRaw)
          continue;

        // Execute the body of the branch
        branch.body.execute(forkedContext, dynamicContext);
        return;
      }

      if (this.elseBody != null)
        this.elseBody.execute(context, dynamicContext);
    }

    public void semanticAnalysis(SemanticContext context) {
      for (IfStatementBranch branch : this.branches.clauses) {
        LoLangType conditionType = branch.condition.evaluateType(context);
        if (!(new LoLangType.Boolean().isEquivalent(conditionType) || Caster.toBooleanType(conditionType))) {
          context.addException(
              new SemanticAnalyzerException("Condition must be a boolean, received: " + conditionType.toString(),
                  branch.conditionToken));
        }

        SemanticContext forkedContext = context.fork();
        branch.body.semanticAnalysis(forkedContext);
      }

      if (this.elseBody != null)
        this.elseBody.semanticAnalysis(context);
    }
  }

  public static class VariableDeclaration extends StatementNode {
    Node.VariableDeclaration declaration;

    VariableDeclaration(Node.VariableDeclaration declaration) {
      this.declaration = declaration;
    }

    public String toString() {
      return this.declaration.toString();
    }

    public void toDot(DOTGenerator builder) {
      this.declaration.toDot(builder);
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      try {
        this.declaration.addToContext(context, dynamicContext);
      } catch (EnvironmentAlreadyDeclaredException e) {
        throw new RuntimeError("Cannot redeclare variable \"" + this.declaration.identifier.lexeme + "\"",
            this.declaration.identifier);
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      LoLangType declarationType = this.declaration.type.evaluate(context);

      try {
        context.variableEnvironment.define(this.declaration.identifier.lexeme, declarationType,
            false);
      } catch (EnvironmentAlreadyDeclaredException e) {
        context.exceptions.add(
            new SemanticAnalyzerException("Cannot redeclare variable \"" + this.declaration.identifier.lexeme + "\"",
                this.declaration.identifier));
      }

      if (this.declaration.expression != null) {
        LoLangType expressionType = this.declaration.expression.evaluateType(context);
        if (!declarationType.isEquivalent(expressionType)) {
          if (this.declaration.equalsToken == null)
            throw new Error("INVARIANT VIOLATION: equalsToken should not be null");

          context.addException(new SemanticAnalyzerException(
              String.format("Type of expression (%s) does not match declared type (%s)", expressionType.toString(),
                  declarationType.toString()),
              this.declaration.equalsToken));
        }
      }
    }
  }

  public static class ConstantDeclaration extends StatementNode {
    Token identifier;
    TypeExpressionNode type;
    ExpressionNode expression;

    ConstantDeclaration(Token identifier, TypeExpressionNode type, ExpressionNode expression) {
      this.identifier = identifier;
      this.type = type;
      this.expression = expression;
    }

    public String toString() {
      return String.format("[ConstantDeclaration: [Name=%s] [Type=%s] [Init=%s]]",
          this.identifier.lexeme.replace("\"", "\'"),
          this.type.toString(), this.expression.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "ConstantDeclaration [lexeme=" + this.identifier.lexeme.replace("\"", "\'") + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());

      if (this.expression != null) {
        this.expression.toDot(builder);
        builder.addEdge(this.hashCode(), this.expression.hashCode());
      }
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      try {
        context.environment.define(this.identifier.lexeme, this.expression.evaluate(context, dynamicContext), true);
      } catch (EnvironmentAlreadyDeclaredException e) {
        throw new RuntimeError("Cannot redeclare constant \"" + this.identifier.lexeme + "\"", this.identifier);
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      try {
        context.variableEnvironment.define(this.identifier.lexeme, this.expression.evaluateType(context), true);
      } catch (EnvironmentAlreadyDeclaredException e) {
        context.exceptions.add(
            new SemanticAnalyzerException("Cannot redeclare constant \"" + this.identifier.lexeme + "\"",
                this.identifier));
      }
    }
  }

  public static class Block extends StatementNode {
    Node.StatementList statements;

    Block(Node.StatementList statements) {
      this.statements = statements;
    }

    Block() {
      this.statements = new Node.StatementList();
    }

    public String toString() {
      String statementsString = String.join("\n", this.statements.toString());
      return String.format("[Block: %s]", statementsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Block");
      this.statements.toDot(builder);
      builder.addEdge(this.hashCode(), this.statements.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      ExecutionContext forkedContext = context.fork();

      for (StatementNode statement : this.statements.statements)
        statement.execute(forkedContext, dynamicContext);
    }

    public void semanticAnalysis(SemanticContext context) {
      SemanticContext forkedContext = context.fork();
      forkedContext.loadTypesFromStatementList(this.statements);

      for (StatementNode statement : this.statements.statements)
        statement.semanticAnalysis(forkedContext);
    }
  }

  public static class Return extends StatementNode {
    Token returnToken;
    ExpressionNode expression;

    Return(Token returnToken, ExpressionNode expression) {
      this.expression = expression;
      this.returnToken = returnToken;
    }

    Return(Token returnToken) {
      this.returnToken = returnToken;
      this.expression = null;
    }

    public String toString() {
      return this.expression != null ? String.format("[Return: %s]", this.expression.toString()) : "[Return]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Return");

      if (this.expression != null) {
        this.expression.toDot(builder);
        builder.addEdge(this.hashCode(), this.expression.hashCode());
      }
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      throw new LoLangThrowable.Return(
          this.expression != null ? this.expression.evaluate(context, dynamicContext) : null, this.returnToken);
    }

    public void semanticAnalysis(SemanticContext context) {
      if (!context.isInScope(Scope.FUNCTION_BODY)) {
        context.addException(new SemanticAnalyzerException("Return statement is not allowed outside of function body",
            this.returnToken));
        return;
      }

      LoLangType expectedReturnType = context.returnType;

      if (expectedReturnType == null) {
        if (this.expression != null)
          context.addException(new SemanticAnalyzerException("Return statement must not contain a return value",
              this.returnToken));

        return;
      }

      if (this.expression == null) {
        context.addException(new SemanticAnalyzerException(
            "Return statement must contain a return value of type " + expectedReturnType.toString(),
            this.returnToken));

        return;
      }

      LoLangType expressionReturnType = this.expression.evaluateType(context);
      if (!expectedReturnType.isEquivalent(expressionReturnType)) {
        context.addException(new SemanticAnalyzerException(
            "Return value must be of type " + expectedReturnType.toString() + ", received: "
                + expressionReturnType.toString(),
            this.returnToken));
      }
    }
  }

  public static class TryCatch extends StatementNode {
    StatementNode body;
    Token identifier;
    StatementNode catchBody;

    TryCatch(StatementNode body, Token identifier, StatementNode catchBody) {
      this.body = body;
      this.identifier = identifier;
      this.catchBody = catchBody;
    }

    public String toString() {
      return String.format("[TryCatch: %s %s %s]", this.body.toString(), this.identifier, this.catchBody.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "TryCatch");
      this.body.toDot(builder);
      builder.addEdge(this.hashCode(), this.body.hashCode());

      builder.addNode(this.hashCode(), String.format("TryCatch [identifier=%s]", this.identifier));
      this.catchBody.toDot(builder);
      builder.addEdge(this.hashCode(), this.catchBody.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      try {
        ExecutionContext forkedContext = context.fork();
        this.body.execute(forkedContext, dynamicContext);
      } catch (LoLangThrowable.Error errorException) {
        ExecutionContext forkedContext = context.fork();
        forkedContext.environment.tryDefine(identifier.lexeme, new LoLangValue.String(errorException.token.lexeme),
            true);
        this.catchBody.execute(forkedContext, dynamicContext);
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      SemanticContext forkedContext = context.fork();
      this.body.semanticAnalysis(forkedContext);

      SemanticContext forkedCatchContext = context.fork();
      forkedCatchContext.variableEnvironment.tryDefine(this.identifier.lexeme, new LoLangType.String(), true);

      this.catchBody.semanticAnalysis(forkedCatchContext);
    }
  }

  public static class Throw extends StatementNode {
    Token errorMessasge;

    Throw(Token errorMessasge) {
      this.errorMessasge = errorMessasge;
    }

    public String toString() {
      return String.format("[Throw: %s]", this.errorMessasge.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Throw [errorMessage=" + this.errorMessasge.lexeme.replace("\"", "\'") + "]");
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      throw new LoLangThrowable.Error(errorMessasge);
    }

    public void semanticAnalysis(SemanticContext context) {
      // no static analysis is required for throw statements
    }
  }

  public static class Switch extends StatementNode {
    SwitchCaseList cases;
    ExpressionNode expr;
    Token leftParenToken;

    Switch(ExpressionNode expr, SwitchCaseList cases, Token leftParenToken) {
      this.expr = expr;
      this.cases = cases;
      this.leftParenToken = leftParenToken;
    }

    Switch(ExpressionNode expr, Token leftParenToken) {
      this.expr = expr;
      this.cases = new SwitchCaseList();
      this.leftParenToken = leftParenToken;
    }

    public String toString() {
      return String.format("[Switch: %s %s]", this.expr.toString(), this.cases.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Switch");
      this.expr.toDot(builder);
      builder.addEdge(this.hashCode(), this.expr.hashCode());

      this.cases.toDot(builder);
      builder.addEdge(this.hashCode(), this.cases.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue value = this.expr.evaluate(context, dynamicContext);
      if (!(value instanceof LoLangValue.Number) && !(value instanceof LoLangValue.String)) {
        throw new RuntimeError("Switch expression value be a number or string", this.leftParenToken);
      }

      for (SwitchCase caseNode : this.cases.namedCases) {
        if (isValueEqualWithToken(value, caseNode.literal)) {
          executeStatementsWithBreak(caseNode.statements, context, dynamicContext);
          return;
        }
      }

      // if no case matched, execute default case
      if (this.cases.defaultCase != null)
        executeStatementsWithBreak(this.cases.defaultCase.statements, context, dynamicContext);
    }

    void executeStatements(Block statements, ExecutionContext context, ExecutionContext dynamicContext) {
      try {
        statements.execute(context, dynamicContext);
      } catch (LoLangThrowable.SwitchGoto gotoException) {
        for (SwitchCase caseNode : this.cases.namedCases) {
          if (isValueEqualWithToken(gotoException.label, caseNode.literal)) {
            executeStatements(caseNode.statements, context, dynamicContext);
            break;
          }
        }

        // if no case matched, we should rethrow the exception since maybe there's
        // another switch case in the parent
        throw gotoException;
      }
    }

    void executeStatementsWithBreak(Block statements, ExecutionContext context, ExecutionContext dynamicContext) {
      try {
        executeStatements(statements, context, dynamicContext);
      } catch (LoLangThrowable.SwitchBreak breakException) {
        return;
      } catch (LoLangThrowable.SwitchGoto gotoException) {
        // this is an actual interpreter error
        throw new RuntimeError("Encountered goto statement with no matching case", gotoException.source);
      }
    }

    static boolean isValueEqualWithToken(LoLangValue value, Token token) {
      if (value instanceof LoLangValue.Number) {
        return ((LoLangValue.Number) value).value == Double.parseDouble(token.lexeme);
      }

      else if (value instanceof LoLangValue.String) {
        return ((LoLangValue.String) value).value.equals(token.lexeme);
      }

      return false;
    }

    public void semanticAnalysis(SemanticContext context) {
      LoLangType exprType = this.expr.evaluateType(context);
      if (!(exprType instanceof LoLangType.Number) && !(exprType instanceof LoLangType.String)) {
        if (!(exprType instanceof LoLangType.Any))
          context.addException(new SemanticAnalyzerException(
              "Switch expression value must be a number or string, received: " + exprType.toString(),
              this.leftParenToken));
        return;
      }

      // add labels to context
      SemanticContext sharedContext = context.fork();
      for (SwitchCase caseNode : this.cases.namedCases)
        sharedContext.gotoLabels.add(new SemanticContext.GotoLabel(caseNode.literal));

      for (SwitchCase caseNode : this.cases.namedCases) {
        if (caseNode.literal.type == TokenType.NUMBER_LITERAL && !(exprType instanceof LoLangType.Number)) {
          sharedContext.addException(
              new SemanticAnalyzerException("Switch case label must be a number, received: string",
                  this.leftParenToken));
          return;
        }

        else if (caseNode.literal.type == TokenType.STRING_LITERAL && !(exprType instanceof LoLangType.String)) {
          sharedContext
              .addException(new SemanticAnalyzerException("Switch case label must be a string, received: number",
                  this.leftParenToken));
          return;
        }

        SemanticContext forkedContext = sharedContext.fork();
        forkedContext.pushScope(Scope.SWITCH_CASE_BODY);

        for (SwitchCase _caseNode : this.cases.namedCases)
          forkedContext.gotoLabels.add(new SemanticContext.GotoLabel(_caseNode.literal));

        caseNode.statements.semanticAnalysis(forkedContext);
      }

      if (this.cases.defaultCase != null) {
        SemanticContext forkedContext = context.fork();

        for (SwitchCase _caseNode : this.cases.namedCases)
          forkedContext.gotoLabels.add(new SemanticContext.GotoLabel(_caseNode.literal));

        forkedContext.pushScope(Scope.SWITCH_CASE_BODY);
        this.cases.defaultCase.statements.semanticAnalysis(forkedContext);
      }
    }
  }

  public static class SwitchBreak extends StatementNode {
    Token token;

    SwitchBreak(Token token) {
      this.token = token;
    }

    public String toString() {
      return "[SwitchBreak]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "SwitchBreak");
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      throw new LoLangThrowable.SwitchBreak(this.token);
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.SWITCH_CASE_BODY))
        return;

      context.addException(
          new SemanticAnalyzerException("Switch break is not allowed outside of switch case body",
              this.token));
    }
  }

  public static class SwitchGoto extends StatementNode {
    Token gotoKeywordToken;
    Token gotoTargetToken;

    SwitchGoto(Token gotoKeywordToken, Token gotoTargetToken) {
      this.gotoKeywordToken = gotoKeywordToken;
      this.gotoTargetToken = gotoTargetToken;
    }

    public String toString() {
      return String.format("[SwitchGoto: %s]", this.gotoTargetToken.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "SwitchGoto [lexeme=" + this.gotoTargetToken.lexeme.replace("\"", "\'") + "]");
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue value = gotoTargetToken.type == TokenType.STRING_LITERAL
          ? new LoLangValue.String(gotoTargetToken.lexeme)
          : new LoLangValue.Number(Double.parseDouble(gotoTargetToken.lexeme));

      throw new LoLangThrowable.SwitchGoto(value, this.gotoTargetToken, gotoKeywordToken);
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.SWITCH_CASE_BODY) == false) {
        context.addException(
            new SemanticAnalyzerException("Switch goto statement is not allowed outside of switch case body",
                this.gotoKeywordToken));
        return;
      }

      // check if token is in the list of goto labels
      for (SemanticContext.GotoLabel label : context.gotoLabels) {
        if (label.stringLabel != null && this.gotoTargetToken.type == TokenType.STRING_LITERAL
            && label.stringLabel.equals(this.gotoTargetToken.lexeme))
          return;

        else if (label.intLabel != -1 && this.gotoTargetToken.type == TokenType.NUMBER_LITERAL
            && label.intLabel == Integer.parseInt(this.gotoTargetToken.lexeme))
          return;
      }

      // List possible labels here
      String possibleLabels = context.gotoLabels.stream().map(label -> label.stringLabel != null
          ? ("\"" + label.stringLabel + "\"")
          : String.format("%d", label.intLabel)).collect(Collectors.joining(", "));

      context.addException(new SemanticAnalyzerException(String
          .format("No matching switch case found within the context for goto target %s, expected: [%s]",
              this.gotoTargetToken.lexeme, possibleLabels),
          this.gotoTargetToken));
    }
  }

  public static class ForEachLoop extends StatementNode {
    Token variableIdentifier;
    TypeExpressionNode type;
    ExpressionNode iterator;
    StatementNode statement;
    Token ofToken;

    ForEachLoop(Token variableIdentifier, TypeExpressionNode type, ExpressionNode iterator, StatementNode stmt,
        Token ofToken) {
      this.variableIdentifier = variableIdentifier;
      this.type = type;
      this.iterator = iterator;
      this.statement = stmt;
      this.ofToken = ofToken;
    }

    public String toString() {
      return String.format("[ForEachLoop: [Lexeme: %s] [Type: %s] [Iterator: %s] [Body: %s]]",
          this.variableIdentifier.lexeme.replace("\"", "\'"),
          this.type.toString(), this.iterator.toString(), this.statement.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "ForEachLoop [lexeme=" + this.variableIdentifier.lexeme.replace("\"", "\'") + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());

      this.iterator.toDot(builder);
      builder.addEdge(this.hashCode(), this.iterator.hashCode());

      this.statement.toDot(builder);
      builder.addEdge(this.hashCode(), this.statement.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue iteratorValue = this.iterator.evaluate(context, dynamicContext);
      if (!(iteratorValue instanceof LoLangValue.Array))
        throw new RuntimeError("Iterate value must be an array", this.ofToken);

      LoLangValue.Array array = (LoLangValue.Array) iteratorValue;

      for (LoLangValue value : array.values) {
        ExecutionContext forkedContext = context.fork();
        forkedContext.environment.tryDefine(this.variableIdentifier.lexeme, value, false);

        try {
          this.statement.execute(forkedContext, dynamicContext);
        } catch (LoLangThrowable.LoopBreak breakException) {
          break;
        } catch (LoLangThrowable.LoopContinue continueException) {
          continue;
        }
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      LoLangType iteratorType = this.iterator.evaluateType(context);
      if (!(iteratorType instanceof LoLangType.Array)) {
        if (!(iteratorType instanceof LoLangType.Any))
          context.addException(new SemanticAnalyzerException("Iterator must be an array, received: " + iteratorType,
              this.ofToken));

        return;
      }

      LoLangType.Array array = (LoLangType.Array) iteratorType;
      LoLangType elementType = array.elementType;
      LoLangType declaredType = this.type.evaluate(context);

      if (declaredType.isEquivalent(elementType) == false) {
        context.addException(new SemanticAnalyzerException(
            String.format(
                "Declared type of foreach variable must be equivalent to the element type of the iterator, expected: %s but received: %s",
                elementType.toString(), declaredType.toString()),
            this.ofToken));

        return;
      }

      SemanticContext forkedContext = context.fork();
      forkedContext.pushScope(Scope.LOOP_BODY);
      forkedContext.variableEnvironment.tryDefine(this.variableIdentifier.lexeme, elementType, true);
      this.statement.semanticAnalysis(forkedContext);
    }
  }

  public static class CounterControlledLoop extends StatementNode {
    CounterLoopInit init;
    ExpressionNode condition;
    ExpressionList increment;
    StatementNode stmt;
    Token conditionSemicolonToken;

    private CounterControlledLoop(CounterLoopInit init, ExpressionNode condition, ExpressionList increment,
        StatementNode stmt, Token conditionSemicolonToken) {
      this.init = init;
      this.condition = condition;
      this.increment = increment;
      this.stmt = stmt;
      this.conditionSemicolonToken = conditionSemicolonToken;
    }

    public static CounterControlledLoop all(CounterLoopInit init, ExpressionNode condition, ExpressionList increment,
        StatementNode stmt, Token conditionSemicolonToken) {
      return new CounterControlledLoop(init, condition, increment, stmt, conditionSemicolonToken);
    }

    public static CounterControlledLoop missingInit(ExpressionNode condition, ExpressionList increment,
        StatementNode stmt, Token conditionSemicolonToken) {
      return new CounterControlledLoop(null, condition, increment, stmt, conditionSemicolonToken);
    }

    public static CounterControlledLoop missingCondition(CounterLoopInit init, ExpressionList increment,
        StatementNode stmt, Token conditionSemicolonToken) {
      return new CounterControlledLoop(init, null, increment, stmt, conditionSemicolonToken);
    }

    public static CounterControlledLoop missingIncrement(CounterLoopInit init, ExpressionNode condition,
        StatementNode stmt, Token conditionSemicolonToken) {
      return new CounterControlledLoop(init, condition, null, stmt, conditionSemicolonToken);
    }

    public static CounterControlledLoop onlyInit(CounterLoopInit init, StatementNode stmt,
        Token conditionSemicolonToken) {
      return new CounterControlledLoop(init, null, null, stmt, conditionSemicolonToken);
    }

    public static CounterControlledLoop onlyCondition(ExpressionNode condition, StatementNode stmt,
        Token conditionSemicolonToken) {
      return new CounterControlledLoop(null, condition, null, stmt, conditionSemicolonToken);
    }

    public static CounterControlledLoop onlyIncrement(ExpressionList increment, StatementNode stmt,
        Token conditionSemicolonToken) {
      return new CounterControlledLoop(null, null, increment, stmt, conditionSemicolonToken);
    }

    public static CounterControlledLoop none(StatementNode stmt, Token conditionSemicolonToken) {
      return new CounterControlledLoop(null, null, null, stmt, conditionSemicolonToken);
    }

    public String toString() {
      String ret = "[CounterControlledLoop: ";

      if (this.init != null)
        ret += String.format("[Init: %s] ", this.init.toString());

      if (this.condition != null)
        ret += String.format("[Condition: %s] ", this.condition.toString());

      if (this.increment != null)
        ret += String.format("[Increment: %s] ", this.increment.toString());

      ret += String.format("[Body: %s]]", this.stmt.toString());

      return ret + " ]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "CounterControlledLoop");

      if (this.init != null) {
        this.init.toDot(builder);
        builder.addEdge(this.hashCode(), this.init.hashCode());
      }

      if (this.condition != null) {
        this.condition.toDot(builder);
        builder.addEdge(this.hashCode(), this.condition.hashCode());
      }

      if (this.increment != null) {
        this.increment.toDot(builder);
        builder.addEdge(this.hashCode(), this.increment.hashCode());
      }

      this.stmt.toDot(builder);
      builder.addEdge(this.hashCode(), this.stmt.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      ExecutionContext forkedContext = context.fork();

      // Run init by adding declarations to the shared context across all runs
      if (this.init != null)
        for (Node.VariableDeclaration declaration : this.init.declarations) {
          try {
            declaration.addToContext(forkedContext, dynamicContext);
          } catch (EnvironmentAlreadyDeclaredException e) {
            throw new RuntimeError("Cannot redeclare variable \"" + declaration.identifier.lexeme + "\"",
                declaration.identifier);
          }
        }

      while (true) {
        // If there is a condition, then we need to check if we shold run
        if (this.condition != null) {
          LoLangValue condition = this.condition.evaluate(forkedContext, dynamicContext);
          boolean conditionValue = false;

          try {
            conditionValue = Caster.toBoolean(condition);
          } catch (Caster.CastingException e) {
            throw new RuntimeError("Condition must be a boolean", this.conditionSemicolonToken);
          }

          if (conditionValue == false)
            break;
        }

        try {
          ExecutionContext loopBodyContext = forkedContext.fork();
          this.stmt.execute(loopBodyContext, dynamicContext);
        } catch (LoLangThrowable.LoopBreak breakException) {
          break;
        } catch (LoLangThrowable.LoopContinue continueException) {
        }

        if (this.increment != null)
          for (ExpressionNode increment : this.increment.expressions)
            increment.evaluate(forkedContext, dynamicContext);

        continue;
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      // Forked context represents header, loop body context represents entire loop
      SemanticContext forkedContext = context.fork();

      if (this.init != null) {
        for (Node.VariableDeclaration init : this.init.declarations)
          forkedContext.variableEnvironment.tryDefine(init.identifier.lexeme, init.type.evaluate(context), false);
      }

      if (this.condition != null) {
        LoLangType conditionType = this.condition.evaluateType(forkedContext);
        if (!(conditionType instanceof LoLangType.Boolean || Caster.toBooleanType(conditionType))) {
          if (!(conditionType instanceof LoLangType.Any))
            context
                .addException(new SemanticAnalyzerException("Condition must be a boolean, received: " + conditionType,
                    this.conditionSemicolonToken));
          return;
        }
      }

      SemanticContext loopBodyContext = forkedContext.fork();
      loopBodyContext.pushScope(Scope.LOOP_BODY);
      this.stmt.semanticAnalysis(loopBodyContext);
    }
  }

  public static class LoopBreak extends StatementNode {
    Token token;

    LoopBreak(Token token) {
      this.token = token;
    }

    public String toString() {
      return "[LoopBreak]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "LoopBreak");
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      throw new LoLangThrowable.LoopBreak(token);
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.LOOP_BODY) == false) {
        context.addException(new SemanticAnalyzerException("Loop break statement is not allowed outside of loop body",
            this.token));
        return;
      }
    }
  }

  public static class LoopContinue extends StatementNode {
    Token token;

    LoopContinue(Token token) {
      this.token = token;
    }

    public String toString() {
      return "[LoopContinue]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "LoopContinue");
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      throw new LoLangThrowable.LoopContinue(token);
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.LOOP_BODY))
        return;
      context.addException(new SemanticAnalyzerException("Loop continue statement is not allowed outside of loop body",
          this.token));
    }
  }

  public static class WhileLoop extends StatementNode {
    ExpressionNode condition;
    StatementNode statement;
    Token leftParenToken;

    WhileLoop(ExpressionNode condition, StatementNode statement, Token leftParenToken) {
      this.condition = condition;
      this.statement = statement;
      this.leftParenToken = leftParenToken;
    }

    WhileLoop(StatementNode statement, Token leftParenToken) {
      this.condition = null;
      this.statement = statement;
      this.leftParenToken = leftParenToken;
    }

    public String toString() {
      if (this.condition == null)
        return String.format("[WhileLoop: %s]", this.statement.toString());
      else
        return String.format("[WhileLoop: [Condition: %s] %s]", this.condition.toString(),
            this.statement.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "WhileLoop");

      if (this.condition != null) {
        this.condition.toDot(builder);
        builder.addEdge(this.hashCode(), this.condition.hashCode());
      }

      this.statement.toDot(builder);
      builder.addEdge(this.hashCode(), this.statement.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      while (true) {
        LoLangValue condition = this.condition != null ? this.condition.evaluate(context, dynamicContext)
            : new LoLangValue.Boolean(true);

        boolean conditionValue = false;
        try {
          conditionValue = Caster.toBoolean(condition);
        } catch (Caster.CastingException e) {
          throw new RuntimeError("Condition must be a boolean", this.leftParenToken);
        }

        if (conditionValue == false)
          break;

        try {
          ExecutionContext forkedContext = context.fork();
          this.statement.execute(forkedContext, dynamicContext);
        } catch (LoLangThrowable.LoopBreak breakException) {
          break;
        } catch (LoLangThrowable.LoopContinue continueException) {
          continue;
        }
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      if (this.condition != null) {
        LoLangType conditionType = this.condition.evaluateType(context);
        if (!(conditionType instanceof LoLangType.Boolean || Caster.toBooleanType(conditionType))) {
          if (!(conditionType instanceof LoLangType.Any))
            context.addException(
                new SemanticAnalyzerException(
                    "While-loop condition must be a boolean, received: " + conditionType.toString(),
                    this.leftParenToken));
          return;
        }
      }

      SemanticContext forkedContext = context.fork();
      forkedContext.pushScope(Scope.LOOP_BODY);
      this.statement.semanticAnalysis(forkedContext);
    }
  }

  public static class Expression extends StatementNode {
    ExpressionNode expression;

    Expression(ExpressionNode expression) {
      this.expression = expression;
    }

    public String toString() {
      return String.format("[Expression: %s]", this.expression.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ExpressionStatement");
      this.expression.toDot(builder);
      builder.addEdge(this.hashCode(), this.expression.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      this.expression.evaluate(context, dynamicContext);
    }

    public void semanticAnalysis(SemanticContext context) {
      this.expression.evaluateType(context);
    }
  }

  public static class ObjectTypeDeclaration extends StatementNode {
    Node.PropertyList properties;
    public final Token identifier;

    ObjectTypeDeclaration(Token identifier, Node.PropertyList properties) {
      this.identifier = identifier;
      this.properties = properties;
    }

    ObjectTypeDeclaration(Token identifier) {
      this.identifier = identifier;
      this.properties = new Node.PropertyList();
    }

    public String toString() {
      return String.format("[ObjectTypeDeclaration: %s]", this.properties.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ObjectTypeDeclaration");
      this.properties.toDot(builder);
      builder.addEdge(this.hashCode(), this.properties.hashCode());
    }

    public void execute(ExecutionContext context, ExecutionContext dynamicContext) {
      // This method does nothing at runtime
    }

    public void semanticAnalysis(SemanticContext context) {
      // This method does nothing at analysis time
    }

    public LoLangType.Object convertToType(SemanticContext context) {
      HashMap<String, LoLangType> fields = new HashMap<>();

      for (Node.PropertyDefinition property : this.properties.definitions) {
        LoLangType type = property.type.evaluate(context);
        fields.put(property.identifier.lexeme, type);
      }

      return new LoLangType.Object(fields);
    }
  }
}
