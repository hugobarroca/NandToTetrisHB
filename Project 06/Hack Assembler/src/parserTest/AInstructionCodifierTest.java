package parserTest;
import org.junit.jupiter.api.Test;
import parser.AInstructionCodifier;

class AInstructionCodifierTest {

	@Test
	void test() {
		AInstructionCodifier codifier = new AInstructionCodifier("15");
		assert(codifier.getBinary().equals("0000000000001111"));
	}

}
