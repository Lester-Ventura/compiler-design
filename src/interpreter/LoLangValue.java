package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

import parser.Node;
import parser.StatementNode;

public class LoLangValue {
	public static class String extends LoLangValue {
		public java.lang.String value;

		public String(java.lang.String value) {
			this.value = value;
		}
	}

	public static class Number extends LoLangValue {
		public double value;

		public Number(double value) {
			this.value = value;
		}
	}

	public static class Boolean extends LoLangValue {
		public boolean value;

		public Boolean(boolean value) {
			this.value = value;
		}
	}

	public static class Null extends LoLangValue {
		public Null() {
		}
	}

	public static class Object extends LoLangValue {
		HashMap<String, LoLangValue> fields;

		public Object(HashMap<String, LoLangValue> fields) {
			this.fields = fields;
		}
	}

	public static class Array extends LoLangValue {
		ArrayList<LoLangValue> values;

		public Array(ArrayList<LoLangValue> values) {
			this.values = values;
		}
	}

	public static class UserDefinedFunction extends LoLangValue {
		Node.ParameterList parameters;
		StatementNode body;
		ExecutionContext context;

		public UserDefinedFunction(Node.ParameterList parameters, StatementNode body, ExecutionContext context) {
			this.parameters = parameters;
			this.body = body;
			this.context = context;
		}
	}
}
