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
		Parser parser = new Parser(addresser.runAddresser());
		Codifier codifier = new Codifier(parser.readString());
		codifier.codeInstructionsToBinary();
		codifier.writeOutputToFile(args[0].replaceAll(".asm", ".hack"));

	}
}
