package ui;

import java.util.Scanner;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import modularisation.ModuleNetworkDependencyManager;

/**
 * This class provides a command-line user interface for interacting with the ModuleNetworkDependencyManager. 
 * @author audunvennesland
 *
 */
public class ModuleNetworkDependencyManagerUI {
	
	/**
	 * 
	 * @param args The arguments (parameters) are ontologyModule (the path to the ontology module) and ontologyModuleNetworkFolder (the path to the folder holding all ontology modules within our ontology network)
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Enter path to ontology module for which dependencies will be resolved: ");
		String ontologyModule = scanner.next();
		
		System.out.print("Enter path to folder where the modules in the network resides: ");
		String ontologyModuleNetworkFolder = scanner.next();

		ModuleNetworkDependencyManager.createDependency(ontologyModule, ontologyModuleNetworkFolder);
		
		scanner.close();
		
	}

}
