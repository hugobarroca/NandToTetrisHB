package parser;

public class AInstructionCodifier {
	private String address;
	private final int maxAddressableRange = 65536;
	
	public AInstructionCodifier(String address) {
		if(Integer.parseInt(address) > maxAddressableRange) {
			System.out.println("");
		}
		this.address = address;
	}
	
	public String getBinary() {
		return String.format("%16s", Integer.toBinaryString(Integer.parseInt(address))).replace(' ', '0');
	}
	
	
}
