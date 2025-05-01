package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

class SymbolTableEntry {
  public LoLangValue value;
  public boolean constant;

  public SymbolTableEntry(LoLangValue value, boolean constant) {
    this.value = value;
    this.constant = constant;
  }
}

public class Environment {
  public Environment parent = null;
  public ArrayList<Environment> siblings = new ArrayList<>();

  public HashMap<String, SymbolTableEntry> variables = new HashMap<>();

  public Environment(Environment parent) {
    this.parent = parent;
  }

  public Environment() {
  }

  public LoLangValue get(String name) {
    if (this.variables.containsKey(name))
      return this.variables.get(name).value;

    else if (this.parent != null)
      return this.parent.get(name);

    for (Environment sibling : this.siblings) {
      LoLangValue value = sibling.get(name);
      if (value != null)
        return value;
    }

    throw new InterpreterError("Cannot find variable \"" + name + "\"");
  }

  public void define(String name, LoLangValue value, boolean constant) {
    SymbolTableEntry newEntry = new SymbolTableEntry(value, constant);
    this.variables.put(name, newEntry);
  }

  public void declare(String name) {
    // only time this gets called is when declaring a variable with no initial value
    this.define(name, null, false);
  }

  public void assign(String name, LoLangValue value) {
    if (this.variables.containsKey(name)) {
      SymbolTableEntry entry = this.variables.get(name);

      if (entry.constant)
        throw new InterpreterError("Cannot assign to constant variable \"" + name + "\"");

      entry.value = value;
      return;
    }

    if (this.parent != null) {
      this.parent.assign(name, value);
      return;
    }

    throw new InterpreterError("Cannot assign to undeclared variable \"" + name + "\"");
  }
}
