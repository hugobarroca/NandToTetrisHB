package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Addresser {
	private File assemblyFile;
	private AssemblerSymbolTable table;
	private String output;

	public Addresser(File assemblyFile) {
		this.assemblyFile = assemblyFile;
		this.table = new AssemblerSymbolTable();
	}

	public String runAddresser() {
		dealWithLoopSymbols();
		dealWithVariables();
		replaceSymbols();
		return output;

	}

	// This method will run through the code once, and will add any loop tags to the
	// symbol list.
	private void dealWithLoopSymbols() {

		try {
			Scanner fileScanner = new Scanner(assemblyFile);
			int lineCounter = 0;
			while (fileScanner.hasNextLine()) {
				String instruction = fileScanner.nextLine();
				instruction = instruction.split("/")[0];
				instruction = instruction.replace(" ", "");

				if (!instruction.equals("")) {
					if (instruction.startsWith("(")) {
						instruction = instruction.substring(instruction.indexOf("(") + 1);
						instruction = instruction.substring(0, instruction.indexOf(")"));
						table.addSymbolToTable(instruction, Integer.toString(lineCounter));
					} else {
						lineCounter++;
					}
				}
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File was not found. Check if you've run this program in the correct directory.");
			e.printStackTrace();
		}

	}

	// This method will run through the code once, and will add any variable tags to
	// the symbol list.
	private void dealWithVariables() {
		this.output = "";
		try {
			Scanner fileScanner = new Scanner(assemblyFile);
			int variableAddress = 16;

			while (fileScanner.hasNextLine()) {
				String instruction = fileScanner.nextLine();
				if (instruction.equals("@ponggame.0")) {
					System.out.println("Here!");
				}
				instruction = instruction.split("/")[0];
				instruction = instruction.replace(" ", "");

				if (!instruction.equals("")) {
					if (instruction.startsWith("@")) {
						String tempInstruction = instruction.replace("@", "");
						try {
							Integer.parseInt(tempInstruction);
						} catch (NumberFormatException nfe) {
							table.addSymbolToTable(instruction.substring(1), Integer.toString(variableAddress));
							variableAddress++;
						}

					}
				}
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File was not found. Check if you've run this program in the correct directory.");
			e.printStackTrace();
		}

	}

	// This method will run through the code once, and replace any symbol with it's
	// corresponding address
	private void replaceSymbols() {
		try {
			output = "";
			Scanner fileScanner = new Scanner(assemblyFile);

			while (fileScanner.hasNextLine()) {
				String instruction = fileScanner.nextLine();
				if (instruction.equals("@ponggame.0")) {
					System.out.println("Here!");
				}
				if (!instruction.startsWith("(")) {
					instruction = instruction.replace(" ", "");
					if (instruction.startsWith("@")) {
						String address = instruction;
						if (address.contains("/"))
							address = instruction.split("/")[1];
						address = address.replace("@", "");
						if (table.contains(address)) {
							output += "@" + table.getValue(address) + "\n";
							System.out.println("Value=" + table.getValue(address) + "; Key=" + address);
						} else {
							output += instruction + "\n";
						}
					} else {
						output += instruction + "\n";
					}
				}

			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File was not found. Check if you've run this program in the correct directory.");
			e.printStackTrace();
		}

	}
}
