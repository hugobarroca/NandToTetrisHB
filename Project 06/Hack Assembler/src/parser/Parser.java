package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
	private File assemblyFile;
	private boolean isAInstruction;

	public Parser(String assemblyFile) {
		this.assemblyFile = new File(assemblyFile);
	}

	public void readFile() {
		try {
			Scanner fileScanner = new Scanner(assemblyFile);

			while (fileScanner.hasNextLine()) {
				readLine(fileScanner.nextLine());
			}

			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File was not found. Check if you've run this program in the correct directory.");
			e.printStackTrace();
		}
	}

	public String readLine(String currentLine) {
		String convertedLine = "";
		if (currentLine.startsWith("@")) {
			currentLine = currentLine.split("[ /]")[0].substring(1); 	// Removes any trailing whitespace or comments, as well as the starting "@".
			
		}
		return convertedLine;
	}

}
