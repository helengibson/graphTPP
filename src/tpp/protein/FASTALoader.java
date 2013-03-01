package tpp.protein;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.converters.FileSourcedConverter;

/**
 * Reads sequences from multiply aligned .fasta protein sequence files. For
 * .fasta format see http://en.wikipedia.org/wiki/Fasta_format.
 */
public class FASTALoader extends ProteinAlignmentLoader implements SequenceLoader, FileSourcedConverter {

	private boolean rp;

	/** Load sequences from a stream */
	public Vector<Protein> readSequences(InputStream in) throws IOException {
		// Load the sequences from the fasta file
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Vector<Protein> proteins = new Vector<Protein>();

		String line = "";
		Protein protein = new Protein();
		while (reader.ready()) {
			line = reader.readLine();

			// A new protein
			if (line.startsWith(">")) {
				protein = new Protein();
				// System.out.println("Next protein: " + line.substring(1));
				protein.setLabel(line.substring(1));
				proteins.add(protein);
			} else

			// part of an existing sequence
			if (!line.trim().equals(""))
				protein.appendSequence(line.trim());
		}
		return proteins;
	}

	@Override
	public String getFileDescription() {
		return "FASTA alignment";
	}

	@Override
	public String getFileExtension() {
		return ".fasta";
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { ".fasta", ".fa" };
	}

	@Override
	public boolean getUseRelativePath() {
		return rp;
	}

	@Override
	public File retrieveFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFile(File file) throws IOException {
		this.file = file;
		setSource(file);

	}

	@Override
	public void setUseRelativePath(boolean rp) {
		this.rp = rp;
	}

	public String globalInfo() {
		String info;
		info= "Load data from a FASTA file containing a protein multiple sequence alignment and convert each sequence into a vector. If the AminoAcidProperty is set then this will be used to convert the amino acid at each position into a numeric value. If it is null then the position will be treated as a nominal attribute. If the classDelimiter is non-null then the functional class of each protein will be extracted from the label and converted into a class attribute: the character will be used to tokenize the label of each protein and the classPiece will be used to extract the relevant class identifier.\n For example, if classDelimiter=\"|\" and classPiece=2 then the protein labelled \">gi|XYZXYZ|2.1.1\" will be assigned to class \"2.1.1\" If askUser=true then a ProteinDialog box will be launched that will allow the user to specify the property, class delimiter and piece.see\n\n";
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
		result.setValue(Field.AUTHOR, "Faith,J");
		result.setValue(Field.YEAR, "2010");
		result.setValue(Field.TITLE,
				"Predicting Functional Residues of Protein Sequence Alignments as a Feature Selection Task");
		result.setValue(Field.JOURNAL, "International Journal of Data Mining and Bioinformatics");
		return result;
	}

}
