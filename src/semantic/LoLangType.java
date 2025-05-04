package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import interpreter.Global;
import semantic.SemanticAnalyzerException.GenericReturnTypeException;

public abstract class LoLangType {
  abstract public boolean isEquivalent(LoLangType other);

  abstract public java.lang.String toString();

  public static interface DotGettable {
    public boolean hasKey(java.lang.String key);

    public LoLangType getKey(SemanticContext context, java.lang.String key);
  }

  static abstract class Intrinsic extends LoLangType {
  }

  public static class Number extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Number;
    }

    public java.lang.String toString() {
      return "number";
    }
  }

  public static class Boolean extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Boolean;
    }

    public java.lang.String toString() {
      return "boolean";
    }
  }

  public static class Null extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Null;
    }

    public java.lang.String toString() {
      return "null";
    }
  }

  public static class Void extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Void;
    }

    public java.lang.String toString() {
      return "void";
    }
  }

  public static class String extends Intrinsic implements DotGettable {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof String;
    }

    public boolean hasKey(java.lang.String key) {
      return Global.StringMethods.containsKey(key);
    }

    public LoLangType getKey(SemanticContext context, java.lang.String key) {
      return Global.StringMethods.get(key).type(this, null);
    }

    public java.lang.String toString() {
      return "string";
    }
  }

  public static class Array extends Intrinsic implements DotGettable {
    public LoLangType elementType;

    public Array(LoLangType elementType) {
      this.elementType = elementType;
    }

    public boolean isEquivalent(LoLangType other) {
      if (other instanceof Any)
        return true;

      if (!(other instanceof Array))
        return false;

      Array otherArray = (Array) other;
      return this.elementType.isEquivalent(otherArray.elementType);
    }

    public java.lang.String toString() {
      return "[" + this.elementType.toString() + "]";
    }

    public LoLangType getKey(SemanticContext context, java.lang.String key) {
      return Global.ArrayMethods.get(key).type(this, context);
    }

    public boolean hasKey(java.lang.String key) {
      return Global.ArrayMethods.containsKey(key);
    }
  }

  public static class Object extends Intrinsic implements DotGettable {
    HashMap<java.lang.String, LoLangType> fields;

    public Object(HashMap<java.lang.String, LoLangType> fields) {
      this.fields = fields;
    }

    public boolean isEquivalent(LoLangType other) {
      if (other instanceof Any)
        return true;

      if (!(other instanceof Object))
        return false;

      Object otherObject = (Object) other;
      for (Map.Entry<java.lang.String, LoLangType> entry : this.fields.entrySet()) {
        if (!otherObject.fields.containsKey(entry.getKey()))
          return false;

        if (!entry.getValue().isEquivalent(otherObject.fields.get(entry.getKey())))
          return false;
      }

      // check if otherObject has more fields than what this type wants
      for (Map.Entry<java.lang.String, LoLangType> entry : otherObject.fields.entrySet()) {
        if (!this.fields.containsKey(entry.getKey()))
          return false;
      }

      return true;
    }

    public boolean hasKey(java.lang.String key) {
      return this.fields.containsKey(key);
    }

    public LoLangType getKey(SemanticContext context, java.lang.String key) {
      return this.fields.get(key);
    }

    public java.lang.String toString() {
      java.lang.String ret = "{";

      for (java.lang.String key : this.fields.keySet())
        ret += key + ": " + this.fields.get(key).toString() + ", ";

      return ret + "}";
    }
  }

  public static class Lambda extends LoLangType {
    public static interface GenerateGenericReturnType {
      LoLangType run(SemanticContext context, ArrayList<LoLangType> parameterTypes)
          throws GenericReturnTypeException;
    }

    public final LoLangType returnType;
    public final ArrayList<LoLangType> parameterList;
    public final boolean isGeneric;
    public final GenerateGenericReturnType generateGenericReturnType;

    public Lambda(LoLangType returnType) {
      this(returnType, new ArrayList<>(), null);
    }

    public Lambda(LoLangType returnType, ArrayList<LoLangType> parameterList) {
      this(returnType, parameterList, null);
    }

    public Lambda(GenerateGenericReturnType generateGenericReturnType, ArrayList<LoLangType> parameterList) {
      this(new LoLangType.Any(), parameterList, generateGenericReturnType);
    }

    public Lambda(GenerateGenericReturnType generateGenericReturnType) {
      this(new LoLangType.Any(), new ArrayList<>(), generateGenericReturnType);
    }

    private Lambda(LoLangType returnType, ArrayList<LoLangType> parameterList,
        GenerateGenericReturnType generateGenericReturnType) {
      this.returnType = returnType;
      this.parameterList = parameterList;
      this.isGeneric = generateGenericReturnType != null;
      this.generateGenericReturnType = generateGenericReturnType;
    }

    public boolean isEquivalent(LoLangType other) {
      if (other instanceof Any)
        return true;

      if (!(other instanceof Lambda))
        return false;

      Lambda otherLambda = (Lambda) other;
      if (!this.returnType.isEquivalent(otherLambda.returnType))
        return false;

      if (this.parameterList.size() != otherLambda.parameterList.size())
        return false;

      for (int i = 0; i < this.parameterList.size(); i++) {
        if (!this.parameterList.get(i).isEquivalent(otherLambda.parameterList.get(i)))
          return false;
      }

      return true;
    }

    public java.lang.String toString() {
      java.lang.String ret = "lambda";

      for (LoLangType parameter : this.parameterList)
        ret += parameter.toString() + ",";

      return ret + ") ->" + this.returnType.toString();
    }
  }

  public static class Any extends LoLangType {
    public boolean isEquivalent(LoLangType other) {
      return true;
    }

    public java.lang.String toString() {
      return "any";
    }
  }

  public static class Unknown extends LoLangType {
    public boolean isEquivalent(LoLangType other) {
      return true;
    }

    public java.lang.String toString() {
      return "unknown";
    }
  }
}
