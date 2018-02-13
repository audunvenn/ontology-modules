package misc;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;


public class StringUtils {

	//private static OWLAxiomIndex ontology;
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

	// ***Methods not in use***

	/*	*//**
	 * Takes as input a Set of strings along with a separator (usually whitespace) and uses StringBuilder to create a string from the Set.
	 * @param set
	 * @param sep
	 * @return result
	 *//*
	public static String join(Set<String> set, String sep) {
		String result = null;
		if(set != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<String> it = set.iterator();
			if(it.hasNext()) {
				sb.append(it.next());
			}
			while(it.hasNext()) {
				sb.append(sep).append(it.next());
			}
			result = sb.toString();
		}
		return result;
	}*/

	/*	*//**
	 * Takes as input a String and produces an array of Strings from this String
	 * @param s
	 * @return result
	 *//*
	public static String[] split(String s) {
		String[] result = s.split(" ");

		return result;
	}*/

	/*	*//**
	 * Takes as input two arrays of String and compares each string in one array with each string in the other array if they are equal
	 * @param s1
	 * @param s2
	 * @return results - basically an iterator that counts the number of equal strings in the two arrays
	 *//*
	public static int commonWords(String[] s1, String[] s2) {

		int results = 0;

		for (int i = 0; i < s1.length; i++) {
			for (int j = 0; j < s2.length; j++) {
				if (s1[i].equals(s2[j])) {
					results++;
				}
			}
		}

		return results;
	}*/

	/*	public static String removeDuplicates(String s) {

		return new LinkedHashSet<String>(Arrays.asList(s.split(" "))).toString().replaceAll("(^\\[|\\]$)", "").replace(", ", " ");


	}*/

	/*public static String getString(OWLEntity e, OWLOntology ontology) {

		String label = e.getIRI().toString();

		if (label.contains("#")) {
			label = label.substring(label.indexOf('#')+1);
			return label;
		}

		if (label.contains("/")) {
			label = label.substring(label.lastIndexOf('/')+1);
			return label;
		}

		Set<OWLAnnotation> labels = e.getAnnotations(ontology);
		//.getAnnotationPropertiesInSignature();

		if (labels != null && labels.size() > 0) {
			label = ((OWLAnnotation) labels.toArray()[0]).getValue().toString();
			if (label.startsWith("\"")) {
				label = label.substring(1);
			}

			if (label.contains("\"")) {
				label = label.substring(0, label.lastIndexOf('"'));
			}
		}

		return label;
	}*/

	public static void main(String args[]) {
		String testString = "motionPicture";
		String experiment = "biblio-bibo";

		System.out.println(tokenize(testString, true));

		String onto1 = experiment.substring(0, experiment.lastIndexOf("-"));
		String onto2 = experiment.substring(experiment.lastIndexOf("-")+1, experiment.length());
		System.out.println(onto1);

		System.out.println(onto2);

		String test = "academicArticle";

		String newString = stringTokenize(test, false);

		System.out.println("Original string: " + test + ", tokenized string: " + newString);

		String prop = "hasCar";
		System.out.println("Without prefix the property name is " + stripPrefix(prop));

		String s = "Testing underscore";
		System.out.println("Without underscore: " + replaceUnderscore(s));

	}



}
