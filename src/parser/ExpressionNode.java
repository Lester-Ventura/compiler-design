package parser;

import java.util.ArrayList;
import java.util.HashMap;
import interpreter.ExecutionContext;
import interpreter.InterpreterExceptions;
import interpreter.RuntimeError;
import interpreter.LoLangValue;
import lexer.Token;
import lexer.TokenType;
import semantic.LoLangType;
import semantic.SemanticAnalyzerException;
import semantic.SemanticAnalyzerException.GenericReturnTypeArityException;
import semantic.SemanticAnalyzerException.GenericReturnTypeException;
import semantic.SemanticAnalyzerException.GenericReturnTypeParameterMismatchException;
import semantic.SemanticContext;
import utils.DOTGenerator;
import utils.EnvironmentException;
import utils.Environment.SymbolTableEntry;

public abstract class ExpressionNode extends Node {
  abstract LoLangValue evaluate(ExecutionContext staticContext, ExecutionContext dynamicContext);

  abstract LoLangType evaluateType(SemanticContext context);

  public static class FunctionExpression extends ExpressionNode {
    Node.ParameterList parameters;
    TypeExpressionNode returnType;
    StatementNode.Block body;
    Token token;

    FunctionExpression(Node.ParameterList parameters, TypeExpressionNode returnType, StatementNode.Block body,
        Token token) {
      this.parameters = parameters;
      this.returnType = returnType;
      this.body = body;
      this.token = token;
    }

    FunctionExpression(TypeExpressionNode returnType, StatementNode.Block body, Token token) {
      this.parameters = new Node.ParameterList();
      this.returnType = returnType;
      this.body = body;
      this.token = token;
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

    LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      return new LoLangValue.UserDefinedFunction(this.parameters, this.body, context);
    }

    public LoLangType evaluateType(SemanticContext context) {
      ArrayList<LoLangType> parameterTypes = new ArrayList<>();
      SemanticContext forkedContext = context.cleanFunctionFork(this.returnType.evaluate(context));

      for (Node.VariableDeclarationHeader parameter : this.parameters.declarations) {
        parameterTypes.add(parameter.type.evaluate(context));

        try {
          forkedContext.variableEnvironment.define(parameter.identifier.lexeme, parameter.type.evaluate(context), true);
        } catch (EnvironmentException.EnvironmentAlreadyDeclaredException e) {
          context.addException(new SemanticAnalyzerException(
              "Cannot redeclare parameter \"" + parameter.identifier.lexeme + "\"", parameter.identifier));
        }
      }

      LoLangType returnTypeValue = this.returnType.evaluate(context);
      if (!returnTypeValue.isEquivalent(new LoLangType.Void())) {
        if (this.body.statements.statements.size() == 0) {
          context.addException(new SemanticAnalyzerException(
              "Function expects a return value but contains no statements", token));
        } else {
          StatementNode stmt = this.body.statements.statements.get(this.body.statements.statements.size() - 1);
          if (!(stmt instanceof StatementNode.Return) && !(stmt instanceof StatementNode.Throw)) {
            context.addException(new SemanticAnalyzerException(
                "Function should end with either feed or recast", token));
          }
        }
      }

      // We also need to verify the body
      this.body.semanticAnalysis(forkedContext);
      return new LoLangType.Lambda(returnTypeValue, parameterTypes);
    }
  }

  public static class ArrayLiteral extends ExpressionNode {
    ExpressionList expressions;
    Token leftBracket;

    ArrayLiteral(ExpressionList expressions, Token leftBracket) {
      this.expressions = expressions;
      this.leftBracket = leftBracket;
    }

    ArrayLiteral(Token leftBracket) {
      this.expressions = new ExpressionList();
      this.leftBracket = leftBracket;
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

    LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      ArrayList<LoLangValue> values = new ArrayList<>();

      for (int i = this.expressions.expressions.size() - 1; i >= 0; i--)
        values.add(this.expressions.expressions.get(i).evaluate(context, dynamicContext));

      return new LoLangValue.Array(values);
    }

    public LoLangType evaluateType(SemanticContext context) {
      if (this.expressions.expressions.size() > 0) {
        // Let the first element of the array determine the type of the array
        LoLangType elementType = this.expressions.expressions.get(0).evaluateType(context);

        for (int i = 1; i < this.expressions.expressions.size(); i++) {
          LoLangType nextType = this.expressions.expressions.get(i).evaluateType(context);

          if (!elementType.isEquivalent(nextType)) {
            context.addException(
                new SemanticAnalyzerException(
                    "All elements of array must be of the same type: " + elementType.toString(),
                    this.leftBracket));
            break;
          }
        }

        return new LoLangType.Array(elementType);
      }

      return new LoLangType.Array(new LoLangType.Any());
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

    LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      HashMap<String, LoLangValue> fields = new HashMap<>();

      for (Node.ObjectLiteralField field : this.fields.fields)
        fields.put(field.identifier.lexeme, field.expression.evaluate(context, dynamicContext));

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

    LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue left = this.left.evaluate(context, dynamicContext);
      if ((left instanceof LoLangValue.DotGettable) == false)
        throw new RuntimeError("Cannot access dot on non-object", identifier);

      try {
        return ((LoLangValue.DotGettable) left).getDot(this.identifier.lexeme);
      } catch (InterpreterExceptions.DotAccessNonExistentException e) {
        throw e.toRuntimeError(identifier);
      }
    }

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType left = this.left.evaluateType(context);
      if (!(left instanceof LoLangType.DotGettable)) {
        if (!(left instanceof LoLangType.Any))
          context.addException(new SemanticAnalyzerException("Left side of dot access must be an object, received: "
              + left.toString(), this.identifier));
        return new LoLangType.Any();
      }

      LoLangType.DotGettable gettable = (LoLangType.DotGettable) left;

      if (!(gettable.hasKey(this.identifier.lexeme))) {
        context.addException(new SemanticAnalyzerException("Cannot access property \"" + this.identifier.lexeme
            + "\"on non-object: " + left.toString(), this.identifier));
        return new LoLangType.Any();
      }

      return gettable.getKey(context, this.identifier.lexeme);
    }
  }

  public static class FunctionCall extends ExpressionNode {
    ExpressionNode left;
    ExpressionList parameters;
    Token functionCallToken;

    FunctionCall(ExpressionNode left, ExpressionList parameters, Token functionCallToken) {
      this.left = left;
      this.parameters = parameters;
      this.functionCallToken = functionCallToken;
    }

    FunctionCall(ExpressionNode left, Token functionCallToken) {
      this.left = left;
      this.parameters = new ExpressionList();
      this.functionCallToken = functionCallToken;
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

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue left = this.left.evaluate(context, dynamicContext);

      if (!(left instanceof LoLangValue.Callable))
        throw new RuntimeError("Cannot call non-callable value", functionCallToken);

      LoLangValue.Callable callable = (LoLangValue.Callable) left;

      ArrayList<LoLangValue> arguments = new ArrayList<>();
      if (this.parameters != null) {
        for (ExpressionNode parameter : this.parameters.expressions)
          arguments.add(parameter.evaluate(context, dynamicContext));
      }

      try {
        return callable.call(dynamicContext.fork(new ExecutionContext.CallStackEntry(this.functionCallToken)),
            arguments);
      } catch (InterpreterExceptions e) {
        throw e.toRuntimeError(functionCallToken);
      }
    }

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType lambdaValue = this.left.evaluateType(context);

      if (!(lambdaValue instanceof LoLangType.Lambda)) {
        if (!(lambdaValue instanceof LoLangType.Any))
          context.addException(
              new SemanticAnalyzerException(
                  "Left side of function-call expression must return a lambda, received: " + lambdaValue.toString(),
                  this.functionCallToken));
        return new LoLangType.Any();
      }

      LoLangType.Lambda lambda = (LoLangType.Lambda) lambdaValue;

      if (lambda.isGeneric && lambda.generateGenericReturnType != null) {
        ArrayList<LoLangType> argumentTypes = new ArrayList<>();
        for (int i = 0; i < this.parameters.expressions.size(); i++)
          argumentTypes.add(this.parameters.expressions.get(i).evaluateType(context));

        try {
          return lambda.generateGenericReturnType.run(context, argumentTypes);
        } catch (GenericReturnTypeArityException e) {
          context.addException(new SemanticAnalyzerException(
              String.format("Incorrect number of parameters passed to function, expected %d, received %d", e.expected,
                  e.received),
              this.functionCallToken));

          return new LoLangType.Any();
        } catch (GenericReturnTypeParameterMismatchException e) {
          context.addException(
              new SemanticAnalyzerException(
                  String.format("Incorrect parameter type passed to function at index %d, expected %s, received %s",
                      e.index, e.expected.toString(), e.received.toString()),
                  this.functionCallToken));

          return new LoLangType.Any();
        } catch (GenericReturnTypeException e) {
          // should be unreachable
          context.addException(new SemanticAnalyzerException("Error while generating generic return type",
              this.functionCallToken));
        }
      }

      if (lambda.parameterList.size() != this.parameters.expressions.size()) {
        context.addException(new SemanticAnalyzerException("Incorrect number of parameters passed to function",
            this.functionCallToken));
        return new LoLangType.Any();
      }

      ArrayList<LoLangType> parameterTypes = new ArrayList<>();

      // Verify that argument types match the parameter types
      for (int i = 0; i < lambda.parameterList.size(); i++) {
        LoLangType parameterType = lambda.parameterList.get(i);
        parameterTypes.add(parameterType);

        ExpressionNode argumentTypeNode = this.parameters.expressions.get(i);
        LoLangType argumentType = argumentTypeNode.evaluateType(context);

        if (!parameterType.isEquivalent(argumentType)) {
          int index = lambda.parameterList.size() - i - 1;
          context.addException(
              new SemanticAnalyzerException(
                  String.format("Incorrect parameter type passed to function at index %d, expected %s, received %s",
                      index, parameterType.toString(), argumentType.toString()),
                  this.functionCallToken));
        }
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

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      try {
        LoLangValue value = context.environment.get(this.identifier.lexeme);
        if (value == null)
          throw new RuntimeError("Undefined variable \"" + this.identifier.lexeme + "\"", this.identifier);

        return value;
      } catch (EnvironmentException.EnvironmentUndeclaredException e) {
        throw new RuntimeError("Undeclared variable \"" + this.identifier.lexeme + "\"", this.identifier);
      }
    }

    public LoLangType evaluateType(SemanticContext context) {
      try {
        return context.variableEnvironment.get(this.identifier.lexeme);
      } catch (EnvironmentException.EnvironmentUndeclaredException e) {
        context.addException(new SemanticAnalyzerException("Cannot find variable \"" + this.identifier.lexeme + "\"",
            this.identifier));
        return new LoLangType.Any();
      }
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

    boolean isIncrement() {
      return this.token.type == TokenType.DOUBLE_PLUS;
    }

    public String toString() {
      String symbol = isIncrement() ? "++" : "--";

      return String.format("[Incrementation: %s%s]",
          this.isPostfix ? this.left.toString() : symbol,
          !this.isPostfix ? this.left.toString() : symbol);
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), String.format("Incrementation [operation=%s] [order=%s]",
          (isIncrement() ? "++" : "--").replace("\"", "\'"),
          this.isPostfix ? "postfix" : "prefix"));

      this.left.toDot(builder);
      builder.addEdge(this.hashCode(), this.left.hashCode());
    }

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue left = this.left.evaluate(context, dynamicContext);

      if (!(left instanceof LoLangValue.Number))
        throw new RuntimeError(String.format("Cannot %s non-number", isIncrement() ? "increment" : "decrement"), token);

      double value = ((LoLangValue.Number) left).value;
      double modifier = isIncrement() ? 1 : -1;

      double newValue = value + modifier;
      setValue(context, this.left, new LoLangValue.Number(newValue), this.token, dynamicContext);

      return new LoLangValue.Number(
          this.isPostfix ? value : newValue);
    }

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType left = this.left.evaluateType(context);

      if (!(left instanceof LoLangType.Number)) {
        if (!(left instanceof LoLangType.Any))
          context.addException(
              new SemanticAnalyzerException(String.format("%s side of %s must have a type of number, received %s",
                  isPostfix ? "left" : "right", isIncrement() ? "increment" : "decrement", left.toString()),
                  this.token));

        return new LoLangType.Any();
      }

      return left;
    }
  }

  public static class Assignment extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    Token equalsSign;

    Assignment(ExpressionNode left, ExpressionNode right, Token equalsSign) {
      this.left = left;
      this.right = right;
      this.equalsSign = equalsSign;
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

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue newValue = this.right.evaluate(context, dynamicContext);
      return setValue(context, left, newValue, this.equalsSign, dynamicContext);
    }

    public LoLangType evaluateType(SemanticContext context) {
      if (this.left instanceof ExpressionNode.Identifier) {
        String identifier = ((ExpressionNode.Identifier) this.left).identifier.lexeme;

        try {
          SymbolTableEntry<LoLangType> entry = context.variableEnvironment.getSymbolTableEntry(identifier);

          if (entry.constant)
            context.addException(new SemanticAnalyzerException("Cannot assign to constant variable", equalsSign));
        } catch (EnvironmentException.EnvironmentUndeclaredException e) {
          context.addException(new SemanticAnalyzerException("Cannot find variable \"" + identifier + "\"",
              ((ExpressionNode.Identifier) this.left).identifier));
          return new LoLangType.Any();
        }
      }

      LoLangType left = this.left.evaluateType(context);
      LoLangType right = this.right.evaluateType(context);

      if (left.isEquivalent(right) == false) {
        context.addException(new SemanticAnalyzerException(String.format(
            "Cannot assign values to different types %s and %s", left.toString(), right.toString()), equalsSign));
        return new LoLangType.Any();
      }

      return left;
    }
  }

  public static LoLangValue setValue(ExecutionContext context, ExpressionNode left, LoLangValue newValue, Token token,
      ExecutionContext dynamicContext) {
    if (left instanceof ExpressionNode.Identifier) {
      ExpressionNode.Identifier identifier = (ExpressionNode.Identifier) left;

      try {
        context.environment.assign(identifier.identifier.lexeme, newValue);
      } catch (EnvironmentException.EnvironmentUndeclaredException e) {
        throw new RuntimeError("Cannot assign to undeclared variable", identifier.identifier);
      }
    }

    else if (left instanceof ExpressionNode.IndexAccess) {
      ExpressionNode.IndexAccess indexAccess = (ExpressionNode.IndexAccess) left;
      LoLangValue leftValue = indexAccess.left.evaluate(context, dynamicContext);
      LoLangValue rightValue = indexAccess.right.evaluate(context, dynamicContext);

      if (!(leftValue instanceof LoLangValue.Array))
        throw new RuntimeError("Cannot assign to non-array value", indexAccess.leftBracket);

      if (!(rightValue instanceof LoLangValue.Number))
        throw new RuntimeError("Cannot use non-number value as index to array ", indexAccess.leftBracket);

      LoLangValue.Array array = (LoLangValue.Array) leftValue;
      double index = ((LoLangValue.Number) rightValue).value;
      int intIndex = (int) Math.max(Math.round(index), 0);

      try {
        array.setIndex(intIndex, newValue);
      } catch (InterpreterExceptions.IndexAccessOutOfBoundsException e) {
        throw e.toRuntimeError(indexAccess.leftBracket);
      }
    }

    else if (left instanceof ExpressionNode.DotAccess) {
      ExpressionNode.DotAccess dotAccess = (ExpressionNode.DotAccess) left;
      LoLangValue leftValue = dotAccess.left.evaluate(context, dynamicContext);

      if (!(leftValue instanceof LoLangValue.Object))
        throw new RuntimeError("Cannot assign to non-object value", dotAccess.identifier);

      LoLangValue.Object object = (LoLangValue.Object) leftValue;

      try {
        object.setDot(dotAccess.identifier.lexeme, newValue);
      } catch (InterpreterExceptions.DotAccessNonExistentException e) {
        throw e.toRuntimeError(dotAccess.identifier);
      }
    }

    else
      throw new RuntimeError("Invalid assignment target", token);

    return newValue;
  }

  public static class IndexAccess extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    Token leftBracket;

    IndexAccess(ExpressionNode left, ExpressionNode right, Token leftBracket) {
      this.left = left;
      this.right = right;
      this.leftBracket = leftBracket;
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

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue left = this.left.evaluate(context, dynamicContext);
      LoLangValue right = this.right.evaluate(context, dynamicContext);

      if (!(left instanceof LoLangValue.Array))
        throw new RuntimeError("Cannot access index on non-array value", leftBracket);

      if (!(right instanceof LoLangValue.Number))
        throw new RuntimeError("Cannot use non-number value as index to array ", leftBracket);

      LoLangValue.Array array = (LoLangValue.Array) left;
      double index = ((LoLangValue.Number) right).value;
      int intIndex = (int) Math.max(Math.round(index), 0);

      try {
        return array.getIndex(intIndex);
      } catch (InterpreterExceptions.IndexAccessOutOfBoundsException e) {
        throw e.toRuntimeError(leftBracket);
      }
    }

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType left = this.left.evaluateType(context);
      LoLangType right = this.right.evaluateType(context);

      if (!(left instanceof LoLangType.Array)) {
        if (!(left instanceof LoLangType.Any))
          context.addException(
              new SemanticAnalyzerException("Left side of index access should be an array type, received: "
                  + left.toString(), leftBracket));
        return new LoLangType.Any();
      }

      if (!(right instanceof LoLangType.Number)) {
        if (!(right instanceof LoLangType.Any))
          context.addException(
              new SemanticAnalyzerException("Index should be a number type, received" + right.toString(),
                  leftBracket));
        return new LoLangType.Any();
      }

      LoLangType.Array array = (LoLangType.Array) left;
      return array.elementType;
    }
  }

  public static class Literal extends ExpressionNode {
    Token token;

    Literal(Token token) {
      this.token = token;
    }

    public String toString() {
      return String.format("[Literal: %s (%s)]", this.token.lexeme.replace("\"", "\'"),
          this.token.type.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "Literal [lexeme=" + this.token.lexeme.replace("\"", "\'") + "] [type=" + this.token.type.toString()
              + "]");
    }

    LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      switch (this.token.type) {
        case STRING_LITERAL:
          return new LoLangValue.String(this.token.lexeme);
        case BOOLEAN_LITERAL:
          return new LoLangValue.Boolean(this.token.lexeme.equals("faker"));
        case NULL_LITERAL:
          return new LoLangValue.Null();
        case NUMBER_LITERAL:
          return new LoLangValue.Number(parseNumber(this.token.lexeme));
        default:
          throw new RuntimeError("Invalid token literal type", this.token);
      }
    }

    public LoLangType evaluateType(SemanticContext context) {
      switch (this.token.type) {
        case STRING_LITERAL:
          return new LoLangType.String();
        case BOOLEAN_LITERAL:
          return new LoLangType.Boolean();
        case NULL_LITERAL:
          return new LoLangType.Null();
        case NUMBER_LITERAL:
          return new LoLangType.Number();
        default:
          // Ok it should never reach here. Parser error if it did
          context.addException(new SemanticAnalyzerException(
              "Invalid token literal type, got: " + this.token.type.toString(), this.token));
          return new LoLangType.Any();
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

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue left = this.left.evaluate(context, dynamicContext);
      LoLangValue right = this.right.evaluate(context, dynamicContext);

      if (left instanceof LoLangValue.Number && right instanceof LoLangValue.Number) {
        LoLangValue.Number leftNumber = (LoLangValue.Number) left;
        LoLangValue.Number rightNumber = (LoLangValue.Number) right;

        if (this.operation.lexeme.equals("+"))
          return new LoLangValue.Number(leftNumber.value + rightNumber.value);
        else if (this.operation.lexeme.equals("-"))
          return new LoLangValue.Number(leftNumber.value - rightNumber.value);
        else if (this.operation.lexeme.equals("*"))
          return new LoLangValue.Number(leftNumber.value * rightNumber.value);
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
        else if (this.operation.lexeme.equals("/")) {
          if (rightNumber.value == 0)
            throw new RuntimeError("Division by zero", this.operation);
          return new LoLangValue.Number(leftNumber.value / rightNumber.value);
        }

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

        throw new RuntimeError("Invalid binary operation \"" + this.operation.lexeme + "\" on Number, Number",
            this.operation);
      }

      else if (left instanceof LoLangValue.Boolean && right instanceof LoLangValue.Boolean) {
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

        throw new RuntimeError("Invalid binary operation \"" + this.operation.lexeme + "\" on Boolean, Boolean",
            this.operation);
      }

      else if (left instanceof LoLangValue.String && right instanceof LoLangValue.String) {
        LoLangValue.String leftString = (LoLangValue.String) left;
        LoLangValue.String rightString = (LoLangValue.String) right;

        if (this.operation.lexeme.equals("+"))
          return new LoLangValue.String(leftString.value + rightString.value);
        else if (this.operation.lexeme.equals("=="))
          return new LoLangValue.Boolean(leftString.value.equals(rightString.value));

        throw new RuntimeError("Invalid binary operation \"" + this.operation.lexeme + "\" on String, String",
            this.operation);
      }

      else if (left instanceof LoLangValue.String && right instanceof LoLangValue.Number) {
        LoLangValue.Number rightNumber = (LoLangValue.Number) right;
        return new LoLangValue.String(left.toString() + rightNumber.value);
      }

      else if (left instanceof LoLangValue.String && right instanceof LoLangValue.Boolean) {
        LoLangValue.Boolean rightBoolean = (LoLangValue.Boolean) right;
        return new LoLangValue.String(left.toString() + rightBoolean.value);
      }

      else if (left instanceof LoLangValue.String && right instanceof LoLangValue.Null) {
        return new LoLangValue.String(left.toString() + "null");
      }

      else
        throw new RuntimeError(String.format("Cannot find operations for types %s and %s",
            left.getClass().getName(), right.getClass().getName()), this.operation);
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
        if ((this.operation.lexeme.equals("&&"))
            || (this.operation.lexeme.equals("||"))
            || (this.operation.lexeme.equals("=="))
            || (this.operation.lexeme.equals("!=")))
          return new LoLangType.Boolean();
      }

      if (left instanceof LoLangType.String && right instanceof LoLangType.String) {
        if (this.operation.lexeme.equals("+"))
          return new LoLangType.String();
        else if (this.operation.lexeme.equals("=="))
          return new LoLangType.Boolean();
      }

      if (left instanceof LoLangType.String && right instanceof LoLangType.Number) {
        if (this.operation.lexeme.equals("+"))
          return new LoLangType.String();
      }

      if (left instanceof LoLangType.String && right instanceof LoLangType.Boolean) {
        if (this.operation.lexeme.equals("+"))
          return new LoLangType.String();
      }

      if (left instanceof LoLangType.String && right instanceof LoLangType.Null) {
        return new LoLangType.String();
      }

      context.addException(new SemanticAnalyzerException("Invalid binary operation \"" + this.operation.lexeme
          + "\" on " + left.getClass().getName() + ", " + right.getClass().getName(), this.operation));
      return new LoLangType.Any();
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

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      LoLangValue operand = this.operand.evaluate(context, dynamicContext);
      String lexeme = this.operationToken.lexeme;

      if (operand instanceof LoLangValue.Number && lexeme.equals("-"))
        return new LoLangValue.Number(-1 * ((LoLangValue.Number) operand).value);

      else if (operand instanceof LoLangValue.Boolean && lexeme.equals("!"))
        return new LoLangValue.Boolean(!((LoLangValue.Boolean) operand).value);

      throw new RuntimeError("Invalid operation \"" + lexeme + "\" on " + operand.getClass().getName(),
          this.operationToken);
    }

    public LoLangType evaluateType(SemanticContext context) {
      LoLangType operandType = this.operand.evaluateType(context);
      String lexeme = this.operationToken.lexeme;

      if (operandType instanceof LoLangType.Number && lexeme.equals("-"))
        return new LoLangType.Number();

      else if (operandType instanceof LoLangType.Boolean && lexeme.equals("!"))
        return new LoLangType.Boolean();

      context.addException(new SemanticAnalyzerException("Invalid operation \"" + lexeme + "\" on type "
          + operandType.getClass().getName(), this.operationToken));
      return new LoLangType.Any();
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

    public LoLangValue evaluate(ExecutionContext context, ExecutionContext dynamicContext) {
      return this.expression.evaluate(context, dynamicContext);
    }

    public LoLangType evaluateType(SemanticContext context) {
      return this.expression.evaluateType(context);
    }
  }
}
