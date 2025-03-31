import fs from "fs";

const grammar: string = fs.readFileSync("slr1_table.txt", "utf8");
const terminals = new Set<string>();
const variables = new Set<string>();
const states = grammar.split("\n");

for (const state of states) {
  for (const col of state.split(", ")) {
    const [symbol, action] = col.split("=");
    if (symbol.startsWith("<")) variables.add(symbol.substring(1, symbol.length - 1));
    else terminals.add(symbol.substring(1, symbol.length - 1));
  }
}

const terminalsArray = Array.from(terminals);
const variablesArray = Array.from(variables);

let entire = "state," + terminalsArray.join(",") + "," + variablesArray.join(",") + "\n";

function handleState(state: string, index: number) {
  let ret = `state ${index},`;
  const ActionsMap = new Map<string, string>();
  const GotoMap = new Map<string, string>();

  for (const col of state.split(", ")) {
    const [symbol, action] = col.split("=");
    if (symbol.startsWith("<")) GotoMap.set(symbol.substring(1, symbol.length - 1), action);
    else ActionsMap.set(symbol.substring(1, symbol.length - 1), action);
  }

  for (const terminal of terminalsArray) {
    ret += `${ActionsMap.has(terminal) ? ActionsMap.get(terminal) : ""},`;
  }

  for (const variable of variablesArray) {
    ret += `${GotoMap.has(variable) ? GotoMap.get(variable) : ""},`;
  }

  return ret + "\n";
}

for (let i = 0; i < states.length; i++) {
  entire += handleState(states[i], i);
}

fs.writeFileSync("table.csv", entire);
