package interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import parser.Node;
import parser.StatementNode;

public abstract class LoLangValue {
	public abstract java.lang.String toString();

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
				throws InterpreterExceptions.IndexAccessOutOfBoundsException;;
	}

	public interface Callable {
		public LoLangValue call(ArrayList<LoLangValue> arguments)
				throws InterpreterExceptions.FunctionCallArityException;

		public int getArity();
	}

	public static class String extends LoLangValue {
		public java.lang.String value;

		public String(java.lang.String value) {
			this.value = value;
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.String]: %s", this.value);
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
	}

	public static class Boolean extends LoLangValue {
		public boolean value;

		public Boolean(boolean value) {
			this.value = value;
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.Boolean]: %s", this.value ? "true" : "false");
		}
	}

	public static class Null extends LoLangValue {
		public Null() {
		}

		public java.lang.String toString() {
			return "[LoLangValue.Null]";
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
				throw new InterpreterExceptions.DotAccessNonExistentException();

			this.fields.put(key, value);
		}

		public LoLangValue getDot(java.lang.String key) throws InterpreterExceptions.DotAccessNonExistentException {
			if (this.fields.containsKey(key) == false)
				throw new InterpreterExceptions.DotAccessNonExistentException();

			return this.fields.get(key);
		}

		public java.lang.String toString() {
			java.lang.String ret = "[LoLangValue.Object]: {";

			for (java.lang.String key : this.fields.keySet())
				ret += key + ": " + this.fields.get(key).toString() + ", ";

			return ret + "}";
		}
	}

	public static class Array extends LoLangValue implements IndexGettable, IndexSettable {
		public final ArrayList<LoLangValue> values;

		public Array() {
			this.values = new ArrayList<>();
		}

		public Array(ArrayList<LoLangValue> values) {
			this.values = values;
		}

		public void setIndex(int index, LoLangValue value) throws InterpreterExceptions.IndexAccessOutOfBoundsException {
			if (index < 0 || index >= this.values.size())
				throw new InterpreterExceptions.IndexAccessOutOfBoundsException();
			this.values.set(index, value);
		}

		public LoLangValue getIndex(int index) throws InterpreterExceptions.IndexAccessOutOfBoundsException {
			if (index < 0 || index >= this.values.size())
				throw new InterpreterExceptions.IndexAccessOutOfBoundsException();
			return this.values.get(index);
		}

		public java.lang.String toString() {
			java.lang.String ret = "[LoLangValue.Array]: [";

			for (LoLangValue value : this.values)
				ret += value.toString() + ", ";

			return ret + "]";
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

		public LoLangValue call(ArrayList<LoLangValue> arguments) {
			ExecutionContext forkedContext = context.fork();

			for (int i = 0; i < this.parameters.declarations.size(); i++) {
				Node.VariableDeclarationHeader declaration = this.parameters.declarations.get(i);
				forkedContext.environment.define(declaration.identifier.lexeme, arguments.get(i), true);
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
	}

	public static class SystemDefinedFunction extends LoLangValue implements Callable {
		SystemDefinedFunctionLambda lambda;
		int arity;

		public SystemDefinedFunction(SystemDefinedFunctionLambda lambda, int arity) {
			this.lambda = lambda;
			this.arity = arity;
		}

		public LoLangValue call(ArrayList<LoLangValue> arguments) throws InterpreterExceptions.FunctionCallArityException {
			if (arguments.size() != this.arity)
				throw new InterpreterExceptions.FunctionCallArityException();

			Collections.reverse(arguments);
			return this.lambda.run(arguments.toArray(new LoLangValue[this.arity]));
		}

		public java.lang.String toString() {
			return java.lang.String.format("[LoLangValue.SystemDefinedFunction]: 0x%s", this.hashCode());
		}

		public int getArity() {
			return this.arity;
		}
	}

	public static interface SystemDefinedFunctionLambda {
		LoLangValue run(LoLangValue... arguments);
	}
}
