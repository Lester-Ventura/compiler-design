import java.util.ArrayList;

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
      return String.format("[ArrayLiteral:\n%s]", expressionsString);
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
      return String.format("[ObjectLiteral:\n%s]", fieldsString);
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
      return String.format("[DotAccess: %s.%s]", this.left.toString(), this.right);
    }
  }

  public static class FunctionCall extends ExpressionNode {
    ExpressionNode left;
    ExpressionList parameters;

    FunctionCall(ExpressionNode left, ExpressionList parameters) {
      this.left = left;
      this.parameters = parameters;
    }

    public String toString() {
      String parametersString = String.join(", ", this.parameters.toString());
      return String.format("[FunctionCall: %s(%s)]", this.left.toString(), parametersString);
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
  }

  public static class Literal extends ExpressionNode {
    Token token;

    Literal(Token token) {
      this.token = token;
    }

    public String toString() {
      return String.format("[Literal: %s]", this.token.lexeme.toString());
    }
  }

  public static class Binary extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    String operation;

    Binary(ExpressionNode left, String operation, ExpressionNode right) {
      this.left = left;
      this.right = right;
      this.operation = operation;
    }

    public String toString() {
      return String.format("[Binary: %s %s %s]", this.left.toString(), this.operation, this.right.toString());
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
  }

  public static class Grouping extends ExpressionNode {
    ExpressionNode expression;

    Grouping(ExpressionNode expression) {
      this.expression = expression;
    }

    public String toString() {
      return String.format("[Grouping: %s]", this.expression.toString());
    }
  }

}
