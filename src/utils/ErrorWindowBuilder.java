package utils;

import java.util.ArrayList;

import lexer.Token;

public class ErrorWindowBuilder {
  public static String buildErrorWindow(Token currentToken) {
    // Point to the area of the file that caused the error
    String[] lines = FileLoader.loadFile(currentToken.sourcePath).split("\n");

    int length = Math.max(
        String.format("%d", currentToken.line).length(),
        currentToken.line < lines.length ? String.format("%d", currentToken.line + 1).length() : 0);

    String second = "", third = "";

    for (int i = 0; i < currentToken.column + length + 3; i++) {
      second += " ";
      third += "─";
    }
    second += "│";
    third += "┘";

    String window = String.format("%" + length + "d | %s\n%s\n%s\n", currentToken.line,
        lines.length <= currentToken.line - 1 ? "" : lines[currentToken.line - 1],
        second, third);
    if (currentToken.line < lines.length)
      window += String.format("%" + length + "d | %s\n", currentToken.line + 1, lines[currentToken.line]);

    return window;
  }

  public static void printErrors(ArrayList<? extends LoLangExceptionLike> errors) {
    for (LoLangExceptionLike exception : errors) {
      if (exception.getToken() != null) {
        System.out.println(String.format("%s:%d:%d  -  %s", exception.getToken().sourcePath,
            exception.getToken().line, exception.getToken().column, exception.getMessage()));
        String errorWindow = ErrorWindowBuilder.buildErrorWindow(exception.getToken());
        System.out.println(errorWindow);
      } else
        System.out.println(exception.getMessage());
    }
  }
}
