import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    private static boolean hadError = false;

    public static void main(String[] args) {
        //test(50,"CustomExample");
        //A small test
        //test(50,CodeReader.longList[0]);
        //For testing the list of tokens
        test(20,"TokenList");
        //For testing in actual code
        //test(100,CodeReader.validList);
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

    static void testEverything(int delay) {
        CodeReader codeReader = new CodeReader();
        try {
            ArrayList<String> codeList = new ArrayList<>();
            codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.allList));
            int index = 0;
            for (String code : codeList) {
                System.out.println("Reading: " + (index + 1) + " " + CodeReader.allList[index]);
                Parser parser = new Parser(code);
                parser.parseDelayed(delay);
                index++;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    static void test(int delay,String... listofFiles) {
        CodeReader codeReader = new CodeReader();
        try {
            ArrayList<String> codeList = new ArrayList<>();
            codeList.addAll(codeReader.retrieveDefinedFiles(listofFiles));
            int index = 0;
            for (String code : codeList) {
                System.out.println("Reading: " + (index + 1) + " " + listofFiles[index]);
                Parser parser = new Parser(code);
                parser.parseDelayed(delay);
                index++;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }
}
    
