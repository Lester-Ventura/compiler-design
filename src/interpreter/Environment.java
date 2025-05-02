package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

public class Environment<InternalValue> {
  public static class SymbolTableEntry<InternalValue> {
    public InternalValue value;
    public boolean constant;

    public SymbolTableEntry(InternalValue value, boolean constant) {
      this.value = value;
      this.constant = constant;
    }
  }

  public Environment<InternalValue> parent = null;
  public ArrayList<Environment<InternalValue>> siblings = new ArrayList<>();

  public HashMap<String, SymbolTableEntry<InternalValue>> variables = new HashMap<>();

  public Environment(Environment<InternalValue> parent) {
    this.parent = parent;
  }

  public Environment() {
  }

  public SymbolTableEntry<InternalValue> getSymbolTableEntry(String name) {
    if (this.variables.containsKey(name))
      return this.variables.get(name);

    else if (this.parent != null)
      return this.parent.getSymbolTableEntry(name);

    for (Environment<InternalValue> sibling : this.siblings) {
      SymbolTableEntry<InternalValue> value = sibling.getSymbolTableEntry(name);
      if (value != null)
        return value;
    }

    throw new InterpreterError("Cannot find variable \"" + name + "\"");
  }

  public InternalValue get(String name) {
    return this.getSymbolTableEntry(name).value;
  }

  public void define(String name, InternalValue value, boolean constant) {
    SymbolTableEntry<InternalValue> newEntry = new SymbolTableEntry<InternalValue>(value, constant);
    this.variables.put(name, newEntry);
  }

  public void declare(String name) {
    // only time this gets called is when declaring a variable with no initial value
    this.define(name, null, false);
  }

  public void assign(String name, InternalValue value) {
    if (this.variables.containsKey(name)) {
      SymbolTableEntry<InternalValue> entry = this.variables.get(name);

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
