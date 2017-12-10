
import com.sun.org.apache.bcel.internal.classfile.Code;

import java.awt.*;
import java.io.*;
import java.util.Hashtable;

public class CodeWriter {

    static BufferedWriter outputFile;
    private int numberOfJump;
    public String fileName;
    private Hashtable<String, String> segmentsTable;
    private Hashtable<String, String> jumpTable;
    private static final String CONSTANT = "constant";
    private static final String LOCAL = "local";
    private static final String ARGUMENT = "argument";
    private static final String THIS = "this";
    private static final String THAT = "that";
    private static final String STATIC = "static";
    private static final String TEMP = "temp";
    private static final String POINTER = "pointer";
    private static final String ADD = "add";
    private static final String SUB = "sub";
    private static final String AND = "and";
    private static final String OR = "or";
    private static final String NEG = "neg";
    private static final String NOT = "not";
    private static final String EQUAL = "eq";
    private static final String LESS_THAN = "lt";
    private static final String GREATER_THAN = "gt";
    //*******************************************************************ahmad edit
    private static final String LABEL = "label";
    private static String currentFunc;
    private static int returnNum;


    /**
     * Opens the output file/stream and gets ready to write into it.
     *
     * @param fileName
     */
    public CodeWriter(String fileName) {

        this.fileName = fileName;
        this.segmentsTable = MakesegmentsTable();
        this.jumpTable = makeSomeJump();
        this.numberOfJump = 0;
    }

    /**
     * Writes the assembly code that is the translation of the given
     * command, where command is either C_PUSH or C_POP.
     *
     * @param commandType
     * @param segment
     * @param index
     * @throws IOException
     */
    public void writePushPop(String commandType, String segment, int index) throws IOException {

        if (commandType.equals(Parser.C_PUSH)) {

            if (segment.equals(CONSTANT)) {
                CodeWriter.outputFile.write("@" + index + "\nD=A\n" + pushtoStack());
            } else if (segment.equals(LOCAL) || segment.equals(ARGUMENT) || segment.equals(THIS) ||
                    segment.equals(THAT)) {
                CodeWriter.outputFile.write("@" + index + "\nD=A\n" + segmentsTable.get(segment) +
                        "\nA=M+D\nD=M\n" + pushtoStack());
            } else if (segment.equals(STATIC)) {
//                System.out.println("fat write push static - "+fileName);
                CodeWriter.outputFile.write("@" + fileName + index + "\nD=M\n" + pushtoStack());
            } else if (segment.equals(TEMP)) {
                CodeWriter.outputFile.write("@" + (index + 5) + "\nD=M\n" + pushtoStack());
            } else if (segment.equals(POINTER)) {
                if (index == 0) {
                    CodeWriter.outputFile.write("@THIS\nD=M\n" + pushtoStack());
                } else {
                    CodeWriter.outputFile.write("@THAT\nD=M\n" + pushtoStack());
                }
            }


        } else if (commandType.equals(Parser.C_POP)) {
            if (segment.equals(LOCAL) || segment.equals(ARGUMENT) || segment.equals(THIS) || segment.equals(THAT)) {
                CodeWriter.outputFile.write("@" + index + "\nD=A\n" + segmentsTable.get(segment) +
                        "\nD=M+D\n@R13\nM=D\n" + popFromStack());
            } else if (segment.equals(STATIC)) {
//                System.out.println("fat write  pop static");

                CodeWriter.outputFile.write("@SP\nM=M-1\nA=M\nD=M\n" +"@" + fileName + index + "\nM=D\n");

            } else if (segment.equals(TEMP)) {
                CodeWriter.outputFile.write("@" + (index + 5) + "\nD=A\n@R13\nM=D\n" + popFromStack());
            } else if (segment.equals(POINTER)) {
                if (index == 0) {
                    CodeWriter.outputFile.write("@SP\nM=M-1\nA=M\nD=M\n@THIS\nM=D\n");
                } else if (index == 1) {
                    CodeWriter.outputFile.write("@SP\nM=M-1\nA=M\nD=M\n@THAT\nM=D\n");
                }
            }
        }
    }

//    else if(commandType.equals(LABEL)){
//        CodeWriter.outputFile.write();
//    }

    /**
     * Writes the assembly code that is the translation
     * of the given arithmetic command.
     *
     * @param command
     * @throws IOException
     */
    public void writeArithmetic(String command) throws IOException {
//        System.out.println(command);
        if (command.equals(ADD)) {
            CodeWriter.outputFile.write(binaryOperate() + "M=M+D\n");
        } else if (command.equals(SUB)) {

            CodeWriter.outputFile.write(binaryOperate() + "M=M-D\n");
        } else if (command.equals(AND)) {
            CodeWriter.outputFile.write(binaryOperate() + "M=M&D\n");
        } else if (command.equals(OR)) {
            CodeWriter.outputFile.write(binaryOperate() + "M=M|D\n");
        } else if (command.equals(NEG)) {
            CodeWriter.outputFile.write("@0\nD=A\n@SP\nA=M-1\nM=D-M\n");
        } else if (command.equals(NOT)) {
            CodeWriter.outputFile.write("@SP\nA=M-1\nM=!M\n");
        }
        else if (command.equals(EQUAL)) {
            CodeWriter.outputFile.write(makeJump("JEQ"));
            this.numberOfJump++;
        } else if (command.equals(LESS_THAN)) {
            CodeWriter.outputFile.write(makeJump("JLT"));
            this.numberOfJump++;
        } else if (command.equals(GREATER_THAN)) {
            CodeWriter.outputFile.write(makeJump("JGT"));
            this.numberOfJump++;
        }

    }

    public void writeFuncCall(String funcName, int argsNumber) throws IOException {
//        System.out.println(funcName);
        CodeWriter.outputFile.write("@RETURN_"+funcName+"_"+(returnNum)+"\nD=A\n"+pushtoStack() +
                "@LCL\nD=M\n"+pushtoStack() +
                "@ARG\nD=M\n"+pushtoStack() +
                "@THIS\nD=M\n"+pushtoStack() +
                "@THAT\nD=M\n"+pushtoStack());
        CodeWriter.outputFile.write("@"+(argsNumber + 5) +"\n D=A\n@SP\nD=M-D\n@ARG\nM=D\n");
        CodeWriter.outputFile.write("@SP\nD=M\n@LCL\nM=D\n");
        CodeWriter.outputFile.write("@" + funcName + "\n0;JMP\n");
        CodeWriter.outputFile.write("(RETURN_"+funcName+"_"+(returnNum++)+")\n");

    }

    /**
     * declare a label for the function entry and initialize all og the local variables to 0
     * @param funcName function name
     * @param k number of local function
     * @throws IOException
     */
    public void writeDeclareFunc(String funcName, int k) throws IOException {
//        System.out.println(funcName);
        CodeWriter.outputFile.write("(" + funcName + ")\n");
        for (int i = 0; i < k; i++){
            CodeWriter.outputFile.write("@SP\nA=M\nM=0\n@SP\nM=M+1\n");
        }
        currentFunc = funcName;
    }

    /**
     * write assemply code for return command
     * @throws IOException
     */
    public void funReturn() throws IOException {
        CodeWriter.outputFile.write("@LCL\nD=M\n@FRAME\nM=D\n");//FRAME is temporary variable
        CodeWriter.outputFile.write("@5\nA=D-A\nD=M\n@RET\nM=D\n");//put return address in the temp cariable RET
        CodeWriter.outputFile.write("@SP\nA=M-1\nD=M\n@ARG\nA=M\nM=D\n");//reposition the return value for the caller
        CodeWriter.outputFile.write("@ARG\nD=M+1\n@SP\nM=D\n");//restore SP of the caller
//        CodeWriter.outputFile.write("@ARG\nD=M+1\n@SP\nM=D\n");//restore THAT of the caller

        CodeWriter.outputFile.write("@1\nD=A\n@FRAME\nA=M-D\nD=M\n@THAT\nM=D\n");//restore THAT of the caller
        CodeWriter.outputFile.write("@2\nD=A\n@FRAME\nA=M-D\nD=M\n@THIS\nM=D\n");//restore THIS of the caller
        CodeWriter.outputFile.write("@3\nD=A\n@FRAME\nA=M-D\nD=M\n@ARG\nM=D\n");//restore ARG of the caller
        CodeWriter.outputFile.write("@4\nD=A\n@FRAME\nA=M-D\nD=M\n@LCL\nM=D\n");//restore LCL of the caller

        CodeWriter.outputFile.write("@RET\nA=M\n0;JMP\n");//go to return address in the caller code

    }




    public void writeLabel(String label) throws IOException {
        CodeWriter.outputFile.write("(" +currentFunc+"$"+label + ")\n");
    }

    public void writeGoTo(String label) throws IOException {
        CodeWriter.outputFile.write("@" +currentFunc+"$"+label+ "\n" + "0;JMP\n");
    }
    public void writeIfGoTo(String label) throws IOException {
        CodeWriter.outputFile.write("@SP\nM=M-1\nA=M\nD=M\n@" + currentFunc+"$"+label+"\n" + "D;JNE\n");
    }



    /**
     * get file out name
     *
     * @return -file out name
     */
    public String getFileOutPath() {
        return this.fileName;
    }


    /*
     * make some segments table ,and translate it to assembly.
     */
    private Hashtable<String, String> MakesegmentsTable() {
        segmentsTable = new Hashtable<String, String>();
        segmentsTable.put(LOCAL, "@LCL");
        segmentsTable.put(ARGUMENT, "@ARG");
        segmentsTable.put(THIS, "@THIS");
        segmentsTable.put(THAT, "@THAT");
//        segmentsTable.put(STATIC, "@" + fileName);
//        segmentsTable.put(STATIC, "@" + fileOutName.substring(fileOutName.lastIndexOf("/") + 1,
//                fileOutName.indexOf(".") + 1));
        return segmentsTable;

    }

    /*
     *push value to the memory location that SP points at.
     */
    private String pushtoStack() {
        return "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
    }

    /*
     *pop value from the memory location that SP-1 points at.
     */
    private String popFromStack() {
        return "@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
    }

    /*
     *prepare the last to values in the stack.
     */
    private String binaryOperate() {
        return "@SP\nM=M-1\nA=M\nD=M\nA=A-1\n";
    }

    /*
     *make some jump table
     */
    private Hashtable<String, String> makeSomeJump() {
        jumpTable = new Hashtable<String, String>();
        jumpTable.put("JEQ", "D;JEQ\n");
        jumpTable.put("JLT", "D;JLT\n");
        jumpTable.put("JGT", "D;JGT\n");
        return jumpTable;
    }

    /*
     *jumb according to the conditions.
     */
    private String makeJump(String jumpType) {
//        System.out.println("faaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaat");
        return "@SP\nM=M-1\nA=M\nD=M\n@R13\nM=D\n" +
                "@SECONDNEG" + numberOfJump +
                "\nD;JLT\n" +
                "@SP\nM=M-1\nA=M\nD=M\n" +
                "@SECONDPOS&FIRSTNEG" + numberOfJump +
                "\nD;JLT\n" +
                "@R13\nD=D-M\n" +
                "@CHECK" + numberOfJump +
                "\n0;JMP\n" +
                "(SECONDNEG" + numberOfJump + ")\n" +
                "@SP\nM=M-1\nA=M\nD=M\n" +
                "@SECONDNEG&FIRSTPOS" + numberOfJump + "\n" +
                "D;JGT\n" +
                "@R13\nD=D-M\n" +
                "@CHECK" + numberOfJump + "\n" +
                "0;JMP\n" +
                "(SECONDPOS&FIRSTNEG" + numberOfJump + ")\n" +
                "D=-1\n" +
                "@CHECK" + numberOfJump + "\n" +
                "0;JMP\n" +
                "(SECONDNEG&FIRSTPOS" + numberOfJump + ")\n" +
                "D=1\n" +
                "@CHECK" + numberOfJump + "\n" +
                "0;JMP\n" +
                "(CHECK" + numberOfJump + ")\n" +
                "@TRUE" + numberOfJump + "\n" +
                jumpTable.get(jumpType)+
                "D=0\n" +
                "@AFTR" + numberOfJump + "\n" +
                "0;JMP\n" +
                "(TRUE" + numberOfJump + ")\n" +
                "D=-1\n" +
                "@AFTR" + numberOfJump + "\n" +
                "0;JMP\n" +
                "(AFTR" + numberOfJump + ")\n" +
                "@SP\nA=M\nM=D\n@SP\nM=M+1\n";

    }
    /**
     * Writes assembly code that effects the VM initialization(bootstarp)
     * this code must placed at the begining of the output file
     * @throws IOException
     */
    public void writeInit() throws IOException {

        CodeWriter.outputFile.write("@256\nD=A\n@SP\nM=D\n");
        currentFunc = "Sys.init";
        writeFuncCall(currentFunc, 0);

    }
}