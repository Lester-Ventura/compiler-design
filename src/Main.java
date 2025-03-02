import java.io.File;
import java.util.*;

public class Main {

    static boolean hadError = false;

    public static void main(String[] args) {
        String content;

        try {
            if (args.length == 0) {
                System.out.println("Please provide a file to run");
                return;
            }

            Scanner scanner = new Scanner(new File(args[0]));
            content = scanner.useDelimiter("\\Z").next() + "\n";
            scanner.close();
        } catch (Exception err) {
            System.out.println("Error while loading the file");
            System.exit(1);
            return;
        }

        Parser parser = new Parser(content);
        parser.parse();
    }
}
