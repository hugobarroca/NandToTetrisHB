package main;
import parser.Codifier;
import parser.Parser;

public class Main {
	public static void main(String[] args) {
		if(args == null) {
			System.out.println("You need to enter the relative path to the .asm file!");
			return;
		}
		
		Parser parser = new Parser(args[0]);
		Codifier codifier = new Codifier(parser.readFile());
		codifier.parseInstructions();
		codifier.writeOutputToFile(args[0].replaceAll(".asm", ".hack"));
		
	}
}
