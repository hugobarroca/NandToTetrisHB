package main;

import java.io.File;
import java.io.FileNotFoundException;

public class JackAnalyzer {
	String path;
	boolean verboseMode;

	public JackAnalyzer(String path, boolean verboseMode) {
		this.path = path;
		this.verboseMode = verboseMode;
	}

	public void compileFileOrDirectory() {
		File programToParse;

		programToParse = new File(path);

		if (programToParse.isDirectory())
			parseDirectory(programToParse);
		else
			parseFile(programToParse);
	}

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

	public void parseFile(File programFile) {
		try {
			File outputFile = new File(programFile.getAbsolutePath().replace("jack", "xml"));
			CompilationEngine engine = new CompilationEngine(programFile, outputFile);
			engine.compileClass();
			engine.writeToOutput();
			if (verboseMode)
				System.out.println("Processed file: " + programFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			if (verboseMode)
				System.out.println("Could not process the file: " + programFile.getAbsolutePath());
			e.printStackTrace();
		}
	}
}
