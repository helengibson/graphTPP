package tpp;

import javax.swing.JFileChooser;

import weka.core.Instances;
import weka.core.converters.CSVLoader;

/** A class for importing TPPModels from a CSV or TSV file */
public class CSVDataImporter implements DataImporter {
	private static final CSVFileFilter FILE_FILTER = new CSVFileFilter();

	/** The default directory for file operations */
	private static final String DEFAULT_DIRECTORY = ".";

	/* (non-Javadoc)
	 * @see tpp.ModelImporter#importModel()
	 */
	public Instances importData() throws Exception {
		// Read the instances to be displayed
		Instances in = null;

		// Get a new data file from the file chooser
		JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
		chooser.setFileFilter(FILE_FILTER);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;

		// Read data from file
		System.out.println("Reading data from file " + chooser.getSelectedFile().getName());
		CSVLoader loader = new CSVLoader();
		loader.setFile(chooser.getSelectedFile());
		in=loader.getDataSet();
		return in;
	}

}
