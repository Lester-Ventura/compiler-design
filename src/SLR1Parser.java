import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

public class SLR1Parser {
  static class StateNode {
    int stateIndex;

    StateNode(int stateIndex) {
      this.stateIndex = stateIndex;
    }

    public String toString() {
      return String.format("State: %d", stateIndex);
    }
  }

  static abstract class SLR1StackSymbol {

  }

  static class SLR1StackToken extends SLR1StackSymbol {
    Token token;

    SLR1StackToken(Token token) {
      this.token = token;
    }

    public String toString() {
      return token.toString();
    }
  }

  static class SLR1StackInternalNode extends SLR1StackSymbol {
    Node node;

    SLR1StackInternalNode(Node node) {
      this.node = node;
    }

    public String toString() {
      return node.toString();
    }
  }

  ArrayList<SLR1GrammarParser.SLR1GrammarProduction> productions;
  ArrayList<SLR1TableParser.SLR1TableState> states;
  String input;

  SLR1Parser(
      ArrayList<SLR1GrammarParser.SLR1GrammarProduction> productions,
      ArrayList<SLR1TableParser.SLR1TableState> states,
      String input) {
    this.productions = productions;
    this.states = states;
    this.input = input;
  }

  Stack<StateNode> statesStack = new Stack<>();
  Stack<SLR1StackSymbol> symbolsStack = new Stack<>();

  Node parse() {
    // initialize the stacks with the start state
    statesStack = new Stack<>();
    symbolsStack = new Stack<>();
    statesStack.push(new StateNode(0));

    HashMap<TokenType, String> converter = TokenTypeConverter.generate();
    HashMap<Integer, ReductionTable.Reduction> reducers = ReductionTable.generateReductions();

    Lexer lexer = new Lexer(input);

    // actual LL1 driver goes here
    boolean done = false;
    while (!done) {
      StateNode currentNode = statesStack.peek();

      Token token = lexer.peekNextToken();
      String converted = converter.get(token.type);

      SLR1TableParser.SLR1TableProcess action = states.get(currentNode.stateIndex).actions.get(converted);

      if (action == null) {
        System.out.println("NULL ACTION FOUND");
        System.out.println("Current state: " + currentNode.stateIndex + " | Token: "
            + token.type);
      }

      if (action.type == SLR1TableParser.SLR1TableProcessType.SHIFT) {
        // add current token to the stack and push the next state
        token = lexer.getNextToken();

        statesStack.push(new StateNode(action.value));
        symbolsStack.push(new SLR1StackToken(token));
      } else if (action.type == SLR1TableParser.SLR1TableProcessType.REDUCE) {
        if (action.value == 0) {
          done = false;
          break;
        }

        SLR1GrammarParser.SLR1GrammarProduction production = productions.get(action.value);
        ArrayList<SLR1StackSymbol> popped = new ArrayList<SLR1StackSymbol>();

        // pop the stack and reduce by this production
        for (int i = 0; i < production.rhs.size(); i++) {
          popped.add(symbolsStack.pop());
          statesStack.pop();
        }

        // create the new node and add to symbols stack
        ReductionTable.Reduction reduction = reducers.get(action.value);
        if (reduction == null) {
          throw new Error("Was not able to find a reducer for: " + action.value);
        }

        Collections.reverse(popped); // reverse the order of items being popped
        Node result = reduction.reducer.run(new ReductionTable.ReductionInput(popped));
        SLR1StackInternalNode node = new SLR1StackInternalNode(result);
        symbolsStack.add(node);

        StateNode topNode = statesStack.peek();
        SLR1TableParser.SLR1TableProcess gotoAction = states.get(topNode.stateIndex).gotos.get(production.lhs);
        if (gotoAction.type != SLR1TableParser.SLR1TableProcessType.GOTO)
          throw new Error("Expected goto action, received: " + gotoAction.type);

        statesStack.push(new StateNode(gotoAction.value));
        // done = true;
      }

    }

    SLR1StackSymbol top = (SLR1StackSymbol) symbolsStack.peek();
    if (top instanceof SLR1StackInternalNode)
      return ((SLR1StackInternalNode) top).node;
    else
      throw new Error("Top of stack was not a program node");
  }
}

class ReductionTable {
  interface Reducer {
    Node run(ReductionInput input);
  }

  static class Reduction {
    public Reducer reducer;

    Reduction(Reducer reducer) {
      this.reducer = reducer;
    }
  }

  // This class wraps popped symbols and provides methods for easier access
  static class ReductionInput {
    ArrayList<SLR1Parser.SLR1StackSymbol> symbols;

    ReductionInput(ArrayList<SLR1Parser.SLR1StackSymbol> symbols) {
      this.symbols = symbols;
    }

    Token getToken(int index) {
      SLR1Parser.SLR1StackSymbol symbol = symbols.get(index);
      if (!(symbol instanceof SLR1Parser.SLR1StackToken))
        throw new Error("Can't get a token from a non-token");

      return ((SLR1Parser.SLR1StackToken) symbol).token;
    }

    SLR1Parser.SLR1StackInternalNode getInternalNode(int index) {
      SLR1Parser.SLR1StackSymbol symbol = symbols.get(index);
      if (!(symbol instanceof SLR1Parser.SLR1StackInternalNode))
        throw new Error("Can't get an internal node from a non-internal node");

      return (SLR1Parser.SLR1StackInternalNode) symbol;

    }

    Node.ExpressionNode getExpressionNode(int index) {
      SLR1Parser.SLR1StackSymbol symbol = symbols.get(index);
      if (!(symbol instanceof SLR1Parser.SLR1StackInternalNode))

        throw new Error("Can't get an internal node from a non-internal node");
      SLR1Parser.SLR1StackInternalNode node = (SLR1Parser.SLR1StackInternalNode) symbol;
      if (!(node.node instanceof Node.ExpressionNode))
        throw new Error("Can't get an expression node from a non-expression node");
      return (Node.ExpressionNode) node.node;

    }
  }

  static class PassthroughReducer extends Reduction {
    PassthroughReducer() {
      super((e) -> e.getInternalNode(0).node);
    }
  }

  public static HashMap<Integer, Reduction> generateReductions() {
    HashMap<Integer, Reduction> reductions = new HashMap<>();

    Reduction binaryOperationReducer = new Reduction((input) -> {
      Node.ExpressionNode left = input.getExpressionNode(0);
      Token operation = input.getToken(1);
      Node.ExpressionNode right = input.getExpressionNode(2);

      return new Node.BinaryExpression(left, operation.lexeme, right);
    });

    reductions.put(1, new PassthroughReducer());
    reductions.put(2, binaryOperationReducer);
    reductions.put(3, binaryOperationReducer);
    reductions.put(4, new PassthroughReducer());
    reductions.put(5, binaryOperationReducer);
    reductions.put(6, binaryOperationReducer);
    reductions.put(7, new PassthroughReducer());

    reductions.put(8, new Reduction((input) -> {
      Token token = input.getToken(0);
      // TODO: add octal and binary number support
      if (token.type == TokenType.DECIMAL_NUMBER) {
        double value = Double.parseDouble(token.lexeme);
        return new Node.NumberLiteralExpression(value);
      }

      return null;
    }));

    return reductions;
  }
}

class TokenTypeConverter {
  public static HashMap<TokenType, String> generate() {
    HashMap<TokenType, String> map = new HashMap<>();
    map.put(TokenType.PLUS, "plus");
    map.put(TokenType.MINUS, "minus");
    map.put(TokenType.STAR, "star");
    map.put(TokenType.FORWARD_SLASH, "forward_slash");
    map.put(TokenType.DECIMAL_NUMBER, "number");
    map.put(TokenType.EOF, "eof");

    return map;
  }
}