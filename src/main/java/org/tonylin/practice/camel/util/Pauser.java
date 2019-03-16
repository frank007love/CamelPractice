package org.tonylin.practice.camel.util;

import java.util.Scanner;

public class Pauser {

	private static Pauser instance = new Pauser();
	
	private Scanner scanner;
	
	private Pauser() {
		scanner = new Scanner(System.in);
	}
	
	public static Pauser get() {
		return instance;
	}
	
	public void execute() {
		scanner.next();
	}
}
