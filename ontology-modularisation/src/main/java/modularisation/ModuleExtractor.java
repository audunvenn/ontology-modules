package modularisation;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * The CreateModule class contains methods for extracting modules from ontologies. A module is considered a logical subset of the ontology. This code is based on the OWL API functionality for extracting locality-based modules.
 * @author audunvennesland
 *
 */
public class ModuleExtractor {

	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

	/**
	 * Extracts a module from an OWL ontology given a "seed signature". 
	 * @param monoFileName The OWL ontology from which a module will be extracted.
	 * @param moduleName The name given to the module extracted.
	 * @param storageFolder The folder in which the extracted module will be placed.
	 * @param seedSignature The signature guiding the extraction of the module. A seed signature can be an OWL entity (class, object property, etc.).
	 * @throws OWLOntologyStorageException
	 * @throws OWLOntologyCreationException
	 */
	public static void modularise(String monoFileName, String moduleName, String storageFolder, String seedSignature) throws OWLOntologyStorageException, OWLOntologyCreationException {
		File ontoFile = new File(monoFileName);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology AIRM_onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		OWLDataFactory df = OWLManager.getOWLDataFactory();

		//create seed signature
		String seed = seedSignature;
		OWLClass cls = df.getOWLClass(IRI.create(AIRM_onto.getOntologyID().getOntologyIRI() + seed));

		Set<OWLEntity> sig = new HashSet<OWLEntity>();
		sig.add(cls);	

		// We now add all subclasses (direct and indirect) of the chosen classes. 
		Set<OWLEntity> seedSig = new HashSet<OWLEntity>();

		//using the Hermit reasoner
		Reasoner reasoner=new Reasoner(AIRM_onto);

		for (OWLEntity ent : sig) {
			seedSig.add(ent);

			if (OWLClass.class.isAssignableFrom(ent.getClass())) {

				NodeSet<OWLClass> subClasses = reasoner.getSubClasses((OWLClass) ent, false);
				seedSig.addAll(subClasses.getFlattened());

			}
		}



		//extract module according to locality-based modularisation from OLW API       
		SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(manager, AIRM_onto, ModuleType.STAR);
		File owl_file = new File("extracted_module" +moduleName+ ".owl");
		IRI tempIRI = IRI.create(owl_file.toURI());


		OWLOntology mod = sme.extractAsOntology(seedSig, tempIRI);		

		//save locality-based module
		manager.saveOntology(mod, tempIRI);

		Set<OWLClass> classes = mod.getClassesInSignature();
		Set<OWLEntity> opSet = new HashSet<OWLEntity>();

		//create an extended ontology module that also contains object properties, data properties and individuals associated with the classes
		//from the module
		File complete_owl_file = new File(storageFolder + "/" + moduleName + ".owl");
		IRI documentIRI = IRI.create(complete_owl_file.toURI());
		IRI ontologyIRI = IRI.create("http://www.project-best.eu/owl/airm-mod/" + moduleName + ".owl");

		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);

		manager.addIRIMapper(mapper);

		OWLOntology complete_ontology = manager.createOntology(ontologyIRI);

		//"clone" the intermediate ontology to the complete ontology
		manager.addAxioms(complete_ontology, mod.getAxioms());

		for (OWLClass c : classes) {

			//get object properties for which class c is the domain class
			Set<OWLEntity> objectPropSet = getObjectProperties(AIRM_onto, c);
			if (objectPropSet != null) {

				for (OWLEntity o : objectPropSet) {

					Set<OWLClassExpression> temp = o.asOWLObjectProperty().getDomains(AIRM_onto);
					for (OWLClassExpression oce : temp) {
						manager.applyChange(new AddAxiom(complete_ontology,df.getOWLObjectPropertyDomainAxiom(o.asOWLObjectProperty(), oce)));
					}

					Set<OWLClassExpression> temp2 = o.asOWLObjectProperty().getRanges(AIRM_onto);

					for (OWLClassExpression oce2 : temp2) {
						manager.applyChange(new AddAxiom(complete_ontology,df.getOWLObjectPropertyRangeAxiom(o.asOWLObjectProperty(), oce2)));
					}

				}
			}

			//get data properties for which class c is the domain class
			Set<OWLEntity> dataPropSet = getDataProperties(AIRM_onto, c);

			if (dataPropSet != null) {

				for (OWLEntity d : dataPropSet) {


					Set<OWLClassExpression> temp = d.asOWLDataProperty().getDomains(AIRM_onto);
					for (OWLClassExpression oce : temp) {

						manager.applyChange(new AddAxiom(complete_ontology,df.getOWLDataPropertyDomainAxiom(d.asOWLDataProperty(), oce)));
					}

					Set<OWLDataRange> temp2 = d.asOWLDataProperty().getRanges(AIRM_onto);

					for (OWLDataRange oce2 : temp2) {
						manager.applyChange(new AddAxiom(complete_ontology,df.getOWLDataPropertyRangeAxiom(d.asOWLDataProperty(), oce2)));
					}
				}
			}

		}

		//rename the IRI of all entities
		OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(complete_ontology));

		List<OWLOntologyChange> changeIRI = null;

		Set<OWLEntity> entities = complete_ontology.getSignature();
		for (OWLEntity e : entities) {
			String newIRIString = ontologyIRI.toString() + "#" + e.getIRI().getFragment();
			IRI newIRI = IRI.create(newIRIString);
			changeIRI = renamer.changeIRI(e, newIRI);
			manager.applyChanges(changeIRI);
		}


		//save completed module and print some metrics about the extracted module
		manager.saveOntology(complete_ontology, documentIRI);

		System.out.println("\n");
		System.out.println("Ontology created!");
		System.out.println("Number of classes: " + getNumClasses(complete_ontology));
		System.out.println("Number of object properties: " + getNumObjectProperties(complete_ontology));
		System.out.println("Number of data properties: " + getNumDataProperties(complete_ontology));
		System.out.println("Number of individuals: " + getNumIndividuals(complete_ontology));
		System.out.println("Number of axioms: " + getNumAxioms(complete_ontology));

	}


	/**
	 * Retrieves all object properties related to a class, that is, all object properties that has OWLClass cls as domain.
	 * @param onto The OWL ontology for which object properties will be retrieved
	 * @param cls The OWL class being the domain of the object properties retrieved
	 * @return A set of object properties (as OWL entities)
	 * @throws OWLOntologyCreationException
	 */
	private static Set<OWLEntity> getObjectProperties(OWLOntology onto, OWLClass cls) throws OWLOntologyCreationException{

		Set<OWLObjectProperty> opSet = new HashSet<OWLObjectProperty>();
		Set<OWLEntity> eSet = new HashSet<OWLEntity>();

		for (OWLObjectPropertyDomainAxiom op : onto.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {                        
			if (op.getDomain().equals(cls)) {   
				for(OWLObjectProperty oop : op.getObjectPropertiesInSignature()){
					opSet.add(oop);
				}
			}
		}

		eSet.addAll(opSet);

		return eSet;

	}

	/**
	 * Retrieves all data properties related to a class, that is, all data properties that has OWLClass cls as domain.
	 * @param onto The OWL ontology for which data properties will be retrieved
	 * @param cls The OWL class being the domain of the data properties retrieved
	 * @return A set of data properties (as OWL entities)
	 * @throws OWLOntologyCreationException
	 */
	private static Set<OWLEntity> getDataProperties(OWLOntology onto, OWLClass cls) throws OWLOntologyCreationException{

		Set<OWLDataProperty> dpSet = new HashSet<OWLDataProperty>(); 
		Set<OWLEntity> eSet = new HashSet<OWLEntity>();

		for (OWLDataPropertyDomainAxiom dp : onto.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
			if (dp.getDomain().equals(cls)) {   
				for(OWLDataProperty odp : dp.getDataPropertiesInSignature()){
					dpSet.add(odp);
				}
			}
		}

		eSet.addAll(dpSet);
		return eSet;

	}

	/**
	 * Get number of classes in an ontology
	 * @param ontoFile	the file path of the OWL ontology
	 * @return numClasses an integer stating how many OWL classes the OWL ontology has
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	private static int getNumClasses(OWLOntology onto) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		int numClasses = onto.getClassesInSignature().size();

		manager.removeOntology(onto);

		return numClasses;
	}

	/**
	 * Returns an integer stating how many object properties an OWL ontology has
	 * @param ontoFile	the file path of the input OWL ontology
	 * @return numObjectProperties an integer stating number of object properties in an OWL ontology
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	private static int getNumObjectProperties(OWLOntology onto) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		int numObjectProperties = onto.getObjectPropertiesInSignature().size();

		manager.removeOntology(onto);

		return numObjectProperties;
	}

	/**
	 * Returns the number of data properties in an ontology (a module) as an integer
	 * @param onto The OWL ontology for which the number of data properties is computed
	 * @return Number of data properties for a given OWL ontology (module)
	 * @throws OWLOntologyCreationException
	 */
	private static int getNumDataProperties(OWLOntology onto) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		int numDataProperties = onto.getDataPropertiesInSignature().size();

		manager.removeOntology(onto);

		return numDataProperties;
	}

	/**
	 * Returns an integer stating how many individuals an OWL ontology has
	 * @param ontoFile the file path of the input OWL ontology
	 * @return numIndividuals an integer stating number of individuals in an OWL ontology
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	private static int getNumIndividuals(OWLOntology onto) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		int numIndividuals = onto.getIndividualsInSignature().size();

		manager.removeOntology(onto);

		return numIndividuals;
	}

	/**
	 * Returns the number of axioms in an ontology (a module) as an integer
	 * @param onto The OWL ontology for which the number of axioms is computed
	 * @return Number of axioms for a given OWL ontology (module)
	 * @throws OWLOntologyCreationException
	 */
	private static int getNumAxioms(OWLOntology onto) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		int axioms = onto.getAxiomCount();

		manager.removeOntology(onto);

		return axioms;
	}

}
