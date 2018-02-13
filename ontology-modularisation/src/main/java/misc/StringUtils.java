package misc;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

/**
 * This class provides helper-methods such as text processing, 
 * @author audunvennesland
 *
 */
public class StringUtils {

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();


	/**
	 * Takes a string as input and returns an arraylist of tokens from this string
	 * @param s: the input string to tokenize
	 * @param lowercase: if the output tokens should be lowercased
	 * @return an ArrayList of tokens
	 */
	public static ArrayList<String> tokenize(String s, boolean lowercase) {
		if (s == null) {
			return null;
		}

		ArrayList<String> strings = new ArrayList<String>();

		String current = "";
		Character prevC = 'x';

		for (Character c: s.toCharArray()) {

			if ((Character.isLowerCase(prevC) && Character.isUpperCase(c)) || 
					c == '_' || c == '-' || c == ' ' || c == '/' || c == '\\' || c == '>') {

				current = current.trim();

				if (current.length() > 0) {
					if (lowercase) 
						strings.add(current.toLowerCase());
					else
						strings.add(current);
				}

				current = "";
			}

			if (c != '_' && c != '-' && c != '/' && c != '\\' && c != '>') {
				current += c;
				prevC = c;
			}
		}

		current = current.trim();

		if (current.length() > 0) {
			if (!(current.length() > 4 && Character.isDigit(current.charAt(0)) && 
					Character.isDigit(current.charAt(current.length()-1)))) {
				strings.add(current.toLowerCase());
			}
		}

		return strings;
	}

	/**
	 * Returns a string of tokens
	 * @param s: the input string to be tokenized
	 * @param lowercase: whether the output tokens should be in lowercase
	 * @return a string of tokens from the input string
	 */
	public static String stringTokenize(String s, boolean lowercase) {
		String result = "";

		ArrayList<String> tokens = tokenize(s, lowercase);
		for (String token: tokens) {
			result += token + " ";
		}

		return result.trim();
	}


	/**
	 * Removes prefix from property names (e.g. hasCar is transformed to car)
	 * @param s: the input property name to be 
	 * @return a string without any prefix
	 */
	public static String stripPrefix(String s) {

		if (s.startsWith("has")) {
			s = s.replaceAll("^has", "");
		} else if (s.startsWith("is")) {
			s = s.replaceAll("^is", "");
		} else if (s.startsWith("is_a_")) {
			s = s.replaceAll("^is_a_", "");
		} else if (s.startsWith("has_a_")) {
			s = s.replaceAll("^has_a_", "");
		} else if (s.startsWith("was_a_")) {
			s = s.replaceAll("^was_a_", "");
		} else if (s.endsWith("By")) {
			s = s.replaceAll("By", "");
		} else if (s.endsWith("_by")) {
			s = s.replaceAll("_by^", "");
		} else if (s.endsWith("_in")) {
			s = s.replaceAll("_in^", "");
		} else if (s.endsWith("_at")) {
			s = s.replaceAll("_at^", "");
		}
		s = s.replaceAll("_", " ");
		s = stringTokenize(s,true);

		return s;
	}
	
	//line1 = line1.replace("\"", "");
	public static String removeSymbols(String s) {
		s = s.replace("\"", "");
		s = s.replace(".", "");
		s = s.replace("@en", "");
		
		return s;
	}

	/**
	 * Takes a filename as input and removes the IRI prefix from this file
	 * @param fileName
	 * @return filename - without IRI
	 */
	public static String stripPath(String fileName) {
		String trimmedPath = fileName.substring(fileName.lastIndexOf("/") + 1);
		return trimmedPath;

	}

	/**
	 * Takes a string as input, tokenizes it, and removes stopwords from this string
	 * @param analyzer
	 * @param str
	 * @return results - as a string of tokens, without stopwords
	 */
	public static String tokenize(Analyzer analyzer, String str) {
		String result = null;
		StringBuilder sb = new StringBuilder();

		try {
			TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
			stream.reset();
			while (stream.incrementToken()) {
				sb.append(stream.getAttribute(CharTermAttribute.class).toString());
				sb.append(" ");
			}
			stream.close();
		} catch (IOException e) {

			throw new RuntimeException(e);
		}


		result = sb.toString();
		return result;
	}


	/**
	 * Returns the label from on ontology concept without any prefix
	 * @param label: an input label with a prefix (e.g. an IRI prefix) 
	 * @return a label without any prefix
	 */
	public static String getString(String label) {

		if (label.contains("#")) {
			label = label.substring(label.indexOf('#')+1);
			return label;
		}

		if (label.contains("/")) {
			label = label.substring(label.lastIndexOf('/')+1);
			return label;
		}

		return label;
	}

	/**
	 * Removes underscores from a string (replaces underscores with "no space")
	 * @param input: string with an underscore
	 * @return string without any underscores
	 */
	public static String replaceUnderscore (String input) {
		String newString = null;
		Pattern p = Pattern.compile( "_([a-zA-Z])" );
		Matcher m = p.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase());
		}

		m.appendTail(sb);
		newString = sb.toString();

		return newString;
	}

	/**
	 * Checks if an input string is an abbreviation (by checking if there are two consecutive uppercased letters in the string)
	 * @param s input string
	 * @return boolean stating whether the input string represents an abbreviation
	 */
	public static boolean isAbbreviation(String s) {

		boolean isAbbreviation = false;

		int counter = 0;

		//iterate through the string
		for (int i=0; i<s.length(); i++) {

			if (Character.isUpperCase(s.charAt(i))) {
				counter++;
			}
			if (counter > 2) {
				isAbbreviation = true;
			} else {
				isAbbreviation = false;
			}
		} 

		return isAbbreviation;
	}

	/**
	 * Returns the names of the ontology from the full file path (including owl or rdf suffixes)
	 * @param ontology name without suffix
	 * @return
	 */
	public static String stripOntologyName(String fileName) {

		String trimmedPath = fileName.substring(fileName.lastIndexOf("/") + 1);
		String owl = ".owl";
		String rdf = ".rdf";
		String stripped = null;

		if (fileName.endsWith(".owl")) {
			stripped = trimmedPath.substring(0, trimmedPath.indexOf(owl));
		} else {
			stripped = trimmedPath.substring(0, trimmedPath.indexOf(rdf));
		}

		return stripped;
	}


	
	/**
	 * Removes typical stopwords from a string representation. Uses the stopword library of Lucene. 
	 * @param inputText
	 * @return
	 * @throws IOException
	 */
	public static String removeStopWordsFromString(String inputText) throws IOException {

		StringBuilder tokens = new StringBuilder();


		Analyzer analyzer = new StopAnalyzer(Version.LUCENE_36);
		TokenStream tokenStream = analyzer.tokenStream(
				LuceneConstants.CONTENTS, new StringReader(inputText));
		TermAttribute term = tokenStream.addAttribute(TermAttribute.class);
		while(tokenStream.incrementToken()) {
			tokens.append(term + " ");
		}

		String tokenizedText = tokens.toString();
		return tokenizedText;

	}





}
