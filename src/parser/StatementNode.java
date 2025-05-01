package parser;

import interpreter.ExecutionContext;
import interpreter.InterpreterError;
import interpreter.LoLangThrowable;
import interpreter.LoLangValue;
import lexer.Token;
import parser.Node.SwitchCaseList.SwitchCase;
import utils.DOTGenerator;

public abstract class StatementNode extends Node {
  // Uncomment this line when it's time to implement execution
  abstract void execute(ExecutionContext context);

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
  }

  public static class Import extends StatementNode {
    String lexeme;

    Import(String lexeme) {
      this.lexeme = lexeme;
    }

    public String toString() {
      return String.format("[Import: %s]", this.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Import [lexeme=" + this.lexeme.replace("\"", "\'") + "]");
    }

    public void execute(ExecutionContext context) {
      throw new InterpreterError("Import statements are not implemented yet");
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
  }

  public static class ConstantDeclaration extends StatementNode {
    String lexeme;
    TypeExpressionNode type;
    ExpressionNode expression;

    ConstantDeclaration(String lexeme, TypeExpressionNode type, ExpressionNode expression) {
      this.lexeme = lexeme;
      this.type = type;
      this.expression = expression;
    }

    public String toString() {
      return String.format("[ConstantDeclaration: [Name=%s] [Type=%s] [Init=%s]]", this.lexeme.replace("\"", "\'"),
          this.type.toString(), this.expression.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ConstantDeclaration [lexeme=" + this.lexeme.replace("\"", "\'") + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());

      if (this.expression != null) {
        this.expression.toDot(builder);
        builder.addEdge(this.hashCode(), this.expression.hashCode());
      }
    }

    public void execute(ExecutionContext context) {
      context.environment.define(this.lexeme, this.expression.evaluate(context), true);
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
  }

  public static class TryCatch extends StatementNode {
    StatementNode body;
    String identifier;
    StatementNode catchBody;

    TryCatch(StatementNode body, String identifier, StatementNode catchBody) {
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
        forkedContext.environment.define(identifier, errorException.message, true);
        this.catchBody.execute(forkedContext);
      }
    }
  }

  public static class Throw extends StatementNode {
    String errorMessasge;

    Throw(String errorMessasge) {
      this.errorMessasge = errorMessasge;
    }

    public String toString() {
      return String.format("[Throw: %s]", this.errorMessasge.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Throw [errorMessage=" + this.errorMessasge.replace("\"", "\'") + "]");
    }

    public void execute(ExecutionContext context) {
      throw new LoLangThrowable.Error(new LoLangValue.String(errorMessasge));
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
      }
    }

    void executeStatementsWithBreak(Block statements, ExecutionContext context) {
      try {
        executeStatements(statements, context);
      } catch (LoLangThrowable.SwitchBreak breakException) {
        return;
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
      ExecutionContext sharedContext = context.fork();

      // Run init by adding declarations to the shared context across all runs
      if (this.init != null)
        for (Node.VariableDeclaration declaration : this.init.declarations)
          declaration.addToContext(sharedContext);

      while (true) {
        // Check if should run
        LoLangValue condition = this.condition.evaluate(sharedContext);
        if (!(condition instanceof LoLangValue.Boolean))
          throw new InterpreterError("Condition must be a boolean");

        boolean conditionValue = ((LoLangValue.Boolean) condition).value;
        if (conditionValue == false)
          break;

        try {
          ExecutionContext forkedContext = sharedContext.fork();
          this.stmt.execute(forkedContext);
        } catch (LoLangThrowable.LoopBreak breakException) {
          break;
        } catch (LoLangThrowable.LoopContinue continueException) {
        }

        if (this.increment != null)
          for (ExpressionNode increment : this.increment.expressions)
            increment.evaluate(sharedContext);

        continue;
      }
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
  }

  public static class ObjectTypeDeclaration extends StatementNode {
    Node.PropertyList properties;

    ObjectTypeDeclaration(Node.PropertyList properties) {
      this.properties = properties;
    }

    ObjectTypeDeclaration() {
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
      // TODO: implement
      throw new Error("Not implemented yet");
    }
  }
}
