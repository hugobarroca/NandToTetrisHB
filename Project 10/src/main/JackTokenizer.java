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
	private HashMap<String, String> lexicalElements; // Value, LexicalElement

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

			while (fileContent.startsWith("/**")) {
				fileContent = fileContent.split(Pattern.quote("*/"))[1];
			}

			// Remove all leading whitespace.
			fileContent = fileContent.replaceFirst("^ *", "");

			// Checks if the next word is either a symbol or a keyword.
			for (String symbol : lexicalElements.keySet()) {
				if (fileContent.startsWith(symbol)) {
					tokenList.add(symbol);
					fileContent = fileContent.split(Pattern.quote(symbol), 2)[1];
					unrecognizedSymbol = false;
				}
			}

			if (!unrecognizedSymbol)
				continue;

			if (fileContent.startsWith("\"")) {
				fileContent = fileContent.split(Pattern.quote("\""), 2)[1];
				String[] tempString = fileContent.split(Pattern.quote("\""), 2);
				tokenList.add(tempString[0]);
				fileContent = tempString[1];
				unrecognizedSymbol = false;
			} else if (Character.isDigit(fileContent.charAt(0))) {
				String[] tempString = fileContent.split(" ", 2);
				tokenList.add(tempString[0]);
				fileContent = tempString[1];
				unrecognizedSymbol = false;
			} else if (Character.isDigit(fileContent.charAt(0))) {
				String[] tempString = fileContent.split(" ", 2);
				tokenList.add(tempString[0]);
				fileContent = tempString[1];
				unrecognizedSymbol = false;
			} else if (!Character.isDigit(fileContent.charAt(0))) {
				String[] tempString = fileContent.split(" ", 2);
				tokenList.add(tempString[0]);
				fileContent = tempString[1];
				unrecognizedSymbol = false;
			}

		}

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
	
	public void printXML() {
		//TODO
		return;
	}
}
