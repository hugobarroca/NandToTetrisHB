package main;

import java.io.File;
import java.io.FileNotFoundException;

public class JackAnalyzer {
	String path;
	
	public JackAnalyzer(String path) {
		this.path = path;
	}
	
	
	
	public void compileFileOrDirectory() {
		
		File[] programFiles;
		File programToParse;
		

		
		System.out.println("Starting compilation process!");
		
		programToParse = new File(path);
		
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
			tokenizer.generateXML(programFile.getAbsolutePath().replace("jack", "xml"));
//			System.out.println("Printing token list:");
//			tokenizer.printTokenList();
		} catch (FileNotFoundException e) {
			System.out.println("There was an issue opening the file!");
			e.printStackTrace();
		}
	}
}
