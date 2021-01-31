package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class JackTokenizer {
	private String currentToken;
	private String fileContent;
	private ArrayList<String[]> tokenList;
	private HashMap<String, String> lexicalElements; //Value, LexicalElement

	public JackTokenizer(File inputFile) throws FileNotFoundException {
		currentToken = null;
		lexicalElements = new HashMap<String, String>();
		fileContent = "";
		tokenList  = new ArrayList<String[]>();
		populateLexicalElements();
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
				if (!(currentLine.startsWith("//"))) {
					lineScanner = new Scanner(currentLine);
					while (lineScanner.hasNext()) {
						currentWord = lineScanner.next();
						if (!currentWord.equals("//")) {
							stripTokens(currentWord);
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

	private void stripTokens(String tokens) {
		//TODO: Implement method
	}
	
	public boolean hasMoreTokens() {
		// TODO: Implement method.
		return true;
	}
	
	private void populateLexicalElements() {
		//Populate keywords
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
		//Populate Symbols
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
