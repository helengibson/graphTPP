/*
Added by Helen Gibson 
*/
package tpp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class GraphImporter {

	private static final CSVFileFilter FILE_FILTER = new CSVFileFilter();

	/** The default directory for file operations */
	private static final String DEFAULT_DIRECTORY = ".";
	private Graph graph;
	private Instances in;
	private int index;
	private String delimiter;
	private boolean header;
	private FileReader reader;
	private boolean weight;

	String unimportedEdges = "";


	/**
	 * Imports a graph from a separate edge list csv file
	 * 
	 * @param in
	 *            A list of instances which defines the nodes
	 * @param index
	 *            The index of the attribute that uniquely identifies the node
	 * @param instancesFileName
	 *            The location of the file which holds the edge data
	 * @param delimiter
	 *            The specified delimiter between nodes
	 * @param header
	 *            Does the csv file have a header to exclude?
	 * @param weight
	 *            Are the edges weighted?
	 * @return the graph which has been generated from the file
	 * @throws Exception
	 */
	public Graph importGraph(Instances in, int index, String instancesFileName,
			String delimiter, boolean header, boolean weight) throws Exception {

		this.in = in;
		this.index = index;
		this.delimiter = delimiter;
		this.header = header;
		this.weight = weight;

		// Read data from file
		System.out.println("Reading graph data from file " + instancesFileName);

		// File selectedFile = chooser.getSelectedFile();
		File selectedFile = new File(instancesFileName);

		Scanner fileScanner = new Scanner(selectedFile);
		graph = new Graph();
		if (header)
			fileScanner.nextLine();
		// read the each line separately
		while (fileScanner.hasNextLine()) {
			readLine(fileScanner.nextLine());
		}

		if (unimportedEdges.equals(""))
			JOptionPane.showMessageDialog(null,
					"All edges imported sucessfully");
		else
			JOptionPane.showMessageDialog(null,
					"The following edges were not imported successfully: \n"
							+ unimportedEdges);

		// TODO Add option to say no graph was imported. i.e. when the correct
		// delimited has not being
		// used.

		return graph;
	}

	/**
	 * Imports the graph from the ARFF file
	 * 
	 * @param filePath
	 *            The path to the file that holds the edges
	 * @param projectionInstances
	 *            The projection instances
	 * @return the graph that has been generated from the ARFF file
	 */
	public Graph importGraph(String filePath, Instances projectionInstances) {

		System.out.println("Reading data from file " + filePath);
		try {
			reader = new FileReader(filePath);
			in = new Instances(reader);

			index = in.attribute("NodeId").index();
			int pIndex = projectionInstances.attribute("NodeId").index();

			// get a list of the nodes as attributes
			int[] nodeList = new int[in.numInstances()];
			int j = 0;

			// pick out the attributes that are designated to be edges
			for (int i = 0; i < in.numAttributes(); i++) {
				String name = in.attribute(i).name();
				if (name.startsWith("_")) {
					nodeList[j] = i;
					j++;
				}
			}
			graph = new Graph();
			Enumeration instances = in.enumerateInstances();
			while (instances.hasMoreElements()) {
				Instance inst = (Instance) instances.nextElement();
				for (int node : nodeList) {
					String nodeA = inst.stringValue(index);
					double edgeValue = inst.value(node);
					if (edgeValue != 0) {
						String nodeB = in.attribute(node).name();
						nodeB = nodeB.substring(1);
						if (validateConnection(nodeA, nodeB)) {
							Connection cnxn = new Connection(
									projectionInstances, pIndex, nodeA, nodeB,
									edgeValue);
							graph.add(cnxn);
						}
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
	}

	private void readLine(String aLine) {
		System.out.println(aLine);
		Scanner lineScanner = new Scanner(aLine).useDelimiter(delimiter);

		String nodeA = lineScanner.next();

		// while(lineScanner.hasNext()){
		String nodeB = lineScanner.next();
		if (validateConnection(nodeA, nodeB)) {
			double edgeWeight = 1.0;
			if (weight)
				edgeWeight = lineScanner.nextDouble();
			Connection cnxn = new Connection(in, index, nodeA, nodeB,
					edgeWeight);
			graph.add(cnxn);
		} else {
			String missingEdge = "Edge " + nodeA + "  to " + nodeB
					+ " could not be imported. \n";
			unimportedEdges += missingEdge;
		}
	}

	private boolean validateConnection(String nodeA, String nodeB) {

		Instance sourceInstance = getNodeInstance(in, nodeA, index);
		Instance targetInstance = getNodeInstance(in, nodeB, index);

		int sourceIndex = indexOf(in, sourceInstance);
		int targetIndex = indexOf(in, targetInstance);

		if (sourceIndex >= 0 && targetIndex >= 0)
			return true;
		else
			return false;
	}

	private Instance getNodeInstance(Instances ins, String node, int index) {

		Instance nodeInstance = null;

		for (int i = 0; i < ins.numInstances(); i++) {
			Instance in = ins.instance(i);
			String attVal = in.stringValue(index);

			if (attVal.equals(node)) {
				nodeInstance = in;
			}
		}

		return nodeInstance;

	}

	// Copied from TPPModel
	private int indexOf(Instances ins, Instance in) {
		for (int i = 0; i < ins.numInstances(); i++)
			if (ins.instance(i).equals(in)) {
				return i;
			}
		return -1;
	}

}
