public abstract class Node {
  public static abstract class ExpressionNode extends Node {

  }

  public static class NumberLiteralExpression extends ExpressionNode {
    double value;

    NumberLiteralExpression(double value) {
      this.value = value;
    }

    public String toString() {
      return String.format("[NumberLiteralExpression: %f]", value);
    }
  }

  public static class BinaryExpression extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    String operation;

    BinaryExpression(ExpressionNode left, String operation, ExpressionNode right) {
      this.left = left;
      this.right = right;
      this.operation = operation;
    }

    public String toString() {
      return String.format("[BinaryExpression: %s %s %s]", this.left.toString(), this.operation, this.right.toString());
    }
  }
}
