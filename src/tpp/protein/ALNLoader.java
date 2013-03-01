package tpp.protein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.converters.FileSourcedConverter;

/**
 * Reads multiply aligned CLUSTAL .aln protein sequence files. For .aln format
 * see http://www.ebi.ac.uk/help/formats_frame.html.
 */
public class ALNLoader extends ProteinAlignmentLoader implements SequenceLoader, FileSourcedConverter {

	private boolean rp;

	/** Load sequences from a stream */
	public Vector<Protein> readSequences(InputStream in) throws IOException {
		// Load the sequences from the fasta file
		// System.out.println("Opening aln file: " + sequenceFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		// A hash of proteins, keyed by their name
		HashMap<String, Protein> hProteins = new HashMap<String, Protein>();
		Protein protein;
		String line, label, sequence;

		// Read the first line and confirm its a heading
		line = reader.readLine();
		if (!line.startsWith("CLUSTAL"))
			throw new IOException(".aln files must start with a CLUSTAL* heading");

		while (reader.ready()) {
			// For each line
			line = reader.readLine();

			// if it starts with a label, rather than whitespace, then extract
			// the label
			if (!line.trim().equals("") && !line.startsWith(" ")) {
				// convert tabs to spaces
				line = line.replace('\t', ' ');
				label = line.split(" ")[0];

				// if there is no existing protein with this label then create a
				// new one
				protein = hProteins.get(label);
				if (protein == null) {
					protein = new Protein();
					protein.setLabel(label);
					hProteins.put(label, protein);
				}

				// extract the sequence information from the remainder by
				// taking everything that follows the label, removing leading
				// whitespace.
				// NB we don't use string.split since the label may include some
				// regex characters
				sequence = line.substring(label.length()).trim();

				// and add this to the sequence for this protein.
				protein.appendSequence(sequence);

			}
		}
		return new Vector<Protein>(hProteins.values());
	}

	@Override
	public String getFileDescription() {
		return "CLUSTAL alignment";
	}

	@Override
	public String getFileExtension() {
		return ".aln";
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { ".aln" };
	}

	public String globalInfo() {
		String info;
		info= "Load data from a CLUSTAL (.aln) file containing a protein multiple sequence alignment and convert each sequence into a vector. If the AminoAcidProperty is set then this will be used to convert the amino acid at each position into a numeric value. If it is null then the position will be treated as a nominal attribute. If the classDelimiter is non-null then the functional class of each protein will be extracted from the label and converted into a class attribute: the character will be used to tokenize the label of each protein and the classPiece will be used to extract the relevant class identifier.\n For example, if classDelimiter=\"|\" and classPiece=2 then the protein labelled \">gi|XYZXYZ|2.1.1\" will be assigned to class \"2.1.1\" If askUser=true then a ProteinDialog box will be launched that will allow the user to specify the property, class delimiter and piece.see\n\n";
		info += getTechnicalInformation().toString();
		for (String option: getOptions()) info+="\n"+option;
		return info;
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
		result.setValue(Field.AUTHOR, "C.Haddow, M.Durrant, J.Perry and J.Faith");
		result.setValue(Field.YEAR, "2010");
		result.setValue(Field.TITLE,
				"Predicting Functional Residues of Protein Sequence Alignments as a Feature Selection Task");
		result.setValue(Field.JOURNAL, "International Journal of Data Mining and Bioinformatics");
		return result;
	}

}
