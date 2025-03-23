public abstract class TypeExpressionNode extends Node {
  // abstract void evaluate();

  public static class Identifier extends TypeExpressionNode {
    String lexeme;

    Identifier(String lexeme) {
      this.lexeme = lexeme;
    }

    public String toString() {
      return String.format("[TypeIdentifier: %s]", this.lexeme);
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
  }

}
