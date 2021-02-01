package main;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {
	public static void main(String[] args) {
		File[] programFiles;
		File programToParse;


		System.out.println("Starting compilation process!");

		programToParse = new File (args[0]);
		
		if(programToParse.isDirectory()) {
			System.out.println("Parsing directory: " + programToParse.getAbsolutePath());
			programFiles = programToParse.listFiles();
			for(File programFile : programFiles) {
				parseFile(programFile);
			}
		}else {
			parseFile(programToParse);
		}
		
		System.out.println("Done!");
	}
	
	public static void parseFile(File programFile) {
		JackTokenizer tokenizer;
		 try {
			System.out.println("Attempting to process file: " + programFile.getAbsolutePath());
			tokenizer = new JackTokenizer(programFile);
			tokenizer.printFileContent();
		} catch (FileNotFoundException e) {
			System.out.println("There was an issue opening the file!");
			e.printStackTrace();
		}
	}
	
}
