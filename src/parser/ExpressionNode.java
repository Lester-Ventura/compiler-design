package parser;

import java.util.ArrayList;
import java.util.HashMap;
import interpreter.ExecutionContext;
import interpreter.InterpreterError;
import interpreter.LoLangValue;
import interpreter.Environment.SymbolTableEntry;
import lexer.Token;
import lexer.TokenType;
import semantic.LoLangType;
import semantic.SemanticAnalysisError;
import semantic.SemanticContext;
import utils.DOTGenerator;

public abstract class ExpressionNode extends Node {
  abstract LoLangValue evaluate(ExecutionContext context);

  abstract LoLangType evaluateType(SemanticContext context);

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

    public LoLangType evaluateType(SemanticContext context) {
      ArrayList<LoLangType> parameterTypes = new ArrayList<>();

      for (Node.VariableDeclarationHeader parameter : this.parameters.declarations)
        parameterTypes.add(parameter.type.evaluate(context));

      // We also need to verify the body
      SemanticContext forkedContext = context.cleanFunctionFork(this.returnType.evaluate(context));
      this.body.semanticAnalysis(forkedContext);

      return new LoLangType.Lambda(this.returnType.evaluate(context), parameterTypes);
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

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType elementType = this.expressions.expressions.get(0).evaluateType(context);

      for (int i = 1; i < this.expressions.expressions.size(); i++) {
        LoLangType nextType = this.expressions.expressions.get(i).evaluateType(context);
        if (!elementType.isEquivalent(nextType))
          throw new SemanticAnalysisError("All elements of array must be of the same type");
      }

      return new LoLangType.Array(elementType);
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
        fields.put(field.identifier.lexeme, field.expression.evaluate(context));

      return new LoLangValue.Object(fields);
    }

    public LoLangType evaluateType(SemanticContext context) {
      HashMap<String, LoLangType> fields = new HashMap<>();

      for (Node.ObjectLiteralField field : this.fields.fields)
        fields.put(field.identifier.lexeme, field.expression.evaluateType(context));

      return new LoLangType.Object(fields);
    }
  }

  public static class DotAccess extends ExpressionNode {
    ExpressionNode left;
    Token identifier;

    DotAccess(ExpressionNode left, Token identifier) {
      this.left = left;
      this.identifier = identifier;
    }

    public String toString() {
      return String.format("[DotAccess: %s %s]", this.left.toString(),
          this.identifier.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "DotAccess [lexeme=" + this.identifier.lexeme.replace("\"", "\'") + "]");
      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());
    }

    LoLangValue evaluate(ExecutionContext context) {
      LoLangValue left = this.left.evaluate(context);
      if ((left instanceof LoLangValue.DotGettable) == false)
        throw new InterpreterError("Cannot access dot on non-object");

      return ((LoLangValue.DotGettable) left).getDot(this.identifier.lexeme);
    }

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType left = this.left.evaluateType(context);
      if (!(left instanceof LoLangType.DotGettable))
        throw new SemanticAnalysisError("Left hand side of dot access must be an object");

      LoLangType.DotGettable gettable = (LoLangType.DotGettable) left;

      if (!(gettable.hasKey(this.identifier.lexeme)))
        throw new SemanticAnalysisError("Cannot access property \"" + this.identifier.lexeme + "\"on non-object");

      return gettable.getKey(this.identifier.lexeme);
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
      this.parameters = new ExpressionList();
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

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType returnType = this.left.evaluateType(context);

      if (!(returnType instanceof LoLangType.Lambda))
        throw new SemanticAnalysisError("Function expression must return a lambda");

      LoLangType.Lambda lambda = (LoLangType.Lambda) returnType;

      if (lambda.parameterList.size() != this.parameters.expressions.size())
        throw new SemanticAnalysisError("Incorrect number of parameters passed to function");

      for (int i = 0; i < lambda.parameterList.size(); i++) {
        LoLangType parameterType = lambda.parameterList.get(i);
        ExpressionNode argumentType = this.parameters.expressions.get(i);
        if (!parameterType.isEquivalent(argumentType.evaluateType(context)))
          throw new SemanticAnalysisError("Incorrect parameter type passed to function");
      }

      return lambda.returnType;
    }
  }

  public static class Identifier extends ExpressionNode {
    Token identifier;

    Identifier(Token identifier) {
      this.identifier = identifier;
    }

    public String toString() {
      return String.format("[Identifier: %s]", this.identifier.lexeme);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "Identifier [lexeme=" + this.identifier.lexeme.replace("\"", "\'") + "]");
    }

    public LoLangValue evaluate(ExecutionContext context) {
      return context.environment.get(this.identifier.lexeme);
    }

    public LoLangType evaluateType(SemanticContext context) {
      return context.variableEnvironment.get(this.identifier.lexeme);
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

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType left = this.left.evaluateType(context);
      if (!(left instanceof LoLangType.Number))
        throw new SemanticAnalysisError("Left hand side of incrementation must be a number");

      return left;
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

    public LoLangType evaluateType(SemanticContext context) {
      if (this.left instanceof ExpressionNode.Identifier) {
        String identifier = ((ExpressionNode.Identifier) this.left).identifier.lexeme;
        SymbolTableEntry<LoLangType> entry = context.variableEnvironment.getSymbolTableEntry(identifier);

        if (entry.constant)
          throw new SemanticAnalysisError("Cannot assign to constant variable");
      }

      LoLangType left = this.left.evaluateType(context);
      LoLangType right = this.right.evaluateType(context);

      if (left.isEquivalent(right) == false)
        throw new SemanticAnalysisError("Cannot assign to different types");

      return left;
    }
  }

  public static LoLangValue setValue(ExecutionContext context, ExpressionNode left, LoLangValue newValue) {
    if (left instanceof ExpressionNode.Identifier) {
      ExpressionNode.Identifier identifier = (ExpressionNode.Identifier) left;
      context.environment.assign(identifier.identifier.lexeme, newValue);
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
      object.setDot(dotAccess.identifier.lexeme, newValue);
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

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType left = this.left.evaluateType(context);
      LoLangType right = this.right.evaluateType(context);

      if (left instanceof LoLangType.Array && right instanceof LoLangType.Number) {
        LoLangType.Array array = (LoLangType.Array) left;
        return array.elementType;
      }

      throw new SemanticAnalysisError("Invalid index access on " + left.getClass().getName());
    }
  }

  public static class Literal extends ExpressionNode {
    Token identifier;

    Literal(Token identifier) {
      this.identifier = identifier;
    }

    public String toString() {
      return String.format("[Literal: %s (%s)]", this.identifier.lexeme.replace("\"", "\'"),
          this.identifier.type.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "Literal [lexeme=" + this.identifier.lexeme.replace("\"", "\'") + "] [type=" + this.identifier.type.toString()
              + "]");
    }

    LoLangValue evaluate(ExecutionContext context) {
      switch (this.identifier.type) {
        case STRING_LITERAL:
          return new LoLangValue.String(this.identifier.lexeme);
        case BOOLEAN_LITERAL:
          return new LoLangValue.Boolean(this.identifier.lexeme.equals("faker"));
        case NULL_LITERAL:
          return new LoLangValue.Null();
        case NUMBER_LITERAL:
          return new LoLangValue.Number(parseNumber(this.identifier.lexeme));
        default:
          throw new InterpreterError("Invalid token literal type");
      }
    }

    public LoLangType evaluateType(SemanticContext context) {
      switch (this.identifier.type) {
        case STRING_LITERAL:
          return new LoLangType.String();
        case BOOLEAN_LITERAL:
          return new LoLangType.Boolean();
        case NULL_LITERAL:
          return new LoLangType.Null();
        case NUMBER_LITERAL:
          return new LoLangType.Number();
        default:
          throw new SemanticAnalysisError("Invalid token literal type");
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

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType left = this.left.evaluateType(context);
      LoLangType right = this.right.evaluateType(context);

      if (left instanceof LoLangType.Number && right instanceof LoLangType.Number) {
        if ((this.operation.lexeme.equals("+"))
            || (this.operation.lexeme.equals("-"))
            || (this.operation.lexeme.equals("*"))
            || (this.operation.lexeme.equals("/"))
            || (this.operation.lexeme.equals("%"))
            || (this.operation.lexeme.equals("**"))
            || (this.operation.lexeme.equals("&"))
            || (this.operation.lexeme.equals("|"))
            || (this.operation.lexeme.equals("^")))
          return new LoLangType.Number();

        if ((this.operation.lexeme.equals("<"))
            || (this.operation.lexeme.equals(">"))
            || (this.operation.lexeme.equals("<="))
            || (this.operation.lexeme.equals(">="))
            || (this.operation.lexeme.equals("=="))
            || (this.operation.lexeme.equals("!=")))
          return new LoLangType.Boolean();
      }

      if (left instanceof LoLangType.Boolean && right instanceof LoLangType.Boolean) {
        return new LoLangType.Boolean();
      }

      if (left instanceof LoLangType.String && right instanceof LoLangType.String) {
        return new LoLangType.String();
      }

      throw new SemanticAnalysisError("Invalid binary operation \"" + this.operation.lexeme + "\" on "
          + left.getClass().getName() + ", " + right.getClass().getName());
    }
  }

  public static class Unary extends ExpressionNode {
    ExpressionNode operand;
    Token operationToken;

    Unary(Token operationToken, ExpressionNode operand) {
      this.operand = operand;
      this.operationToken = operationToken;
    }

    public String toString() {
      return String.format("[Unary: %s %s]", this.operationToken.lexeme, this.operand.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Unary [operation: " + this.operationToken.lexeme + "]");
      this.operand.toDot(builder);
      builder.addEdge(this.hashCode(), this.operand.hashCode());
    }

    public LoLangValue evaluate(ExecutionContext context) {
      LoLangValue operand = this.operand.evaluate(context);
      String lexeme = this.operationToken.lexeme;

      if (operand instanceof LoLangValue.Number && lexeme.equals("-"))
        return new LoLangValue.Number(-1 * ((LoLangValue.Number) operand).value);

      else if (operand instanceof LoLangValue.Boolean && lexeme.equals("!"))
        return new LoLangValue.Boolean(!((LoLangValue.Boolean) operand).value);

      throw new InterpreterError("Invalid operation \"" + lexeme + "\" on " + operand.getClass().getName());
    }

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType operandType = this.operand.evaluateType(context);
      String lexeme = this.operationToken.lexeme;

      if (operandType instanceof LoLangType.Number && lexeme.equals("-"))
        return new LoLangType.Number();

      else if (operandType instanceof LoLangType.Boolean && lexeme.equals("!"))
        return new LoLangType.Boolean();

      throw new SemanticAnalysisError(
          "Invalid operation \"" + lexeme + "\" on type " + operandType.getClass().getName());
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

    public LoLangType evaluateType(SemanticContext context) {
      return this.expression.evaluateType(context);
    }
  }
}
