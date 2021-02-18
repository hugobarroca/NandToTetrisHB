package main;

import java.io.File;
import java.io.FileNotFoundException;

// This class takes in the path to a .jack file, or a directory with multiple .jack files,
// and compiles them into their respective .vm files.
public class JackAnalyzer {
	String path;
	boolean verboseMode; // Turns on informative messages to the console.

	public JackAnalyzer(String path, boolean verboseMode) {
		this.path = path;
		this.verboseMode = verboseMode;
	}

// Takes in the field "path" and compiles all corresponding jack files.
	public void compileFileOrDirectory() {
		File programToParse;

		programToParse = new File(path);

		if (programToParse.isDirectory())
			parseDirectory(programToParse);
		else
			parseFile(programToParse);
	}

// Compiles all jack files in the directory passed as an argument.
	public void parseDirectory(File programToParse) {
		File[] programFiles;

		if (verboseMode)
			System.out.println("Parsing directory: " + programToParse.getAbsolutePath());

		programFiles = programToParse.listFiles();
		for (File programFile : programFiles) {
			if (programFile.getName().endsWith(".jack"))
				parseFile(programFile);
		}
	}

// Compiles a single jack file.
	public void parseFile(File programFile) {
		try {
			JackTokenizer tokenizer = new JackTokenizer(programFile, verboseMode);
			tokenizer.generateXMLFile(programFile.getAbsolutePath().replace("jack", "xml"));
			if (verboseMode)
				System.out.println("Processed file: " + programFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			if (verboseMode)
				System.out.println("Could not process the file: " + programFile.getAbsolutePath());
			e.printStackTrace();
		}
	}
}
