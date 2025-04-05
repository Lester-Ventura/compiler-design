package parser;

import java.util.ArrayList;
import java.util.HashMap;

import lexer.Token;

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
    ArrayList<LR1Parser.LR1StackSymbol> symbols;

    ReductionInput(ArrayList<LR1Parser.LR1StackSymbol> symbols) {
      this.symbols = symbols;
    }

    public String toString() {
      String symbolsString = String.join(", ", this.symbols.toString());
      return String.format("[ReductionInput: %s]", symbolsString);
    }

    Token getToken(int index) {
      LR1Parser.LR1StackSymbol symbol = symbols.get(index);
      if (!(symbol instanceof LR1Parser.LR1StackToken))
        throw new Error("Can't get a token from a non-token");

      return ((LR1Parser.LR1StackToken) symbol).token;
    }

    Node getInternalNode(int index) {
      LR1Parser.LR1StackSymbol symbol = symbols.get(index);
      if (!(symbol instanceof LR1Parser.LR1StackInternalNode))
        throw new Error("Can't get an internal node from a non-internal node");

      return ((LR1Parser.LR1StackInternalNode) symbol).node;
    }

    StatementNode getStatementNode(int index) {
      Node node = getInternalNode(index);
      if (!(node instanceof StatementNode))
        throw new Error(
            "Can't get an statement node from a non-statement node. Received: " + node.getClass().getName());
      return (StatementNode) node;
    }

    ExpressionNode getExpressionNode(int index) {
      Node node = getInternalNode(index);
      if (!(node instanceof ExpressionNode))
        throw new Error("Can't get an expression node from a non-expression node");
      return (ExpressionNode) node;
    }

    TypeExpressionNode getTypeExpressionNode(int index) {
      Node node = getInternalNode(index);
      if (!(node instanceof TypeExpressionNode))
        throw new Error("Can't get an type-expression node from a non-type-expression node, Received: "
            + node.getClass().getName());
      return (TypeExpressionNode) node;
    }
  }

  public static void addALotOfSameReduction(int from, int to, Reduction reduction,
      HashMap<Integer, Reduction> reductions) {
    for (int i = from; i <= to; i++)
      reductions.put(i, reduction);
  }

  public static HashMap<Integer, Reduction> generateReductions() {
    HashMap<Integer, Reduction> reductions = new HashMap<>();

    Reduction passthroughReducer = new Reduction((e) -> e.getInternalNode(0));
    Reduction binaryOperationReducer = new Reduction(
        (input) -> new ExpressionNode.Binary(input.getExpressionNode(0), input.getToken(1),
            input.getExpressionNode(2)));
    Reduction unaryOperationReducer = new Reduction(
        (input) -> new ExpressionNode.Unary(input.getToken(0).lexeme,
            input.getExpressionNode(1)));

    reductions.put(1, new Reduction((input) -> new StatementNode.Program()));
    reductions.put(2, new Reduction((input) -> {
      Node statements = input.getInternalNode(0);
      if (statements instanceof Node.StatementList)
        return new StatementNode.Program((Node.StatementList) statements);
      return null;
    }));
    reductions.put(3, new Reduction((input) -> {
      Node statements = input.getInternalNode(0);
      Node newStatement = input.getInternalNode(1);
      if (statements instanceof Node.StatementList && newStatement instanceof StatementNode)
        return ((Node.StatementList) statements).add((StatementNode) newStatement);
      return null;
    }));
    reductions.put(4, new Reduction((input) -> {
      Node newStatement = input.getInternalNode(0);
      if (newStatement instanceof StatementNode)
        return new Node.StatementList((StatementNode) newStatement);
      return null;
    }));

    // HANDLE <STATEMENT> -> reductions;
    addALotOfSameReduction(5, 16, passthroughReducer, reductions);
    reductions.put(17, new Reduction((input) -> new StatementNode.Import(input.getToken(1).lexeme)));

    // BEGIN HANDLING IF CONDITIONS
    reductions.put(18, new Reduction((input) -> {
      ExpressionNode cond = input.getExpressionNode(2);
      StatementNode stmt = input.getStatementNode(4);
      Node branches = input.getInternalNode(5);
      StatementNode elseBody = input.getStatementNode(7);

      if (branches instanceof Node.IfStatementBranches) {
        Node.IfStatementBranch newBranch = new Node.IfStatementBranch(cond, stmt);
        ((Node.IfStatementBranches) branches).add(newBranch);

        return new StatementNode.If((Node.IfStatementBranches) branches, elseBody);
      }
      return null;
    }));
    reductions.put(19, new Reduction((input) -> {
      ExpressionNode cond = input.getExpressionNode(2);
      StatementNode stmt = input.getStatementNode(4);
      Node branches = input.getInternalNode(5);

      if (branches instanceof Node.IfStatementBranches) {
        Node.IfStatementBranch newBranch = new Node.IfStatementBranch(cond, stmt);
        ((Node.IfStatementBranches) branches).add(newBranch);

        return new StatementNode.If((Node.IfStatementBranches) branches, null);
      }
      return null;
    }));
    reductions.put(20, new Reduction((input) -> {
      ExpressionNode cond = input.getExpressionNode(2);
      StatementNode stmt = input.getStatementNode(4);
      StatementNode elseBody = input.getStatementNode(6);
      return new StatementNode.If(new Node.IfStatementBranches(new Node.IfStatementBranch(cond, stmt)), elseBody);
    }));
    reductions.put(21, new Reduction((input) -> {
      ExpressionNode cond = input.getExpressionNode(2);
      StatementNode stmt = input.getStatementNode(4);
      return new StatementNode.If(new Node.IfStatementBranches(new Node.IfStatementBranch(cond, stmt)));
    }));
    reductions.put(22, new Reduction((input) -> ((Node.IfStatementBranches) input.getInternalNode(0))
        .add((Node.IfStatementBranch) input.getInternalNode(1))));
    reductions.put(23,
        new Reduction((input) -> new Node.IfStatementBranches((Node.IfStatementBranch) input.getInternalNode(0))));
    reductions.put(24,
        new Reduction((input) -> new Node.IfStatementBranch(input.getExpressionNode(2), input.getStatementNode(4))));
    // END OF HANDLING IF CONDITIONS

    // BEGIN HANDLING VARIABLE AND CONSTANT DECLARATIONS
    reductions.put(25, new Reduction((input) -> {
      Node.VariableDeclaration declaration = (Node.VariableDeclaration) input.getInternalNode(0);
      return new StatementNode.VariableDeclaration(declaration);
    }));
    reductions.put(26, new Reduction((input) -> {
      Node.VariableDeclarationHeader header = (Node.VariableDeclarationHeader) input.getInternalNode(0);
      return new Node.VariableDeclaration(header.lexeme, header.type);
    }));
    reductions.put(27, new Reduction((input) -> {
      Node.VariableDeclarationHeader header = (Node.VariableDeclarationHeader) input.getInternalNode(0);
      ExpressionNode expr = input.getExpressionNode(2);
      return new Node.VariableDeclaration(header.lexeme, header.type, expr);
    }));
    reductions.put(28, new Reduction((input) -> {
      Token identifier = input.getToken(1);
      TypeExpressionNode type = input.getTypeExpressionNode(3);
      return new Node.VariableDeclarationHeader(identifier.lexeme, type);
    }));
    reductions.put(29, new Reduction((input) -> {
      Token identifier = input.getToken(1);
      TypeExpressionNode type = input.getTypeExpressionNode(3);
      ExpressionNode expr = input.getExpressionNode(5);
      return new StatementNode.ConstantDeclaration(identifier.lexeme, type, expr);
    }));
    // END OF HANDLING VARIABLE AND CONSTANT DECLARATIONS

    reductions.put(30, new Reduction((input) -> new StatementNode.Block()));
    reductions.put(31,
        new Reduction((input) -> new StatementNode.Block((Node.StatementList) input.getInternalNode(1))));
    reductions.put(32, new Reduction((input) -> new StatementNode.Return(input.getExpressionNode(1))));
    reductions.put(33, new Reduction((input) -> new StatementNode.Return()));
    reductions.put(34, new Reduction((input) -> {
      StatementNode.Block block = (StatementNode.Block) input.getInternalNode(1);
      Token identifier = input.getToken(4);
      StatementNode.Block errorHandler = (StatementNode.Block) input.getInternalNode(6);

      return new StatementNode.TryCatch(block, identifier.lexeme, errorHandler);
    }));
    reductions.put(35, new Reduction((input) -> new StatementNode.Throw(input.getToken(1).lexeme)));

    // BEGIN HANDLING SWITCH STATEMENTS
    reductions.put(36, new Reduction((input) -> new StatementNode.Switch(input.getExpressionNode(2),
        (Node.SwitchCaseList) input.getInternalNode(5))));
    reductions.put(37, new Reduction((input) -> new StatementNode.Switch(input.getExpressionNode(2))));
    reductions.put(38, new Reduction((input) -> {
      Node.SwitchCaseList list = (Node.SwitchCaseList) input.getInternalNode(0);
      Node.StatementList defaultHandler = (Node.StatementList) input.getInternalNode(3);
      return list.setDefaultCase(defaultHandler);
    }));
    reductions.put(39,
        new Reduction((input) -> new Node.SwitchCaseList(
            new Node.SwitchCaseList.SwitchCase(((Node.StatementList) input.getInternalNode(2))))));
    reductions.put(40, passthroughReducer);
    reductions.put(41, new Reduction((input) -> ((Node.SwitchCaseList) input.getInternalNode(0))
        .addNamedCase((Node.SwitchCaseList.SwitchCase) input.getInternalNode(1))));
    reductions.put(42,
        new Reduction((input) -> new Node.SwitchCaseList((Node.SwitchCaseList.SwitchCase) input.getInternalNode(0))));

    Reduction switchCaseReducer = new Reduction((input) -> new Node.SwitchCaseList.SwitchCase(
        input.getToken(2),
        ((Node.StatementList) input.getInternalNode(5))));

    reductions.put(43, switchCaseReducer);
    reductions.put(44, switchCaseReducer);
    reductions.put(45,
        new Reduction((input) -> ((Node.StatementList) input.getInternalNode(1))));
    reductions.put(46,
        new Reduction((input) -> new Node.StatementList()));

    reductions.put(47,
        new Reduction((input) -> ((Node.StatementList) input.getInternalNode(0)).add(input.getStatementNode(1))));
    reductions.put(48, new Reduction((input) -> new Node.StatementList(input.getStatementNode(0))));
    reductions.put(49, new Reduction((input) -> input.getStatementNode(0)));
    reductions.put(50, new Reduction((input) -> new StatementNode.SwitchBreak()));
    reductions.put(51, new Reduction((input) -> new StatementNode.SwitchGoto(input.getToken(1).lexeme)));
    reductions.put(52, new Reduction((input) -> new StatementNode.SwitchGoto(input.getToken(1).lexeme)));
    // END HANDLING SWITCH STATEMENTS

    // TEMPLATE FOR NEW REDUCTIONS
    // reductions.put(51, new Reduction((input) -> {}));

    // BEGIN HANDLING LOOPS
    reductions.put(53, passthroughReducer);
    reductions.put(54, passthroughReducer);
    reductions.put(55, passthroughReducer);

    reductions.put(56, new Reduction((input) -> {
      Token identifier = input.getToken(3);
      TypeExpressionNode type = input.getTypeExpressionNode(5);
      ExpressionNode expr = input.getExpressionNode(7);
      StatementNode body = input.getStatementNode(9);
      return new StatementNode.ForEachLoop(identifier, type, expr, body);
    }));
    reductions.put(57, new Reduction((input) -> {
      Node.CounterLoopInit init = (Node.CounterLoopInit) input.getInternalNode(2);
      ExpressionNode condition = input.getExpressionNode(4);
      Node.ExpressionList increment = (Node.ExpressionList) input.getInternalNode(6);
      StatementNode body = input.getStatementNode(8);
      return StatementNode.CounterControlledLoop.all(init, condition, increment, body);
    }));
    reductions.put(58, new Reduction((input) -> {
      ExpressionNode condition = input.getExpressionNode(3);
      Node.ExpressionList increment = (Node.ExpressionList) input.getInternalNode(5);
      StatementNode body = input.getStatementNode(7);
      return StatementNode.CounterControlledLoop.missingInit(condition, increment, body);
    }));
    reductions.put(59, new Reduction((input) -> {
      Node.CounterLoopInit init = (Node.CounterLoopInit) input.getInternalNode(2);
      Node.ExpressionList increment = (Node.ExpressionList) input.getInternalNode(5);
      StatementNode body = input.getStatementNode(7);
      return StatementNode.CounterControlledLoop.missingCondition(init, increment, body);
    }));
    reductions.put(60, new Reduction((input) -> {
      Node.CounterLoopInit init = (Node.CounterLoopInit) input.getInternalNode(2);
      ExpressionNode condition = input.getExpressionNode(4);
      StatementNode body = input.getStatementNode(7);
      return StatementNode.CounterControlledLoop.missingIncrement(init, condition, body);
    }));
    reductions.put(61, new Reduction((input) -> {
      Node.ExpressionList increment = (Node.ExpressionList) input.getInternalNode(4);
      StatementNode body = input.getStatementNode(6);
      return StatementNode.CounterControlledLoop.onlyIncrement(increment, body);
    }));
    reductions.put(62, new Reduction((input) -> {
      Node.CounterLoopInit init = (Node.CounterLoopInit) input.getInternalNode(2);
      StatementNode body = input.getStatementNode(6);
      return StatementNode.CounterControlledLoop.onlyInit(init, body);
    }));
    reductions.put(63, new Reduction((input) -> {
      ExpressionNode condition = input.getExpressionNode(3);
      StatementNode body = input.getStatementNode(6);
      return StatementNode.CounterControlledLoop.onlyCondition(condition, body);
    }));

    reductions.put(64, new Reduction((input) -> {
      StatementNode body = input.getStatementNode(5);
      return StatementNode.CounterControlledLoop.none(body);
    }));

    // HANDLE COUNTER_LOOP_INIT UNTIL LOOP_BODY_STATEMENT
    reductions.put(65,
        new Reduction(
            (input) -> ((Node.CounterLoopInit) input.getInternalNode(0)).add((Node.VariableDeclaration) input
                .getInternalNode(1))));
    reductions.put(66, new Reduction((input) -> new Node.CounterLoopInit((Node.VariableDeclaration) input
        .getInternalNode(0))));
    reductions.put(67,
        new Reduction((input) -> ((Node.ExpressionList) input.getInternalNode(2)).add(input.getExpressionNode(0))));
    reductions.put(68, new Reduction((input) -> new Node.ExpressionList(input.getExpressionNode(0))));
    reductions.put(69,
        new Reduction((input) -> new StatementNode.WhileLoop(input.getExpressionNode(2),
            (StatementNode) input.getStatementNode(4))));
    reductions.put(70,
        new Reduction((input) -> new StatementNode.WhileLoop((StatementNode) input.getInternalNode(3))));
    reductions.put(71,
        new Reduction((input) -> new StatementNode.Block()));
    reductions.put(72,
        new Reduction((input) -> new StatementNode.Block((Node.StatementList) input.getInternalNode(1))));
    reductions.put(73,
        new Reduction((input) -> ((Node.StatementList) input.getInternalNode(0)).add(input.getStatementNode(1))));
    reductions.put(74, new Reduction((input) -> new Node.StatementList(input.getStatementNode(0))));
    reductions.put(75, passthroughReducer);
    reductions.put(76, new Reduction((input) -> new StatementNode.LoopBreak()));
    reductions.put(77, new Reduction((input) -> new StatementNode.LoopContinue()));

    // EXPRESSION STATEMENTS AND EXPRESSIONS BELOW
    reductions.put(78, new Reduction((input) -> new StatementNode.Expression(input.getExpressionNode(0))));
    // passthrough for FUNCTION EXPRESSION / ARRAY LITERAL / OBJECT LITERAL /
    // LOGICAL EXPRESSION
    reductions.put(79, passthroughReducer);
    reductions.put(80, passthroughReducer);
    reductions.put(81, passthroughReducer);
    reductions.put(82, passthroughReducer);

    // HANDLE FUNCTION EXPRESSIONS
    reductions.put(83, new Reduction((input) -> {
      Node.ParameterList parameterList = (Node.ParameterList) input.getInternalNode(2);
      TypeExpressionNode returnType = (TypeExpressionNode) input.getTypeExpressionNode(5);
      StatementNode body = (StatementNode) input.getInternalNode(7);
      return new ExpressionNode.FunctionExpression(parameterList, returnType, body);
    }));
    reductions.put(84, new Reduction((input) -> {
      TypeExpressionNode returnType = (TypeExpressionNode) input.getTypeExpressionNode(4);
      StatementNode body = (StatementNode) input.getInternalNode(6);
      return new ExpressionNode.FunctionExpression(returnType, body);
    }));
    reductions.put(85, new Reduction((input) -> {
      Node.VariableDeclarationHeader variableDeclarationHeader = (Node.VariableDeclarationHeader) input
          .getInternalNode(0);
      Node.ParameterList parameterList = (Node.ParameterList) input.getInternalNode(2);
      return parameterList.add(variableDeclarationHeader);
    }));
    reductions.put(86, new Reduction((input) -> {
      Node.VariableDeclarationHeader variableDeclarationHeader = (Node.VariableDeclarationHeader) input
          .getInternalNode(0);
      return new Node.ParameterList(variableDeclarationHeader);
    }));
    // END HANDLING FUNCTION EXPRESSIONS

    // HANDLE ARRAY AND OBJECT LITERALS
    reductions.put(87,
        new Reduction((input) -> new ExpressionNode.ArrayLiteral((Node.ExpressionList) input.getInternalNode(1))));
    reductions.put(88, new Reduction((input) -> new ExpressionNode.ArrayLiteral()));
    reductions.put(89, new Reduction(
        (input) -> new ExpressionNode.ObjectLiteral((Node.ObjectLiteralFieldList) input.getInternalNode(1))));
    reductions.put(90, new Reduction((input) -> new ExpressionNode.ObjectLiteral()));
    reductions.put(91, new Reduction((input) -> ((Node.ObjectLiteralFieldList) input.getInternalNode(2))
        .add((Node.ObjectLiteralField) input.getInternalNode(0))));
    reductions.put(92,
        new Reduction((input) -> new Node.ObjectLiteralFieldList((Node.ObjectLiteralField) input.getInternalNode(0))));
    reductions.put(93,
        new Reduction((input) -> new Node.ObjectLiteralField(input.getToken(0).lexeme, input.getExpressionNode(2))));

    // BEGIN FUNNY
    reductions.put(94, binaryOperationReducer);
    reductions.put(95, binaryOperationReducer);
    reductions.put(96, passthroughReducer);
    reductions.put(97, binaryOperationReducer);
    reductions.put(98, binaryOperationReducer);
    reductions.put(99, binaryOperationReducer);
    reductions.put(100, passthroughReducer);
    reductions.put(101, binaryOperationReducer);
    reductions.put(102, binaryOperationReducer);
    reductions.put(103, passthroughReducer);
    reductions.put(104, binaryOperationReducer);
    reductions.put(105, binaryOperationReducer);
    reductions.put(106, binaryOperationReducer);
    reductions.put(107, binaryOperationReducer);
    reductions.put(108, passthroughReducer);
    reductions.put(109, binaryOperationReducer);
    reductions.put(110, binaryOperationReducer);
    reductions.put(111, passthroughReducer);
    reductions.put(112, binaryOperationReducer);
    reductions.put(113, binaryOperationReducer);
    reductions.put(114, passthroughReducer);
    reductions.put(115, binaryOperationReducer);
    reductions.put(116, binaryOperationReducer);
    reductions.put(117, binaryOperationReducer);
    reductions.put(118, passthroughReducer);
    reductions.put(119, unaryOperationReducer);
    reductions.put(120, unaryOperationReducer);
    reductions.put(121, passthroughReducer);
    reductions.put(122, binaryOperationReducer);
    reductions.put(123, passthroughReducer);
    reductions.put(124, passthroughReducer);
    reductions.put(125, passthroughReducer);
    reductions.put(126, passthroughReducer);
    reductions.put(127, passthroughReducer);

    // HANDLE INCREMENTATION AND ASSIGNMENT
    reductions.put(128, new Reduction(
        (input) -> new ExpressionNode.Incrementation(input.getExpressionNode(0), input.getToken(1), true)));
    reductions.put(129, new Reduction(
        (input) -> new ExpressionNode.Incrementation(input.getExpressionNode(0), input.getToken(1), true)));
    reductions.put(130, new Reduction(
        (input) -> new ExpressionNode.Incrementation(input.getExpressionNode(1), input.getToken(0), false)));
    reductions.put(131, new Reduction(
        (input) -> new ExpressionNode.Incrementation(input.getExpressionNode(1), input.getToken(0), false)));
    reductions.put(132, new Reduction(
        (input) -> new ExpressionNode.Assignment(input.getExpressionNode(0), input.getExpressionNode(2))));

    // HANDLE ASSIGNABLE LEFT EXPRESSIONS
    reductions.put(133, new Reduction((input) -> new ExpressionNode.Identifier(input.getToken(0).lexeme)));
    reductions.put(134,
        new Reduction((input) -> new ExpressionNode.DotAccess(input.getExpressionNode(0), input.getToken(2).lexeme)));
    reductions.put(135,
        new Reduction(
            (input) -> new ExpressionNode.DotAccess(new ExpressionNode.FunctionCall(input.getExpressionNode(0),
                (Node.ExpressionList) input.getInternalNode(2)),
                input.getToken(5).lexeme)));
    reductions.put(136,
        new Reduction(
            (input) -> new ExpressionNode.DotAccess(new ExpressionNode.FunctionCall(input.getExpressionNode(0)),
                input.getToken(4).lexeme)));

    reductions.put(137,
        new Reduction(
            (input) -> new ExpressionNode.IndexAccess(input.getExpressionNode(0), input.getExpressionNode(2))));
    reductions.put(138,
        new Reduction(
            (input) -> new ExpressionNode.IndexAccess(new ExpressionNode.FunctionCall(input.getExpressionNode(0),
                (Node.ExpressionList) input.getInternalNode(2)),
                input.getExpressionNode(5))));
    reductions.put(139,
        new Reduction(
            (input) -> new ExpressionNode.IndexAccess(new ExpressionNode.FunctionCall(input.getExpressionNode(0)),
                input.getExpressionNode(4))));

    // HANDLE IDENTIFIER STACK EXPRESSIONS
    reductions.put(140, new Reduction((input) -> {
      try {
        return new ExpressionNode.Identifier(input.getToken(0).lexeme);
      } catch (Error e) {
        // <ASSIGNABLE_TARGET_EXPRESSION> uses this reduction when it spots an r_paren
        return input.getExpressionNode(0);
      }
    }));

    reductions.put(141,
        new Reduction((input) -> new ExpressionNode.DotAccess(input.getExpressionNode(0), input.getToken(2).lexeme)));
    reductions.put(142,
        new Reduction(
            (input) -> new ExpressionNode.IndexAccess(input.getExpressionNode(0), input.getExpressionNode(2))));
    reductions.put(143,
        new Reduction(
            (input) -> new ExpressionNode.FunctionCall(input.getExpressionNode(0),
                (Node.ExpressionList) input.getInternalNode(2))));
    reductions.put(144,
        new Reduction(
            (input) -> new ExpressionNode.FunctionCall(input.getExpressionNode(0))));

    // HANDLE LITERALS AND GROUPING EXPRESSION
    addALotOfSameReduction(145, 148,
        new Reduction((input) -> new ExpressionNode.Literal(input.getToken(0).lexeme, input.getToken(0).type)),
        reductions);
    reductions.put(149,
        new Reduction(
            (input) -> new ExpressionNode.Grouping(input.getExpressionNode(1))));

    // HANDLE TYPES
    reductions.put(150,
        new Reduction(
            (input) -> new StatementNode.ObjectTypeDeclaration((Node.PropertyList) input.getInternalNode(3))));
    reductions.put(151,
        new Reduction(
            (input) -> new StatementNode.ObjectTypeDeclaration()));
    reductions.put(152, new Reduction((input) -> ((Node.PropertyList) input.getInternalNode(0))
        .add((Node.PropertyDefinition) input.getInternalNode(1))));
    reductions.put(153,
        new Reduction((input) -> new Node.PropertyList((Node.PropertyDefinition) input.getInternalNode(0))));
    reductions.put(154, new Reduction(
        (input) -> new Node.PropertyDefinition(input.getToken(0).lexeme, input.getTypeExpressionNode(2))));
    reductions.put(155, new Reduction((input) -> new TypeExpressionNode.Array(input.getTypeExpressionNode(0))));

    Reduction typeReductionEndpoint = new Reduction(
        (input) -> new TypeExpressionNode.Identifier(input.getToken(0).lexeme));
    addALotOfSameReduction(156, 159, typeReductionEndpoint, reductions);
    reductions.put(160, new Reduction((input) -> new TypeExpressionNode.Identifier(input.getToken(1).lexeme)));
    reductions.put(161, passthroughReducer);

    // HANDLE LAMBDA TYPES
    reductions.put(162,
        new Reduction((input) -> new TypeExpressionNode.Lambda((Node.LambdaParamterList) input.getInternalNode(2),
            input.getTypeExpressionNode(5))));
    reductions.put(163, new Reduction((input) -> new TypeExpressionNode.Lambda(input.getTypeExpressionNode(4))));
    reductions.put(164, new Reduction(
        (input) -> ((Node.LambdaParamterList) input.getInternalNode(2)).add(input.getTypeExpressionNode(0))));
    reductions.put(165, new Reduction((input) -> new Node.LambdaParamterList(input.getTypeExpressionNode(0))));

    return reductions;
  }
}
