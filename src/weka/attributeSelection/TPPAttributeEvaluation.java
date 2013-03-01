package weka.attributeSelection;

import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/** A dummy attribute evaluator to be used by TPPAttributeSearch */
public class TPPAttributeEvaluation extends ASEvaluation implements TechnicalInformationHandler {

	@Override
	public void buildEvaluator(Instances data) throws Exception {
	}
	
	public int[] postProcess(int[] a){
		return a;
	}


	public String globalInfo() {
		return "A dummy attribute evaluation method to be used with TPPAttributeSearch.";
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "C.Haddow, J.Perry, M.Durrant, J.Faith");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.JOURNAL, "International Journal of Data Mining in Bioinformatics");
		result.setValue(Field.TITLE,
				"Predicting Functional Residues of Protein Sequence Alignments as a Feature Selection Task");
		return result;
	}

}
