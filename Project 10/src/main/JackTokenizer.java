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
		populateTokenList(inputFile);
	}

	private void populateTokenList(File inputFile) throws FileNotFoundException {
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

		fileContent.replace(" ", "");

		while (fileContent != "") {
			boolean unrecognizedSymbol = true;

			if (fileContent.startsWith(Pattern.quote("\\/\\*\\*"))) {
				fileContent = fileContent.split(Pattern.quote("*/"))[1];
			}

			for (String symbol : lexicalElements.keySet()) {
				if (fileContent.startsWith(symbol)) {
					tokenList.add(symbol);
					fileContent = fileContent.split(Pattern.quote(symbol), 2)[1];
					unrecognizedSymbol = false;
					break;
				}
			}
			if (unrecognizedSymbol) {
				System.out.println("Current symbol was not recognized!");
			}
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
}
