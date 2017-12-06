
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public  String curCommand ;
    static BufferedReader reader;
    public boolean endOfFile = false;
    private String fileInputName;
    public static final String C_ARITHMETIC ="C_ARITHMETIC";
    public static final String C_PUSH ="C_PUSH";
    public static final String C_POP ="C_POP";
    public static final String C_LABEL ="C_LABEL";
    public static final String C_CALL ="C_CALL";
    public static final String C_GOTO ="C_GOTO";
    public static final String C_IF ="C_IF";
    public static final String C_FUNCTION ="C_FUNCTION";
    public static final String C_RETURN ="C_RETURN";
    private ArrayList<String> arithmeticOperators;


    /**
     * Parser constructor
     * Open the input file/stream and gets ready to parse it
     * @param fileName- the file to parse
     */
    public Parser(String fileName) {
            arithmeticOperators = new ArrayList<String>();
            fileInputName = fileName;
            makeArrayOperators();

    }


    /**
     * Are there more commands in the input
     * @return -true if there are more commands in the input
     * -false otherwise
     */
    boolean hasMoreCommands() {
        try {
            return reader.ready();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to check.", "id the buffer ready to read from it");
            return false;
        }
    }




    /**
     * Reads the next command from the input and makes it the current command.
     * Should be called only if  hasMoreCommands() is true. Initially ther is no
     * current command.
     */
    public void advance() throws IOException {

            curCommand = reader.readLine();



            if(curCommand.startsWith("/"))
            {
                advance();
            }
            if (curCommand.contains("/")) {
                curCommand = curCommand.substring(0, curCommand.lastIndexOf('/') - 1);
            }

            curCommand = removeSpaces(removePerfixSpace(curCommand));
            if (curCommand.equals("")) {
                if(this.hasMoreCommands()) {
                    advance();
                }
                else{

                    endOfFile = true;

                }
            }
    }




    /**
     * Returns the type of the current VM command.
     * C_ARITHMETIC is returned for all the arithmetic commands.
     * @return - command type
     */
    public String commandType(){
        if (curCommand.contains("push")) {
            return C_PUSH;
        }
        else if(curCommand.contains("pop")){
            return C_POP;
        }
        else if (arithmeticOperators.contains(curCommand)) {

            return C_ARITHMETIC;
        }
        else if(curCommand.contains("label")){
            return C_LABEL;
        }
        else if (curCommand.contains("call")){
            return C_CALL;
        }
        else if(curCommand.contains("goto")){
            return C_GOTO;
        }
        else if (curCommand.contains("if")){
            return C_IF;
        }
        else if(curCommand.contains("function")){
            return C_FUNCTION;
        }
        else if (curCommand.contains("return")){
            return C_RETURN;
        }
        else {
            //sholud be null???????????????????????????????????????????????????????????
            return "UNKMOWN TYPE ";
        }


    }


    /**
     * Returns the first arg. of the current command.
     *In the case of C_ARITHMETIC, the command itself
     *(add,sub, etc.) is returned. Should not be called
     *if the current command is C_RETURN.
     * @return - the first arq
     */
    public String firstArg() {
        // maybe its better to chnage this by using regex?????????????????????????????????????????????????????????????????????????????????????/
        if (commandType().equals(C_ARITHMETIC)) {
//            System.out.println(curCommand+  "   curcomman");
            return curCommand.substring(0, curCommand.length()); /// for what we are doing substiong?????????????????????????????????????????????/
        } else if (commandType().equals(C_POP) || commandType().equals(C_PUSH)) {
//            System.out.println("********");
//
//            System.out.println(curCommand);
//            System.out.println(curCommand.substring(curCommand.indexOf(" ")+1, curCommand.lastIndexOf(" ")));
//            System.out.println("********");

            return curCommand.substring(curCommand.indexOf(" ") + 1, curCommand.lastIndexOf(" "));
        } else if (commandType().equals(C_FUNCTION)) {
            Pattern pFunction = Pattern.compile("\\s*function\\s+([\\w\\\\.]+)\\s+\\d+\\s*"); //check ith safwat the correct of regex for the name of the function ??
                                                                                                        // there is need fpr sapaces and tha name should not start with number ! how to make that?????????
            Matcher matchfun = pFunction.matcher(curCommand);
            if (matchfun.find()) {
                return matchfun.group(1);
            }
        } else if (commandType().equals(C_CALL)) {
            Pattern pCall = Pattern.compile("\\s*call\\s+([\\w\\\\.]+)\\s+\\d+\\s*"); //check the correct of regex for the name of the function ?? there is need fpr sapavces?????????
            Matcher matchCall = pCall.matcher(curCommand);
            if (matchCall.find()) {
                return matchCall.group(1);
            }
        } else if (commandType().equals("C_IF")) {

            Pattern pIf = Pattern.compile("\\s*if-goto\\s+([\\w]+)\\s*");
            Matcher matchIf = pIf.matcher(curCommand);
            if (matchIf.find()) {
                return matchIf.group(1);
            }
        } else if (commandType().equals("C_GOTO")) {

            Pattern pGoto = Pattern.compile("\\s*goto\\s+([\\w]+)\\s*");
            Matcher matchGoto = pGoto.matcher(curCommand);
            if (matchGoto.find()) {
                return matchGoto.group(1);
            }
        } else if (commandType().equals("C_LABEL")) {

            Pattern pLabel = Pattern.compile("\\s*label\\s+([\\w]+)\\s*");
            Matcher matchLabel = pLabel.matcher(curCommand);
            if (matchLabel.find()) {
                return matchLabel.group(1);
            }

        }
        return null;

    }


    /**
     * Returns the second argument of the current
     *command. Should be called only if the current
     *command is C_PUSH,C_POP,C_FUNCTION, or C_CALL.
     * @return - the second argument
     */
    public int seconrArg(){
        if(commandType().equals("C_PUSH")){
            Pattern pPush = Pattern.compile("\\s*push\\s+[\\w]+\\s+(\\d+)\\s*");
            Matcher matchPush = pPush.matcher(curCommand);
            if(matchPush.find()){
                return Integer.parseInt(matchPush.group(1));
            }
        }
        else if(commandType().equals("C_POP")){

            Pattern pPop = Pattern.compile("\\s*pop\\s+[\\w]+\\s+(\\d+)\\s*");
            Matcher matchPop = pPop.matcher(curCommand);
            if(matchPop.find()){
                return Integer.parseInt(matchPop.group(1));
            }
        }
        else if(commandType().equals("C_FUNCTION")){

            Pattern pFunc = Pattern.compile("\\s*function\\s+[\\w\\\\.]+\\s+(\\d+)\\s*");
            Matcher matchFun = pFunc.matcher(curCommand);
            if(matchFun.find()){
                return Integer.parseInt(matchFun.group(1));
            }
        }
        else if(commandType().equals("C_CALL")){

            Pattern pCall= Pattern.compile("\\s*call\\s+[\\w\\\\.]+\\s+(\\d+)\\s*");
            Matcher matchCall = pCall.matcher(curCommand);
            if(matchCall.find()){
                return Integer.parseInt(matchCall.group(1));
            }
        }
        return 0;
    }





    /*
     * remove perfix spac
     */
    private static String removePerfixSpace(String line){
        Pattern pattern = Pattern.compile("\\s*(.*)");
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()){
            line = matcher.group(1);
        }
        return line;
    }

    /**
     * remove unnecessary spaces
     */
    public static String removeSpaces(String line){
        String result = "";
        if (line.length() != 0){
            String[] parts = line.split(" ");
            for (String str: parts){
                result += str;
                result+= " ";
            }
            result = result.substring(0, result.lastIndexOf(" "));
        }
        return result;
    }

    /*
     * make array of arithmetic operator
     */
    private void makeArrayOperators(){
        this.arithmeticOperators.add("add");
        this.arithmeticOperators.add("sub");
        this.arithmeticOperators.add("neq");
        this.arithmeticOperators.add("eq");
        this.arithmeticOperators.add("gt");
        this.arithmeticOperators.add("lt");
        this.arithmeticOperators.add("and");
        this.arithmeticOperators.add("or");
        this.arithmeticOperators.add("not");

    }
}
