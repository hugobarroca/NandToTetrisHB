package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class JackTokenizer {
	private String currentToken;
	private String fileContent;

	public JackTokenizer(File inputFile) throws FileNotFoundException {
		currentToken = null;
		fileContent = "";
		readFileIntoString(inputFile);

	}

	private void readFileIntoString(File inputFile) throws FileNotFoundException {
		String currentLine = null;
		String currentWord = null;
		Scanner fileScanner = null;
		Scanner lineScanner = null;

		if (!inputFile.isDirectory()) {
			fileScanner = new Scanner(inputFile);

			while (fileScanner.hasNextLine()) {
				currentLine = fileScanner.nextLine();
				if (!(currentLine.startsWith("//") | currentLine.startsWith("/**"))) {
					lineScanner = new Scanner(currentLine);
					while (lineScanner.hasNext()) {
						currentWord = lineScanner.next();
						if (!currentWord.equals("//")) {
							fileContent += currentWord;
							fileContent += " ";
						} else {
							break;
						}
					}
					lineScanner.close();
				}
			}
			fileScanner.close();
		} else {
			System.out.println("ERROR: JackTokenizer was given a directory!");
		}
	}

	public boolean hasMoreTokens() {
		// TODO: Implement method.
		return true;
	}

	public void printFileContent() {
		System.out.println(fileContent);
	}
}
