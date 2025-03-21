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
  }

  public static class Import extends StatementNode {
    String lexeme;

    Import(String lexeme) {
      this.lexeme = lexeme;
    }

    public String toString() {
      return String.format("[Import: %s]", this.lexeme);
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
      return String.format("[If: %s %s]", this.branches.toString(),
          this.elseBody.toString());
    }
  }

  public static class VariableDeclaration extends StatementNode {
    String lexeme;
    TypeExpressionNode type;
    ExpressionNode expression = null;

    VariableDeclaration(String lexeme, TypeExpressionNode type) {
      this.lexeme = lexeme;
      this.type = type;
    }

    VariableDeclaration(String lexeme, TypeExpressionNode type, ExpressionNode expression) {
      this.lexeme = lexeme;
      this.type = type;
      this.expression = expression;
    }

    public String toString() {
      return this.expression != null
          ? String.format("[VariableDeclaration: %s %s %s]", this.lexeme,
              this.type.toString(),
              this.expression.toString())
          : String.format("[VariableDeclaration: %s %s]", this.lexeme, this.type.toString());
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
      return String.format("[ConstantDeclaration: [Name=%s] [Type=%s] [Init=%s]]", this.lexeme,
          this.type.toString(), this.expression.toString());
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
  }

  public static class Throw extends StatementNode {
    String errorMessasge;

    Throw(String errorMessasge) {
      this.errorMessasge = errorMessasge;
    }

    public String toString() {
      return String.format("[Throw: %s]", this.errorMessasge.toString());
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
  }

  public static class SwitchBreak extends StatementNode {
    public String toString() {
      return "[SwitchBreak]";
    }
  }

  public static class SwitchGoto extends StatementNode {
    String lexeme;

    SwitchGoto(String lexeme) {
      this.lexeme = lexeme;
    }

    public String toString() {
      return String.format("[SwitchGoto: %s]", this.lexeme);
    }
  }

  public static class ForEachLoop extends StatementNode {
    Token lexeme;
    TypeExpressionNode type;
    ExpressionNode iterator;
    StatementNode statement;

    ForEachLoop(Token lexeme, TypeExpressionNode type, ExpressionNode iterator, StatementNode stmt) {
      this.lexeme = lexeme;
      this.type = type;
      this.iterator = iterator;
      this.statement = stmt;
    }

    public String toString() {
      return String.format("[Loop: [Lexeme: %s] [Type: %s] [Iterator: %s] [Body: %s]]", this.lexeme.type,
          this.type.toString(), this.iterator.toString(), this.statement.toString());
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
      return String.format("[CounterControlledLoop: [Init: %s] [Condition: %s] [Increment: %s] [Body: %s]]",
          this.init.toString(), this.condition.toString(), this.increment.toString(), this.stmt.toString());
    }
  }

  public static class LoopBreak extends StatementNode {
    public String toString() {
      return "[LoopBreak]";
    }
  }

  public static class LoopContinue extends StatementNode {
    public String toString() {
      return "[LoopContinue]";
    }
  }

  public static class WhileLoop extends StatementNode {
    ExpressionNode condition;
    StatementList statements;

    WhileLoop(ExpressionNode condition, StatementList statements) {
      this.condition = condition;
      this.statements = statements;
    }

    WhileLoop(StatementList statements) {
      this.condition = null;
      this.statements = statements;
    }

    public String toString() {
      return String.format("[WhileLoop: [Condition: %s] [Body: %s]]", this.condition.toString(),
          this.statements.toString());
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
  }

}
