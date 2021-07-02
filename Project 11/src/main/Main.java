package main;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("No arguments were given! Quitting program!");
			System.exit(1);
		}
		
		JackAnalyzer analyzer = new JackAnalyzer(args[0], true);
		analyzer.compileFileOrDirectory();

	}

}
