package parser;

import lexer.Token;
import utils.DOTGenerator;

public abstract class ExpressionNode extends Node {
  public static class FunctionExpression extends ExpressionNode {
    Node.ParameterList parameters;
    TypeExpressionNode returnType;
    StatementNode body;

    FunctionExpression(Node.ParameterList parameters, TypeExpressionNode returnType, StatementNode body) {
      this.parameters = parameters;
      this.returnType = returnType;
      this.body = body;
    }

    FunctionExpression(TypeExpressionNode returnType, StatementNode body) {
      this.parameters = new Node.ParameterList();
      this.returnType = returnType;
      this.body = body;
    }

    public String toString() {
      String parametersString = String.join("\n", this.parameters.toString());
      return String.format("[FunctionExpression: (%s) -> %s]", parametersString, this.returnType.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "FunctionExpression");
      this.parameters.toDot(builder);
      builder.addEdge(this.hashCode(), this.parameters.hashCode());

      this.returnType.toDot(builder);
      builder.addEdge(this.hashCode(), this.returnType.hashCode());

      this.body.toDot(builder);
      builder.addEdge(this.hashCode(), this.body.hashCode());
    }
  }

  public static class ArrayLiteral extends ExpressionNode {
    ExpressionList expressions;

    ArrayLiteral(ExpressionList expressions) {
      this.expressions = expressions;
    }

    ArrayLiteral() {
      this.expressions = new ExpressionList();
    }

    public String toString() {
      String expressionsString = String.join("\n", this.expressions.toString());
      return String.format("[ArrayLiteral: %s]", expressionsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ArrayLiteral");
      this.expressions.toDot(builder);
      builder.addEdge(this.hashCode(), this.expressions.hashCode());
    }
  }

  public static class ObjectLiteral extends ExpressionNode {
    ObjectLiteralFieldList fields;

    ObjectLiteral(ObjectLiteralFieldList fields) {
      this.fields = fields;
    }

    ObjectLiteral() {
      this.fields = new ObjectLiteralFieldList();
    }

    public String toString() {
      String fieldsString = String.join("\n", this.fields.toString());
      return String.format("[ObjectLiteral: %s]", fieldsString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "ObjectLiteral");
      this.fields.toDot(builder);
      builder.addEdge(this.hashCode(), this.fields.hashCode());
    }
  }

  public static class DotAccess extends ExpressionNode {
    ExpressionNode left;
    String right;

    DotAccess(ExpressionNode left, String right) {
      this.left = left;
      this.right = right;
    }

    public String toString() {
      return String.format("[DotAccess: %s %s]", this.left.toString(),
          this.right.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "DotAccess [lexeme=" + this.right.replace("\"", "\'") + "]");
      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());
    }
  }

  public static class FunctionCall extends ExpressionNode {
    ExpressionNode left;
    ExpressionList parameters;

    FunctionCall(ExpressionNode left, ExpressionList parameters) {
      this.left = left;
      this.parameters = parameters;
    }

    FunctionCall(ExpressionNode left) {
      this.left = left;
      this.parameters = null;
    }

    public String toString() {
      String parametersString = this.parameters != null ? String.join(", ", this.parameters.toString()) : "";
      return String.format("[FunctionCall: %s(%s)]", this.left.toString(), parametersString);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "FunctionCall");
      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());

      if (this.parameters != null) {
        this.parameters.toDot(builder);
        builder.addEdge(this.hashCode(), this.parameters.hashCode());
      }
    }
  }

  public static class Identifier extends ExpressionNode {
    String lexeme;

    Identifier(String lexeme) {
      this.lexeme = lexeme;
    }

    public String toString() {
      return String.format("[Identifier: %s]", this.lexeme);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "Identifier [lexeme=" + this.lexeme.replace("\"", "\'") + "]");
    }
  }

  public static class Incrementation extends ExpressionNode {
    ExpressionNode left;
    Token token;
    boolean isPostfix;

    Incrementation(ExpressionNode left, Token token, boolean isPostfix) {
      this.left = left;
      this.token = token;
      this.isPostfix = isPostfix;
    }

    public String toString() {
      return String.format("[Incrementation: %s%s]",
          this.isPostfix ? this.left.toString() : this.token.toString(),
          !this.isPostfix ? this.left.toString() : this.token.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), String.format("Incrementation [operation=%s] [order=%s]",
          this.token.lexeme.replace("\"", "\'"),
          this.isPostfix ? "postfix" : "prefix"));

      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());
    }
  }

  public static class Assignment extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;

    Assignment(ExpressionNode left, ExpressionNode right) {
      this.left = left;
      this.right = right;
    }

    public String toString() {
      return String.format("[Assignment: %s = %s]", this.left.toString(), this.right.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Assignment");
      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());

      this.right.toDot(builder);
      builder.addEdge(this.hashCode(), this.right.hashCode());
    }
  }

  public static class IndexAccess extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;

    IndexAccess(ExpressionNode left, ExpressionNode right) {
      this.left = left;
      this.right = right;
    }

    public String toString() {
      return String.format("[IndexAccess: (%s)[%s]]", this.left.toString(), this.right.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "IndexAccess");
      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());

      this.right.toDot(builder);
      builder.addEdge(this.hashCode(), this.right.hashCode());
    }
  }

  public static class Literal extends ExpressionNode {
    Token token;

    Literal(Token token) {
      this.token = token;
    }

    public String toString() {
      return String.format("[Literal: %s]", this.token.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "Literal [lexeme=" + this.token.lexeme.replace("\"", "\'") + "]");
    }
  }

  public static class Binary extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    Token operation;

    Binary(ExpressionNode left, Token operation, ExpressionNode right) {
      this.left = left;
      this.right = right;
      this.operation = operation;
    }

    public String toString() {
      return String.format("[Binary: %s %s %s]", this.left.toString(), this.operation, this.right.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Binary [operation: " + this.operation.type.toString() + "]");
      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());

      this.right.toDot(builder);
      builder.addEdge(this.hashCode(), this.right.hashCode());
    }
  }

  public static class Unary extends ExpressionNode {
    ExpressionNode operand;
    String operation;

    Unary(String operation, ExpressionNode operand) {
      this.operand = operand;
      this.operation = operation;
    }

    public String toString() {
      return String.format("[Unary: %s %s]", this.operation, this.operand.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Unary [operation: " + this.operation + "]");
      this.operand.toDot(builder);
      builder.addEdge(this.hashCode(), this.operand.hashCode());
    }
  }

  public static class Grouping extends ExpressionNode {
    ExpressionNode expression;

    Grouping(ExpressionNode expression) {
      this.expression = expression;
    }

    public String toString() {
      return String.format("[Grouping: %s]", this.expression.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Grouping");
      this.expression.toDot(builder);
      builder.addEdge(this.hashCode(), this.expression.hashCode());
    }
  }
}
