package utils;

import java.util.ArrayList;
import java.util.HashMap;

import utils.EnvironmentException.EnvironmentAlreadyDeclaredException;

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

  private SymbolTableEntry<InternalValue> tryGetSymbolTableEntry(String name) {
    if (this.variables.containsKey(name))
      return this.variables.get(name);

    else if (this.parent != null && this.parent.tryGetSymbolTableEntry(name) != null)
      return this.parent.tryGetSymbolTableEntry(name);

    for (Environment<InternalValue> sibling : this.siblings) {
      SymbolTableEntry<InternalValue> value = sibling.tryGetSymbolTableEntry(name);
      if (value != null)
        return value;
    }

    return null;
  }

  public SymbolTableEntry<InternalValue> getSymbolTableEntry(String name)
      throws EnvironmentException.EnvironmentUndeclaredException {
    SymbolTableEntry<InternalValue> entry = this.tryGetSymbolTableEntry(name);
    if (entry == null)
      throw new EnvironmentException.EnvironmentUndeclaredException("Cannot find symbol table entry \"" + name + "\"");

    return entry;
  }

  public InternalValue get(String name) throws EnvironmentException.EnvironmentUndeclaredException {
    return this.getSymbolTableEntry(name).value;
  }

  public void tryDefine(String name, InternalValue value, boolean constant) {
    try {
      this.define(name, value, constant);
    } catch (EnvironmentAlreadyDeclaredException e) {
      System.out.println("INVARIANT - reached here");
    }
  }

  public void define(String name, InternalValue value, boolean constant) throws EnvironmentAlreadyDeclaredException {
    if (this.variables.containsKey(name))
      throw new EnvironmentAlreadyDeclaredException("Cannot redeclare variable \"" + name + "\"");

    SymbolTableEntry<InternalValue> newEntry = new SymbolTableEntry<InternalValue>(value, constant);
    this.variables.put(name, newEntry);
  }

  public void declare(String name) throws EnvironmentAlreadyDeclaredException {
    // only time this gets called is when declaring a variable with no initial value
    this.define(name, null, false);
  }

  public void assign(String name, InternalValue value) throws EnvironmentException.EnvironmentUndeclaredException {
    if (this.variables.containsKey(name)) {
      SymbolTableEntry<InternalValue> entry = this.variables.get(name);

      if (entry.constant)
        throw new EnvironmentException.EnvironmentUndeclaredException(
            "Cannot assign to constant variable \"" + name + "\"");

      entry.value = value;
      return;
    }

    if (this.parent != null) {
      this.parent.assign(name, value);
      return;
    }

    throw new EnvironmentException.EnvironmentUndeclaredException(
        "Cannot assign to undeclared variable \"" + name + "\"");
  }
}
