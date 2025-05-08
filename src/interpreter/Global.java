package interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import semantic.LoLangType;
import semantic.SemanticContext;
import semantic.SemanticAnalyzerException.GenericReturnTypeArityException;
import semantic.SemanticAnalyzerException.GenericReturnTypeParameterMismatchException;
import utils.InputScanner;

public class Global {
  static public ExecutionContext createGlobalExecutionContext() {
    ExecutionContext context = new ExecutionContext();

    context.environment.tryDefine("broadcast",
        new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
          System.out.println(arguments[0].toString());
          return new LoLangValue.Null();
        }, 1), true);

    context.environment.tryDefine("chat",
        new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
          System.out.print(((LoLangValue.String) arguments[0]).value);
          String input = InputScanner.globalScanner.nextLine();

          return new LoLangValue.String(input);
        }, 1), true);

    context.environment.tryDefine("ff",
        new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
          LoLangValue.Number number = (LoLangValue.Number) arguments[0];
          System.exit(number.value == 15 ? (int) 0 : (int) Math.floor(number.value));
          return new LoLangValue.Null();
        }, 1), true);

    context.environment.tryDefine("dump_symbol_table",
        new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
          return new LoLangValue.Null();
        }, 0), true);

    context.environment.tryDefine("dump_call_stack",
        new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
          dynamicContext.printCallStack();
          return new LoLangValue.Null();
        }, 0), true);

    // context.environment.define("testing", new
    // LoLangValue.SystemDefinedFunction((LoLangValue[] arguments) -> {
    // LoLangValue.Number number = (LoLangValue.Number) arguments[0];
    // LoLangValue.String string = (LoLangValue.String) arguments[1];
    // System.out.println(number.value);
    // System.out.println(string.value);
    // return new LoLangValue.Null();
    // }, 2), true);

    return context;
  };

  static public SemanticContext createGlobalSemanticContext() {
    SemanticContext context = new SemanticContext();

    context.typeEnvironment.tryDefine("message", new LoLangType.String(), true);
    context.typeEnvironment.tryDefine("stats", new LoLangType.Number(), true);
    context.typeEnvironment.tryDefine("goat", new LoLangType.Boolean(), true);
    context.typeEnvironment.tryDefine("cooldown", new LoLangType.Null(), true);
    context.typeEnvironment.tryDefine("passive", new LoLangType.Void(), true);

    context.variableEnvironment.tryDefine("dump_symbol_table", new LoLangType.Lambda(
        (SemanticContext localContext, ArrayList<LoLangType> parameterTypes) -> {
          localContext.printSymbolTableToParent();
          return new LoLangType.Void();
        }), true);

    context.variableEnvironment.tryDefine("dump_call_stack", new LoLangType.Lambda(
        (SemanticContext localContext, ArrayList<LoLangType> parameterTypes) -> {
          return new LoLangType.Void();
        }), true);

    LoLangType.Lambda broadcastType = new LoLangType.Lambda(new LoLangType.Void(),
        createParameters(new LoLangType[] { new LoLangType.Any() }));
    context.variableEnvironment.tryDefine("broadcast", broadcastType, true);

    LoLangType.Lambda chatType = new LoLangType.Lambda(new LoLangType.String(),
        createParameters(new LoLangType[] { new LoLangType.String() }));
    context.variableEnvironment.tryDefine("chat", chatType, true);

    LoLangType.Lambda ffType = new LoLangType.Lambda(new LoLangType.Void(),
        createParameters(new LoLangType[] { new LoLangType.Number() }));
    context.variableEnvironment.tryDefine("ff", ffType, true);

    // LoLangType.Lambda testingType = new LoLangType.Lambda(new LoLangType.Void(),
    // createParameters(new LoLangType[] { new LoLangType.Number(), new
    // LoLangType.String() }));
    // context.variableEnvironment.define("testing", testingType, true);

    return context;
  }

  public static ArrayList<LoLangType> createParameters(LoLangType[] parameterTypes) {
    ArrayList<LoLangType> returned = new ArrayList<>(Arrays.asList(parameterTypes));
    Collections.reverse(returned);
    return returned;
  }

  public abstract static class InternalMethod<T extends LoLangValue> {
    public abstract LoLangValue run(T thisValue);

    public abstract LoLangType type(LoLangType thisType, SemanticContext context);
  }

  public static HashMap<String, InternalMethod<LoLangValue.String>> StringMethods = new HashMap<String, InternalMethod<LoLangValue.String>>() {
    {
      put("length", new InternalMethod<LoLangValue.String>() {
        public LoLangValue run(LoLangValue.String internalString) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            return new LoLangValue.Number(internalString.value.length());
          }, 0);
        }

        public LoLangType type(LoLangType thisType, SemanticContext context) {
          return new LoLangType.Lambda(new LoLangType.Number());
        }
      });

      put("charAt", new InternalMethod<LoLangValue.String>() {
        public LoLangValue run(LoLangValue.String internalString) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            if (arguments.length != 1)
              throw new InterpreterExceptions.FunctionCallArityException(1, arguments.length);

            if (!(arguments[0] instanceof LoLangValue.Number))
              throw new InterpreterExceptions.FunctionCallArgumentMismatchException(0,
                  new LoLangType.Number(), arguments[0].getType());

            double index = ((LoLangValue.Number) arguments[0]).value;
            int intIndex = (int) Math.max(Math.round(index), 0);

            if (internalString.value.length() < 0 || intIndex >= internalString.value.length())
              throw new InterpreterExceptions.IndexAccessOutOfBoundsException(intIndex);

            return new LoLangValue.String(internalString.value.charAt(intIndex) + "");
          }, 1);
        }

        public LoLangType type(LoLangType thisType, SemanticContext context) {
          return new LoLangType.Lambda(new LoLangType.String(),
              new ArrayList<>(Arrays.asList(new LoLangType.Number())));
        }
      });
    }
  };

  public static HashMap<String, InternalMethod<LoLangValue.Array>> ArrayMethods = new HashMap<String, InternalMethod<LoLangValue.Array>>() {
    {
      put("length", new InternalMethod<LoLangValue.Array>() {
        public LoLangValue run(LoLangValue.Array internalArray) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            return new LoLangValue.Number(internalArray.values.size());
          }, 0);
        }

        public LoLangType type(LoLangType thisType, SemanticContext context) {
          return new LoLangType.Lambda(new LoLangType.Number());
        }
      });

      put("filter", new InternalMethod<LoLangValue.Array>() {
        public LoLangValue run(LoLangValue.Array internalArray) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            LoLangValue.UserDefinedFunction function = (LoLangValue.UserDefinedFunction) arguments[0];
            ArrayList<LoLangValue> response = new ArrayList<>();

            for (int i = 0; i < internalArray.values.size(); i++) {
              LoLangValue condition = function.call(dynamicContext,
                  new ArrayList<>(Arrays.asList(internalArray.values.get(i))));

              if (!(condition instanceof LoLangValue.Boolean))
                throw new InterpreterExceptions.FunctionCallReturnTypeMismatchException(new LoLangType.Boolean(),
                    condition.getType());
              else if (((LoLangValue.Boolean) condition).value == true)
                response.add(internalArray.values.get(i));
            }

            return new LoLangValue.Array(response);
          }, 1);
        }

        public LoLangType type(LoLangType thisType, SemanticContext _context) {
          final LoLangType.Array arrayType = (LoLangType.Array) thisType;

          return new LoLangType.Lambda((SemanticContext context, ArrayList<LoLangType> parameterTypes) -> {
            if (parameterTypes.size() != 1)
              throw new GenericReturnTypeArityException(1, parameterTypes.size());

            if (!(parameterTypes.get(0) instanceof LoLangType.Lambda))
              throw new GenericReturnTypeParameterMismatchException(0,
                  new LoLangType.Lambda(new LoLangType.Boolean(),
                      new ArrayList<>(createParameters(new LoLangType[] { arrayType.elementType }))),
                  parameterTypes.get(0));

            LoLangType.Lambda lambda = (LoLangType.Lambda) parameterTypes.get(0);

            if (lambda.parameterList.size() != 1 || !lambda.returnType.isEquivalent(new LoLangType.Boolean()))
              throw new GenericReturnTypeParameterMismatchException(0,
                  new LoLangType.Lambda(new LoLangType.Boolean(),
                      new ArrayList<>(createParameters(new LoLangType[] { arrayType.elementType }))),
                  parameterTypes.get(0));

            return new LoLangType.Array(arrayType.elementType);
          });
        }
      });

      put("map", new InternalMethod<LoLangValue.Array>() {
        public LoLangValue run(LoLangValue.Array internalArray) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            LoLangValue.UserDefinedFunction function = (LoLangValue.UserDefinedFunction) arguments[0];
            ArrayList<LoLangValue> response = new ArrayList<>();

            for (int i = 0; i < internalArray.values.size(); i++)
              response.add(function.call(dynamicContext, new ArrayList<>(Arrays.asList(internalArray.values.get(i)))));

            return new LoLangValue.Array(response);
          }, 1);
        }

        public LoLangType type(LoLangType thisType, SemanticContext _context) {
          final LoLangType.Array arrayType = (LoLangType.Array) thisType;

          return new LoLangType.Lambda((SemanticContext context, ArrayList<LoLangType> parameterTypes) -> {
            if (parameterTypes.size() != 1)
              throw new GenericReturnTypeArityException(1, parameterTypes.size());

            if (!(parameterTypes.get(0) instanceof LoLangType.Lambda))
              throw new GenericReturnTypeParameterMismatchException(0,
                  new LoLangType.Lambda(new LoLangType.Any(),
                      new ArrayList<>(createParameters(new LoLangType[] { arrayType.elementType }))),
                  parameterTypes.get(0));

            LoLangType.Lambda lambda = (LoLangType.Lambda) parameterTypes.get(0);

            if (lambda.parameterList.size() != 1)
              throw new GenericReturnTypeParameterMismatchException(0,
                  new LoLangType.Lambda(new LoLangType.Any(),
                      new ArrayList<>(createParameters(new LoLangType[] { arrayType.elementType }))),
                  parameterTypes.get(0));

            return new LoLangType.Array(lambda.returnType);
          });
        }
      });

      put("toSorted", new InternalMethod<LoLangValue.Array>() {
        public LoLangValue run(LoLangValue.Array internalArray) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            LoLangValue[] cloned = new LoLangValue[internalArray.values.size()];
            for (int i = 0; i < internalArray.values.size(); i++)
              cloned[i] = internalArray.values.get(i);

            LoLangValue.UserDefinedFunction function = (LoLangValue.UserDefinedFunction) arguments[0];

            try {
              Arrays.sort(cloned, new Comparator<LoLangValue>() {
                public int compare(LoLangValue o1, LoLangValue o2) {
                  ArrayList<LoLangValue> args = new ArrayList<>(Arrays.asList(o1, o2));

                  try {
                    LoLangValue result = function.call(dynamicContext, args);

                    if (!(result instanceof LoLangValue.Number))
                      throw new SortingError(
                          new InterpreterExceptions.FunctionCallReturnTypeMismatchException(new LoLangType.Number(),
                              result.getType()));

                    return ((LoLangValue.Number) result).value > 0 ? -1
                        : ((LoLangValue.Number) result).value < 0 ? 1 : 0;
                  } catch (InterpreterExceptions e) {
                    throw new SortingError(e);
                  }
                }
              });
            } catch (SortingError e) {
              throw e.exception;
            }

            return new LoLangValue.Array(new ArrayList<>(Arrays.asList(cloned)));
          }, 1);
        }

        public LoLangType type(LoLangType thisType, SemanticContext _context) {
          final LoLangType.Array arrayType = (LoLangType.Array) thisType;

          return new LoLangType.Lambda((SemanticContext context, ArrayList<LoLangType> parameterTypes) -> {
            if (parameterTypes.size() != 1)
              throw new GenericReturnTypeArityException(1, parameterTypes.size());

            if (!(parameterTypes.get(0) instanceof LoLangType.Lambda))
              throw new GenericReturnTypeParameterMismatchException(0,
                  new LoLangType.Lambda(new LoLangType.Number(),
                      new ArrayList<>(
                          createParameters(new LoLangType[] { arrayType.elementType, arrayType.elementType }))),
                  parameterTypes.get(0));

            LoLangType.Lambda lambda = (LoLangType.Lambda) parameterTypes.get(0);

            if (lambda.parameterList.size() != 2
                || lambda.returnType.isEquivalent(new LoLangType.Number()) == false
                || lambda.parameterList.get(0).isEquivalent(arrayType.elementType) == false
                || lambda.parameterList.get(1).isEquivalent(arrayType.elementType) == false)
              throw new GenericReturnTypeParameterMismatchException(0,
                  new LoLangType.Lambda(new LoLangType.Number(),
                      new ArrayList<>(
                          createParameters(new LoLangType[] { arrayType.elementType, arrayType.elementType }))),
                  parameterTypes.get(0));

            return new LoLangType.Array(lambda.returnType);
          });
        }
      });

      put("push", new InternalMethod<LoLangValue.Array>() {
        public LoLangValue run(LoLangValue.Array internalArray) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            if (arguments.length != 1)
              throw new InterpreterExceptions.FunctionCallArityException(1, arguments.length);

            internalArray.values.add(arguments[0]);
            return new LoLangValue.Number(internalArray.values.size());
          }, 1);
        }

        public LoLangType type(LoLangType thisType, SemanticContext _context) {
          final LoLangType.Array arrayType = (LoLangType.Array) thisType;

          return new LoLangType.Lambda((SemanticContext context, ArrayList<LoLangType> parameterTypes) -> {
            if (parameterTypes.size() != 1)
              throw new GenericReturnTypeArityException(1, parameterTypes.size());

            if (parameterTypes.get(0).isEquivalent(arrayType.elementType) == false)
              throw new GenericReturnTypeParameterMismatchException(0,
                  arrayType.elementType, parameterTypes.get(0));

            return new LoLangType.Number();
          });
        }
      });

      put("pop", new InternalMethod<LoLangValue.Array>() {
        public LoLangValue run(LoLangValue.Array internalArray) {
          return new LoLangValue.SystemDefinedFunction((ExecutionContext dynamicContext, LoLangValue[] arguments) -> {
            if (internalArray.values.size() == 0)
              throw new InterpreterExceptions.IndexAccessOutOfBoundsException(0);

            LoLangValue last = internalArray.values.get(internalArray.values.size() - 1);
            internalArray.values.remove(internalArray.values.size() - 1);
            return last;
          }, 0);
        }

        public LoLangType type(LoLangType thisType, SemanticContext _context) {
          final LoLangType.Array arrayType = (LoLangType.Array) thisType;

          return new LoLangType.Lambda((SemanticContext context, ArrayList<LoLangType> parameterTypes) -> {
            if (parameterTypes.size() <= 0)
              return arrayType.elementType;

            throw new GenericReturnTypeArityException(0, parameterTypes.size());
          });
        }
      });
    }
  };
}

class SortingError extends Error {
  public final InterpreterExceptions exception;

  public SortingError(InterpreterExceptions exception) {
    this.exception = exception;
  }
}