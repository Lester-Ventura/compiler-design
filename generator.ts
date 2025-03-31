import fs from "fs";
const grammar: string = fs.readFileSync("example.txt", "utf8");

type Token =
  | { type: "variable"; name: string }
  | { type: "terminal"; name: string }
  | { type: "semicolon" }
  | { type: "colon" }
  | { type: "eof" };

function Lexer(input: string) {
  const tokens: Token[] = [];
  const terminals: Set<string> = new Set();
  const variables: Set<string> = new Set();

  let currentCharacterIndex = 0;

  function parseVariable() {
    currentCharacterIndex++;
    let name = "";

    while (currentCharacterIndex < input.length && input[currentCharacterIndex] !== ">") {
      name += input[currentCharacterIndex];
      currentCharacterIndex++;
    }

    currentCharacterIndex++;
    tokens.push({ type: "variable" as const, name });
    variables.add(name);
  }

  function parseTerminal() {
    currentCharacterIndex++;
    let name = "";

    while (currentCharacterIndex < input.length && input[currentCharacterIndex] !== "]") {
      name += input[currentCharacterIndex];
      currentCharacterIndex++;
    }

    currentCharacterIndex++;
    tokens.push({ type: "terminal" as const, name });
    terminals.add(name);
  }

  while (currentCharacterIndex < input.length) {
    const currentCharacter = input[currentCharacterIndex];
    if (currentCharacter === " " || currentCharacter === "\n") currentCharacterIndex++;
    else if (currentCharacter === "<") parseVariable();
    else if (currentCharacter === "[") parseTerminal();
    else if (currentCharacter === ";") {
      tokens.push({ type: "semicolon" });
      currentCharacterIndex++;
    } else if (currentCharacter === ":") {
      tokens.push({ type: "colon" });
      currentCharacterIndex++;
    }
  }

  return { tokens, terminals: [...terminals], variables: [...variables] };
}

// Tokenize the input and also fetch the list of terminals and variables
const { tokens, terminals, variables } = Lexer(grammar);

type Production = { name: string; rhs: Token[]; initial: boolean };
function Parse(tokens: Token[]) {
  const productions: Production[] = [];
  let currentTokenIndex = 0;

  function expect(type: Token["type"]) {
    if (tokens[currentTokenIndex].type === type) {
      currentTokenIndex++;
      return true;
    } else throw new Error(`Expected ${type} but got ${tokens[currentTokenIndex].type}`);
  }

  let hasCapturedInitial = false;
  function captureProduction() {
    const currentToken = tokens[currentTokenIndex];
    currentTokenIndex++;
    expect("colon");

    const rhsTokens: Token[] = [];
    while (currentTokenIndex < tokens.length && tokens[currentTokenIndex].type !== "semicolon") {
      rhsTokens.push(tokens[currentTokenIndex]);
      currentTokenIndex++;
    }

    expect("semicolon");
    productions.push({
      initial: !hasCapturedInitial,
      name: currentToken.type === "variable" ? currentToken.name : "trash token",
      rhs: rhsTokens,
    });

    hasCapturedInitial = true;
  }

  while (currentTokenIndex < tokens.length) {
    const currentToken = tokens[currentTokenIndex];
    if (currentToken.type === "variable") captureProduction();
    else throw new Error(`Unexpected token ${currentToken.type}`);
  }

  return productions;
}

// Generate the list of productions from the tokens
const productions = Parse(tokens);

const xContainsAllOfY = <T>(xs: Set<T>, ys: Set<T>) => [...ys].every((x) => xs.has(x));

// Creates a list of first sets for each production
function computeFirstSets(allProductions: Production[]) {
  // Initialize the hashmap
  const ret = new Map<string, Set<string>>();
  for (const production of allProductions) ret.set(production.name, new Set());

  // Keep iterating through all productions until no edits are made
  let wasEdited = true;
  while (wasEdited) {
    wasEdited = false;

    for (const production of allProductions) {
      const toBeModified = ret.get(production.name)!;

      const firstRhsToken = production.rhs[0];
      if (firstRhsToken.type === "variable") {
        const add = ret.get(firstRhsToken.name)!;
        if (add == null) throw new Error("add is null, " + firstRhsToken.name);

        // check if the hashmap of the current production already contains all the items to be added
        // if not then add them and set wasEdited to true so we iterate one more time
        if (!xContainsAllOfY(toBeModified, add)) {
          wasEdited = true;
          ret.set(production.name, new Set([...toBeModified, ...add]));
        }
      } else if (firstRhsToken.type === "terminal") {
        // add terminal to toBeModified and set wasEdited to true
        if (toBeModified.has(firstRhsToken.name) == false) {
          wasEdited = true;
          ret.set(production.name, new Set([...toBeModified, firstRhsToken.name]));
        }
      }
    }
  }

  return ret;
}

function computeFollowSets(allProductions: Production[]) {
  const firstSets = computeFirstSets(allProductions);

  // create a list of follow sets and add EOF to the initial production
  const ret = new Map<string, Set<string>>();
  for (const production of allProductions) ret.set(production.name, new Set());
  ret.get(allProductions.find((p) => p.initial)!.name)!.add("EOF");

  let wasEdited = true;
  while (wasEdited) {
    wasEdited = false;

    for (const variable of ret.keys()) {
      for (const currentProduction of allProductions) {
        // check the rhs of current production for variable
        const rhs = currentProduction.rhs;

        for (let i = 0; i < rhs.length; i++) {
          const currentRhsToken = rhs[i];

          if (currentRhsToken.type === "variable" && currentRhsToken.name === variable) {
            if (i === rhs.length - 1) {
              // we are at the end of rhs, so whatever is in currentProduction
              // we need to also need to add to variable
              const toBeAdded = ret.get(currentProduction.name)!;
              const receiver = ret.get(variable)!;
              if (!xContainsAllOfY(receiver, toBeAdded)) {
                wasEdited = true;
                ret.set(variable, new Set([...toBeAdded, ...receiver]));
              }
            } else {
              // not at the end of rhs, so we need to add the FIRST set of the next rhs token
              const nextRhsToken = rhs[i + 1];
              if (nextRhsToken.type === "terminal") {
                // next token is a terminal, check if its in existng follow set and add if it doesn't exist
                const existing = ret.get(variable)!;
                if (!existing.has(nextRhsToken.name)) {
                  wasEdited = true;
                  ret.set(variable, new Set([...existing, nextRhsToken.name]));
                }
              } else if (nextRhsToken.type === "variable") {
                // next token is a variable, get the first set of the variable
                // check if it's not in the followset and add if not
                const firstSet = firstSets.get(nextRhsToken.name)!;
                
                if (!xContainsAllOfY(ret.get(variable)!, firstSet)) {
                  wasEdited = true;
                  ret.set(variable, new Set([...ret.get(variable)!, ...firstSet]));
                }
              }
            }
          }
        }
      }
    }
  }

  return { firstSets, followSets: ret };
}

const { followSets, firstSets } = computeFollowSets(productions);

type Item = { lhs: string; rhs: Token[]; dot: number; lookahead: string[] };

// This funciton takes a token and determines what the next lookahead should be
// The implementation of this function ensures that the table is an LR(1) parsing table
const determineNextLookAhead = ([next]: Token[]) => {
  if (next.type === "terminal") return [next.name];
  else if (next.type === "variable") return [...firstSets.get(next.name)!];
};

// This fnuction creates the initial item set based on the
// production provided. It creating the initial item and expands it
function generateInitialItemSet(production: Production) {
  const initialItem = { lhs: production.name, rhs: production.rhs, dot: 0, lookahead: ["EOF"] } as Item;
  return expandItemSet([initialItem]);
}

// This function expands the item sets provided to it
function expandItemSet(items: Item[]) {
  const itemSet: Item[] = [...items];

  // Create a queue of unprocessed items and keep shifting until there are no more items left
  const unprocesssedItems: Item[] = [...items];
  while (unprocesssedItems.length > 0) {
    const currentItem = unprocesssedItems.shift()!;

    // Check if the symbol after the dot is a non terminal
    const after = currentItem.rhs[currentItem.dot];
    if (after == null) continue; // TODO: check if this should be here

    if (after.type === "variable") {
      // Find prodctions whose left hand side is the symbol after the dot
      const newProductions = productions.filter((p) => p.name === after.name);

      // Compute the lookahead for the new productions to be added to the item set
      const rest = currentItem.rhs.slice(currentItem.dot + 1);
      const lookahead = rest.length === 0 ? [...followSets.get(currentItem.lhs)!] : determineNextLookAhead(rest);

      for (const newProduction of newProductions) {
        // Create the new item and check if it already exists in the item set
        // If it doesn't exist, add it to the item set and the queue of unprocessed items
        const newItem = { lhs: newProduction.name, rhs: newProduction.rhs, dot: 0, lookahead } as Item;
        const encoding = JSON.stringify(newItem);

        if (itemSet.some((i) => JSON.stringify(i) === encoding) == false) {
          unprocesssedItems.push(newItem);
          itemSet.push(newItem);
        }
      }
    }
  }

  // Return the item set and the kernel
  return { itemSet, kernel: items[0] };
}

type State = { itemSet: Item[]; kernel: Item; count: number };
const initialItemSet = generateInitialItemSet(productions[0]);

function generateStates(_initialState: { itemSet: Item[]; kernel: Item }) {
  const initialState = { ..._initialState, count: 0 };
  // Create a list of states to be returned and another list of states that have not been vistited
  const states = [initialState] as State[];
  const unprocessedStates = [initialState] as State[];

  // array of maps whose keys is a variable and the value are the state to goto next
  const GotoTable = [] as Map<string, number>[];
  // array of maps whose keys is a terminal is a value of either to shift or reduce
  const ActionTable = [] as Map<string, { action: "shift" | "reduce"; value: number }>[];

  // While there is an unprocessed state, visit it
  while (unprocessedStates.length > 0) {
    const currentState = unprocessedStates.shift()!;
    const nextStates_Goto = new Map<string, Item[]>();
    const nextStates_Shift = new Map<string, Item[]>();

    // determine transitions out of the current state by iterating
    // through all the items in the item set of the state
    calculateNextItem: for (const item of currentState.itemSet) {
      // check next symbol after the dot
      const nextSymbol = item.rhs[item.dot];

      // no next symbol is available, therefore we need to create a reduction
      if (nextSymbol == null) {
        // Find the prodction that this item reduces to using the symbols on its right hand side
        const productionToReduceTo = productions
          .map((p, i) => [p, i] as const)
          .find(([p, _]) => JSON.stringify(p.rhs) === JSON.stringify(item.rhs))![1];

        // for each lookahead in the item, create a new entry in the action table to reduce to the production found
        for (const lookahead of item.lookahead) {
          if (ActionTable[currentState.count] === undefined) ActionTable[currentState.count] = new Map();
          ActionTable[currentState.count]!.set(lookahead, { action: "reduce", value: productionToReduceTo });
        }

        continue calculateNextItem;
      }

      // Create the next item by shifting the dot by 1 place
      const newItem: Item = { ...item, dot: item.dot + 1 };

      // Determine if we're going to create a GOTO or SHIFT based on the type of the symbol after the dot
      if (nextSymbol.type === "variable") {
        // Next symbol is a variable so we need to add it to the GOTO table
        // Check if the GOTO table already has an entry for the next symbola and either push or create the array
        if (nextStates_Goto.has(nextSymbol.name)) nextStates_Goto.get(nextSymbol.name)!.push(newItem);
        else nextStates_Goto.set(nextSymbol.name, [newItem]);
      } else if (nextSymbol.type === "terminal") {
        // Next symbol is a terminal so we need to add it to the SHIFT table
        // Check if the SHIFT table already has an entry for the next symbola and either push or create the array
        if (nextStates_Shift.has(nextSymbol.name)) nextStates_Shift.get(nextSymbol.name)!.push(newItem);
        else nextStates_Shift.set(nextSymbol.name, [newItem]);
      }
    }

    // For each GOTO transition from the current state, expand  the item set that it points to
    // and check if a state exist with the same itemset already exists
    // If it doesn't exist, create a new state and add it to the list of states
    // Add the existing / created state to the GOTO table
    for (const [gotoTransition, gotoItems] of nextStates_Goto.entries()) {
      const expandedItems = expandItemSet(gotoItems);

      // check if a state exist with the same item sets
      let existingState = states.find((s) => {
        return (
          s.kernel.lhs === expandedItems.kernel.lhs &&
          s.kernel.rhs.length === expandedItems.kernel.rhs.length &&
          JSON.stringify(s.kernel.rhs) === JSON.stringify(expandedItems.kernel.rhs) &&
          s.kernel.lookahead.length === expandedItems.kernel.lookahead.length &&
          JSON.stringify(s.kernel.lookahead) === JSON.stringify(expandedItems.kernel.lookahead) &&
          JSON.stringify(s.itemSet) === JSON.stringify(expandedItems.itemSet)
        );
      });

      if (GotoTable[currentState.count] === undefined) GotoTable[currentState.count] = new Map();
      if (existingState == undefined) {
        const newState: State = { itemSet: expandedItems.itemSet, kernel: expandedItems.kernel, count: states.length };
        states.push(newState);
        unprocessedStates.push(newState);
        existingState = newState;
      }

      GotoTable[currentState.count]!.set(gotoTransition, existingState.count);
    }

    // For each SHIFT transition from the current state, expand  the item set that it points to
    // and check if a state exist with the same itemset already exists
    // If it doesn't exist, create a new state and add it to the list of states
    // Add the existing / created state to the SHIFT table
    for (const [shiftTransition, shiftItems] of nextStates_Shift.entries()) {
      const expandedItems = expandItemSet(shiftItems);

      // check if a state exist with the same item sets
      let existingState = states.find((s) => {
        return (
          s.kernel.lhs === expandedItems.kernel.lhs &&
          s.kernel.rhs.length === expandedItems.kernel.rhs.length &&
          JSON.stringify(s.kernel.rhs) === JSON.stringify(expandedItems.kernel.rhs) &&
          s.kernel.lookahead.length === expandedItems.kernel.lookahead.length &&
          JSON.stringify(s.kernel.lookahead) === JSON.stringify(expandedItems.kernel.lookahead) &&
          JSON.stringify(s.itemSet) === JSON.stringify(expandedItems.itemSet)
        );
      });

      // If there is no entry in the ActionTable for the current state, create one
      if (ActionTable[currentState.count] === undefined) ActionTable[currentState.count] = new Map();

      // If no such state exists, create a new state and add it to the list of states
      if (existingState == undefined) {
        const newState: State = { itemSet: expandedItems.itemSet, kernel: expandedItems.kernel, count: states.length };
        states.push(newState);
        unprocessedStates.push(newState);
        existingState = newState;
      }

      if (ActionTable[currentState.count]!.has(shiftTransition)) {
        console.log("action conflict at " + currentState.count + " for transition " + shiftTransition);
      }
      ActionTable[currentState.count]!.set(shiftTransition, { action: "shift", value: existingState.count });
    }
  }

  // return the set of states, the GOTO table and the ACTION table
  return { states, GotoTable, ActionTable };
}

const { states, ActionTable, GotoTable } = generateStates(initialItemSet);

// Pring the states, LR(1) parsing table, and other info to their respective files

fs.writeFileSync("states.json", JSON.stringify(states, null, 2));

const final = ActionTable.map((actions, idx) => {
  const testing = GotoTable[idx] === undefined ? [] : [...GotoTable[idx].entries()].map(([k, v]) => `<${k}>=${v}`);
  return [
    ...[...actions.entries()].map(([k, { action, value }]) => `[${k}]=${action === "reduce" ? "r" : "s"}${value}`),
    ...testing,
  ].join(", ");
}).join("\n");

fs.writeFileSync("table.txt", final);

let csv = `state,${terminals.join(",")},${variables.join(",")}\n`;
for (let stateIndex = 0; stateIndex < states.length; stateIndex++) {
  const actions = ActionTable[stateIndex];
  const gotos = GotoTable[stateIndex];
  let line = `${stateIndex}`;

  for (const terminal of terminals) {
    const action = actions.get(terminal);
    if (action == null) line += ",";
    else line += `,${action.action === "reduce" ? "r" : "s"}${action.value}`;
  }

  // line += `,`;

  for (const variable of variables) {
    const goto = gotos?.get(variable);
    if (goto == null) line += ",";
    else line += `,${goto}`;
  }
  line += "\n";
  csv += line;
}

fs.writeFileSync("table.csv", csv);
fs.writeFileSync("terminals.txt", terminals.join("\n"), { encoding: "utf-8" });

const tokenToString = (token: Token) => {
  if (token.type === "terminal") return `<${token.name.toUpperCase()}>`;
  else if (token.type === "variable") return `<${replacer(token.name)}>`;
  else return "";
};

const replacer = (e: string) =>
  e
    .split("_")
    .map((e) => e[0] + e.toLowerCase().substring(1, e.length))
    .join("-");

const testing = productions
  .map((production) => `<${replacer(production.name)}> -> ${production.rhs.map((r) => tokenToString(r)).join(" ")}`)
  .join("\n");
fs.writeFileSync("grammar_cleansed.txt", testing, { encoding: "utf-8" });

let firstSet = "";
for (const [key, value] of firstSets.entries()) {
  firstSet += `${key}=>${[...value].join(",")}\n`;
}
fs.writeFileSync("first.txt", firstSet, { encoding: "utf-8" });

let followSet = "";
for (const [key, value] of followSets.entries()) {
  followSet += `${key}=>${[...value].join(",")}\n`;
}
fs.writeFileSync("follow.txt", followSet, { encoding: "utf-8" });
