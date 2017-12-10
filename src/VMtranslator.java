import java.io.*;

import java.util.Arrays;
import java.io.File;



public class VMtranslator {

    /**
     * translate vm code to assembly code.
     * @param args -fles to translate
     */
    public static void main (String[] args){
        int i =0;

        if (args.length != 1) {
            System.out.println("there is something in the inputs arguments");
            return;
        }
        try {

            File fileIn = new File(args[0]);
            if (fileIn.isFile() && fileIn.getName().endsWith(".vm")) {

                CodeWriter codewriter = new CodeWriter(fileIn.getName());
                CodeWriter.outputFile = new BufferedWriter(new FileWriter(fileIn.getCanonicalPath().substring
                        (0, fileIn.getCanonicalPath().lastIndexOf(".")) + ".asm"));

                Parser parser = new Parser(fileIn.getName());
                Parser.reader = new BufferedReader(new FileReader(fileIn.getAbsolutePath()));

                while (parser.hasMoreCommands()) {
                    parser.advance();
                    if (parser.endOfFile) {
                        break;
                    }
                    if (parser.commandType().equals(Parser.C_PUSH) || parser.commandType().equals(Parser.C_POP)) {
                        codewriter.writePushPop(parser.commandType(), parser.firstArg(), parser.seconrArg());
                    } else if (parser.commandType().equals(Parser.C_ARITHMETIC)) {
                        codewriter.writeArithmetic(parser.firstArg());
                    }
                    if(parser.commandType().equals(Parser.C_LABEL)){
                        codewriter.writeLabel(parser.firstArg());
                    }
                    if(parser.commandType().equals(Parser.C_GOTO)){
                        codewriter.writeGoTo(parser.firstArg());
                    }
                    if(parser.commandType().equals(Parser.C_IF_GOTO)){
                        codewriter.writeIfGoTo(parser.firstArg());
                    }
                    if(parser.commandType().equals(Parser.C_CALL)){
                        codewriter.writeFuncCall(parser.firstArg(), parser.seconrArg());
                    }
                    if(parser.commandType().equals(Parser.C_FUNCTION)){
                        codewriter.declareFunc(parser.firstArg(), parser.seconrArg());
                    }
                    if(parser.commandType().equals(Parser.C_RETURN)){
                        codewriter.funReturn();
                    }

                }
                CodeWriter.outputFile.close();
            } else {
                File[] files1 = fileIn.listFiles();
                File[] files = Arrays.copyOfRange(files1, 1, files1.length);
                CodeWriter codewriter = new CodeWriter(fileIn.getName());
                CodeWriter.outputFile = new BufferedWriter(new FileWriter(fileIn.getAbsolutePath() +
                                                                        "/" + codewriter.getFileOutPath() + ".asm"));
                codewriter.writeInit();

                for (File f : files) {
                    if (f.getName().endsWith(".vm")) {
//                                            System.out.println(f.getName());

                        Parser parser = new Parser(f.getName());
                        Parser.reader = new BufferedReader(new FileReader(f.getAbsolutePath()));
                        while (parser.hasMoreCommands()) {
                            parser.advance();
                            if (parser.endOfFile) {
                                break;
                            }
                            if (parser.commandType().equals(Parser.C_PUSH) || parser.commandType().
                                                                                                equals(Parser.C_POP)) {
                                codewriter.writePushPop(parser.commandType(), parser.firstArg(), parser.seconrArg());
                            } if (parser.commandType().equals(Parser.C_ARITHMETIC)) {
                                codewriter.writeArithmetic(parser.firstArg());
                            }
                            if(parser.commandType().equals(Parser.C_LABEL)){
                                codewriter.writeLabel(parser.firstArg());
                            }
                            if(parser.commandType().equals(Parser.C_GOTO)){
                                codewriter.writeGoTo(parser.firstArg());
                            }
                            if(parser.commandType().equals(Parser.C_IF_GOTO)){
                                System.out.println(parser.firstArg());

                                codewriter.writeIfGoTo(parser.firstArg());
                            }
                            if(parser.commandType().equals(Parser.C_CALL)){
                                codewriter.writeFuncCall(parser.firstArg(), parser.seconrArg());
                            }
                            if(parser.commandType().equals(Parser.C_FUNCTION)){
                                codewriter.declareFunc(parser.firstArg(), parser.seconrArg());
                            }
                            if(parser.commandType().equals(Parser.C_RETURN)){
                                codewriter.funReturn();
                            }
                        }
                    }
                }
                CodeWriter.outputFile.close();
            }
        }
        catch(Exception e){
                System.err.format("Exception occurred trying to read /Write");
                e.printStackTrace();
            }
    }
}
