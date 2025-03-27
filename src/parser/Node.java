package parser;

import java.util.ArrayList;
import java.util.LinkedList;

import lexer.Token;
import utils.DOTGenerator;

public abstract class Node {
  abstract public String toString();

  abstract public void toDot(DOTGenerator builder);

  public static class StatementList extends Node {
    ArrayList<StatementNode> statements;

    StatementList(ArrayList<StatementNode> statements) {
      this.statements = statements;
    }

    StatementList(StatementNode stmt) {
      this.statements = new ArrayList<>();
      this.statements.add(stmt);
    }

    StatementList() {
      this.statements = new ArrayList<>();
    }

    public StatementList add(StatementNode statement) {
      this.statements.add(statement);
      return this;
    }

    public String toString() {
      String statementsString = String.join(",", this.statements.toString());
      return String.format("[StatementList: %s]", statementsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "StatementList");
      for (StatementNode statement : this.statements) {
        statement.toDot(builder);
        builder.addEdge(this.hashCode(), statement.hashCode());
      }
    }
  }

  public static class VariableDeclarationHeader extends Node {
    String lexeme;
    TypeExpressionNode type;

    VariableDeclarationHeader(String lexeme, TypeExpressionNode type) {
      this.lexeme = lexeme;
      this.type = type;
    }

    public String toString() {
      return String.format("[VariableDeclarationHeader: %s %s]", this.lexeme, this.type.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "VariableDeclarationHeader [lexeme=" + this.lexeme + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());
    }
  }

  public static class VariableDeclaration extends Node {
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

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "VariableDeclaration [lexeme=" + this.lexeme + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());

      if (this.expression != null) {
        this.expression.toDot(builder);
        builder.addEdge(this.hashCode(), this.expression.hashCode());
      }
    }
  }

  public static class SwitchCaseList extends Node {
    public static class SwitchCase extends Node {
      Token literal;
      StatementList statements;

      SwitchCase(Token literal, StatementList statements) {
        this.literal = literal;
        this.statements = statements;
      }

      SwitchCase(StatementList statements) {
        this.literal = null;
        this.statements = statements;
      }

      SwitchCase(Token literal) {
        this.literal = literal;
        this.statements = new Node.StatementList();
      }

      public String toString() {
        String statementsString = String.join("\n", this.statements.toString());
        return String.format("[SwitchCase: %s %s]", this.literal == null ? "default" : this.literal.toString(),
            statementsString);
      }

      public void toDot(DOTGenerator builder) {
        builder.addNode(this.hashCode(), String.format("SwitchCase [literal=%s]", this.literal == null ? "default"
            : this.literal.lexeme.replace("\"", "\'")));
        this.statements.toDot(builder);
        builder.addEdge(this.hashCode(), this.statements.hashCode());
      }
    }

    ArrayList<SwitchCase> namedCases;
    SwitchCase defaultCase = null;

    SwitchCaseList() {
      this.namedCases = new ArrayList<>();
    }

    SwitchCaseList(SwitchCase namedCase) {
      this.namedCases = new ArrayList<>();
      this.namedCases.add(namedCase);
    }

    SwitchCaseList(ArrayList<SwitchCase> namedCases) {
      this.namedCases = namedCases;
    }

    static SwitchCaseList defaultOnly(SwitchCase defaultCase) {
      return new SwitchCaseList(new ArrayList<>(), defaultCase);
    }

    SwitchCaseList(ArrayList<SwitchCase> namedCases, SwitchCase defaultCase) {
      this.namedCases = namedCases;
      this.defaultCase = defaultCase;
    }

    public SwitchCaseList addNamedCase(SwitchCase newCase) {
      this.namedCases.add(newCase);
      return this;
    }

    public SwitchCaseList setDefaultCase(StatementList statements) {
      this.defaultCase = new SwitchCase(null, statements);
      return this;
    }

    public String toString() {
      String casesString = String.join("\n", this.namedCases.toString());
      return String.format("[SwitchCaseList: %s]", casesString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "SwitchCaseList");

      for (SwitchCase namedCase : this.namedCases) {
        namedCase.toDot(builder);
        builder.addEdge(this.hashCode(), namedCase.hashCode());
      }

      if (this.defaultCase != null) {
        this.defaultCase.toDot(builder);
        builder.addEdge(this.hashCode(), this.defaultCase.hashCode());
      }
    }
  }

  public static class CounterLoopInit extends Node {
    ArrayList<Node.VariableDeclaration> declarations;

    CounterLoopInit(ArrayList<Node.VariableDeclaration> declarations) {
      this.declarations = declarations;
    }

    CounterLoopInit(Node.VariableDeclaration decl) {
      this.declarations = new ArrayList<>();
      this.declarations.add(decl);
    }

    public CounterLoopInit add(Node.VariableDeclaration decl) {
      this.declarations.add(decl);
      return this;
    }

    public String toString() {
      String declsString = String.join("\n", this.declarations.toString());
      return String.format("[CounterLoopInit: %s]", declsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "CounterLoopInit");
      for (Node.VariableDeclaration decl : this.declarations) {
        decl.toDot(builder);
        builder.addEdge(this.hashCode(), decl.hashCode());
      }
    }
  }

  public static class ExpressionList extends Node {
    ArrayList<ExpressionNode> expressions;

    public ExpressionList(ArrayList<ExpressionNode> expressions) {
      this.expressions = expressions;
    }

    public ExpressionList(ExpressionNode expr) {
      this.expressions = new ArrayList<>();
      this.expressions.add(expr);
    }

    public ExpressionList() {
      this.expressions = new ArrayList<>();
    }

    public ExpressionList add(ExpressionNode expr) {
      this.expressions.add(expr);
      return this;
    }

    public String toString() {
      String exprsString = String.join("\n", this.expressions.toString());
      return String.format("[ExpressionList: %s]", exprsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ExpressionList");
      for (ExpressionNode expr : this.expressions) {
        expr.toDot(builder);
        builder.addEdge(this.hashCode(), expr.hashCode());
      }
    }
  }

  public static class ObjectLiteralFieldList extends Node {
    ArrayList<ObjectLiteralField> fields;

    public ObjectLiteralFieldList(ArrayList<ObjectLiteralField> fields) {
      this.fields = fields;
    }

    public ObjectLiteralFieldList(ObjectLiteralField field) {
      this.fields = new ArrayList<>();
      this.fields.add(field);
    }

    public ObjectLiteralFieldList() {
      this.fields = new ArrayList<>();
    }

    public ObjectLiteralFieldList add(ObjectLiteralField field) {
      this.fields.add(field);
      return this;
    }

    public String toString() {
      String fieldsString = String.join("\n", this.fields.toString());
      return String.format("[ObjectLiteralFieldList %s]", fieldsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ObjectLiteralFieldList");
      for (ObjectLiteralField field : this.fields) {
        field.toDot(builder);
        builder.addEdge(this.hashCode(), field.hashCode());
      }
    }
  }

  public static class ObjectLiteralField extends Node {
    String lexeme;
    ExpressionNode expression;

    public ObjectLiteralField(String lexeme, ExpressionNode expression) {
      this.lexeme = lexeme;
      this.expression = expression;
    }

    public String toString() {
      return String.format("[ObjectLiteralField: %s %s]", this.lexeme, this.expression.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ObjectLiteralField [lexeme=" + this.lexeme + "]");
      this.expression.toDot(builder);
      builder.addEdge(this.hashCode(), this.expression.hashCode());
    }
  }

  public static class ParameterList extends Node {
    ArrayList<StatementNode.VariableDeclarationHeader> declarations;

    public ParameterList(ArrayList<StatementNode.VariableDeclarationHeader> declarations) {
      this.declarations = declarations;
    }

    public ParameterList(StatementNode.VariableDeclarationHeader decl) {
      this.declarations = new ArrayList<>();
      this.declarations.add(decl);
    }

    public ParameterList add(StatementNode.VariableDeclarationHeader decl) {
      this.declarations.add(decl);
      return this;
    }

    public ParameterList() {
      this.declarations = new ArrayList<>();
    }

    public String toString() {
      String declsString = String.join("\n", this.declarations.toString());
      return String.format("[ParameterList: %s]", declsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ParameterList");
      for (StatementNode.VariableDeclarationHeader decl : this.declarations) {
        decl.toDot(builder);
        builder.addEdge(this.hashCode(), decl.hashCode());
      }
    }
  }

  public static class IfStatementBranches extends Node {
    LinkedList<IfStatementBranch> clauses;

    public IfStatementBranches(LinkedList<IfStatementBranch> clauses) {
      this.clauses = clauses;
    }

    public IfStatementBranches(IfStatementBranch clause) {
      this.clauses = new LinkedList<>();
      this.clauses.add(clause);
    }

    public IfStatementBranches() {
      this.clauses = new LinkedList<>();
    }

    IfStatementBranches add(IfStatementBranch newClause) {
      clauses.add(newClause);
      return this;
    }

    public String toString() {
      return String.format("[IfStatementBranches: %s]", this.clauses.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "IfStatementBranches");

      for (IfStatementBranch clause : this.clauses) {
        clause.toDot(builder);
        builder.addEdge(this.hashCode(), clause.hashCode());
      }
    }
  }

  public static class IfStatementBranch extends Node {
    ExpressionNode condition;
    StatementNode body;

    IfStatementBranch(ExpressionNode condition, StatementNode body) {
      this.condition = condition;
      this.body = body;
    }

    public String toString() {
      return String.format("[IfStatementBranch: %s %s", this.condition.toString(), this.body.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "IfStatementBranch");
      this.condition.toDot(builder);
      builder.addEdge(this.hashCode(), this.condition.hashCode());
      this.body.toDot(builder);
      builder.addEdge(this.hashCode(), this.body.hashCode());
    }
  }

  public static class PropertyList extends Node {
    ArrayList<PropertyDefinition> definitions;

    PropertyList(ArrayList<PropertyDefinition> definitions) {
      this.definitions = definitions;
    }

    PropertyList(PropertyDefinition definition) {
      this.definitions = new ArrayList<>();
      this.definitions.add(definition);
    }

    PropertyList() {
      this.definitions = new ArrayList<>();
    }

    public PropertyList add(PropertyDefinition definition) {
      this.definitions.add(definition);
      return this;
    }

    public String toString() {
      String definitionsString = String.join("\n", this.definitions.toString());
      return String.format("[PropertyList: %s]", definitionsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "PropertyList");

      for (PropertyDefinition definition : definitions) {
        definition.toDot(builder);
        builder.addEdge(this.hashCode(), definition.hashCode());
      }
    }
  }

  public static class PropertyDefinition extends Node {
    String lexeme;
    TypeExpressionNode type;

    PropertyDefinition(String lexeme, TypeExpressionNode type) {
      this.lexeme = lexeme;
      this.type = type;
    }

    public String toString() {
      return String.format("[PropertyDefinition: %s %s]", this.lexeme, this.type.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "PropertyDefinition [lexeme=" + this.lexeme + "]");
      this.type.toDot(builder);
      builder.addEdge(this.hashCode(), this.type.hashCode());
    }
  }

  public static class LambdaParamterList extends Node {
    ArrayList<TypeExpressionNode> parameters;

    LambdaParamterList(ArrayList<TypeExpressionNode> parameters) {
      this.parameters = parameters;
    }

    LambdaParamterList(TypeExpressionNode parameter) {
      this.parameters = new ArrayList<>();
      this.parameters.add(parameter);
    }

    LambdaParamterList() {
      this.parameters = new ArrayList<>();
    }

    public LambdaParamterList add(TypeExpressionNode parameter) {
      this.parameters.add(parameter);
      return this;
    }

    public String toString() {
      return String.format("[LambdaParamterList: %s]", this.parameters.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "LambdaParamterList");

      for (TypeExpressionNode parameter : this.parameters) {
        parameter.toDot(builder);
        builder.addEdge(this.hashCode(), parameter.hashCode());
      }
    }
  }
}