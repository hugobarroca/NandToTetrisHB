package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JackTokenizer {
	private String fileContent;
	private ArrayList<String> tokenList;
	private HashMap<String, String> lexicalElements;

	public JackTokenizer(File inputFile) throws FileNotFoundException {
		lexicalElements = new HashMap<String, String>();
		fileContent = "";
		tokenList = new ArrayList<String>();
		populateLexicalElements();
		readFileContent(inputFile);
		populateTokenList();
	}

	private void populateTokenList() throws FileNotFoundException {

		while (fileContent != "") {
			boolean unrecognizedSymbol = true;
			fileContent = fileContent.strip();
			
			while (fileContent.startsWith("/**")) {
				fileContent = fileContent.split(Pattern.quote("*/"))[1];
				fileContent = fileContent.strip();
			}

			// Checks if the next word is either a symbol or a keyword.
			for (String symbol : lexicalElements.keySet()) {
				if (fileContent.startsWith(symbol)) {
					tokenList.add(symbol);
					fileContent = fileContent.split(Pattern.quote(symbol), 2)[1];
					unrecognizedSymbol = false;
					fileContent = fileContent.strip();
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
				processSingleLineComment();
			} else if (isDigit) {
				processNumber();
			} else if (isAlphabetic) {
				processAlphabetic();
			}

		}

	}

	private void processSingleLineComment() {
		fileContent = fileContent.strip();
		fileContent = fileContent.split(Pattern.quote("\""), 2)[1];
		String[] tempString = fileContent.split(Pattern.quote("\""), 2);
		tokenList.add(tempString[0]);
		fileContent = tempString[1];
	}
	
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
		tokenList.add(number);
	}
	
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
		tokenList.add(identifier);
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
		lexicalElements.put("class", "KEYWORD");
		lexicalElements.put("constructor", "KEYWORD");
		lexicalElements.put("function", "KEYWORD");
		lexicalElements.put("method", "KEYWORD");
		lexicalElements.put("field", "KEYWORD");
		lexicalElements.put("static", "KEYWORD");
		lexicalElements.put("var", "KEYWORD");
		lexicalElements.put("int", "KEYWORD");
		lexicalElements.put("char", "KEYWORD");
		lexicalElements.put("boolean", "KEYWORD");
		lexicalElements.put("void", "KEYWORD");
		lexicalElements.put("true", "KEYWORD");
		lexicalElements.put("false", "KEYWORD");
		lexicalElements.put("null", "KEYWORD");
		lexicalElements.put("this", "KEYWORD");
		lexicalElements.put("let", "KEYWORD");
		lexicalElements.put("do", "KEYWORD");
		lexicalElements.put("if", "KEYWORD");
		lexicalElements.put("else", "KEYWORD");
		lexicalElements.put("while", "KEYWORD");
		lexicalElements.put("return", "KEYWORD");

		// Populate Symbols
		lexicalElements.put("{", "SYMBOL");
		lexicalElements.put("}", "SYMBOL");
		lexicalElements.put("(", "SYMBOL");
		lexicalElements.put(")", "SYMBOL");
		lexicalElements.put("[", "SYMBOL");
		lexicalElements.put("]", "SYMBOL");
		lexicalElements.put(".", "SYMBOL");
		lexicalElements.put(",", "SYMBOL");
		lexicalElements.put(";", "SYMBOL");
		lexicalElements.put("+", "SYMBOL");
		lexicalElements.put("-", "SYMBOL");
		lexicalElements.put("*", "SYMBOL");
		lexicalElements.put("/", "SYMBOL");
		lexicalElements.put("&", "SYMBOL");
		lexicalElements.put("|", "SYMBOL");
		lexicalElements.put("<", "SYMBOL");
		lexicalElements.put(">", "SYMBOL");
		lexicalElements.put("=", "SYMBOL");
		lexicalElements.put("~", "SYMBOL");
	}

	public void printFileContent() {
		System.out.println(fileContent);
	}

	public void printTokenList() {
		for (String token : tokenList) {
			System.out.println(token);
		}
	}

	public void printXML() {
		// TODO
		return;
	}
}
