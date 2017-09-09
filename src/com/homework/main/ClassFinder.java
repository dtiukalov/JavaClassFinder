package com.homework.main;

import java.io.IOException;

public class ClassFinder {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage: ./class-finder <filename> <pattern>");
			return;
		}

		Finder finder = new Finder(args[0], args[1]);
		try{
			finder.printFound();
		}catch(IOException e){
			e.printStackTrace();
		}

	}
}