import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

public class CodeReader {
  SLR1Parser parser;

  public CodeReader(SLR1Parser parser) {
    this.parser = parser;
  }

  public void run(boolean interactive) {
    Scanner scanner = new Scanner(System.in);

    while (true) {
      File[] files = getFiles();

      for (int i = 0; i < files.length; i++) {
        System.out.println(String.format("[%d]: %s", i + 1, files[i].getName()));
      }
      System.out.println(String.format("[%d]: %s", files.length + 1, "Exit"));

      System.out.println("===============================================");
      System.out.print("Enter the file number to read: ");

      int fileNumber = scanner.nextInt();
      scanner.nextLine(); // consume the newline

      int index = fileNumber - 1;
      if (index == files.length) {
        System.out.println("Exiting...");
        break;
      } else if (index < 0 || index >= files.length) {
        System.out.println("Invalid file number, restarting...");
        continue;
      }

      File file = files[fileNumber - 1];
      System.out.println("Scanning " + file.getName());

      try {
        parseFile(file);
        if (interactive == false)
          System.exit(0);

        System.out.println("===============================================");
        System.out.println("Scanning complete! Enter any key to continue...");
        scanner.nextLine(); // consume the newline
      } catch (IOException e) {
        System.out.println("Error has occured while scanning file: " + e.getMessage());
      }
    }

    scanner.close();
  }

  public static File[] getFiles() {
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".lol");
      }
    };

    // get the list of files containing the .lol extension, trying in the current
    // directory first, then in the parent directory
    File[] files = new File("./ExampleCodes").listFiles(filter);
    if (files == null || files.length == 0)
      files = new File("../ExampleCodes").listFiles(filter);

    return files;
  }

  public void parseFile(File file) throws IOException {
    Scanner scanner = new Scanner(file, "UTF-8");

    // need to check if there is a next line,
    // otherwise the scanner will throw an error
    String source = scanner.hasNext() ? scanner.useDelimiter("\\Z").next() + "\n" : "\n";
    Node node = parser.parse(source);
    System.out.println(node);

    String output = DOTGenerator.generate(node);
    String path = "output.dot";
    FileWriter writer = new FileWriter(path);

    try {
      writer.write(output);
      System.out.println("File written successfully to " + path);
    } catch (IOException e) {
      System.out.println("Error has occured while writing file: " + e.getMessage());
    }

    writer.close();
    scanner.close();
  }
}