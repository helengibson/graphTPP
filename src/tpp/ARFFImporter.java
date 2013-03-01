package tpp;

import java.io.FileReader;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import weka.core.Instances;

public class ARFFImporter implements DataImporter {
	private static final ARFFFileFilter ARFF_FILE_FILTER = new ARFFFileFilter();

	/** The default directory for file operations */
	private static final String DEFAULT_DIRECTORY = ".";

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.ModelImporter#importModel()
	 */
	public Instances importData() {
		try {
			// Read the instances to be displayed
			String instancesFileName;
			FileReader reader;
			Instances in;

			// Get a new data file from the file chooser
			JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
			chooser.setFileFilter(ARFF_FILE_FILTER);
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				instancesFileName = chooser.getSelectedFile().getPath();
			else
				return null;

			// Read data from file
			System.out.println("Reading data from file " + instancesFileName);
			reader = new FileReader(instancesFileName);
			in = new Instances(reader);
			return in;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
