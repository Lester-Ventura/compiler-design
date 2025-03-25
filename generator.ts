/**
 * TO USE THIS FILE:
 * - create a file called "example.txt" with the grammar, copy to a file called grammar.txt
 * - run tsx generator.ts to generate the table.txt file
 * - copy the table.txt file to slr1_table.txt
 */

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

  return tokens;
}

const tokens = Lexer(grammar);
type Production = {
  name: string;
  rhs: Token[];
  initial: boolean;
};

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

const productions = Parse(tokens);

const eqSet = <T>(xs: Set<T>, ys: Set<T>) => xs.size === ys.size && [...xs].every((x) => ys.has(x));
const xContainsAllOfY = <T>(xs: Set<T>, ys: Set<T>) => [...ys].every((x) => xs.has(x));

function computeFirstSets(allProductions: Production[]) {
  // create a list of first sets
  const ret = new Map<string, Set<string>>();
  for (const production of allProductions) ret.set(production.name, new Set());

  let wasEdited = true;
  while (wasEdited) {
    wasEdited = false;

    for (const production of allProductions) {
      const toBeModified = ret.get(production.name)!;

      const firstRhsToken = production.rhs[0];
      if (firstRhsToken.type === "variable") {
        const add = ret.get(firstRhsToken.name)!;
        if (!xContainsAllOfY(toBeModified, add)) {
          wasEdited = true;
          ret.set(production.name, new Set([...toBeModified, ...add]));
        }
      } else if (firstRhsToken.type === "terminal") {
        // add terminal to toBeModified
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
  ret.get(allProductions.find((p) => p.initial)!.name)!.add("eof");

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
                // next token is a terminal, check if its in existng follow set
                // if not, add it
                const existing = ret.get(variable)!;
                if (!existing.has(nextRhsToken.name)) {
                  wasEdited = true;
                  ret.set(variable, new Set([...existing, nextRhsToken.name]));
                }
              } else if (nextRhsToken.type === "variable") {
                // next token is a variable, get the first set of the variable
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

type Item = { lhs: string; rhs: Token[]; dot: number; lookahead: string[] };
function generateInitialItemSet(production: Production, allProductions: Production[]) {
  const initialItem = { lhs: production.name, rhs: production.rhs, dot: 0, lookahead: ["eof"] } as Item;
  return expandItemSet([initialItem], allProductions);
}

function expandItemSet(items: Item[], allProductions: Production[]) {
  const { followSets, firstSets } = computeFollowSets(allProductions);
  const itemSet: Item[] = [...items];
  const unprocesssedItems: Item[] = [...items];
  const determineNextSequence = ([next]: Token[]) => {
    if (next.type === "terminal") return [next.name];
    else if (next.type === "variable") return [...firstSets.get(next.name)!];
  };

  while (unprocesssedItems.length > 0) {
    const currentItem = unprocesssedItems.shift()!;
    // check if the symbol after the period is a non-terminal
    const after = currentItem.rhs[currentItem.dot];
    if (after == null) continue; // TODO: check if this should be here

    if (after.type === "variable") {
      // we need to expand this
      const newProductions = allProductions.filter((p) => p.name === after.name);
      const rest = currentItem.rhs.slice(currentItem.dot + 1);
      // determine the set of first tokens for rest (LL1 GRAMMAR!)
      const lookahead = rest.length === 0 ? [...followSets.get(currentItem.lhs)!] : determineNextSequence(rest);

      for (const newProduction of newProductions) {
        const newItem = { lhs: newProduction.name, rhs: newProduction.rhs, dot: 0, lookahead } as Item;
        const encoding = JSON.stringify(newItem);
        if (itemSet.some((i) => JSON.stringify(i) === encoding) == false) {
          unprocesssedItems.push(newItem);
          itemSet.push(newItem);
        }
      }
    }
  }

  return { itemSet, initialItem: items[0] };
}

type State = { itemSet: Item[]; initialItem: Item; count: number };
const initialItemSet = generateInitialItemSet(productions[0], productions);

function generateStates(_initialState: { itemSet: Item[]; initialItem: Item }) {
  const initialState = { ..._initialState, count: 0 };
  const states = [initialState] as State[];
  const unprocessedStates = [initialState] as State[];

  // array of maps whose keys is a variable and the value are the state to goto next
  const GotoTable = [] as Map<string, number>[];
  // array of maps whose keys is a terminal is a value of either to shift or reduce
  const ActionTable = [] as Map<string, { action: "shift" | "reduce"; value: number }>[];

  while (unprocessedStates.length > 0) {
    const currentState = unprocessedStates.shift()!;
    const nextStates_Goto = new Map<string, Item[]>();
    const nextStates_Shift = new Map<string, Item[]>();

    // determine transitions out of currentState
    calculateNextItem: for (const item of currentState.itemSet) {
      // check next symbol after the dot
      const nextSymbol = item.rhs[item.dot];

      if (nextSymbol == null) {
        // we are at the end of the production, so we need to add the lookahead
        const productionToReduceTo = productions
          .map((p, i) => [p, i] as const)
          .find(([p, _]) => JSON.stringify(p.rhs) === JSON.stringify(item.rhs))![1];

        for (const lookahead of item.lookahead) {
          if (ActionTable[currentState.count] === undefined) ActionTable[currentState.count] = new Map();
          ActionTable[currentState.count]!.set(lookahead, { action: "reduce", value: productionToReduceTo });
        }

        continue calculateNextItem;
      }

      const newItem: Item = { ...item, dot: item.dot + 1 };

      if (nextSymbol.type === "variable") {
        // already exists, so create the new item from existing item
        if (nextStates_Goto.has(nextSymbol.name)) nextStates_Goto.get(nextSymbol.name)!.push(newItem);
        else nextStates_Goto.set(nextSymbol.name, [newItem]);
      } else if (nextSymbol.type === "terminal") {
        // next symbol is a terminal
        if (nextStates_Shift.has(nextSymbol.name)) nextStates_Shift.get(nextSymbol.name)!.push(newItem);
        else nextStates_Shift.set(nextSymbol.name, [newItem]);
      }
    }

    // Handle GOTOs and SHIFTs
    for (const [gotoTransition, gotoItems] of nextStates_Goto.entries()) {
      const expandedItems = expandItemSet(gotoItems, productions);

      // check if a state exist with the same initial item
      let existingState = states.find((s) => {
        return (
          s.initialItem.lhs === expandedItems.initialItem.lhs &&
          s.initialItem.rhs.length === expandedItems.initialItem.rhs.length &&
          JSON.stringify(s.initialItem.rhs) === JSON.stringify(expandedItems.initialItem.rhs) &&
          s.initialItem.lookahead.length === expandedItems.initialItem.lookahead.length &&
          JSON.stringify(s.initialItem.lookahead) === JSON.stringify(expandedItems.initialItem.lookahead) &&
          JSON.stringify(s.itemSet) === JSON.stringify(expandedItems.itemSet)
        );
      });

      // console.log({ expandedItems, gotoItems, existingState });
      // no such state exists
      if (existingState == undefined) {
        const newState: State = {
          itemSet: expandedItems.itemSet,
          initialItem: expandedItems.initialItem,
          count: states.length,
        };
        states.push(newState);
        unprocessedStates.push(newState);

        if (GotoTable[currentState.count] === undefined) GotoTable[currentState.count] = new Map();
        GotoTable[currentState.count]!.set(gotoTransition, newState.count);
      } else {
        if (GotoTable[currentState.count] === undefined) GotoTable[currentState.count] = new Map();
        GotoTable[currentState.count]!.set(gotoTransition, existingState.count);
      }
    }

    for (const [shiftTransition, shiftItems] of nextStates_Shift.entries()) {
      const expandedItems = expandItemSet(shiftItems, productions);

      // check if a state exist with the same initial item
      let existingState = states.find((s) => {
        return (
          s.initialItem.lhs === expandedItems.initialItem.lhs &&
          s.initialItem.rhs.length === expandedItems.initialItem.rhs.length &&
          JSON.stringify(s.initialItem.rhs) === JSON.stringify(expandedItems.initialItem.rhs) &&
          s.initialItem.lookahead.length === expandedItems.initialItem.lookahead.length &&
          JSON.stringify(s.initialItem.lookahead) === JSON.stringify(expandedItems.initialItem.lookahead) &&
          JSON.stringify(s.itemSet) === JSON.stringify(expandedItems.itemSet)
        );
      });

      // no such state exists
      if (existingState == undefined) {
        const newState: State = {
          itemSet: expandedItems.itemSet,
          initialItem: expandedItems.initialItem,
          count: states.length,
        };
        states.push(newState);
        unprocessedStates.push(newState);

        if (ActionTable[currentState.count] === undefined) ActionTable[currentState.count] = new Map();
        if (ActionTable[currentState.count]!.has(shiftTransition)) {
          console.log("conflict");
        }

        ActionTable[currentState.count]!.set(shiftTransition, { action: "shift", value: newState.count });
      } else {
        if (ActionTable[currentState.count] === undefined) ActionTable[currentState.count] = new Map();
        if (ActionTable[currentState.count]!.has(shiftTransition)) {
          console.log("conflict");
        }

        ActionTable[currentState.count]!.set(shiftTransition, { action: "shift", value: existingState.count });
      }
    }

    // console.log(nextStates_Goto);
    // console.log(nextStates_Shift);
  }

  return { states, GotoTable, ActionTable };
}

const { states, ActionTable, GotoTable } = generateStates(initialItemSet);
fs.writeFileSync("states.json", JSON.stringify(states, null, 2));

const final = ActionTable.map((actions, idx) => {
  const testing = GotoTable[idx] === undefined ? [] : [...GotoTable[idx].entries()].map(([k, v]) => `<${k}>=${v}`);
  return [
    ...[...actions.entries()].map(([k, { action, value }]) => `[${k}]=${action === "reduce" ? "r" : "s"}${value}`),
    ...testing,
  ].join(", ");
}).join("\n");

console.log(final);
fs.writeFileSync("table.txt", final);
