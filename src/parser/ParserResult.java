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
}
