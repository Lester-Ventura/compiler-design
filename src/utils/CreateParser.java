package utils;

import parser.LR1Parser;

@FunctionalInterface
public interface CreateParser {
  LR1Parser run(String input, String inputPath);
}