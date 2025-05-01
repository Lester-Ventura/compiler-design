package parser;

import java.util.ArrayList;
import java.util.HashMap;
import interpreter.ExecutionContext;
import interpreter.InterpreterError;
import interpreter.LoLangValue;
import lexer.Token;
import lexer.TokenType;
import utils.DOTGenerator;

public abstract class ExpressionNode extends Node {
  abstract LoLangValue evaluate(ExecutionContext context);

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

    LoLangValue evaluate(ExecutionContext context) {
      return new LoLangValue.UserDefinedFunction(this.parameters, this.body, context);
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

    LoLangValue evaluate(ExecutionContext context) {
      ArrayList<LoLangValue> values = new ArrayList<>();

      for (ExpressionNode expression : this.expressions.expressions)
        values.add(expression.evaluate(context));

      return new LoLangValue.Array(values);
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

    LoLangValue evaluate(ExecutionContext context) {
      HashMap<String, LoLangValue> fields = new HashMap<>();

      for (Node.ObjectLiteralField field : this.fields.fields)
        fields.put(field.lexeme, field.expression.evaluate(context));

      return new LoLangValue.Object(fields);
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

    LoLangValue evaluate(ExecutionContext context) {
      LoLangValue left = this.left.evaluate(context);
      if ((left instanceof LoLangValue.DotGettable) == false)
        throw new InterpreterError("Cannot access dot on non-object");

      return ((LoLangValue.DotGettable) left).getDot(this.right);
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

    public LoLangValue evaluate(ExecutionContext context) {
      LoLangValue left = this.left.evaluate(context);

      if (!(left instanceof LoLangValue.Callable))
        throw new InterpreterError("Cannot call non-callable value");

      LoLangValue.Callable callable = (LoLangValue.Callable) left;
      ArrayList<LoLangValue> arguments = new ArrayList<>();

      if (this.parameters != null) {
        for (ExpressionNode parameter : this.parameters.expressions)
          arguments.add(parameter.evaluate(context));
      }

      return callable.call(arguments);
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

    public LoLangValue evaluate(ExecutionContext context) {
      return context.environment.get(this.lexeme);
    }
  }

  public static class Incrementation extends ExpressionNode {
    ExpressionNode left;
    boolean isIncrement;
    boolean isPostfix;

    Incrementation(ExpressionNode left, Token token, boolean isPostfix) {
      this.left = left;
      this.isIncrement = token.type == TokenType.DOUBLE_PLUS;
      this.isPostfix = isPostfix;
    }

    public String toString() {
      String symbol = this.isIncrement ? "++" : "--";

      return String.format("[Incrementation: %s%s]",
          this.isPostfix ? this.left.toString() : symbol,
          !this.isPostfix ? this.left.toString() : symbol);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), String.format("Incrementation [operation=%s] [order=%s]",
          (this.isIncrement ? "++" : "--").replace("\"", "\'"),
          this.isPostfix ? "postfix" : "prefix"));

      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());
    }

    public LoLangValue evaluate(ExecutionContext context) {
      LoLangValue left = this.left.evaluate(context);

      if (!(left instanceof LoLangValue.Number))
        throw new InterpreterError(String.format("Cannot %s non-number", this.isIncrement ? "increment" : "decrement"));

      double value = ((LoLangValue.Number) left).value;
      double modifier = this.isIncrement ? 1 : -1;

      double newValue = value + modifier;
      setValue(context, this.left, new LoLangValue.Number(newValue));

      return new LoLangValue.Number(
          this.isPostfix ? value : newValue);
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

    public LoLangValue evaluate(ExecutionContext context) {
      LoLangValue newValue = this.right.evaluate(context);
      return setValue(context, left, newValue);
    }
  }

  public static LoLangValue setValue(ExecutionContext context, ExpressionNode left, LoLangValue newValue) {
    if (left instanceof ExpressionNode.Identifier) {
      ExpressionNode.Identifier identifier = (ExpressionNode.Identifier) left;
      context.environment.assign(identifier.lexeme, newValue);
    } else if (left instanceof ExpressionNode.IndexAccess) {
      ExpressionNode.IndexAccess indexAccess = (ExpressionNode.IndexAccess) left;
      LoLangValue leftValue = indexAccess.left.evaluate(context);

      if (!(leftValue instanceof LoLangValue.Array))
        throw new InterpreterError("Invalid left-hand side of assignment");

      LoLangValue.Array array = (LoLangValue.Array) leftValue;
      double index = ((LoLangValue.Number) indexAccess.right.evaluate(context)).value;
      array.setIndex((int) Math.max(Math.round(index), 0), newValue);
    } else if (left instanceof ExpressionNode.DotAccess) {
      ExpressionNode.DotAccess dotAccess = (ExpressionNode.DotAccess) left;
      LoLangValue leftValue = dotAccess.left.evaluate(context);

      if (!(leftValue instanceof LoLangValue.Object))
        throw new InterpreterError("Invalid left-hand side of assignment");

      LoLangValue.Object object = (LoLangValue.Object) leftValue;
      object.setDot(dotAccess.right, newValue);
    } else {
      throw new InterpreterError("Invalid left-hand side of assignment");
    }

    return newValue;
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

    public LoLangValue evaluate(ExecutionContext context) {
      LoLangValue left = this.left.evaluate(context);
      LoLangValue right = this.right.evaluate(context);

      if (left instanceof LoLangValue.Array && right instanceof LoLangValue.Number) {
        LoLangValue.Array array = (LoLangValue.Array) left;
        double index = ((LoLangValue.Number) right).value;

        return array.getIndex((int) Math.max(Math.round(index), 0));
      }

      throw new InterpreterError("Invalid index access on " + left.getClass().getName());
    }
  }

  public static class Literal extends ExpressionNode {
    String lexeme;
    TokenType type;

    Literal(String lexeme, TokenType type) {
      this.lexeme = lexeme;
      this.type = type;
    }

    public String toString() {
      return String.format("[Literal: %s (%s)]", this.lexeme.replace("\"", "\'"), this.type.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "Literal [lexeme=" + this.lexeme.replace("\"", "\'") + "] [type=" + this.type.toString() + "]");
    }

    LoLangValue evaluate(ExecutionContext context) {
      switch (this.type) {
        case STRING_LITERAL:
          return new LoLangValue.String(this.lexeme);
        case BOOLEAN_LITERAL:
          return new LoLangValue.Boolean(Boolean.parseBoolean(this.lexeme));
        case NULL_LITERAL:
          return new LoLangValue.Null();
        case NUMBER_LITERAL:
          return new LoLangValue.Number(parseNumber(this.lexeme));
        default:
          throw new InterpreterError("Invalid token literal type");
      }
    }
  }

  public static double parseNumber(String lexeme) {
    // handle if number is in hex format / octal format / binary format
    if (lexeme.startsWith("0x") || lexeme.startsWith("0X"))
      return Integer.parseInt(lexeme.substring(2), 16);
    else if (lexeme.startsWith("0o") || lexeme.startsWith("0O"))
      return Integer.parseInt(lexeme.substring(2), 8);
    else if (lexeme.startsWith("0b") || lexeme.startsWith("0B"))
      return Integer.parseInt(lexeme.substring(2), 2);
    return Double.parseDouble(lexeme);
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

    public LoLangValue evaluate(ExecutionContext context) {
      LoLangValue left = this.left.evaluate(context);
      LoLangValue right = this.right.evaluate(context);

      if (left instanceof LoLangValue.Number && right instanceof LoLangValue.Number) {
        LoLangValue.Number leftNumber = (LoLangValue.Number) left;
        LoLangValue.Number rightNumber = (LoLangValue.Number) right;

        if (this.operation.lexeme.equals("+"))
          return new LoLangValue.Number(leftNumber.value + rightNumber.value);
        else if (this.operation.lexeme.equals("-"))
          return new LoLangValue.Number(leftNumber.value - rightNumber.value);
        else if (this.operation.lexeme.equals("*"))
          return new LoLangValue.Number(leftNumber.value * rightNumber.value);
        else if (this.operation.lexeme.equals("/"))
          return new LoLangValue.Number(leftNumber.value / rightNumber.value);
        else if (this.operation.lexeme.equals("%"))
          return new LoLangValue.Number(leftNumber.value % rightNumber.value);
        else if (this.operation.lexeme.equals("**"))
          return new LoLangValue.Number(Math.pow(leftNumber.value, rightNumber.value));
        else if (this.operation.lexeme.equals("&"))
          return new LoLangValue.Number((int) leftNumber.value & (int) rightNumber.value);
        else if (this.operation.lexeme.equals("|"))
          return new LoLangValue.Number((int) leftNumber.value | (int) rightNumber.value);
        else if (this.operation.lexeme.equals("^"))
          return new LoLangValue.Number((int) leftNumber.value ^ (int) rightNumber.value);

        else if (this.operation.lexeme.equals("<"))
          return new LoLangValue.Boolean(leftNumber.value < rightNumber.value);
        else if (this.operation.lexeme.equals(">"))
          return new LoLangValue.Boolean(leftNumber.value > rightNumber.value);
        else if (this.operation.lexeme.equals("<="))
          return new LoLangValue.Boolean(leftNumber.value <= rightNumber.value);
        else if (this.operation.lexeme.equals(">="))
          return new LoLangValue.Boolean(leftNumber.value >= rightNumber.value);
        else if (this.operation.lexeme.equals("=="))
          return new LoLangValue.Boolean(leftNumber.value == rightNumber.value);
        else if (this.operation.lexeme.equals("!="))
          return new LoLangValue.Boolean(leftNumber.value != rightNumber.value);

        throw new InterpreterError("Invalid binary operation \"" + this.operation.lexeme + "\" on Number, Number");
      }

      if (left instanceof LoLangValue.Boolean && right instanceof LoLangValue.Boolean) {
        LoLangValue.Boolean leftBoolean = (LoLangValue.Boolean) left;
        LoLangValue.Boolean rightBoolean = (LoLangValue.Boolean) right;

        if (this.operation.lexeme.equals("&&"))
          return new LoLangValue.Boolean(leftBoolean.value && rightBoolean.value);
        else if (this.operation.lexeme.equals("||"))
          return new LoLangValue.Boolean(leftBoolean.value || rightBoolean.value);
        else if (this.operation.lexeme.equals("=="))
          return new LoLangValue.Boolean(leftBoolean.value == rightBoolean.value);
        else if (this.operation.lexeme.equals("!="))
          return new LoLangValue.Boolean(leftBoolean.value != rightBoolean.value);

        throw new InterpreterError("Invalid binary operation \"" + this.operation.lexeme + "\" on Boolean, Boolean");
      }

      if (left instanceof LoLangValue.String && right instanceof LoLangValue.String) {
        LoLangValue.String leftString = (LoLangValue.String) left;
        LoLangValue.String rightString = (LoLangValue.String) right;

        if (this.operation.lexeme.equals("+"))
          return new LoLangValue.String(leftString.value + rightString.value);

        throw new InterpreterError("Invalid binary operation \"" + this.operation.lexeme + "\" on String, String");
      }

      throw new InterpreterError("Invalid binary operation \"" + this.operation.lexeme + "\" on "
          + left.getClass().getName() + ", " + right.getClass().getName());
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

    public LoLangValue evaluate(ExecutionContext context) {
      LoLangValue operand = this.operand.evaluate(context);

      if (operand instanceof LoLangValue.Number && this.operation.equals("-"))
        return new LoLangValue.Number(-1 * ((LoLangValue.Number) operand).value);

      else if (operand instanceof LoLangValue.Boolean && this.operation.equals("!"))
        return new LoLangValue.Boolean(!((LoLangValue.Boolean) operand).value);

      throw new InterpreterError("Invalid operation \"" + this.operation + "\" on " + operand.getClass().getName());
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

    public LoLangValue evaluate(ExecutionContext context) {
      return this.expression.evaluate(context);
    }
  }
}
