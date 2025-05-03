package parser;

import java.util.*;
import lexer.*;
import parser.DefaultProductions.DefaultProduction;
import parser.LR1TableParser.LR1TableProcess;
import parser.LR1TableParser.LR1TableProcessType;

public class LR1Parser {
  static class StateNode {
    int stateIndex;

    StateNode(int stateIndex) {
      this.stateIndex = stateIndex;
    }

    public String toString() {
      return String.format("State: %d", stateIndex);
    }
  }

  static abstract class LR1StackSymbol {

  }

  static class LR1StackToken extends LR1StackSymbol {
    Token token;

    LR1StackToken(Token token) {
      this.token = token;
    }

    public String toString() {
      return token.toString();
    }
  }

  static class LR1StackInternalNode extends LR1StackSymbol {
    Node node;

    LR1StackInternalNode(Node node) {
      this.node = node;
    }

    public String toString() {
      return node.toString();
    }
  }

  String input;
  String inputPath;
  ArrayList<LR1GrammarParser.LR1GrammarProduction> productions;
  ArrayList<LR1TableParser.LR1TableState> states;
  HashMap<Integer, ReductionTable.Reduction> reducers = ReductionTable.generateReductions();
  RegexEngine lexer;

  public LR1Parser(
      String input, String inputPath,
      ArrayList<LR1GrammarParser.LR1GrammarProduction> productions,
      ArrayList<LR1TableParser.LR1TableState> states) {
    this.productions = productions;
    this.states = states;
    this.input = input;
    lexer = RegexEngine.createRegexEngine(input, inputPath);
  }

  Stack<StateNode> statesStack = new Stack<>();
  Stack<LR1StackSymbol> symbolsStack = new Stack<>();
  ArrayList<ParserException> exceptions = new ArrayList<>();

  public ParserResult parse() {
    // initialize the stacks, error list and the start state
    statesStack = new Stack<>();
    symbolsStack = new Stack<>();
    exceptions = new ArrayList<>();
    statesStack.push(new StateNode(0));

    while (true) {
      StateNode currentNode = statesStack.peek();

      Token token = lexer.peekNextToken();
      LR1TableParser.LR1TableProcess action = states.get(currentNode.stateIndex).actions.get(token.type.toString());

      if (action == null) {
        try {
          token = sync();
        } catch (ParserException exception) {
          exceptions.add(exception);
          break;
        }

        token = token != null ? token : lexer.peekNextToken();
        currentNode = statesStack.peek();
        action = states.get(currentNode.stateIndex).actions.get(token.type.toString());
      }

      if (action.type == LR1TableParser.LR1TableProcessType.SHIFT) {
        // add current token to the stack and push the next state
        token = lexer.getNextToken();

        statesStack.push(new StateNode(action.value));
        symbolsStack.push(new LR1StackToken(token));
      }

      else if (action.type == LR1TableParser.LR1TableProcessType.REDUCE) {
        if (action.value == 0)
          break;

        LR1GrammarParser.LR1GrammarProduction production = productions.get(action.value);

        // pop the stack and reduce by this production
        ArrayList<LR1StackSymbol> popped = new ArrayList<LR1StackSymbol>();
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
          LR1StackInternalNode node = new LR1StackInternalNode(result);
          symbolsStack.add(node);
        } catch (Exception e) {
          crash("Error while performing reduction for production: " + action.value + ". Reduction input: "
              + reductionInput.toString(), token, e);
        }

        // get the top node and figure out what state to add to state stack
        StateNode topNode = statesStack.peek();
        LR1TableParser.LR1TableProcess gotoAction = states.get(topNode.stateIndex).gotos.get(production.lhs);

        if (gotoAction == null)
          crash("No goto action found for state: " + topNode.stateIndex + " and production: " + production.lhs
              + ". Actions: Reduce by production " + action.value, token);
        else if (gotoAction.type != LR1TableParser.LR1TableProcessType.GOTO)
          crash("Expected GOTO action, received: " + gotoAction.type, token);

        statesStack.push(new StateNode(gotoAction.value));
      }
    }

    LR1StackSymbol top = (LR1StackSymbol) symbolsStack.peek();
    if (top instanceof LR1StackInternalNode)
      return new ParserResult(((LR1StackInternalNode) top).node, exceptions);

    return new ParserResult(null, exceptions);
  }

  private boolean isSafe() {
    Token currentToken = lexer.peekNextToken();
    StateNode currentNode = statesStack.peek();
    return states.get(currentNode.stateIndex).actions.containsKey(currentToken.type.toString());
  }

  // performs panic mode error handling and leaves the parser in a safe state
  private Token sync() throws ParserException {
    Token currentToken = lexer.peekNextToken();
    StateNode currentNode = statesStack.peek();

    // check for extra semicolons
    if (currentToken.type == TokenType.SEMICOLON) {
      while (lexer.peekNextToken().type == TokenType.SEMICOLON)
        currentToken = lexer.getNextToken();
      currentToken = lexer.peekNextToken();

      if (isSafe())
        return null;
    }

    // check for lackng semicolons
    HashMap<String, LR1TableProcess> expecteds = states.get(currentNode.stateIndex).actions;
    if (expecteds.containsKey("SEMICOLON")) {
      LR1TableProcess action = expecteds.get("SEMICOLON");
      Token newToken = new Token(TokenType.SEMICOLON, ";", new ColumnAndRow(currentToken.line, currentToken.column),
          inputPath);
      if (action.type == LR1TableProcessType.REDUCE)
        return newToken;

      symbolsStack.push(new LR1StackToken(
          newToken));
      statesStack.push(new StateNode(action.value));
      exceptions.add(new ParserException(String.format(
          "Expected SEMICOLON but received %s, SEMICOLON was automatically inserted.",
          currentToken.type.toString()), currentToken));

      if (isSafe())
        return null;
    }

    HashMap<String, DefaultProduction> defaultProductions = DefaultProductions.createDefaultProductions();

    // create a user error message by determining the possible next tokens
    String expectedString = String.join(", ", (String[]) expecteds.keySet().toArray(new String[0]));

    String userErrorMessage = String.format("Expected one of the following tokens: %s but got %s",
        expectedString, currentToken.type.toString());
    exceptions.add(new ParserException(userErrorMessage, currentToken));

    String productionToSkip = null;

    // keep popping the stack until the current node has atleast one transition to
    // another GOTO action that has a default production
    outer: while (productionToSkip == null) {
      HashMap<String, LR1TableProcess> currentStateGotos = states.get(currentNode.stateIndex).gotos;

      if (currentStateGotos.size() != 0) {
        for (String gotoName : currentStateGotos.keySet()) {
          if (defaultProductions.containsKey(gotoName)) {
            // If the current state has atleast one goto and one of those gotos has a
            // default production, then set the action to that current variable and break
            productionToSkip = gotoName;
            break outer;
          }
        }
      }

      try {
        statesStack.pop();
        symbolsStack.pop();

        currentNode = statesStack.peek();
      } catch (Exception e) {
        throw new ParserException("No more items in the stacks to pop", null);
      }
    }

    int nextState = states.get(currentNode.stateIndex).gotos.get(productionToSkip).value;

    // determine tokens that can appear the production using the states table
    Set<String> actionFollowSet = states.get(nextState).actions.keySet();

    while (!actionFollowSet.contains(lexer.peekNextToken().type.toString())) {
      Token nextToken = lexer.getNextToken();

      if (nextToken.type == TokenType.EOF) {
        throw new ParserException("Unexpected end of file", nextToken);
      }
    }

    // instantiate and add to the symbols stack, add next state to states stack
    symbolsStack.push(new LR1StackInternalNode(defaultProductions.get(productionToSkip).run()));
    statesStack.push(new StateNode(nextState));
    return null;
  }

  // this function should be called when there is an error with the way that the
  // parser itself works improper reductions, improper parsing of table states,
  // etc.
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
}
