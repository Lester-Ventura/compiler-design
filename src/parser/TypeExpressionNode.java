package parser;

import utils.DOTGenerator;

public abstract class TypeExpressionNode extends Node {
  // abstract void evaluate();

  public static class Identifier extends TypeExpressionNode {
    String lexeme;

    Identifier(String lexeme) {
      this.lexeme = lexeme;
    }

    public String toString() {
      return String.format("[TypeIdentifier: %s]", this.lexeme.replace("\"", "\'"));
    }

    public void toDot(DOTGenerator builder) {
      builder.addNode(this.hashCode(),
          "TypeIdentifier [lexeme=" + this.lexeme.replace("\"", "\'") + "]");
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
  }
}
