package modularisation;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;

/**
 * This class provides functionality for establishing dependencies between ontology modules in an ontology network (of modules). 
 * @author audunvennesland
 * 10. jan. 2018 
 */
public class ModuleNetworkDependencyManager {

	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();


	/**
	 * This method first establishes a list of classes (outlier classes) originally held by other ontologies (modules), then the method identifies which ontology is responsible for such an outlier class, before it automatically imports this ontology.
	 * @param ontologyModule The ontology module for which dependencies will be listed and imported
	 * @param ontologyModuleFolder The folder holding the full set of ontology modules for this ontology network
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 */
	public static void createDependency(String ontologyModule, String ontologyModuleNetworkFolder) throws OWLOntologyCreationException, OWLOntologyStorageException {

		//load the ontology module in which dependencies with other modules will be created
		File ontologyModuleFile = new File(ontologyModule);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		

		AutoIRIMapper mapper=new AutoIRIMapper(new File(ontologyModuleNetworkFolder), false);
		manager.addIRIMapper(mapper);

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontologyModuleFile);

		//find the set of outlier classes for the ontology module
		Set<OWLClass> outliersSet = findOutliers(onto);

		//get all classes from all other ontology modules in folder
		File fileDir = new File(ontologyModuleNetworkFolder);		
		Map<String, String> allOtherClasses = getAllOtherClasses(ontologyModuleFile, fileDir);		

		//find ontologies to import by iterating all outlier classes and searching for similar class in map of all classes from all ontologies
		//use a set to avoid duplicate entries (ontologies)
		Set<String> ontologiesToImport = new HashSet<String>();

		Set<String> classesOutsideOfScope = new HashSet<String>();

		for (OWLClass oc : outliersSet) {
			if (allOtherClasses.containsKey(oc.getIRI().getFragment())) {
				ontologiesToImport.add(allOtherClasses.get(oc.getIRI().getFragment()));
			} else {
				classesOutsideOfScope.add(oc.getIRI().getFragment());
			}
		}

		//automatically declare relevant imports
		for (String s : ontologiesToImport) {
			declareImportStatements(ontologyModuleFile, s, fileDir);
			System.out.println("\nDeclaring import for documentIRI:" + ontologyModuleFile + " and " + " ontologyIRI " + s);
		}
		
		System.out.println("Ontology module saved with imports!");
		
		//print the classes residing in ontologies outside of our ontology network (i.e. ontologies not in the "ontologyModuleNetworkFolder")
		System.out.println("\n*** Classes outside of the defined ontology network: ");
		for (String s : classesOutsideOfScope) {
			System.out.println(s);
		}
	}



	/**
	 * Retrieves the set of outlier classes for an input ontology and returns them in a set
	 * @param inputOntology The ontology for which a set of outlier classes are retrieved
	 * @return A set<OWLClass> of outlier classes
	 * @throws OWLOntologyCreationException
	 */
	private static Set<OWLClass> findOutliers (OWLOntology inputOntology) throws OWLOntologyCreationException {

		//find the main owl superclass of this ontology (the only strict subclass of owl:thing that also has asserted subclasses associated with it)
		Set<OWLClass> allCls = inputOntology.getClassesInSignature();

		//create a list of outlier classes (all classes having owl:thing as direct superclass and not having a class name starting with "_")
		Set<OWLClass> outlierList = new HashSet<OWLClass>();

		OWLReasoner reasoner = reasonerFactory.createReasoner(inputOntology);

		//owl:thing
		Node<OWLClass> topClassNode = reasoner.getTopClassNode();

		NodeSet<OWLClass> superClasses = null;

		for (OWLClass cls : allCls) {

			//need to use the reasoner to get the superclasses otherwise owl:thing is not included in the set
			superClasses = reasoner.getSuperClasses((OWLClassExpression) cls, true);

			//all main owl superclasses begins with _ and this has to be excluded from the list of outlier classes
			boolean mainClass = cls.getIRI().getFragment().startsWith("_");

			Set<OWLClass> superCls = superClasses.getFlattened();

			//if the direct superclass is owl:thing and the first digit in the class name is not '_', the class is added to the list of outliers
			if (superCls.contains(topClassNode.getRepresentativeElement()) && !mainClass) {
				outlierList.add(cls);
			}

		}

		return outlierList;

	}



	/**
	 * Automatically creates import declarations from the input ontology to the ontologies being responsible for a set of outlier classes. 
	 * @param inputOntology The OWL ontology in which import statements are declared.
	 * @param importedOntology 
	 * @param ontologyModuleNetworkFolder
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 */
	private static void declareImportStatements (File inputOntology, String importedOntology, File ontologyModuleNetworkFolder) throws OWLOntologyCreationException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		

		AutoIRIMapper mapper=new AutoIRIMapper(ontologyModuleNetworkFolder, false);
		manager.addIRIMapper(mapper);

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(inputOntology);

		Set<String> imports = getImports(inputOntology, ontologyModuleNetworkFolder);

		OWLDataFactory factory = null;
		OWLImportsDeclaration importDeclaration = null;
		IRI importIRI = null;

		for (String s : imports) {

			importIRI = IRI.create(s);

			factory = manager.getOWLDataFactory();

			importDeclaration = factory.getOWLImportsDeclaration(importIRI);

			AddImport addImport = new AddImport(onto, importDeclaration);

			manager.applyChange(addImport);

		}

		manager.saveOntology(onto);

	}

	/**
	 * Returns a map of documentIRI and ontologyIRI for ontologies that should be imported by a single input ontology module
	 * @param ontologyModuleFile
	 * @param ontologyModuleNetworkFolder
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	private static Set<String> getImports (File ontologyModuleFile, File ontologyModuleNetworkFolder) throws OWLOntologyCreationException {
		Set<String> ontologiesToImport = new HashSet<String>();
		//load ontology module
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	

		//ensure that the imported ontologies are in the right folder
		AutoIRIMapper mapper=new AutoIRIMapper(ontologyModuleNetworkFolder, false);
		manager.addIRIMapper(mapper);

		//load the ontology module
		OWLOntology ontologyModule = manager.loadOntologyFromOntologyDocument(ontologyModuleFile);

		//get outlier classes for the ontology module
		Set<OWLClass> outliersSet = findOutliers(ontologyModule);

		//get all classes from other ontology modules in network
		Map<String, String> allOtherClasses = getAllOtherClasses(ontologyModuleFile, ontologyModuleNetworkFolder);

		for (OWLClass oc : outliersSet) {
			if (allOtherClasses.containsKey(oc.getIRI().getFragment())) {
				ontologiesToImport.add(allOtherClasses.get(oc.getIRI().getFragment()));
			}
		}

		return ontologiesToImport;
	}



	/**
	 * Retrieves all OWL classes from all ontologies in the ontology network except for the ontology module provided as parameter.
	 * @param moduleFile The owl file for the ontology module
	 * @param ontologyModuleNetworkFolder The folder holding all ontology modules in the ontology network
	 * @return A map where the key is the class name and the value is the ontology responsible for this class
	 * @throws OWLOntologyCreationException
	 */
	private static Map<String, String> getAllOtherClasses (File moduleFile, File ontologyModuleNetworkFolder) throws OWLOntologyCreationException {

		Map<String, String> allOtherClasses = new HashMap<String, String>();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	

		File[] filesInDir = ontologyModuleNetworkFolder.listFiles();

		OWLOntology ontology = null;
		Set<OWLClass> classes = null;
		Set<OWLClass> outliers = null;

		//for every ontology file in folder...
		for (int i = 0; i < filesInDir.length; i++) {

			if (filesInDir[i].compareTo(moduleFile) != 0) {

				manager = OWLManager.createOWLOntologyManager();

				//establishes that the source folder of the modules to be imported is the "moduleFolder"
				OWLOntologyIRIMapper autoIRIMapper = new AutoIRIMapper(ontologyModuleNetworkFolder, false);
				manager.addIRIMapper(autoIRIMapper);

				//need to create a new manager to avoid "OntologyID" already exists exception
				ontology = manager.loadOntologyFromOntologyDocument(filesInDir[i]);
				classes = ontology.getClassesInSignature();
				outliers = findOutliers(ontology);

				//removing all outlier classes from classes of each ontology in the folder
				classes.removeAll(outliers);

				//put all classes
				for (OWLClass cls : classes) {
					allOtherClasses.put(cls.getIRI().getFragment(), ontology.getOntologyID().getOntologyIRI().toString());
				}			
			}
		}		
		return allOtherClasses;
	}



}
