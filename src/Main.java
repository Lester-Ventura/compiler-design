import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    private static boolean hadError = false;

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
        if (hadError)
            System.out.println("I had an error");

    }

    /**
     * Handles an error thrown by any of the interpreter's parts.
     * 
     * @param e : the thrown error
     */
    protected static void handleError(Error e) {
        System.err.println(e.getMessage());
        hadError = true;
    }

    static void testEverything() {
        CodeReader codeReader = new CodeReader();
        try {
            ArrayList<String> codeList = new ArrayList<>();
            codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.allList));
            int index = 0;
            for (String code : codeList) {
                System.out.println("Reading: " + (index + 1) + " " + CodeReader.allList[index]);
                Parser parser = new Parser(code);
                parser.parseDelayed(0);
                index++;
            }
        } catch (IOException e) {
            e.getMessage();
        } catch (InterruptedException e) {
            e.getMessage();
        }
    }

    static void testList() {
        CodeReader codeReader = new CodeReader();
        try {
            ArrayList<String> codeList = new ArrayList<>();
            codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.validList));
            int index = 0;
            for (String code : codeList) {
                System.out.println("Reading: " + (index + 1) + " " + CodeReader.validList[index]);
                Parser parser = new Parser(code);
                parser.parseDelayed(0);
                index++;
            }
        } catch (IOException e) {
            e.getMessage();
        } catch (InterruptedException e) {
            e.getMessage();
        }
    }

    static void testCustom(int timeParseSpeed){
        CodeReader codeReader = new CodeReader();
        try {
            ArrayList<String> codeList = new ArrayList<>();
            codeList.add(codeReader.retrieveFile("CustomExample"));
            int index = 0;
            for (String code : codeList) {
                System.out.println("Reading: " + (index + 1) + " " + CodeReader.validList[index]);
                Parser parser = new Parser(code);
                parser.parseDelayed(timeParseSpeed);
                index++;
            }
        } catch (IOException e) {
            e.getMessage();
        } catch (InterruptedException e) {
            e.getMessage();
        }
    }
}
