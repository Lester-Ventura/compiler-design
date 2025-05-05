package interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import interpreter.InterpreterExceptions.DotAccessNonExistentException;
import parser.Node;
import parser.StatementNode;
import semantic.LoLangType;
import utils.EnvironmentException;

public abstract class LoLangValue {
	public abstract java.lang.String toString();

	public abstract LoLangType getType();

	public interface DotSettable {
		public void setDot(java.lang.String key, LoLangValue value)
				throws InterpreterExceptions.DotAccessNonExistentException;
	}

	public interface DotGettable {
		public LoLangValue getDot(java.lang.String key) throws InterpreterExceptions.DotAccessNonExistentException;
	}

	public interface IndexGettable {
		public void setIndex(int index, LoLangValue value)
				throws InterpreterExceptions.IndexAccessOutOfBoundsException;
	}

	public interface IndexSettable {
		public LoLangValue getIndex(int index)
				throws InterpreterExceptions.IndexAccessOutOfBoundsException;
	}

	public interface Callable {
		public LoLangValue call(ArrayList<LoLangValue> arguments) throws InterpreterExceptions;

		public int getArity();
	}

	public static class String extends LoLangValue implements DotGettable {
		public java.lang.String value;

		public String(java.lang.String value) {
			this.value = value;
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.String]: %s", this.value);
		}

		public LoLangValue getDot(java.lang.String key) throws DotAccessNonExistentException {
			return Global.StringMethods.get(key).run(this);
		}

		public LoLangType getType() {
			return new LoLangType.String();
		}
	}

	public static class Number extends LoLangValue {
		public double value;

		public Number(double value) {
			this.value = value;
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.Number]: %s", this.value);
		}

		public LoLangType getType() {
			return new LoLangType.Number();
		}
	}

	public static class Boolean extends LoLangValue {
		public boolean value;

		public Boolean(boolean value) {
			this.value = value;
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.Boolean]: %s", this.value ? "true" : "false");
		}

		public LoLangType getType() {
			return new LoLangType.Boolean();
		}
	}

	public static class Null extends LoLangValue {
		public Null() {
		}

		public java.lang.String toString() {
			return "[LoLangValue.Null]";
		}

		public LoLangType getType() {
			return new LoLangType.Null();
		}
	}

	public static class Object extends LoLangValue implements DotSettable, DotGettable {
		HashMap<java.lang.String, LoLangValue> fields;

		public Object(HashMap<java.lang.String, LoLangValue> fields) {
			this.fields = fields;
		}

		public void setDot(java.lang.String key, LoLangValue value)
				throws InterpreterExceptions.DotAccessNonExistentException {
			if (this.fields.containsKey(key) == false)
				throw new InterpreterExceptions.DotAccessNonExistentException(key);

			this.fields.put(key, value);
		}

		public LoLangValue getDot(java.lang.String key) throws InterpreterExceptions.DotAccessNonExistentException {
			if (this.fields.containsKey(key) == false)
				throw new InterpreterExceptions.DotAccessNonExistentException(key);

			return this.fields.get(key);
		}

		public java.lang.String toString() {
			java.lang.String ret = "[LoLangValue.Object]: {";

			for (java.lang.String key : this.fields.keySet())
				ret += key + ": " + this.fields.get(key).toString() + ", ";

			return ret + "}";
		}

		public LoLangType getType() {
			HashMap<java.lang.String, LoLangType> fieldTypes = new HashMap<>();

			for (java.lang.String key : this.fields.keySet())
				fieldTypes.put(key, this.fields.get(key).getType());

			return new LoLangType.Object(fieldTypes);
		}
	}

	public static class Array extends LoLangValue implements IndexGettable, IndexSettable, DotGettable {
		public final ArrayList<LoLangValue> values;

		public Array() {
			this.values = new ArrayList<>();
		}

		public Array(ArrayList<LoLangValue> values) {
			this.values = values;
		}

		public void setIndex(int index, LoLangValue value) throws InterpreterExceptions.IndexAccessOutOfBoundsException {
			if (index < 0 || index >= this.values.size())
				throw new InterpreterExceptions.IndexAccessOutOfBoundsException(index);
			this.values.set(index, value);
		}

		public LoLangValue getIndex(int index) throws InterpreterExceptions.IndexAccessOutOfBoundsException {
			if (index < 0 || index >= this.values.size())
				throw new InterpreterExceptions.IndexAccessOutOfBoundsException(index);
			return this.values.get(index);
		}

		public java.lang.String toString() {
			java.lang.String ret = "[LoLangValue.Array]: [";

			for (LoLangValue value : this.values)
				ret += value.toString() + ", ";

			return ret + "]";
		}

		public LoLangValue getDot(java.lang.String key) throws InterpreterExceptions.DotAccessNonExistentException {
			return Global.ArrayMethods.get(key).run(this);
		}

		public LoLangType getType() {
			LoLangType internalType = this.values.size() > 0 ? this.values.get(0).getType() : new LoLangType.Any();
			return new LoLangType.Array(internalType);
		}
	}

	public static class UserDefinedFunction extends LoLangValue implements Callable {
		Node.ParameterList parameters;
		StatementNode body;
		ExecutionContext context;

		public UserDefinedFunction(Node.ParameterList parameters, StatementNode body, ExecutionContext context) {
			this.parameters = parameters;
			this.body = body;
			this.context = context;
		}

		public LoLangValue call(ArrayList<LoLangValue> arguments) throws InterpreterExceptions {
			ExecutionContext forkedContext = context.fork();

			for (int i = 0; i < this.parameters.declarations.size(); i++) {
				Node.VariableDeclarationHeader declaration = this.parameters.declarations.get(i);

				try {
					forkedContext.environment.define(declaration.identifier.lexeme, arguments.get(i), true);
				} catch (EnvironmentException.EnvironmentAlreadyDeclaredException e) {
					throw new InterpreterExceptions.RedeclaredVariableException(declaration.identifier.lexeme);
				}
			}

			try {
				this.body.execute(forkedContext);
			} catch (LoLangThrowable.Return returnException) {
				return returnException.value;
			}

			return new LoLangValue.Null();
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.UserDefinedFunction]: 0x%s", this.hashCode());
		}

		public int getArity() {
			return this.parameters.declarations.size();
		}

		public LoLangType getType() {
			return new LoLangType.Lambda(new LoLangType.Any());
		}
	}

	public static class SystemDefinedFunction extends LoLangValue implements Callable {
		SystemDefinedFunctionLambda lambda;
		int arity;

		public SystemDefinedFunction(SystemDefinedFunctionLambda lambda, int arity) {
			this.lambda = lambda;
			this.arity = arity;
		}

		public LoLangValue call(ArrayList<LoLangValue> arguments) throws InterpreterExceptions {
			if (arguments.size() != this.arity)
				throw new InterpreterExceptions.FunctionCallArityException(this.arity, arguments.size());

			Collections.reverse(arguments);
			return this.lambda.run(arguments.toArray(new LoLangValue[this.arity]));
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.SystemDefinedFunction]: 0x%s", this.hashCode());
		}

		public int getArity() {
			return this.arity;
		}

		public LoLangType getType() {
			return new LoLangType.Lambda(new LoLangType.Any());
		}
	}

	public static interface SystemDefinedFunctionLambda {
		LoLangValue run(LoLangValue... arguments) throws InterpreterExceptions;
	}
}
