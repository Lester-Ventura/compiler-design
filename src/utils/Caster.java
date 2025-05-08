package utils;

import interpreter.Global;
import interpreter.LoLangValue;
import semantic.LoLangType;

public class Caster {
  public static class CastingException extends Exception {

  }

  public static boolean toBoolean(LoLangValue value) throws CastingException {
    if (value instanceof LoLangValue.Boolean)
      return ((LoLangValue.Boolean) value).value;

    if (Global.isLenient) {
      if (value instanceof LoLangValue.String) {
        return ((LoLangValue.String) value).value.equals("faker");
      } else if (value instanceof LoLangValue.Number) {
        return ((LoLangValue.Number) value).value != 0;
      } else if (value instanceof LoLangValue.Null) {
        return false;
      }
    }

    throw new CastingException();
  }

  public static boolean toBooleanType(LoLangType type) {
    if (Global.isLenient) {
      if (type instanceof LoLangType.String)
        return true;

      if (type instanceof LoLangType.Number)
        return true;

      if (type instanceof LoLangType.Null)
        return true;
    }

    return false;
  }
}
