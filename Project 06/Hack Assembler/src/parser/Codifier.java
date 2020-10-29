package parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Codifier {
	private ArrayList<String[]> instructions;
	private String output;
	private final int maxAddressableRange = 240576;

	public Codifier(ArrayList<String[]> instructions) {
		this.instructions = instructions;
	}

	public String codeInstructionsToBinary() {
		String output = "";
		for (int counter = 0; counter < instructions.size(); counter++) {
			String codedInstruction = getInstructionCode(instructions.get(counter));
			if(codedInstruction == null)
				return null;
			output += codedInstruction + "\n";
		}
		this.output = output;
		return output;
	}

	public void writeOutputToFile(String filepath) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(filepath));
			writer.write(output);
			writer.close();
		} catch (IOException e) {
			System.out.println("There was a problem while trying to write to file.");
			e.printStackTrace();
		}

	}

	public String getInstructionCode(String[] instruction) {
		if (instruction[1] == null) {
			if (Integer.parseInt(instruction[0]) <= maxAddressableRange) {
				return getBinary(instruction[0]);
			} else {
				System.out.println("An address out of the addressable range was specified of an A-Instruction.");
				return null;
			}
		}

		if (instruction[0] == null) {
			if (instruction[2] == null) {
				// It's only a computation
				return "111" + getCompCode(instruction[1]) + "000000";
			} else {
				// It's a computation with a jump
				return "111" + getCompCode(instruction[1]) + "000" + getJumpCode(instruction[2]);
			}
		} else {
			if (instruction[2] == null) {
				// It's a computation with a destination
				return "111" + getCompCode(instruction[1]) + getDestCode(instruction[0]) + "000";
			} else {
				// It's a computation with a destination and a jump
				return "111" + getCompCode(instruction[1]) + getDestCode(instruction[0]) + getJumpCode(instruction[2]);
			}
		}
	}

	private String getCompCode(String calcExpression) {
		String finalExpression;
		if (calcExpression.contains("M")) {
			finalExpression = "1";
		} else {
			finalExpression = "0";
		}

		switch (calcExpression) {
		case "0":
			return finalExpression += "101010";
		case "1":
			return finalExpression += "111111";
		case "-1":
			return finalExpression += "111010";
		case "D":
			return finalExpression += "001100";
		case "A":
		case "M":
			return finalExpression += "110000";
		case "!D":
			return finalExpression += "001101";
		case "!A":
		case "!M":
			return finalExpression += "110001";
		case "-D":
			return finalExpression += "001111";
		case "-A":
		case "-M":
			return finalExpression += "110011";
		case "D+1":
			return finalExpression += "011111";
		case "A+1":
		case "M+1":
			return finalExpression += "110111";
		case "D-1":
			return finalExpression += "001110";
		case "A-1":
		case "M-1":
			return finalExpression += "110010";
		case "D+A":
		case "D+M":
			return finalExpression += "000010";
		case "D-A":
		case "D-M":
			return finalExpression += "010011";
		case "A-D":
		case "M-D":
			return finalExpression += "000111";
		case "D&A":
		case "D&M":
			return finalExpression += "000000";
		case "D|A":
		case "D|M":
			return finalExpression += "010101";
		default:
			return null;
		}
	}

	private String getDestCode(String destExpression) {
		switch (destExpression) {
		case "null":
			return "000";
		case "M":
			return "001";
		case "D":
			return "010";
		case "MD":
			return "011";
		case "A":
			return "100";
		case "AM":
			return "101";
		case "AD":
			return "110";
		case "AMD":
			return "111";
		default:
			return null;
		}
	}

	private String getJumpCode(String jumpExpression) {
		switch (jumpExpression) {
		case "null":
			return "000";
		case "JGT":
			return "001";
		case "JEQ":
			return "010";
		case "JGE":
			return "011";
		case "JLT":
			return "100";
		case "JNE":
			return "101";
		case "JLE":
			return "110";
		case "JMP":
			return "111";
		default:
			return null;
		}
	}

	private String getBinary(String address) {
		return String.format("%16s", Integer.toBinaryString(Integer.parseInt(address))).replace(' ', '0');
	}

}
