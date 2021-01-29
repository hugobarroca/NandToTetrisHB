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

	public JackTokenizer(File inputFile) {
		currentToken = null;
		String fileContent = "";
		String currentWord = null;

		try {
			Scanner myReader = new Scanner(inputFile);

			while (myReader.hasNextLine()) {
				Scanner check = new Scanner(myReader.nextLine());
				if (!myReader.nextLine().startsWith("//")) {
					while (check.hasNext()) {
						currentWord = check.next();
						if (!currentWord.equals("//")) {
							fileContent += check.next();
						} else {
							break;
						}

					}
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("There was a problem reading the requested file! Make sure the chosen path is correct!");
			e.printStackTrace();
		}

	}

	public boolean hasMoreTokens() {
		// TODO: Implement method.
		return true;
	}
}
