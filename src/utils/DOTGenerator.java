package utils;

import parser.Node;

public class DOTGenerator {
	StringBuilder sb = new StringBuilder();

	public static String generate(Node root) {
		DOTGenerator gen = new DOTGenerator();

		gen.sb.append("digraph G {\n");
		gen.sb.append("\tnode [shape=record fontname=Arial];\n");
		root.toDot(gen);
		gen.sb.append("}");

		return gen.finish();
	}

	public void addNode(int hashcode, String contents) {
		sb.append(String.format("\t n%d [label=\"%s\"]\n", hashcode, contents));
	}

	public void addEdge(int from, int to) {
		sb.append(String.format("\t n%d -> n%d;\n", from, to));
	}

	public String finish() {
		return sb.toString();
	}
}