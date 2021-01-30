package main;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {
	
	public static void main(String[] args) {
		File programToParse;
		File[] programFiles;
		JackTokenizer tokenizer;

		System.out.println("Starting compilation process!");

		programToParse = new File (args[0]);
		if(programToParse.isDirectory()) {
			System.out.println("Parsing directory: " + programToParse.getAbsolutePath());
			programFiles = programToParse.listFiles();
			for(File programFile : programFiles) {
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
		
		System.out.println("Done!");
	}
}
