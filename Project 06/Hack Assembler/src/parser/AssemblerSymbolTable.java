package parser;

import java.util.HashMap;

public class AssemblerSymbolTable {
	HashMap<String, String> symbolAdress;

	public AssemblerSymbolTable() {
		this.symbolAdress = new HashMap<String, String>();
		initializeSymbolTable();
	}

	private void initializeSymbolTable() {
		for (int i = 0; i < 16; i++) {
			symbolAdress.put("R" + i, Integer.toString(i));
		}
		symbolAdress.put("SP", "0");
		symbolAdress.put("LCL", "1");
		symbolAdress.put("ARG", "2");
		symbolAdress.put("THIS", "3");
		symbolAdress.put("THAT", "4");
		symbolAdress.put("SCREEN", "16384");
		symbolAdress.put("KBD", "24576");

	}
	
	public void addSymbolToTable(String key, String value) {
		if(!symbolAdress.containsKey(key)) {
			symbolAdress.put(key, value);
		}
	}
	
	public boolean contains(String key) {
		return symbolAdress.containsKey(key);
	}

	public String getValue(String key) {
		return symbolAdress.get(key);
	}
	
}
