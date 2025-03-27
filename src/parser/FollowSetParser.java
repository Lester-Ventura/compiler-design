package parser;

import java.util.*;

public class FollowSetParser {
  String input;

  public FollowSetParser(String input) {
    this.input = input;
  }

  public Map<String, Set<String>> parse() {
    Map<String, Set<String>> map = new HashMap<>();
    String[] lines = input.split("\n");

    for (String line : lines) {
      String[] parts = line.split("=>");
      String symbol = parts[0];

      String[] tokens = parts[1].split(",");
      map.put(symbol, new HashSet<>(Arrays.asList(tokens)));

      // System.out.println(String.format("%s -> %s\n", symbol, String.join(", ",
      // tokens)));
    }

    return map;
  }
}