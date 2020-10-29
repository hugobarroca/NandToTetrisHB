package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

//This class reads the given file line by line and separates the instructions into their different fields (An address for A-Instructions and the Destination, Computation and Jump fields for D-Instructions.)
public class Parser {
	private File assemblyFile;
	private String assemblyString;

	public Parser(String assemblyFile) {
		this.assemblyFile = new File(assemblyFile);
		this.assemblyString = assemblyFile;
	}

	public ArrayList<String[]> readFile() {
		ArrayList<String[]> output = new ArrayList<String[]>();

		try {
			Scanner fileScanner = new Scanner(assemblyFile);

			// We read the next line and separate it into it's different fields. Each
			// instruction's fields are held in an array of length 3. Each of this arrays is
			// stored into an ArrayList, which holds all the fields for all the instructions
			// of the program.
			while (fileScanner.hasNextLine()) {
				String[] tempString = readLine(fileScanner.nextLine());
				if (tempString != null) {
					output.add(tempString);
				}
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File was not found. Check if you've run this program in the correct directory.");
			e.printStackTrace();
		}

		return output;
	}

	public ArrayList<String[]> readString() {
		ArrayList<String[]> output = new ArrayList<String[]>();

		Scanner fileScanner = new Scanner(assemblyString);

		// We read the next line and separate it into it's different fields. Each
		// instruction's fields are held in an array of length 3. Each of this arrays is
		// stored into an ArrayList, which holds all the fields for all the instructions
		// of the program.
		while (fileScanner.hasNextLine()) {
			String[] tempString = readLine(fileScanner.nextLine());
			if (tempString != null) {
				output.add(tempString);
			}
		}

		fileScanner.close();

		return output;
	}

	/**
	 * This method splits the instruction into different fields, and places them
	 * into the parsedFields array, according to the following:
	 * 
	 * Index 0: Address for an A instruction, Destination or NULL for a
	 * D-Instruction.
	 * 
	 * Index 1: NULL for an A instruction, Computation for a D-Instruction.
	 * 
	 * Index 2: NULL for an A instruction, Jump or NULL for a D-Instruction.
	 * 
	 * It returns NULL for a blank string, or a comment.
	 */
	private String[] readLine(String instruction) {
		//System.out.println(instruction);
		String[] parsedFields = new String[3];

		// Removes any trailing whitespace or comments from the instruction.
		instruction = instruction.split("/")[0];
		instruction = instruction.replace(" ", "");

		// Removes unnecessary character from the instruction, and splits the different
		// fields into the parsedFields array accordingly.
		if (instruction.startsWith("@")) {
			instruction = instruction.substring(1);
			parsedFields[0] = instruction;
		} else {
			if (instruction.contains("=")) {
				if (instruction.contains(";")) {
					parsedFields = instruction.split("[=;]");
				} else {
					String tempArray[] = instruction.split("[=]");
					parsedFields[0] = tempArray[0];
					parsedFields[1] = tempArray[1];
				}
			} else {
				if (instruction.contains(";")) {
					String tempArray[] = instruction.split("[;]");
					parsedFields[1] = tempArray[0];
					parsedFields[2] = tempArray[1];
				} else {
					if (instruction.equals("")) {
						parsedFields = null;
					} else {
						parsedFields[1] = instruction;
					}
				}
			}
		}

		return parsedFields;
	}

}
