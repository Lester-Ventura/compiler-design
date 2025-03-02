import java.io.File;
import java.io.IOException;
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
    static void testEverything(){
        CodeReader codeReader = new CodeReader();
        try{
        ArrayList<String> codeList = new ArrayList<>();
        codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.demoList));
        codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.invalidList));
        codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.validList));
        codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.longList));
        for(String code:codeList){
            Parser parser = new Parser(code);
            Thread.sleep(200); //Small Delay for testing.
            parser.parse();    
            }
        }
        catch(IOException e){
            e.getMessage();
        }
        catch(InterruptedException e){
            e.getMessage();
        }
    }
    static void testSelected(){
        CodeReader codeReader = new CodeReader();
        try{
        ArrayList<String> codeList = new ArrayList<>();
        codeList.addAll(codeReader.retrieveDefinedFiles(CodeReader.validList));
        for(String code:codeList){
            Parser parser = new Parser(code);
            Thread.sleep(200); //Small Delay for testing.
            parser.parse();    
            }
        }
        catch(IOException e){
            e.getMessage();
        }
        catch(InterruptedException e){
            e.getMessage();
        }
    }
}
