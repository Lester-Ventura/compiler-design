package utils;

import interpreter.Global;
import interpreter.LoLangValue;
import semantic.LoLangType;

public class Caster {
  public static class CastingException extends Exception {

  }

  public static boolean toBoolean(LoLangValue value) throws CastingException {
    LoLangValue.Boolean ret = toBooleanLoLangValue(value);
    if (ret != null)
      return ret.value;

    throw new CastingException();
  }

  public static LoLangValue.Boolean toBooleanLoLangValue(LoLangValue value) {
    if (value instanceof LoLangValue.Boolean)
      return (LoLangValue.Boolean) value;

    if (Global.isLenient) {
      if (value instanceof LoLangValue.String) {
        return new LoLangValue.Boolean(((LoLangValue.String) value).value.equals("faker"));
      } else if (value instanceof LoLangValue.Number) {
        return new LoLangValue.Boolean(((LoLangValue.Number) value).value != 0);
      } else if (value instanceof LoLangValue.Null) {
        return new LoLangValue.Boolean(false);
      }
    }

    return null;
  }

  public static boolean toBooleanType(LoLangType type) {
    if (type instanceof LoLangType.Boolean)
      return true;

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
