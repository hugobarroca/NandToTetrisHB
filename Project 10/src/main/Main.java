package main;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {
	
	// Main method of the application
	public static void main(String[] args) {
		File[] programFiles;
		File programToParse;

		if (args.length == 0) {
			System.out.println("No arguments were given! Quitting program!");
			System.exit(1);
		}

		System.out.println("Starting compilation process!");

		programToParse = new File(args[0]);

		if (programToParse.isDirectory()) {
			System.out.println("Parsing directory: " + programToParse.getAbsolutePath());
			programFiles = programToParse.listFiles();
			for (File programFile : programFiles) {
				if (programFile.getName().endsWith(".jack"))
					parseFile(programFile);
			}
		} else {
			parseFile(programToParse);
		}
	}

	
	public static void parseFile(File programFile) {
		try {
			System.out.println("Attempting to process file: " + programFile.getAbsolutePath());
			JackTokenizer tokenizer = new JackTokenizer(programFile);
			tokenizer.printXML();
//			System.out.println("Printing token list:");
//			tokenizer.printTokenList();
		} catch (FileNotFoundException e) {
			System.out.println("There was an issue opening the file!");
			e.printStackTrace();
		}
	}
}
