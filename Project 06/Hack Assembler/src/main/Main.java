package main;
import java.io.File;

import parser.Addresser;
import parser.Codifier;
import parser.Parser;

public class Main {
	public static void main(String[] args) {
		if(args == null) {
			System.out.println("You need to enter the relative path to the .asm file!");
			return;
		}
		Addresser addresser = new Addresser(new File(args[0]));
		String a = addresser.runAddresser();
		Parser parser = new Parser(a);
//		Parser parser = new Parser(args[0]);
		Codifier codifier = new Codifier(parser.readString());
//		Codifier codifier = new Codifier(parser.readFile());
		codifier.codeInstructionsToBinary();
		codifier.writeOutputToFile(args[0].replaceAll(".asm", ".hack"));

	}
}
