package main;
import main.enums.Command;
import main.enums.Segment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    FileWriter writer;

    public VMWriter(File output) throws IOException {
        var writer = new FileWriter(output);
    }

    public void writePush(Segment segment, int index){
    }

    public void writePop(Segment segment, int index){
    }

    public void writeArithmetic(Command command){
    }

    public void writeLabel(String Label){
    }

    public void writeGoto(String Label){
    }

    public void writeIf(String Label){
    }

    public void writeCall(String name, int nArgs){
    }

    public void writeFunction(String name, int nLocals){
    }

    public void writeReturn(){
    }

    public void close() throws IOException {
        writer.close();
    }

}
