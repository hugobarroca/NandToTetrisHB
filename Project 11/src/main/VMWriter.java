package main;
import main.enums.Command;
import main.enums.Segment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    FileWriter writer;

    public VMWriter(File output) throws IOException {
        this.writer = new FileWriter(output);
    }

    public void writePush(Segment segment, int index) {
        String push = String.format("push %s %s", segment, index);
        try {
            writer.write(push);
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + push);
        }
    }

    public void writePop(Segment segment, int index){
        String pop = String.format("pop %s %s", segment, index);
        try {
            writer.write(pop);
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + pop);
        }
    }

    public void writeArithmetic(Command command){
        try {
            writer.write(String.valueOf(command));
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + command);
        }
    }

    public void writeLabel(String Label){
        String finalLabel = "label " + Label;
        try {
            writer.write(finalLabel);
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + finalLabel);
        }
    }

    public void writeGoto(String Label){
        String gotoLabel = "goto " + Label;
        try {
            writer.write(gotoLabel);
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + gotoLabel);
        }
    }

    public void writeIf(String Label){
        String ifGoto = "if-goto " + Label;
        try {
            writer.write(ifGoto);
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + ifGoto);
        }
    }

    public void writeCall(String name, int nArgs){
        String call = "call " + name + nArgs;
        try {
            writer.write(call);
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + call);
        }
    }

    public void writeFunction(String name, int nLocals){
        String function = "function " + name + " " + nLocals;
        try {
            writer.write(function);
        } catch (IOException e) {
            System.out.println("ERROR WRITING: " + function);
        }
    }

    public void writeReturn(){
        try {
            writer.write("return");
        } catch (IOException e) {
            System.out.println("ERROR WRITING: return");
        }
    }

    public void close() throws IOException {
        writer.close();
    }

}
