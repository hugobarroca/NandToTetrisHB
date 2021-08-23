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
	private int currentTokenIndex;

	public JackTokenizer(File inputFile, boolean verboseMode) throws FileNotFoundException {
		lexicalElements = new HashMap<String, String>();
		fileContent = "";
		tokenList = new ArrayList<Token>();
		this.verboseMode = verboseMode;
		currentTokenIndex = 0;
		populateLexicalElements();
		readFileContent(inputFile);
		populateTokenList();
	}

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

	private void populateLexicalElements() {
		populateKeywords();
		populateSymbols();
	}

	private void populateKeywords(){
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
	}

	private void populateSymbols(){
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

	private void populateTokenList() throws FileNotFoundException {
		boolean unrecognizedSymbol = true;

		while (fileContent != "") {
			unrecognizedSymbol = true;

			clearWhiteSpacesAndComments();

			unrecognizedSymbol = tryToProcessWordAsLexicalElement();


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

	private void clearWhiteSpacesAndComments(){
		fileContent = fileContent.strip();

		while (fileContent.startsWith("/**")) {
			fileContent = fileContent.split(Pattern.quote("*/"), 2)[1].strip();
		}
	}

	private boolean tryToProcessWordAsLexicalElement(){
		for (String word : lexicalElements.keySet()) {
			boolean isFullWord = (fileContent.startsWith(word) && (word.length() >= fileContent.length() || !Character.isAlphabetic(fileContent.charAt(word.length()))) && lexicalElements.get(word) == "keyword" );
			boolean isSymbol = fileContent.startsWith(word) && lexicalElements.get(word) == "symbol";
			if (isFullWord || isSymbol) {
				processKeywordOrSymbol(word);
				return false;
			}
		}
//		System.out.println(fileContent.substring(0, 30));
//		System.out.println(fileContent.charAt(word.length()));
//		System.out.println();
		return true;
	}

	private void processKeywordOrSymbol(String word) {
		String type = lexicalElements.get(word);
		String value = replaceSpecialCharacters(word);
		Token token = new Token(type, value);

		tokenList.add(token);
		fileContent = fileContent.split(Pattern.quote(word), 2)[1];
		fileContent = fileContent.strip();
	}

	private String replaceSpecialCharacters(String value){
		if(value.contains("<"))
			return "&lt;";
		if(value.contains(">"))
			return "&gt;";
		if(value.contains("\""))
			return "&quot;";
		if(value.contains("&"))
			return "&amp;";
		return value;
	}

	private void processString() {
		fileContent = fileContent.strip();
		String[] tempString = fileContent.split(Pattern.quote("\""), 3); // Removes first double quote.
		fileContent = tempString[2];

		String type = "stringConstant";
		String value = tempString[1];
		Token token = new Token(type, value);

		tokenList.add(token);
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
		fileContent = fileContent.substring(i);

		String type = "integerConstant";
		String value = number;
		Token token = new Token(type, value);

		tokenList.add(token);
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

		String type = "identifier";
		String value = identifier;
		Token token = new Token(type, value);
		tokenList.add(token);
	}

	public boolean hasMoreTokens() {
		if (currentTokenIndex < tokenList.size())
			return true;
		return false;
	}

	public void advanceToken() {
		if (currentTokenIndex < tokenList.size())
			currentTokenIndex++;
	}

	public String tokenType(){
		return tokenList.get(currentTokenIndex).getType();
	}

	public String keyWord(){
		return tokenList.get(currentTokenIndex).getValue();
	}

	public String symbol(){
		return tokenList.get(currentTokenIndex).getValue();
	}

	public String identifier(){
		return tokenList.get(currentTokenIndex).getValue();
	}

	public String intVal(){
		return tokenList.get(currentTokenIndex).getValue();
	}

	public String stringVal(){
		return tokenList.get(currentTokenIndex).getValue();
	}

	public String nextSymbol(){
		return tokenList.get(currentTokenIndex + 1).getValue();
	}
}
