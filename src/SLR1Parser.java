import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
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
      if (converted == null) {
        // if you get this error, you need to add a new token type to the converter
        throw new Error("Tried to convert " + token.type + ", received null");
      }

      SLR1TableParser.SLR1TableProcess action = states.get(currentNode.stateIndex).actions.get(converted);

      if (action == null) {
        Set<String> expecteds = states.get(currentNode.stateIndex).actions.keySet();
        System.out.println("NULL ACTION FOUND");
        System.out.println("Current state: " + currentNode.stateIndex + " | Token: "
            + token.toString() + " | Converted: " + converted);
        System.out.println("Expected: " + String.join(", ", (String[]) expecteds.toArray(new String[0])));
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
          throw new Error(
              "Was not able to find a reducer for production " + action.value + ".\nCurrent state: "
                  + currentNode.stateIndex
                  + ".\nCurrent input: " + token);
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
