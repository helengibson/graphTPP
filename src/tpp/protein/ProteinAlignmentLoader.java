package tpp.protein;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;

/**
 * Load data from a CLUSTAL (.aln) or FASTA (.fasta) file containing a protein
 * multiple sequence alignment and convert each sequence into a vector. If the
 * AminoAcidProperty is set then this will be used to convert the amino acid at
 * each position into a numeric value. If it is null then the position will be
 * treated as a nominal attribute. If the classDelimiter is non-null then the
 * functional class of each protein will be extracted from the label and
 * converted into a class attribute: the character will be used to tokenize the
 * label of each protein and the classPiece will be used to extract the relevant
 * class identifier.
 * <p>
 * For example, if classDelimiter="|" and classPiece=2 then the protein labelled
 * ">gi|XYZXYZ|2.1.1" will be assigned to class "2.1.1"
 * <p>
 * If askUser="true" then a ProteinDialog box will be launched that will allow
 * the user to specify the property, class delimiter and piece.
 */
public abstract class ProteinAlignmentLoader extends AbstractFileLoader {

	AminoAcidProperty property;

	String classDelimiter;

	int classPiece;

	private InputStream input;

	boolean askUser;

	private boolean rp;

	protected File file;

	public boolean isAskUser() {
		return askUser;
	}

	public void setAskUser(boolean askUser) {
		this.askUser = askUser;
	}

	public AminoAcidProperty getProperty() {
		return property;
	}

	public void setProperty(AminoAcidProperty property) {
		this.property = property;
	}

	public void setProperty(String name) {
		property = null;
		if (name == null || name == "")
			return;
		else
			for (AminoAcidProperty property : AminoAcidProperty.ALL_PROPERTIES)
				if (name.equals(property.getName()))
					this.property = property;
		if (property == null)
			throw new RuntimeException("Unrecognised propery name: " + name);

	}

	public String getClassDelimiter() {
		return classDelimiter;
	}

	public void setClassDelimiter(String classDelimiter) {
		this.classDelimiter = classDelimiter;
	}

	public int getClassPiece() {
		return classPiece;
	}

	public void setClassPiece(int classPiece) {
		this.classPiece = classPiece;
	}

	@Override
	public Instances getDataSet() throws IOException {
		Instances instances = null;
		try {
			Vector<Protein> proteins = readSequences(input);

			if (askUser || property == null) {
				ProteinDialog proteinDialog = new ProteinDialog(null, proteins.get(0).getLabel());
				classPiece = proteinDialog.getClassPiece();
				classDelimiter = proteinDialog.getClassDelimiter();
				property = proteinDialog.getSelectedProperty();
			}

			// Convert sequences

			if (property == null)
				throw new RuntimeException("property is null");

			// construct the attribute information from a protein
			String sequence = proteins.get(0).getSequence();
			FastVector attributes = new FastVector();

			// the name attribute
			// by passing in a null vector of values we are indicating that this
			// is
			// a string attribute rather than a nominal one
			FastVector nullVector = null;
			Attribute names = new Attribute("name", nullVector);
			attributes.addElement(names);

			// the property attributes
			Vector<Attribute> va = property.getAttributesForSequence(proteins.get(0).getSequence());
			for (Attribute a : va)
				attributes.addElement(a);

			// the class attribute
			Attribute classes = null;
			if (classDelimiter != null) {
				// Find the names of all the classes of proteins
				FastVector classNames = new FastVector();
				String className;
				for (int p = 0; p < proteins.size(); p++) {
					className = proteins.get(p).getLabel().split(classDelimiter)[classPiece];
					if (!classNames.contains(className))
						classNames.addElement(className);
				}
				classes = new Attribute("class", classNames);
				attributes.addElement(classes);
			}

			// construct the empty instances
			instances = new Instances(file.getName(), attributes, proteins.size());

			// construct one new instance for each protein
			Protein protein;
			Instance instance;
			double[] values;
			for (int p = 0; p < proteins.size(); p++) {
				protein = proteins.get(p);
				instance = new DenseInstance(attributes.size());
				instance.setDataset(instances);
				names.addStringValue(protein.getLabel());
				instance.setValue(names, protein.getLabel());

				// set the property values for each position
				values = property.getValuesForSequence(protein.getSequence());
				for (int i = 0; i < values.length; i++)
					try {
						instance.setValue(i + 1, values[i]);
					} catch (ArrayIndexOutOfBoundsException ae) {
						System.out.println("Protein " + protein.getLabel() + " has " + (values.length + 1)
								+ " values rather than the " + attributes.size() + " expected");
					}

				// add the class if required
				if (classes != null)
					instance.setValue(classes, protein.getLabel().split(classDelimiter)[classPiece]);

				instances.add(instance);
			}

			instances.setClass(classes);
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			throw new IOException(e);
		}
		return instances;
	}

	abstract Vector<Protein> readSequences(InputStream in) throws IOException;

	@Override
	public Instance getNextInstance(Instances structure) throws IOException {
		throw new IOException("ProteinLoaders can't read data sets incrementally.");
	}

	@Override
	public Instances getStructure() throws IOException {
		return null;
	}

	@Override
	public void reset() {
		property = null;
		classDelimiter = "";
		classPiece = -1;
		file = null;
		input = null;
		askUser = true;
	}

	public String[] getOptions() {
		return new String[] { "-P " + (property != null ? property.getName() : ""),
				"-D " + (classDelimiter.length() > 0 ? classDelimiter : ""),
				"-T " + (classDelimiter.length() > 0 ? " -T " + classPiece : "") };
	}

	public Enumeration listOptions() {
		Vector<Option> options = new Vector<Option>();
		options.add(new Option("The name of the amino acid property to use", "property", 1, "-P <name of property>"));
		options.add(new Option("The delimiter to use when extracting the name of teh class from the label",
				"class delimiter", 1, "-D <delimiter>"));
		options
				.add(new Option("Which piece of the label to use to identify the class", "class piece", 1, "-T <piece>"));
		return options.elements();
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <pre>
	 *                      -P &lt;Property&gt;
	 *                       The name of the amino acid property to use
	 *                      -D &lt;class Delimiter&gt;
	 *                       The delimiter used to distinguish the class of the protein from the rest of the label
	 *                      -T;
	 *                       which piece of the label to use as the class identifier
	 * </pre>
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 * 
	 */
	public void setOptions(String[] options) throws Exception {
		String s = Utils.getOption('P', options);
		if (s.length() != 0) {
			setProperty(s);
		}
		s = Utils.getOption('D', options);
		if (s.length() != 0) {
			setClassDelimiter(s);
		}
		s = Utils.getOption('T', options);
		if (s.length() != 0) {
			setClassPiece(Integer.parseInt(s));
		}
	}

	public boolean getUseRelativePath() {
		return rp;
	}

	public File retrieveFile() {
		return file;
	}

	public void setFile(File file) throws IOException {
		this.file = file;
		input = new FileInputStream(file);
	}

	@Override
	public void setUseRelativePath(boolean rp) {
		this.rp = rp;
	}

	@Override
	public void setSource(File file) throws IOException {
		this.file = file;
		input = new FileInputStream(file);

	}

	@Override
	public void setSource(InputStream input) throws IOException {
		this.input = input;

	}

	@Override
	public String getRevision() {
		// TODO Auto-generated method stub
		return null;
	}

}
