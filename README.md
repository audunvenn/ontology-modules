# ontology-modules
The code in this repository provides functionality for extracting ontology modules from the AIRM (Air Traffic Management Reference Model) and evaluating the modules extracted afterwards. The code is developed in Java. In this current version the software should be considered proof-of-concept. 

Acknowledgements:
* The BEST project has received funding from the SESAR Joint Undertaking under grant agreement No 699298 under the European Union's Horizon 2020 research and innovation program. 
* The OWL API [1] provides the base functionality for the module extraction code. The functionality in OWL API has been extended with methods that also extract properties from the source AIRM ontology (automatically generated from UML).
* The Alignment API [2], which is a java API for ontology matching, is used for analysing redundancy among the extracted modules.
* More information about the theoretic principles of locality modules can be found at http://owl.cs.manchester.ac.uk/research/modularity/ that also contains pointers to research articles explaining the more detailed concepts of locality module extraction. 

Known issues:
- Some simple datatypes are not supported by the Hermit reasoner and have been converted to a supported datatype:
* xsd:duration is converted to xsd:string
* xsd:date is converted to xsd:string


1. M. Horridge and S. Bechhofer, "The OWL API: A Java API for OWL Ontologies," Semant. Web J., vol. 2, no. 1, pp. 11-21, 2011.
2. J. David, J. Euzenat, F. Scharffe, and C. T. Dos Santos, "The alignment API 4.0," Semant. Web, vol. 2, no. 1, pp. 3-10, 2011.
