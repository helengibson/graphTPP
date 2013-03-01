package tpp;

import java.io.FileReader;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class GraphARFFImporter {
	private static final ARFFFileFilter ARFF_FILE_FILTER = new ARFFFileFilter();

	/** The default directory for file operations */
	private static final String DEFAULT_DIRECTORY = "user.dir";

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.ModelImporter#importModel()
	 */
	public Instances importProjectionData(String filePath, boolean edges ) {
		try {
			// Read the instances to be displayed
			FileReader reader;
			Instances in;

			// Read data from file
			System.out.println("Reading data from file " + filePath);
			reader = new FileReader(filePath);
			in = new Instances(reader);
			
			
			
			// remove those attributes designated as edges
			
			if(!edges){
				// ignore any non-numeric attributes before clustering
				Remove removeEdges = new Remove();
				String indices = "";
				System.out.println(in.numAttributes());
					for (int a = 0; a < in.numAttributes(); a++) {
						String name = in.attribute(a).name();
						System.out.println(name);
						if(name.startsWith("_")){
							indices = indices + (a+1) + ",";
						}
					}
					indices = indices.substring(0, indices.length() - 1);
					System.out.println("indices: "+ indices);
					removeEdges.setAttributeIndices(indices);
					removeEdges.setInputFormat(in);
					in = Filter.useFilter(in, removeEdges);
			}
			return in;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
