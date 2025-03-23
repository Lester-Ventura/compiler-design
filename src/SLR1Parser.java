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
  HashMap<Integer, ReductionTable.Reduction> reducers = ReductionTable.generateReductions();

  SLR1Parser(
      ArrayList<SLR1GrammarParser.SLR1GrammarProduction> productions,
      ArrayList<SLR1TableParser.SLR1TableState> states) {
    this.productions = productions;
    this.states = states;
  }

  Stack<StateNode> statesStack = new Stack<>();
  Stack<SLR1StackSymbol> symbolsStack = new Stack<>();

  private void crash(String reason, Token currentToken) {
    // TO DO: replace this with scanner error
    crash(reason, currentToken, new Exception(reason));
  }

  private void crash(String reason, Token currentToken, Exception e) {
    StateNode currentNode = statesStack.peek();

    System.out.println("\nCurrent Token: " + currentToken.toString());
    System.out.println("\nError: " + reason);
    System.out.println("\nCurrent state: " + currentNode.stateIndex);
    System.out.println("\nStates Stack: " + statesStack.toString());
    System.out.println("\nSymbols Stack: " + symbolsStack.toString());

    throw new Error(e);
  }

  Node parse(String input) {
    // initialize the stacks with the start state
    statesStack = new Stack<>();
    symbolsStack = new Stack<>();
    statesStack.push(new StateNode(0));

    // Lexer lexer = new Lexer(input);
    RegexEngine lexer = RegexEngine.createRegexEngine(input);

    while (true) {
      StateNode currentNode = statesStack.peek();

      Token token = lexer.peekNextToken();
      String converted = token.type.toString();
      SLR1TableParser.SLR1TableProcess action = states.get(currentNode.stateIndex).actions.get(converted);

      if (action == null) {
        Set<String> expecteds = states.get(currentNode.stateIndex).actions.keySet();
        String expectedString = String.join(", ", (String[]) expecteds.toArray(new String[0]));
        crash("No action fonud for token: " + token.toString() + ". Expected: " + expectedString, token);
      }

      if (action.type == SLR1TableParser.SLR1TableProcessType.SHIFT) {
        // add current token to the stack and push the next state
        token = lexer.getNextToken();

        statesStack.push(new StateNode(action.value));
        symbolsStack.push(new SLR1StackToken(token));
      } else if (action.type == SLR1TableParser.SLR1TableProcessType.REDUCE) {
        if (action.value == 0)
          break;

        SLR1GrammarParser.SLR1GrammarProduction production = productions.get(action.value);

        // pop the stack and reduce by this production
        ArrayList<SLR1StackSymbol> popped = new ArrayList<SLR1StackSymbol>();
        for (int i = 0; i < production.rhs.size(); i++) {
          popped.add(symbolsStack.pop());
          statesStack.pop();
        }

        // create the new node and add to symbols stack
        ReductionTable.Reduction reduction = reducers.get(action.value);
        if (reduction == null)
          crash("Was not able to find a reducer for production " + action.value, token);

        Collections.reverse(popped); // reverse the order of items being popped
        ReductionTable.ReductionInput reductionInput = new ReductionTable.ReductionInput(popped);

        // try to perform the reduction
        try {
          Node result = reduction.reducer.run(reductionInput);
          SLR1StackInternalNode node = new SLR1StackInternalNode(result);
          symbolsStack.add(node);
        } catch (Exception e) {
          crash("Error while performing reduction for production: " + action.value + ". Reduction input: "
              + reductionInput.toString(), token, e);
        }

        // get the top node and figure out what state to add to state stack
        StateNode topNode = statesStack.peek();
        SLR1TableParser.SLR1TableProcess gotoAction = states.get(topNode.stateIndex).gotos.get(production.lhs);
        if (gotoAction == null)
          crash("No goto action found for state: " + topNode.stateIndex + " and production: " + production.lhs
              + ". Actions: Reduce by production " + action.value, token);
        else if (gotoAction.type != SLR1TableParser.SLR1TableProcessType.GOTO)
          crash("Expected GOTO action, received: " + gotoAction.type, token);

        statesStack.push(new StateNode(gotoAction.value));
      }
    }

    SLR1StackSymbol top = (SLR1StackSymbol) symbolsStack.peek();
    if (top instanceof SLR1StackInternalNode)
      return ((SLR1StackInternalNode) top).node;

    throw new Error("Top of stack was not a program node");
  }
}
