package com.goeuro;

public class App {

	public static void main(String[] args) {
		if (args.length == 1) {
			new Runner().run(args[0]);
		} else {
			System.out.println("Incorrect count of arguments");
		}
	}

}
