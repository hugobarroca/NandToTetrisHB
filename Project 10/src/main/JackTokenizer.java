package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JackTokenizer {
	private String fileContent;
	private ArrayList<String[]> tokenList;
	private HashMap<String, String> lexicalElements;

	public JackTokenizer(File inputFile) throws FileNotFoundException {
		lexicalElements = new HashMap<String, String>();
		fileContent = "";
		tokenList = new ArrayList<String[]>();
		populateLexicalElements();
		readFileContent(inputFile);
		populateTokenList();
	}

	private void populateTokenList() throws FileNotFoundException {
		boolean unrecognizedSymbol = true;

		while (fileContent != "") {
			unrecognizedSymbol = true;
			fileContent = fileContent.strip();

			while (fileContent.startsWith("/**")) {
				fileContent = fileContent.split(Pattern.quote("*/"), 2)[1].strip();
			}

			for (String word : lexicalElements.keySet()) {
				if (fileContent.startsWith(word)) {
					processKeyword(word);
					unrecognizedSymbol = false;
					break;
				}
			}

			if (!unrecognizedSymbol)
				continue;

			if (fileContent == "")
				return;

			boolean isDigit = Character.isDigit(fileContent.charAt(0));
			boolean isAlphabetic = Character.isAlphabetic(fileContent.charAt(0));

			if (fileContent.startsWith("\"")) {
				processString();
			} else if (isDigit) {
				processNumber();
			} else if (isAlphabetic) {
				processAlphabetic();
			}

		}

	}

	// Removes word from fileContent and adds it to tokenList.
	private void processKeyword(String word) {
		String[] classifiedWord = new String[2];
		classifiedWord[0] = word;
		classifiedWord[1] = lexicalElements.get(word);
		
		classifiedWord[0] = classifiedWord[0].replace("<", "&lt;");
		classifiedWord[0] = classifiedWord[0].replace(">", "&gt;");
		classifiedWord[0] = classifiedWord[0].replace("\"", "&quot;");
		classifiedWord[0] = classifiedWord[0].replace("&", "&amp;");
		
		tokenList.add(classifiedWord);
		fileContent = fileContent.split(Pattern.quote(word), 2)[1];
		fileContent = fileContent.strip();
	}

	// Removes string from fileContent, excluding double quotes, and adds it to
	// tokenList.
	private void processString() {
		fileContent = fileContent.strip();
		fileContent = fileContent.split(Pattern.quote("\""), 2)[1];
		String[] tempString = fileContent.split(Pattern.quote("\""), 2);
		String[] classifiedWord = new String[2];
		classifiedWord[0] = tempString[0];
		classifiedWord[1] = "stringConstant";
		tokenList.add(classifiedWord);
		fileContent = tempString[1];
	}

	// Removes number from fileContent and adds it to tokenList.
	private void processNumber() {
		fileContent = fileContent.strip();
		char character = fileContent.charAt(0);
		String number = Character.toString(character);
		int i = 1;
		while (fileContent.length() > i - 1 && Character.isDigit(fileContent.charAt(i))) {
			character = fileContent.charAt(i);
			number += character;
			i++;
		}
		fileContent = fileContent.substring(i + 1);
		String[] classifiedWord = new String[2];
		classifiedWord[0] = number;
		classifiedWord[1] = "integerConstant";
		tokenList.add(classifiedWord);
	}

	// Removes an identifier from fileContent and adds it to tokenList.
	private void processAlphabetic() {
		fileContent = fileContent.strip();
		char character = fileContent.charAt(0);
		String identifier = Character.toString(character);
		int i = 1;
		while ((fileContent.length() > i - 1)
				&& (Character.isAlphabetic(fileContent.charAt(i)) || fileContent.charAt(i) == '_')) {
			character = fileContent.charAt(i);
			identifier += character;
			i++;
		}
		fileContent = fileContent.substring(i);
		String[] classifiedWord = new String[2];
		classifiedWord[0] = identifier;
		classifiedWord[1] = "identifier";
		tokenList.add(classifiedWord);
	}

	private void readFileContent(File inputFile) throws FileNotFoundException {
		Scanner fileScanner = null;

		if (inputFile.isDirectory()) {
			System.out.println("ERROR: JackTokenizer was given a directory!");
			System.exit(1);
		}

		fileScanner = new Scanner(inputFile);

		while (fileScanner.hasNextLine()) {
			String nextLine = fileScanner.nextLine();
			if (nextLine.contains("//")) {
				nextLine = nextLine.split("//")[0];
			}
			fileContent += nextLine;
		}

		fileScanner.close();
	}

	public boolean hasMoreTokens() {
		// TODO: Implement method.
		return true;
	}

	private void populateLexicalElements() {
		// Populate keywords
		lexicalElements.put("class", "keyword");
		lexicalElements.put("constructor", "keyword");
		lexicalElements.put("function", "keyword");
		lexicalElements.put("method", "keyword");
		lexicalElements.put("field", "keyword");
		lexicalElements.put("static", "keyword");
		lexicalElements.put("var", "keyword");
		lexicalElements.put("int", "keyword");
		lexicalElements.put("char", "keyword");
		lexicalElements.put("boolean", "keyword");
		lexicalElements.put("void", "keyword");
		lexicalElements.put("true", "keyword");
		lexicalElements.put("false", "keyword");
		lexicalElements.put("null", "keyword");
		lexicalElements.put("this", "keyword");
		lexicalElements.put("let", "keyword");
		lexicalElements.put("do", "keyword");
		lexicalElements.put("if", "keyword");
		lexicalElements.put("else", "keyword");
		lexicalElements.put("while", "keyword");
		lexicalElements.put("return", "keyword");

		// Populate Symbols
		lexicalElements.put("{", "symbol");
		lexicalElements.put("}", "symbol");
		lexicalElements.put("(", "symbol");
		lexicalElements.put(")", "symbol");
		lexicalElements.put("[", "symbol");
		lexicalElements.put("]", "symbol");
		lexicalElements.put(".", "symbol");
		lexicalElements.put(",", "symbol");
		lexicalElements.put(";", "symbol");
		lexicalElements.put("+", "symbol");
		lexicalElements.put("-", "symbol");
		lexicalElements.put("*", "symbol");
		lexicalElements.put("/", "symbol");
		lexicalElements.put("&", "symbol");
		lexicalElements.put("|", "symbol");
		lexicalElements.put("<", "symbol");
		lexicalElements.put(">", "symbol");
		lexicalElements.put("=", "symbol");
		lexicalElements.put("~", "symbol");
	}

	public void generateXML(String filepath) {
		try {
			File xmlFile = new File(filepath);
			if (xmlFile.createNewFile()) {
				FileWriter xmlWriter = new FileWriter(filepath);
				xmlWriter.write(getXML());
				xmlWriter.close();
				System.out.println("XML created: " + xmlFile.getName());
			} else
				System.out.println("File already exists.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public String getXML() {
		String xmlContent = "<tokens>";
		for (String[] classifiedWord : tokenList) {
			xmlContent += "<" + classifiedWord[1] + ">";
			xmlContent += classifiedWord[0];
			xmlContent += "</" + classifiedWord[1] + ">";
		}
		xmlContent += "</tokens>";
		return xmlContent;
	}
}
