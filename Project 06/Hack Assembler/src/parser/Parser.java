package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

//This class is meant to 
public class Parser {
	private File assemblyFile;

	public Parser(String assemblyFile) {
		this.assemblyFile = new File(assemblyFile);
	}

	public ArrayList<String[]> readFile() {
		try {
			ArrayList<String[]> output = new ArrayList<String[]>();
			Scanner fileScanner = new Scanner(assemblyFile);
			while (fileScanner.hasNextLine()) {
				String[] tempString = readLine(fileScanner.nextLine());
				if (tempString != null) {
					output.add(tempString);
				}
			}

			fileScanner.close();
			return output;
		} catch (FileNotFoundException e) {
			System.out.println("File was not found. Check if you've run this program in the correct directory.");
			e.printStackTrace();
		}
		return null;
	}

	public String[] readLine(String currentLine) {
		String[] convertedLine = new String[3];
		currentLine = currentLine.split("[ /]")[0]; // Removes any trailing whitespace or comments

		if (currentLine.startsWith("@")) {
			currentLine = currentLine.substring(1); // Removes the starting "@".
			convertedLine[0] = currentLine;
		} else {

			if (currentLine.contains("=")) {
				if (currentLine.contains(";")) {
					convertedLine = currentLine.split("[=;]");
				} else {
					String tempArray[] = currentLine.split("[=]");
					convertedLine[0] = tempArray[0];
					convertedLine[1] = tempArray[1];
				}
			} else {
				if (currentLine.contains(";")) {
					String tempArray[] = currentLine.split("[;]");
					convertedLine[1] = tempArray[1];
					convertedLine[2] = tempArray[2];
				} else {
					if (currentLine.equals("")) {
						convertedLine = null;
					}else {
						convertedLine[1] = currentLine;
					}
				}
			}
		}

		return convertedLine;
	}

}
