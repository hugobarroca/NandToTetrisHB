package main;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.Scanner;

public class JackTokenizer {
	private String fileContent;
	private ArrayList<Token> tokenList;
	private HashMap<String, String> lexicalElements;
	private boolean verboseMode;

	public JackTokenizer(File inputFile, boolean verboseMode) throws FileNotFoundException {
		lexicalElements = new HashMap<String, String>();
		fileContent = "";
		tokenList = new ArrayList<Token>();
		this.verboseMode = verboseMode;
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
		String type = lexicalElements.get(word);
		String value = word.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("&", "&amp;");
		Token token = new Token(type, value);

		tokenList.add(token);
		fileContent = fileContent.split(Pattern.quote(word), 2)[1];
		fileContent = fileContent.strip();
	}

// Removes string from fileContent, and adds it to the
// tokenList without the double quotes.
	private void processString() {
		fileContent = fileContent.strip();
		String[] tempString = fileContent.split(Pattern.quote("\""), 3); // Removes first double quote.
		fileContent = tempString[2];

		String type = "stringConstant";
		String value = tempString[1];
		Token token = new Token(type, value);

		tokenList.add(token);
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
		
		String type = "integerConstant";
		String value = number;
		Token token = new Token(type, value);
		
		tokenList.add(token);
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

		String type = "identifier";
		String value = identifier;
		Token token = new Token(type, value);
		tokenList.add(token);
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
		// TODO: Implement this method.
		return false;
	}

//Adds all possible keywords and symbols to the lexical elements hash map.
	private void populateLexicalElements() {
		// Keywords
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

		// Symbols
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

// Writes all read tokens to an XML file.
	public void generateXMLFile(String filepath) {
		try {
			File xmlFile = new File(filepath);
			if (xmlFile.createNewFile()) {
				FileWriter xmlWriter = new FileWriter(filepath);
				xmlWriter.write(getXML());
				xmlWriter.close();
				if (verboseMode)
					System.out.println("XML created: " + xmlFile.getName());
			} else if (verboseMode)
				System.out.println("File already exists: " + xmlFile.getName());

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

// Generates an xml-formated string, with each token tagged with it's token type.
	public String getXML() {
		String xmlContent = "<tokens>";
		for (Token token : tokenList) {
			xmlContent += "<" + token.getType() + ">";
			xmlContent += token.getValue();
			xmlContent += "</" + token.getType() + ">";
		}
		xmlContent += "</tokens>";
		return xmlContent;
	}
}
