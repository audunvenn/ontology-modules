package ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import modularisation.RedundancyReportGenerator;

/**
 * Command line user interface class for the Redundancy Report Generator
 * @author audunvennesland
 * Feb 8, 2018 
 */
public class RedundancyReportUI {
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, AlignmentException, URISyntaxException, IOException {
				
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Enter path to folder holding the ontology modules: ");
		String ontologyModules = scanner.next();
		
		System.out.print("Enter path to folder where the alignments holding duplicate classes will be stored: ");
		String alignmentFolder = scanner.next();

		RedundancyReportGenerator.findDuplicates(alignmentFolder, ontologyModules);
		
		scanner.close();
	}

}
