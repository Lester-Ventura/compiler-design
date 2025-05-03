package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class LoLangType {
  abstract public boolean isEquivalent(LoLangType other);

  public static interface DotGettable {
    public boolean hasKey(java.lang.String key);

    public LoLangType getKey(java.lang.String key);
  }

  static abstract class Intrinsic extends LoLangType {
  }

  public static class Number extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Number;
    }
  }

  public static class Boolean extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Boolean;
    }
  }

  public static class Null extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Null;
    }
  }

  public static class Void extends Intrinsic {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof Void;
    }
  }

  public static class String extends Intrinsic implements DotGettable {
    public boolean isEquivalent(LoLangType other) {
      return other instanceof Any ? true : other instanceof String;
    }

    // TODO: implement
    public boolean hasKey(java.lang.String key) {
      return false;
    }

    // TODO: implement
    public LoLangType getKey(java.lang.String key) {
      return null;
    }
  }

  public static class Array extends Intrinsic {
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

    public LoLangType getKey(java.lang.String key) {
      return this.fields.get(key);
    }
  }

  public static class Lambda extends LoLangType {
    public final LoLangType returnType;
    public final ArrayList<LoLangType> parameterList;

    public Lambda(LoLangType returnType) {
      this.returnType = returnType;
      this.parameterList = new ArrayList<>();
    }

    public Lambda(LoLangType returnType, ArrayList<LoLangType> parameterList) {
      this.returnType = returnType;
      this.parameterList = parameterList;
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
  }

  public static class Any extends LoLangType {
    public boolean isEquivalent(LoLangType other) {
      return true;
    }
  }

  public static class Unknown extends LoLangType {
    public boolean isEquivalent(LoLangType other) {
      return true;
    }
  }
}
