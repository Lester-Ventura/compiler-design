package parser;

import java.util.ArrayList;

import lexer.Token;
import semantic.LoLangType;
import semantic.SemanticAnalyzerException;
import semantic.SemanticContext;
import utils.DOTGenerator;
import utils.EnvironmentException;

public abstract class TypeExpressionNode extends Node {
  abstract LoLangType evaluate(SemanticContext context);

  public static class Identifier extends TypeExpressionNode {
    Token identifier;

    Identifier(Token identifier) {
      this.identifier = identifier;
    }

    public String toString() {
      return String.format("[TypeIdentifier: %s]", this.identifier.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "TypeIdentifier [lexeme=" + this.identifier.lexeme.replace("\"", "\'") + "]");
    }

    public LoLangType evaluate(SemanticContext context) {
      try {
        return context.typeEnvironment.get(this.identifier.lexeme);
      } catch (EnvironmentException e) {
        context.addException(new SemanticAnalyzerException("Cannot find type \"" + this.identifier.lexeme + "\"",
            this.identifier));
        return new LoLangType.Any();
      }
    }
  }

  public static class Array extends TypeExpressionNode {
    TypeExpressionNode elementType;

    Array(TypeExpressionNode elementType) {
      this.elementType = elementType;
    }

    public String toString() {
      return String.format("[Array: (%s)[]]", this.elementType.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Array");
      this.elementType.toDot(builder);
      builder.addEdge(this.hashCode(), this.elementType.hashCode());
    }

    public LoLangType evaluate(SemanticContext context) {
      return new LoLangType.Array(this.elementType.evaluate(context));
    }
  }

  public static class Lambda extends TypeExpressionNode {
    TypeExpressionNode returnType;
    Node.LambdaParamterList parameterTypes;

    Lambda(Node.LambdaParamterList parameterTypes, TypeExpressionNode returnType) {
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
    }

    Lambda(TypeExpressionNode returnType) {
      this.returnType = returnType;
      this.parameterTypes = new Node.LambdaParamterList();
    }

    public String toString() {
      String parameterTypesString = String.join(", ", this.parameterTypes.toString());
      return String.format("[Lambda: (%s) -> %s]", parameterTypesString, this.returnType.toString());
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(), "Lambda");
      this.parameterTypes.toDot(builder);
      builder.addEdge(this.hashCode(), this.parameterTypes.hashCode());

      this.returnType.toDot(builder);
      builder.addEdge(this.hashCode(), this.returnType.hashCode());
    }

    public LoLangType evaluate(SemanticContext context) {
      ArrayList<LoLangType> parameterList = new ArrayList<>();

      for (TypeExpressionNode parameter : this.parameterTypes.parameters) {
        parameterList.add(parameter.evaluate(context));
      }

      return new LoLangType.Lambda(this.returnType.evaluate(context), parameterList);
    }
  }
}
