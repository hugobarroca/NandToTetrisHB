package main;

public class Main {

	public static void main(String[] args) {
		quitIfNoArgumentsWereGiven(args);
		JackAnalyzer analyzer = new JackAnalyzer(args[0], true);
		analyzer.compileFileOrDirectory();

	}

	public static void quitIfNoArgumentsWereGiven(String[] args){
		if (args.length == 0) {
			System.out.println("No arguments were given! Quitting program!");
			System.exit(1);
		}
	}

}
