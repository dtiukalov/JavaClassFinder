package com.homework.test;

import com.homework.main.ClassFinder;
import com.homework.main.Finder;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

public class FinderTest extends Assert{
	private final String fileName = "c:/Download/java/homework/classes.txt";

	/*
	Search pattern `<pattern>` must include class name camelcase upper case letters
	in the right order and it may contain lower case letters to narrow down the search results,
	for example `'FB'`, `'FoBa'` and `'FBar'` searches must all match
	`a.b.FooBarBaz` and `c.d.FooBar` classes.
	*/
	@Test
	public void testSearchPattern() throws NoSuchMethodException{
		Finder finder = new Finder(fileName,"FB");
		assertEquals("*F*B*", finder.toWildCard());
		finder = new Finder(fileName,"FoBa");
		assertTrue(finder.match("a.b.FooBarBaz"));
		assertTrue(finder.match("a.b.FoBarBaz"));
		assertTrue(finder.match("c.d.FooBar"));
		assertTrue(finder.match("c.d.FooBa"));
		assertFalse(finder.match("c.d.FaoBar"));
		assertFalse(finder.match("c.FooCar"));
		finder = new Finder(fileName,"FBar");
		assertTrue(finder.match("a.b.FooBarBaz"));
		assertTrue(finder.match("c.d.FooBar"));
		assertFalse(finder.match("c.FooCar"));
		assertEquals("*F*Bar*", finder.toWildCard());

		finder = new Finder(fileName,"MiRe");
		assertTrue(finder.match("MindReader"));
		assertTrue(finder.match("java.util.MindReader"));
		assertTrue(finder.match("ConcurrentMindReader"));
		assertTrue(finder.match("java.util.concurrent.ConcurrentMindReader"));
		assertFalse(finder.match("java.util.concurrent.MandReader"));
		assertFalse(finder.match("java.util.ConcurrentMindRar"));

		finder = new Finder(fileName,"Wish");
		assertTrue(finder.match("codeborne.WishMaker"));
	}

	/*
	Upper case letters written in the wrong order will not find any results, for example
	`'BF'` will not find `c.d.FooBar`.
	*/
	@Test
	public void testUpperCaseLettersInWrongOrder() {
		Finder finder = new Finder(fileName,"BF");
		assertFalse(finder.match("c.d.FooBar"));
		assertTrue(finder.match("c.d.BF"));
		assertTrue(finder.match("c.d.SomeBarFoo"));
	}

	/*
	 If the search pattern consists of only lower case characters then the search becomes
	case insensitive (`'fbb'` finds `FooBarBaz` but `'fBb'` will not).
	*/
	@Test
	public void testCaseSensitive() {
		Finder finder = new Finder(fileName,"fbb");
		assertFalse(finder.hasUpperCase("fbb"));
		assertTrue(finder.match("FooBarBaz"));

		finder = new Finder(fileName,"fb");
		assertFalse(finder.hasUpperCase("fb"));
		assertTrue(finder.match("FooBarBaz"));

		finder = new Finder(fileName,"fBb");
		assertTrue(finder.hasUpperCase("fBb"));
		assertFalse(finder.match("FooBarBaz"));

		finder = new Finder(fileName,"list");
		assertTrue(finder.match("List"));

		finder = new Finder(fileName,"hama");
		assertFalse(finder.hasUpperCase("hama"));
		assertTrue(finder.match("HashMap"));
	}

	/*
	If the search pattern ends with a space `' '` then the last word in the pattern must
	also be the last word of the found class name (`'FBar '` finds `FooBar` but not `FooBarBaz`).
	*/
	@Test
	public void testPatternEndsWithSpaceLastWordMustMatch() {
		Finder finder = new Finder(fileName,"FBar ");
		assertEquals("*F*Bar", finder.toWildCard());
		assertTrue(finder.match("FooBar"));
		assertFalse(finder.match("FooBarBaz"));
		assertTrue(finder.match("java.util.FooBar"));
	}

	/*
	The search pattern may include wildcard characters `'*'` which match missing letters
	(`'B*rBaz'` finds `FooBarBaz`i but `BrBaz` does not).
	it test for var "spetialSimbol"
	*/
	@Test
	public void testWildcardAsteriskMatchMissingLetters() {
		Finder finder = new Finder(fileName,"B*rBaz");
		assertTrue(finder.match("FooBarBaz"));
		assertFalse(finder.match("BrBaz"));

		finder = new Finder(fileName,"B*rB*z");
		assertTrue(finder.match("FooBarBaz"));
		assertFalse(finder.match("BarBz"));

		finder = new Finder(fileName,"u*l.Hash");
		assertTrue(finder.match("util.Hash"));
		assertTrue(finder.match("java.util.Hash"));
		assertFalse(finder.match("til.Hash"));
		assertFalse(finder.match("ul.Hash"));

		finder = new Finder(fileName,"f*tRCl");
		assertTrue(finder.match("firstRichClient"));
	}

	@Test
	public void testMissedLettersInPattern() {
		Finder finder = new Finder(fileName,"FilBuider");
		assertFalse(finder.match("FileBuilder"));
		assertTrue(finder.match("FileBuider"));

		finder = new Finder(fileName,"ConcHhMap");
		assertFalse(finder.match("ConcurrentHashMap"));
	}

	@Test
	public void testIfWrongFileNameFormatThrowsException() throws IOException {
		String[] FileNames = {"", " "};
		for (String fileName : FileNames) {
			try {
				new Finder(fileName,"\"FooBarBaz\"");
				fail("Expected IllegalArgumentException for \"" + fileName + "\"");
			} catch (IllegalArgumentException ex) {
				assertEquals("Illegal file name: '<filename>' \"" + fileName + "\"", ex.getMessage());
			}
		}
	}

	@Test
	public void testIfWrongPatternFormatThrowsException() {
		String[] wrongPatterns = {"", " "};
		for (String pattern : wrongPatterns) {
			try {
				new Finder(fileName,pattern);
				fail("Expected IllegalArgumentException for \"" + pattern + "\"");
			} catch (IllegalArgumentException ex) {
				assertEquals("Wrong pattern format: '<pattern>' \"" + pattern + "\"", ex.getMessage());
			}
		}
	}

	@Test
	public void testSplitFullClassName() throws IOException{
		Finder finder = new Finder(fileName,"WishMaker");
		Map<String, ArrayList<String>> map = finder.filter();
		Iterator it = map.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> pair = (Map.Entry<String, ArrayList<String>>) it.next();
			ArrayList<String> packageArray = pair.getValue();
			if(packageArray.size() > 0){
				for(String str : packageArray){
					assertEquals("codeborne.", str);
				}
			}
		}
	}

	@Test
	public void testSplitClassNameWithoutPackage() throws IOException{
		Finder finder = new Finder(fileName,"TelephoneOperator");
		Map<String, ArrayList<String>> map = finder.filter();
		Iterator it = map.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> pair = (Map.Entry<String, ArrayList<String>>) it.next();
			ArrayList<String> packageArray = pair.getValue();
			if(packageArray.size() > 0){
				for(String str : packageArray){
					assertEquals("", str);
				}
			}
		}
	}

	@Test
	public void testToWildCardTransformation() {
		Finder finder = new Finder(fileName,"FB");
		assertEquals("*F*B*", finder.toWildCard());
		finder = new Finder(fileName,"geeks");
		assertEquals("*g*e*e*k*s*", finder.toWildCard());
		finder = new Finder(fileName,"pQrst");
		assertEquals("*p*Qrst*", finder.toWildCard());
		finder = new Finder(fileName,"ab*cd");
		assertEquals("*a*b*#**c*d*", finder.toWildCard());
		finder = new Finder(fileName,"ab*Cd");
		assertEquals("*ab#**Cd*", finder.toWildCard());
		finder = new Finder(fileName,"ab*Cd ");
		assertEquals("*ab#**Cd", finder.toWildCard());
		finder = new Finder(fileName,"FoBa ");
		assertEquals("*Fo*Ba", finder.toWildCard());
		finder = new Finder(fileName,"p*Qr************st ");
		assertEquals("*p#**Qr#*#*#*#*#*#*#*#*#*#*#*#*st", finder.toWildCard());
	}

}