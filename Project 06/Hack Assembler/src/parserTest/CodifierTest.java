package parserTest;

import org.junit.jupiter.api.Test;
import parser.Codifier;
import parser.Parser;

class CodifierTest {

	@Test
	void testAdd() {
		String expectedResult = 
				"0000000000000010\n"
				+ "1110110000010000\n"
				+ "0000000000000011\n"
				+ "1110000010010000\n"
				+ "0000000000000000\n"
				+ "1110001100001000\n";
		Parser parser = new Parser("Add.asm");
		Codifier codifier = new Codifier(parser.readFile());
		codifier.codeInstructionsToBinary();
		//System.out.println(codifier.parseInstructions());
		System.out.println(expectedResult);
		assert(codifier.codeInstructionsToBinary().equals(expectedResult));
	}

}
