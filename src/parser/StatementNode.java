package parser;

import lexer.Token;
import utils.DOTGenerator;

public abstract class StatementNode extends Node {
  // Uncomment this line when it's time to implement execution
  // abstract void execute();

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
  }

  public static class VariableDeclaration extends StatementNode {
    String lexeme;
    TypeExpressionNode type;
    ExpressionNode expression = null;

    VariableDeclaration(Node.VariableDeclaration declaration) {
      this.lexeme = declaration.lexeme;
      this.type = declaration.type;
      this.expression = declaration.expression;
    }

    public String toString() {
      return this.expression != null
          ? String.format("[VariableDeclaration: %s %s %s]", this.lexeme.replace("\"", "\'"),
              this.type.toString(),
              this.expression.toString())
          : String.format("[VariableDeclaration: %s %s]", this.lexeme.replace("\"", "\'"), this.type.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "VariableDeclaration [lexeme=" + this.lexeme.replace("\"", "\'") + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());

      if (this.expression != null) {
        this.expression.toDot(builder);
        builder.addEdge(this.hashCode(), this.expression.hashCode());
      }
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
  }

  public static class SwitchBreak extends StatementNode {
    public String toString() {
      return "[SwitchBreak]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "SwitchBreak");
    }
  }

  public static class SwitchGoto extends StatementNode {
    String lexeme;

    SwitchGoto(String lexeme) {
      this.lexeme = lexeme;
    }

    public String toString() {
      return String.format("[SwitchGoto: %s]", this.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "SwitchGoto [lexeme=" + this.lexeme.replace("\"", "\'") + "]");
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
  }

  public static class LoopBreak extends StatementNode {
    public String toString() {
      return "[LoopBreak]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "LoopBreak");
    }
  }

  public static class LoopContinue extends StatementNode {
    public String toString() {
      return "[LoopContinue]";
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "LoopContinue");
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
  }
}
