import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

public class CodeReader {
    Scanner scanner = new Scanner(System.in);

    public void run() {
        while (true) {
            File[] files = getFiles();

            for (int i = 0; i < files.length; i++) {
                System.out.println(String.format("[%d]: %s", i + 1, files[i].getName()));
            }
            System.out.println(String.format("[%d]: %s", files.length+1, "Exit"));

            System.out.println("===============================================");
            System.out.print("Enter the file number to read: ");

            int fileNumber = scanner.nextInt();
            scanner.nextLine(); // consume the newline

            int index = fileNumber - 1;
            if (fileNumber == files.length+1) {
                System.out.println("Exiting...");
                break;
            } else if (index < 0 || index >= files.length) {
                System.out.println("Invalid file number, restarting...");
                continue;
            }

            File file = files[fileNumber - 1];
            System.out.println("Scanning " + file.getName());

            try {
                parseFile(file, 20);
                System.out.println("===============================================");
                System.out.println("Scanning complete! Enter any key to continue...");
                scanner.nextLine(); // consume the newline
            } catch (IOException e) {
                System.out.println("Error has occured while scanning file: " + e.getMessage());
            }
        }
    }

    public void destroy() {
        scanner.close();
    }

    public static File[] getFiles() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
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

    public void parseFile(File file, int delay_in_ms) throws IOException {
        Scanner scanner = new Scanner(file, "UTF-8");

        // need to check if there is a next line,
        // otherwise the scanner will throw an error
        String source = scanner.hasNext() ? scanner.useDelimiter("\\Z").next() + "\n" : "\n";
        Parser parser = new Parser(source);
        parser.parse(delay_in_ms);
        scanner.close();
    }
}
