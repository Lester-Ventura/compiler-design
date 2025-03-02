import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class CodeReader {
    String folderLocation;

    // Automatically adds the .lol extension. Returns a string representation of the code including the \n
    CodeReader() {
        this.folderLocation = "ExampleCodes\\";
    }

    CodeReader(String folderLocation) {
        this.folderLocation = folderLocation;
    }

    public static String[] demoList = {
            "Demo_Arrays", "Demo_Loop_Var",
            "Demo_Object_Record", "Demo_Recursion",
            "Demo_Try_Catch", "Demo_Variable_Operation"
    };
    public static String[] validList = {
            "Ex_Valid_Array", "Ex_Valid_Catch_Block",
            "Ex_Valid_Exit", "Ex_Valid_For",
            "Ex_Valid_Function", "Ex_Valid_If",
            "Ex_Valid_Output", "Ex_Valid_Switch",
            "Ex_Valid_While_Loop", "Ex_Variable_Dec",
            "Ex_Lexical_Scoping"
    };
    public static String[] invalidList = {
            "Ex_Invalid_Array", "Ex_Invalid_Catch_Block",
            "Ex_Invalid_Dec", "Ex_Invalid_For",
            "Ex_Invalid_If", "Ex_Invalid_Output",
            "Ex_Invalid_Switch", "Ex_Invalid_While"
    };
    public static String[] longList = {
            "Long_Demo", "Long_Demo_2", "Long_Demo_3"
    };

    public ArrayList<String> retrieveDefinedFiles(String[] fileList) {
        ArrayList<String> codes = new ArrayList<>();
        try {
            for (String fileName : fileList) {
                File codeFile = new File(folderLocation + fileName + ".lol");
                if (!codeFile.exists())
                    throw new IOException(fileName + " does not exist!");
                String finalCode = "";
                BufferedReader fileReader = new BufferedReader(new FileReader(codeFile));
                while (fileReader.readLine() != null)
                    finalCode = finalCode + fileReader.readLine() + '\n';
                fileReader.close();
                codes.add(finalCode);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return codes;
    }

    public String retrieveFile(String fileName) {
        String finalCode = "";
        try {
            File codeFile = new File(folderLocation + fileName + ".lol");
            if (!codeFile.exists())
                throw new IOException(fileName + " does not exist!");
            BufferedReader fileReader = new BufferedReader(new FileReader(codeFile));
            while (fileReader.readLine() != null)
                finalCode = finalCode + fileReader.readLine() + '\n';
            fileReader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return finalCode;
    }
}
