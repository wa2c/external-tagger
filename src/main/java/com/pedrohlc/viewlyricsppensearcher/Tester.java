package com.pedrohlc.viewlyricsppensearcher;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//try_inverse();
		try {
			for(LyricInfo aresult : ViewLyricsSearcher.search("Foo Fighters", "", 2).getLyricsInfo()) //Artist, Title, Page
				System.out.println(aresult.dump());
		} catch (IOException | NoSuchAlgorithmException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
