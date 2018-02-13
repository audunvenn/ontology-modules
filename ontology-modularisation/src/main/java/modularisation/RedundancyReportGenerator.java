package modularisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;

import misc.ISubMatcher;
import fr.inrialpes.exmo.align.cli.GroupEval;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import misc.StringUtils;

/**
 * The RedundancyReportGenerator checks for redundancy (duplicate classes) in a set of modules. If there are duplicates, these are printed to screen and an alignment file including those duplicates is 
 * stored to "alignmentFolderName". 
 * @author audunvennesland
 * Feb 8, 2018
 */
public class RedundancyReportGenerator {

	static final double threshold = 1.0;
	static File outputAlignment = null;
	static File[] filesInDir = null;
	static final String prefix = "file:";
	static Properties params = new Properties();
	static PrintWriter writer = null;
	static String alignmentFileName = null;
	static String module1 = null;
	static String module2 = null;
	static AlignmentVisitor renderer = null;

	/**
	 * Test method
	 * @param args
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws AlignmentException 
	 */
	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException {

		String alignmentFolder = "./test-files/modules/output-alignments";
		String ontologyModules = "./test-files/modules/moduleNetworkWithoutOutliers";

		findDuplicates(alignmentFolder, ontologyModules);

	}

	/**
	 * Finds duplicate classes in a set of modules. 
	 * @param alignmentFolderName The folder to which alignments holding duplicate classes are stored in.
	 * @param ontologyModuleDir The folder holding modules to be checked for redundancy (duplicate classes)
	 * @throws AlignmentException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void findDuplicates(String alignmentFolderName, String ontologyModuleDir) throws AlignmentException, URISyntaxException, IOException {

		final File ontologyDir = new File(ontologyModuleDir);
		filesInDir = ontologyDir.listFiles();

		System.out.println("Running Redundancy Report Generator...");
		for (int i = 0; i < filesInDir.length; i++) {
			for (int j = i+1; j < filesInDir.length; j++) {

				module1 = StringUtils.stripOntologyName(filesInDir[i].toString());
				module2 = StringUtils.stripOntologyName(filesInDir[j].toString());

				if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {

					AlignmentProcess a = new ISubMatcher();
					a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
					params = new Properties();
					params.setProperty("", "");
					a.align((Alignment)null, params);

					BasicAlignment a2 = (BasicAlignment)(a.clone());
					a2.cut(threshold);

					int numDuplicates = a2.nbCells();

					//only produce alignments when there are duplicates
					if (numDuplicates > 0) {

						System.out.println("\n" + module1 + 
								" and " + module2 + " contain " + a2.nbCells() + " duplicates, and the duplicates are:");

						for (Cell c : a2) {
							System.out.println(c.getObject1() + " - " + c.getObject2());
						}

						//storing the alignment file
						alignmentFileName = alignmentFolderName + "/" + module1 + 
								"-" + module2 + ".rdf";

						outputAlignment = new File(alignmentFileName);

						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						renderer = new RDFRendererVisitor(writer);

						a2.render(renderer);
						writer.flush();
						writer.close();
					}

				}
			}
		}

		System.out.println("\nRedundancy Report Generator completed!");
	}

}