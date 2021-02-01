package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class JackTokenizer {
	private String fileContent;
	private ArrayList<String[]> tokenList;
	private HashMap<String, String> lexicalElements; // Value, LexicalElement

	public JackTokenizer(File inputFile) throws FileNotFoundException {
		lexicalElements = new HashMap<String, String>();
		fileContent = "";
		tokenList = new ArrayList<String[]>();
		populateLexicalElements();
		populateTokenList(inputFile);
	}

	private void populateTokenList(File inputFile) throws FileNotFoundException {
		String currentLine = null;
		String currentWord = null;
		Scanner fileScanner = null;
		boolean multilineComment = false;

		if (inputFile.isDirectory()) {
			System.out.println("ERROR: JackTokenizer was given a directory!");
			System.exit(1);
		}

		fileScanner = new Scanner(inputFile);

		// Iterate through every line in the file.
		while (fileScanner.hasNextLine()) {
			currentLine = fileScanner.nextLine();
			
			if (currentLine.startsWith("/**"))
				multilineComment = true;
			
			if (currentLine.contains("*/"))
				multilineComment = false;
			
			if (currentLine.startsWith("//") || multilineComment)
				continue;
			
			stripTokens(currentWord);
		}

		fileScanner.close();
	}

	private void stripTokens(String tokens) {
		Iterator<?> it = lexicalElements.entrySet().iterator();
		boolean tokenFound = false;
		String[] keyValuePair = {};
		String tempTokens = tokens;

		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			if (tempTokens.startsWith(pair.getValue().toString())) {
				tempTokens.replace(pair.getValue().toString(), "");
				keyValuePair[0] = pair.getKey().toString();
				keyValuePair[1] = pair.getValue().toString();
				tokenList.add(keyValuePair);
				tokenFound = true;
			}
			it.remove();
		}
		if (!tokenFound) {
			System.out.println("ERROR: Unrecognized token: " + tokens);
			System.exit(1);
		}
		if (tempTokens != "") {
			stripTokens(tempTokens);
		} else {
			return;
		}
	}

	public boolean hasMoreTokens() {
		// TODO: Implement method.
		return true;
	}

	private void populateLexicalElements() {
		// Populate keywords
		lexicalElements.put("class",       "KEYWORD");
		lexicalElements.put("constructor", "KEYWORD");
		lexicalElements.put("function",    "KEYWORD");
		lexicalElements.put("method",      "KEYWORD");
		lexicalElements.put("field",       "KEYWORD");
		lexicalElements.put("static",      "KEYWORD");
		lexicalElements.put("var",         "KEYWORD");
		lexicalElements.put("int",         "KEYWORD");
		lexicalElements.put("char",        "KEYWORD");
		lexicalElements.put("boolean",     "KEYWORD");
		lexicalElements.put("void",        "KEYWORD");
		lexicalElements.put("true",        "KEYWORD");
		lexicalElements.put("false",       "KEYWORD");
		lexicalElements.put("null",        "KEYWORD");
		lexicalElements.put("this",        "KEYWORD");
		lexicalElements.put("let",         "KEYWORD");
		lexicalElements.put("do",          "KEYWORD");
		lexicalElements.put("if",          "KEYWORD");
		lexicalElements.put("else",        "KEYWORD");
		lexicalElements.put("while",       "KEYWORD");
		lexicalElements.put("return",      "KEYWORD");

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
