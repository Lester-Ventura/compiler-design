package parser;

import java.util.ArrayList;

public class ParserResult {
  final public ArrayList<ParserException> errors;
  final public Node root;

  public ParserResult(Node root, ArrayList<ParserException> errors) {
    this.root = root;
    this.errors = errors;
  }

  public void addError(ParserException e) {
    this.errors.add(e);
  }

  public void printErrors() {
    if (this.errors.size() != 0) {
      System.out.println("\nThe following errors parsing were encountered: \n");

      for (ParserException exception : this.errors)
        System.out.println(exception.getMessage() + "\n");
    }
  }
}
