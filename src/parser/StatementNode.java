package parser;

import java.util.HashMap;

import interpreter.ExecutionContext;
import interpreter.InterpreterError;
import interpreter.LoLangThrowable;
import interpreter.LoLangValue;
import lexer.Token;
import lexer.TokenType;
import parser.Node.SwitchCaseList.SwitchCase;
import semantic.LoLangType;
import semantic.SemanticAnalysisError;
import semantic.SemanticContext;
import semantic.SemanticContext.Scope;
import utils.DOTGenerator;

public abstract class StatementNode extends Node {
  abstract void execute(ExecutionContext context);

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

    public void execute(ExecutionContext context) {
      for (StatementNode statement : this.statements.statements)
        statement.execute(context);
    }

    public void semanticAnalysis(SemanticContext context) {
      for (StatementNode statement : this.statements.statements)
        statement.semanticAnalysis(context);
    }
  }

  public static class Import extends StatementNode {
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

    public void execute(ExecutionContext context) {
      // TODO: implement
      throw new InterpreterError("Import statements are not implemented yet");
    }

    public void semanticAnalysis(SemanticContext context) {
      // TODO: implement
      throw new SemanticAnalysisError("Import statements are not implemented yet");
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

    public void execute(ExecutionContext context) {
      ExecutionContext forkedContext = context.fork();

      for (IfStatementBranch branch : this.branches.clauses) {
        // Evaluate the condition inside the branch
        LoLangValue condition = branch.condition.evaluate(context);
        if (!(condition instanceof LoLangValue.Boolean))
          throw new InterpreterError("Condition must be a boolean");

        LoLangValue.Boolean conditionValue = (LoLangValue.Boolean) condition;

        if (conditionValue.value == false)
          continue;

        // Execute the body of the branch
        branch.body.execute(forkedContext);
        return;
      }

      if (this.elseBody != null)
        this.elseBody.execute(context);
    }

    public void semanticAnalysis(SemanticContext context) {
      for (IfStatementBranch branch : this.branches.clauses) {
        LoLangType conditionType = branch.condition.evaluateType(context);
        if (!(conditionType instanceof LoLangType.Boolean))
          throw new SemanticAnalysisError("Condition must be a boolean");

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

    public void execute(ExecutionContext context) {
      this.declaration.addToContext(context);
    }

    public void semanticAnalysis(SemanticContext context) {
      context.variableEnvironment.define(this.declaration.identifier.lexeme,
          this.declaration.expression.evaluateType(context), false);

      if (this.declaration.expression != null) {
        LoLangType expressionType = this.declaration.expression.evaluateType(context);
        if (!expressionType.isEquivalent(this.declaration.type.evaluate(context)))
          throw new SemanticAnalysisError("Type of variable declaration does not match type of expression");
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

    public void execute(ExecutionContext context) {
      context.environment.define(this.identifier.lexeme, this.expression.evaluate(context), true);
    }

    public void semanticAnalysis(SemanticContext context) {
      context.variableEnvironment.define(this.identifier.lexeme, this.expression.evaluateType(context), true);
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

    public void execute(ExecutionContext context) {
      ExecutionContext forkedContext = context.fork();

      for (StatementNode statement : this.statements.statements)
        statement.execute(forkedContext);
    }

    public void semanticAnalysis(SemanticContext context) {
      SemanticContext forkedContext = context.fork();
      forkedContext.loadTypesFromStatementList(this.statements);

      for (StatementNode statement : this.statements.statements)
        statement.semanticAnalysis(forkedContext);
    }
  }

  public static class Return extends StatementNode {
    ExpressionNode expression;

    Return(ExpressionNode expression) {
      this.expression = expression;
    }

    Return() {
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

    public void execute(ExecutionContext context) {
      if (this.expression != null)
        context.environment.assign("return", this.expression.evaluate(context));
    }

    public void semanticAnalysis(SemanticContext context) {
      if (!context.isInScope(Scope.FUNCTION_BODY))
        throw new SemanticAnalysisError("Return statement is not allowed outside of function body");

      LoLangType expectedReturnType = context.returnType;

      if (expectedReturnType == null) {
        if (this.expression != null)
          throw new SemanticAnalysisError("Return statement must not contain a return value");
        return;
      }

      if (this.expression == null)
        throw new SemanticAnalysisError("Return statement must contain a return value");
      else if (!expectedReturnType.isEquivalent(this.expression.evaluateType(context)))
        throw new SemanticAnalysisError("Return value must be of type " + expectedReturnType.toString());

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

    public void execute(ExecutionContext context) {
      try {
        ExecutionContext forkedContext = context.fork();
        this.body.execute(forkedContext);
      } catch (LoLangThrowable.Error errorException) {
        ExecutionContext forkedContext = context.fork();
        forkedContext.environment.define(identifier.lexeme, errorException.message, true);
        this.catchBody.execute(forkedContext);
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      SemanticContext forkedContext = context.fork();
      this.body.semanticAnalysis(forkedContext);

      SemanticContext forkedCatchContext = context.fork();
      forkedCatchContext.variableEnvironment.define(this.identifier.lexeme, new LoLangType.String(), true);

      this.catchBody.semanticAnalysis(forkedContext);
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

    public void execute(ExecutionContext context) {
      throw new LoLangThrowable.Error(new LoLangValue.String(errorMessasge.lexeme));
    }

    public void semanticAnalysis(SemanticContext context) {
      // no static analysis is required for throw statements
    }
  }

  public static class Switch extends StatementNode {
    SwitchCaseList cases;
    ExpressionNode expr;

    Switch(ExpressionNode expr, SwitchCaseList cases) {
      this.expr = expr;
      this.cases = cases;
    }

    Switch(ExpressionNode expr) {
      this.expr = expr;
      this.cases = new SwitchCaseList();
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

    public void execute(ExecutionContext context) {
      LoLangValue value = this.expr.evaluate(context);
      if (!(value instanceof LoLangValue.Number) || !(value instanceof LoLangValue.String)) {
        throw new InterpreterError("Switch expression value be a number or string");
      }

      for (SwitchCase caseNode : this.cases.namedCases) {
        if (isValueEqualWithToken(value, caseNode.literal)) {
          executeStatementsWithBreak(caseNode.statements, context);
          return;
        }
      }

      // if no case matched, execute default case
      if (this.cases.defaultCase != null)
        executeStatementsWithBreak(this.cases.defaultCase.statements, context);
    }

    void executeStatements(Block statements, ExecutionContext context) {
      try {
        statements.execute(context);
      } catch (LoLangThrowable.SwitchGoto gotoException) {
        for (SwitchCase caseNode : this.cases.namedCases) {
          if (isValueEqualWithToken(gotoException.label, caseNode.literal)) {
            executeStatements(caseNode.statements, context);
            break;
          }
        }

        // if no case matched, we should rethrow the exception since maybe there's
        // another switch case in the parent
        throw gotoException;
      }
    }

    void executeStatementsWithBreak(Block statements, ExecutionContext context) {
      try {
        executeStatements(statements, context);
      } catch (LoLangThrowable.SwitchBreak breakException) {
        return;
      } catch (LoLangThrowable.SwitchGoto gotoException) {
        // this is an actual interpreter error
        throw new InterpreterError("Encountered goto statement with no matching case");
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
      if (!(exprType instanceof LoLangType.Number) && !(exprType instanceof LoLangType.String))
        throw new SemanticAnalysisError("Switch expression value must be a number or string");

      for (SwitchCase caseNode : this.cases.namedCases) {
        if (caseNode.literal.type == TokenType.NUMBER_LITERAL && !(exprType instanceof LoLangType.Number))
          throw new SemanticAnalysisError("Switch expression value must be a number");
        else if (caseNode.literal.type == TokenType.STRING_LITERAL && !(exprType instanceof LoLangType.String))
          throw new SemanticAnalysisError("Switch expression value must be a string");

        SemanticContext forkedContext = context.fork();
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
    public String toString() {
      return "[SwitchBreak]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "SwitchBreak");
    }

    public void execute(ExecutionContext context) {
      throw new LoLangThrowable.SwitchBreak();
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.SWITCH_CASE_BODY) == false)
        throw new SemanticAnalysisError("SwitchBreak statement is not allowed outside of switch case body");
    }
  }

  public static class SwitchGoto extends StatementNode {
    Token token;

    SwitchGoto(Token token) {
      this.token = token;
    }

    public String toString() {
      return String.format("[SwitchGoto: %s]", this.token.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "SwitchGoto [lexeme=" + this.token.lexeme.replace("\"", "\'") + "]");
    }

    public void execute(ExecutionContext context) {
      throw new LoLangThrowable.SwitchGoto(context.environment.get(this.token.lexeme));
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.SWITCH_CASE_BODY) == false)
        throw new SemanticAnalysisError("SwitchGoto statement is not allowed outside of switch case body");

      // check if token is in the list of goto labels
      for (SemanticContext.GotoLabel label : context.gotoLabels) {
        if (label.stringLabel != null && this.token.type == TokenType.STRING_LITERAL
            && label.stringLabel.equals(this.token.lexeme))
          break;

        else if (label.intLabel != -1 && this.token.type == TokenType.NUMBER_LITERAL
            && label.intLabel == Integer.parseInt(this.token.lexeme))
          break;
      }

      throw new SemanticAnalysisError("No matching switch case found");
    }
  }

  public static class ForEachLoop extends StatementNode {
    Token token;
    TypeExpressionNode type;
    ExpressionNode iterator;
    StatementNode statement;

    ForEachLoop(Token token, TypeExpressionNode type, ExpressionNode iterator, StatementNode stmt) {
      this.token = token;
      this.type = type;
      this.iterator = iterator;
      this.statement = stmt;
    }

    public String toString() {
      return String.format("[ForEachLoop: [Lexeme: %s] [Type: %s] [Iterator: %s] [Body: %s]]",
          this.token.lexeme.replace("\"", "\'"),
          this.type.toString(), this.iterator.toString(), this.statement.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ForEachLoop [lexeme=" + this.token.lexeme.replace("\"", "\'") + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());

      this.iterator.toDot(builder);
      builder.addEdge(this.hashCode(), this.iterator.hashCode());

      this.statement.toDot(builder);
      builder.addEdge(this.hashCode(), this.statement.hashCode());
    }

    public void execute(ExecutionContext context) {
      LoLangValue iteratorValue = this.iterator.evaluate(context);
      if (!(iteratorValue instanceof LoLangValue.Array))
        throw new InterpreterError("Iterator must be an array");

      LoLangValue.Array array = (LoLangValue.Array) iteratorValue;

      for (LoLangValue value : array.values) {
        ExecutionContext forkedContext = context.fork();
        forkedContext.environment.define(this.token.lexeme, value, false);

        try {
          this.statement.execute(forkedContext);
        } catch (LoLangThrowable.LoopBreak breakException) {
          break;
        } catch (LoLangThrowable.LoopContinue continueException) {
          continue;
        }
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      LoLangType iteratorType = this.iterator.evaluateType(context);
      if (!(iteratorType instanceof LoLangType.Array))
        throw new SemanticAnalysisError("Iterator must be an array");

      LoLangType.Array array = (LoLangType.Array) iteratorType;
      LoLangType elementType = array.elementType;
      LoLangType declaredType = this.type.evaluate(context);

      if (declaredType.isEquivalent(elementType) == false)
        throw new SemanticAnalysisError(
            "Declared type of for loop must be equivalent to the element type of the iterator");

      SemanticContext forkedContext = context.fork();
      forkedContext.pushScope(Scope.LOOP_BODY);
      forkedContext.variableEnvironment.define(this.token.lexeme, elementType, true);
      this.statement.semanticAnalysis(forkedContext);
    }
  }

  public static class CounterControlledLoop extends StatementNode {
    CounterLoopInit init;
    ExpressionNode condition;
    ExpressionList increment;
    StatementNode stmt;

    private CounterControlledLoop(CounterLoopInit init, ExpressionNode condition, ExpressionList increment,
        StatementNode stmt) {
      this.init = init;
      this.condition = condition;
      this.increment = increment;
      this.stmt = stmt;
    }

    public static CounterControlledLoop all(CounterLoopInit init, ExpressionNode condition, ExpressionList increment,
        StatementNode stmt) {
      return new CounterControlledLoop(init, condition, increment, stmt);
    }

    public static CounterControlledLoop missingInit(ExpressionNode condition, ExpressionList increment,
        StatementNode stmt) {
      return new CounterControlledLoop(null, condition, increment, stmt);
    }

    public static CounterControlledLoop missingCondition(CounterLoopInit init, ExpressionList increment,
        StatementNode stmt) {
      return new CounterControlledLoop(init, null, increment, stmt);
    }

    public static CounterControlledLoop missingIncrement(CounterLoopInit init, ExpressionNode condition,
        StatementNode stmt) {
      return new CounterControlledLoop(init, condition, null, stmt);
    }

    public static CounterControlledLoop onlyInit(CounterLoopInit init, StatementNode stmt) {
      return new CounterControlledLoop(init, null, null, stmt);
    }

    public static CounterControlledLoop onlyCondition(ExpressionNode condition, StatementNode stmt) {
      return new CounterControlledLoop(null, condition, null, stmt);
    }

    public static CounterControlledLoop onlyIncrement(ExpressionList increment, StatementNode stmt) {
      return new CounterControlledLoop(null, null, increment, stmt);
    }

    public static CounterControlledLoop none(StatementNode stmt) {
      return new CounterControlledLoop(null, null, null, stmt);
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

    public void execute(ExecutionContext context) {
      ExecutionContext forkedContext = context.fork();

      // Run init by adding declarations to the shared context across all runs
      if (this.init != null)
        for (Node.VariableDeclaration declaration : this.init.declarations)
          declaration.addToContext(forkedContext);

      while (true) {
        // If there is a condition, then we need to check if we shold run
        if (this.condition != null) {
          LoLangValue condition = this.condition.evaluate(forkedContext);
          if (!(condition instanceof LoLangValue.Boolean))
            throw new InterpreterError("Condition must be a boolean");

          boolean conditionValue = ((LoLangValue.Boolean) condition).value;
          if (conditionValue == false)
            break;
        }

        try {
          ExecutionContext loopBodyContext = forkedContext.fork();
          this.stmt.execute(loopBodyContext);
        } catch (LoLangThrowable.LoopBreak breakException) {
          break;
        } catch (LoLangThrowable.LoopContinue continueException) {
        }

        if (this.increment != null)
          for (ExpressionNode increment : this.increment.expressions)
            increment.evaluate(forkedContext);

        continue;
      }
    }

    public void semanticAnalysis(SemanticContext context) {
      // Forked context represents header, loop body context represents entire loop
      SemanticContext forkedContext = context.fork();

      if (this.init != null) {
        for (Node.VariableDeclaration init : this.init.declarations)
          forkedContext.variableEnvironment.define(init.identifier.lexeme, init.type.evaluate(context), false);
      }

      if (this.condition != null) {
        LoLangType conditionType = this.condition.evaluateType(context);
        if (!(conditionType instanceof LoLangType.Boolean))
          throw new SemanticAnalysisError("Condition must be a boolean");
      }

      SemanticContext loopBodyContext = forkedContext.fork();
      loopBodyContext.pushScope(Scope.LOOP_BODY);
    }
  }

  public static class LoopBreak extends StatementNode {
    public String toString() {
      return "[LoopBreak]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "LoopBreak");
    }

    public void execute(ExecutionContext context) {
      throw new LoLangThrowable.LoopBreak();
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.LOOP_BODY) == false)
        throw new SemanticAnalysisError("LoopBreak statement is not allowed outside of loop body");
    }
  }

  public static class LoopContinue extends StatementNode {
    public String toString() {
      return "[LoopContinue]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "LoopContinue");
    }

    public void execute(ExecutionContext context) {
      throw new LoLangThrowable.LoopContinue();
    }

    public void semanticAnalysis(SemanticContext context) {
      if (context.isInScope(Scope.LOOP_BODY) == false)
        throw new SemanticAnalysisError("LoopContinue statement is not allowed outside of loop body");
    }
  }

  public static class WhileLoop extends StatementNode {
    ExpressionNode condition;
    StatementNode statement;

    WhileLoop(ExpressionNode condition, StatementNode statement) {
      this.condition = condition;
      this.statement = statement;
    }

    WhileLoop(StatementNode statement) {
      this.condition = null;
      this.statement = statement;
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

    public void execute(ExecutionContext context) {
      while (true) {
        LoLangValue condition = this.condition.evaluate(context);
        if (!(condition instanceof LoLangValue.Boolean))
          throw new InterpreterError("Condition must be a boolean");

        boolean conditionValue = ((LoLangValue.Boolean) condition).value;
        if (conditionValue == false)
          break;

        try {
          ExecutionContext forkedContext = context.fork();
          this.statement.execute(forkedContext);
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
        if (!(conditionType instanceof LoLangType.Boolean))
          throw new SemanticAnalysisError("Condition must be a boolean");
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

    public void execute(ExecutionContext context) {
      this.expression.evaluate(context);
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

    public void execute(ExecutionContext context) {
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
