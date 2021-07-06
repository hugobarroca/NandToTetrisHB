package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

	public void parseDirectory(File directory) {
		File[] programFiles;

		if (verboseMode)
			System.out.println("Parsing directory: " + directory.getAbsolutePath());

		programFiles = directory.listFiles();
		for (File programFile : programFiles) {
			if (programFile.getName().endsWith(".jack"))
				parseFile(programFile);
		}
	}

	public void parseFile(File programFile) {
		try {
			File outputXMLFile = new File(programFile.getAbsolutePath().replace("jack", "xml"));
			File outputVMFile = new File(programFile.getAbsolutePath().replace("jack", "vm"));
			CompilationEngine engine = new CompilationEngine(programFile, outputXMLFile, outputVMFile);
			engine.compileClass();
			engine.writeToOutput();
			if (verboseMode)
				System.out.println("Processed file: " + programFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			if (verboseMode)
				System.out.println("Could not process the file: " + programFile.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
