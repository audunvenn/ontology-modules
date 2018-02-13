package misc;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

/**
 * This string matcher implements the iSub string matching algorithm written by Stolios et al in the paper "A String Metric for Ontology Alignment".
 * @author audunvennesland
 * 2. feb. 2017
 */
public class ISubMatcher extends ObjectAlignment implements AlignmentProcess {

	ISub isubMatcher = new ISub();

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", iSubScore(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * This method returns a measure computed from two input OWL entities (processed as strings) using iSub algorithm (Stolios et al, 2005)
	 * @param o1 object representing an OWL entitiy
	 * @param o2 object representing an OWL entitiy
	 * @return a similarity scored computed from the ISub algorithm
	 * @throws OntowrapException
	 */
	public double iSubScore(Object o1, Object o2) throws OntowrapException {

		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();

		double measure = isubMatcher.score(s1, s2);

		return measure;

	}

	

}


