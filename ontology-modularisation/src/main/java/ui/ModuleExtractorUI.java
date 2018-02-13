package ui;

import java.util.Scanner;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import modularisation.ModuleExtractor;

/**
 * Command-line user interface for creating modules from a "monolithic" ontology. 
 * @author audunvennesland
 * 8. jan. 2018 
 */
public class ModuleExtractorUI {

	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Enter path to (monolithic) ontology file: ");
		String ontoFileName = scanner.next();
		
		System.out.print("Enter path to folder where the ontology module will be stored: ");
		String storageFolder = scanner.next();
		
		System.out.print("Enter name of ontology module to create: ");
		String moduleName = scanner.next();
		
		System.out.print("Enter signature to create module from: ");
		String seedSignature = scanner.next();
		
		ModuleExtractor.modularise(ontoFileName, moduleName, storageFolder, seedSignature);
		
		scanner.close();
	}
	
}
