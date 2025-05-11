package utils;

import java.io.File;
import java.util.Scanner;
/**
 * 
 */
public class FileLoader {
  /**
   * Platform agnostic way of loading files
   */
  public static String loadFile(String path) {
    try {
      File grammarFile = new File("../" + path);
      if (!grammarFile.exists())
        grammarFile = new File("./" + path);

      Scanner scanner = new Scanner(grammarFile);
      String source = scanner.hasNext() ? scanner.useDelimiter("\\Z").next() + "\n" : "\n";
      scanner.close();

      return source;
    } catch (Exception err) {
      throw new Error("Was not able to load the file");
    }
  }
}
