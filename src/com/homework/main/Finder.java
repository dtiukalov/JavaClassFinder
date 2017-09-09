package com.homework.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Finder{
	private String fileName;
	private String pattern;
	private final char spetialSimbol = '#';

	public Finder(String fileName, String pattern){
		if(fileName.trim().isEmpty()){
			throw new IllegalArgumentException("Illegal file name: '<filename>' \"" + fileName + "\"");
		}
		if(pattern.trim().isEmpty()){
			throw new IllegalArgumentException("Wrong pattern format: '<pattern>' \"" + pattern + "\"");
		}

		this.fileName = fileName;
		this.pattern = pattern;
	}

	public Map<String, ArrayList<String>> readFileAndGetClasses() throws IOException{
		String line, packadge, clas;
		Map<String, ArrayList<String>> classes = new TreeMap<>();
		FileReader fileReader = new FileReader(fileName) ;
		BufferedReader reader = new BufferedReader(fileReader);

		while((line = reader.readLine()) != null){
			if(!line.isEmpty()){
				if(line.contains(".")){
					int lastIndexOfDot = line.lastIndexOf(".");
					clas = line.substring(lastIndexOfDot + 1);
					packadge = line.substring(0, lastIndexOfDot + 1);
					ArrayList<String> packageArray;
					if(classes.containsKey(clas)){
						packageArray = classes.get(clas);
						packageArray.add(packadge);
					}else{
						packageArray = new ArrayList<String>();
						packageArray.add(packadge);
						classes.put(clas, packageArray);
					}
				}else{
					classes.put(line.trim(), new ArrayList<String>(0));
				}
			}
		}

		return classes;
	}

	public boolean hasUpperCase(String str){
		return !str.equals(str.toLowerCase());
	}

	public Map<String, ArrayList<String>> filter() throws IOException{
		Map<String, ArrayList<String>> map = readFileAndGetClasses();
		Map<String, ArrayList<String>> filteredMap = new TreeMap<>();
		boolean flag = false;

		Iterator it = map.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<String, ArrayList<String>> pair = (Map.Entry<String, ArrayList<String>>) it.next();
			String className = pair.getKey();
			flag = match(className);
			if(flag){
				filteredMap.put(className, pair.getValue());
			}
		}

		return filteredMap;
	}

	public String toWildCard() {
		StringBuilder wildCard = new StringBuilder();
		final boolean patternHasUpperCase = hasUpperCase(pattern);

		for(char p : pattern.toCharArray()){
			if(hasUpperCase(String.valueOf(p)) || !patternHasUpperCase){
				wildCard.append("*");
			}
			if(wildCard.length() == 0){
				wildCard.append("*");
			}
			if (p == '*'){
				wildCard.append(spetialSimbol);
			}
			if(p != ' '){
				wildCard.append(p);
			}
		}

		if(!pattern.endsWith(" ")){
			wildCard.append("*");
		}

		return wildCard.toString();
	}

	public boolean match(String className) {
		int wildCardStarPos = -1; // Last star position in wildcard
		int textPos = -1; // Position in text
		String wildCard = toWildCard();

		int j = 0;
		for(int i = 0; i < className.length() && j < wildCard.length(); ){
			char cn = className.charAt(i);
			char wc = wildCard.charAt(j);
			if(!hasUpperCase(pattern)){
				cn = String.valueOf(cn).toUpperCase().charAt(0);
				wc = String.valueOf(wc).toUpperCase().charAt(0);
			}
			if(i < className.length() - 1 && j == wildCard.length() - 1 && wc != '*'){
				return false;
			}
			if(wc == spetialSimbol || wc == cn) {
				i++;
				j++;
			} else if( wc == '*'){
				wildCardStarPos = j;
				textPos = i;
				j++;
			} else if(wildCardStarPos != -1){
				j = wildCardStarPos + 1;
				i = textPos + 1;
				textPos++;
			} else{
				return false;
			}
		}
		// Skip all stars
		while(j < wildCard.length() && wildCard.charAt(j) == '*') {
			j++;
		}

		return j == wildCard.length();
	}

	public void printFound() throws IOException{
		Map<String, ArrayList<String>> map = filter();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> pair = (Map.Entry<String, ArrayList<String>>) it.next();
			ArrayList<String> packageArray = pair.getValue();
			if(packageArray.size() > 0){
				Collections.sort(packageArray);
				for(String str : packageArray){
					System.out.println(str + pair.getKey());
				}
			}else{
				System.out.println(pair.getKey());
			}
		}
	}
}
