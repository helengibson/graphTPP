package weka.attributeSelection;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tpp.MatrixUtils;
import tpp.PerturbationPursuitThread;
import tpp.ScatterPlotModel;
import tpp.ScatterPlotViewPanel;
import tpp.SeparatePoints;
import tpp.SeparatePointsInScatterPlot;
import tpp.TPPModelEvent;
import tpp.TPPModelEventListener;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

/**
 * Class that uses Targeted Projection Pursuit as a attribute selection
 * algorithm. TPP is used to produce an N-dimensional (usually N=2) projection
 * of the data that separates classes in the data. The components in the
 * resulting projection are then used to rank each attribute. In order to reduce
 * execution time (if the number of attributes is large, say >1000) we can
 * optionally use IG to preselect a subset of attributes. The user can also see
 * the resulting views of the data as they are produced. Should be used with a
 * TPPAttributeEvaluation evaluator (which is a dummy evaluator that does
 * nothing). The user can also specify the maximum number of cycles in training
 * and/or a convergence limit
 * <p>
 * See C.Haddow, J.Perry, M.Durrant and J.Faith,
 * "Predicting Functional Residues of Protein Sequence Alignments as a Feature Selection Task"
 * , International Journal of Data Mining in Bioinformatics, 2011
 */
public class TPPAttributeSearch extends ASSearch implements WindowListener,
		RankedOutputSearch, OptionHandler, TechnicalInformationHandler,
		TPPModelEventListener, CapabilitiesHandler {

	SeparatePoints separator;

	/**
	 * By what proportion must the resulting projection be changing before the
	 * process is considered to have converged
	 */
	private static final double CONVERGENCE_LIMIT_DEFAULT = 0.001;

	/**
	 * The maximum number of times the projection pursuit process can be run
	 * before halting
	 */
	private static final int EPOCH_LIMIT_DEFAULT = 100;

	private static final int OUTPUT_DIMENSIONS_DEFAULT = 2;

	private int epochLimit = EPOCH_LIMIT_DEFAULT;

	private double convergenceLimit = CONVERGENCE_LIMIT_DEFAULT;

	private transient PerturbationPursuitThread pursuit;

	private transient JFrame frame;

	private transient ScatterPlotViewPanel panel;

	/**
	 * The number of output dimensions (default=2)
	 */
	private int numOutputDimensions = 2;

	/** The ranked scores of the attributes */
	private double[] attributeScores;

	/** The ranked indices of the attributes */
	private int[] attributeIndices;

	/** The number of attributes to select */
	private int numToSelect = 5;

	/** The number of attributes to pre-select */
	private int numToPreSelect = 0;

	/** Whether to show the projections as they are calculated. */
	private boolean showView = true;

	/** The threshold for selecting attributes */
	private double threshold;

	private ScatterPlotModel model;

	private Matrix currentProjection = null;

	private Matrix previousProjection = null;

	/** The number of times that the pursuit process has been tried. */
	private int epochs = 0;

	public TPPAttributeSearch() {
		super();
	}

	public String globalInfo() {
		String info = "Class that uses Targeted Projection Pursuit as a attribute selection algorithm. TPP is used to produce a two-dimensional projection of the data that separates classes in the data. The components in the resulting projection are then used to rank each attribute. In order to reduce execution time (if the number of attributes is >1000 say) we can optionally use IG to preselect a subset of attributes. The user can also see the resulting views of the data as they are produced. Should be used with teh TPPAttributeEvaluation evaluator (which is a dummy evaluator that does nothing). see\n\n";
		info += getTechnicalInformation().toString();
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
		result.setValue(Field.AUTHOR, "C.Haddow, J.Perry, M.Durrant, J.Faith");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.JOURNAL,
				"International Journal of Data Mining in Bioinformatics");
		result.setValue(
				Field.TITLE,
				"Predicting Functional Residues of Protein Sequence Alignments as a Feature Selection Task");
		return result;
	}

	// == ASSearch methods ========================================

	/** Use TPP to select attributes. The Evaluator is ignored. */
	public int[] search(ASEvaluation evaluator, Instances data)
			throws Exception {

		if (!(evaluator instanceof TPPAttributeEvaluation)) {
			JOptionPane
					.showMessageDialog(null,
							"You must use use the TPPAttributeEvaluation method with TPPAttributeSearch");
			return null;
		}

		epochs = 0;
		Instances filteredData;

		// preselect attributes
		AttributeSelection igSelector = null;
		InfoGainAttributeEval igEvaluator = null;
		Ranker igSearch = null;
		if ((numToPreSelect > 0) && (numToPreSelect < data.numAttributes())) {
			igSelector = new AttributeSelection();
			igEvaluator = new InfoGainAttributeEval();
			igSelector.setEvaluator(igEvaluator);
			igSearch = new Ranker();
			igSearch.setNumToSelect(numToPreSelect);
			igSelector.setSearch(igSearch);
			igSelector.setInputFormat(data);
			filteredData = Filter.useFilter(data, igSelector);
			// System.out.println("Preselected ranked attributes");
			// System.out.println(MatrixUtils.array2String(igSearch.rankedAttributes()));
		} else {
			filteredData = data;
		}

		// construct a TPP model from this data
		model = new ScatterPlotModel(numOutputDimensions);
		model.setInstances(filteredData, false, null);
		model.setSeparationAttribute(model.getInstances().classAttribute());
		model.normalizeDataUnit();
		model.randomProjection();
		model.project();
		currentProjection = null;
		previousProjection = null;

		if (showView && numOutputDimensions == 2) {
			// display this in a scatter plot in a frame
			if (frame == null)
				frame = new JFrame();
			frame.addWindowListener(this);
			if (panel == null)
				panel = new ScatterPlotViewPanel();
			panel.setModel(model);
			model.addListener(panel);
			model.addListener(this);
			frame.add(panel);
			frame.setSize(800, 600);
			frame.setVisible(true);
		}

		// Create and start the thread that will separate the points.
		// The difference between these two is that the former also takes care
		// of resizing the plot to fit the panel each time.
		if (showView && numOutputDimensions == 2)
			separator = new SeparatePointsInScatterPlot(model);
		else
			separator = new SeparatePoints(model);
		pursuit = new PerturbationPursuitThread(separator);
		pursuit.start();

		// and wait for it to finish
		pursuit.join();

		// get the size of each row in the projection
		attributeScores = MatrixUtils.rowNorm2(model.getProjection());
//		System.out.println("Attribute scores:\n"
//				+ MatrixUtils.toString(attributeScores));

		// find the indices of the row, ranked by size
		// this also ranks the scores
		attributeIndices = MatrixUtils.rank(attributeScores);
//		System.out.println("Ranked attribute indices:\n"
//				+ MatrixUtils.toString(attributeIndices));

		// If attributes were preselected, then we need to find the indices of
		// the attributes in the original data, rather than the indices in the
		// filtered data
		if (igSearch != null) {
			double[][] igAttributes = igSearch.rankedAttributes();
			for (int i = 0; i < attributeIndices.length; i++)
				attributeIndices[i] = (int) igAttributes[attributeIndices[i]][0];
//			System.out.println("Ranked attribute indices after adjusting for pre-selection:\n"
//					+ MatrixUtils.toString(attributeIndices));

		}

		// TPP just selects amongst the numerical attributes so convert indices
		// of the projection rows into the indices of the
		// corresponding attributes in the original instances
		for (int i = 0; i < attributeIndices.length; i++)
			attributeIndices[i] = model.indexOf(model.getNumericAttributes()
					.get(attributeIndices[i]));
		
//		System.out.println("Ranked attribute indices after allowing for non-numeric attributes:\n"
//				+ MatrixUtils.toString(attributeIndices));
//
//		System.out.println("Attribute scores:\n"
//				+ MatrixUtils.toString(attributeScores));

		return attributeIndices;
	}

	// == Observer methods ========================

	@Override
	public void modelChanged(TPPModelEvent e) {

		if (e.getType() == TPPModelEvent.PROJECTION_CHANGED) {

			// some projection pursuit has been done, so display the result
			panel.repaint();

			// and halt if the process has converged.
			currentProjection = model.getProjection();
			if (previousProjection != null && pursuit != null) {
				double previous = previousProjection.normF();
				double difference = previousProjection.minus(currentProjection)
						.normF();
				// the model may have been changed due to rescaling of the
				// window or the projection, in which case the difference will
				// be zero and we should ignore this change
				if (difference > 0.0) {
					epochs++;
					if (difference / previous < getConvergenceLimit()
							|| epochs > getEpochLimit()) {
						pursuit.stopPerturbationPursuit();
						if (showView) {
							frame.setVisible(false);
							frame.dispose();
						}
//						System.out.println("Stopping pursuit");
					}
				}
			}
			previousProjection = currentProjection.copy();
		}
	}

	// == WindowListener methods ===========================
	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {

		// the frame has been closed so stop the pursuit thread
		// System.out.println("Stopping pursuit");
		pursuit.stopPerturbationPursuit();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	// == RankedOutputSearch methods =====================
	public int getCalculatedNumToSelect() {
		return numToSelect;
	}

	/**
	 * This class will always rank attributes, so this method always returns
	 * true
	 */
	public boolean getGenerateRanking() {
		return true;
	}

	public int getNumToSelect() {
		return numToSelect;
	}

	/** The number of attributes to pre-select using IG */
	public int getNumToPreSelect() {
		return numToPreSelect;
	}

	public double getThreshold() {
		return threshold;
	}

	/** Whether to show the projected data as it is separated */
	public boolean getShowView() {
		return showView;
	}

	/** Whether to show the projected data as it is separated */
	public void setShowView(boolean showView) {
		this.showView = showView;
	}

	public double[][] rankedAttributes() throws Exception {
		double[][] rankedAttributes;

		// how many attributes should we actually select?
		int selected = (numToSelect <= 0 ? attributeIndices.length
				: numToSelect);

		// select them and construct the ranked attributes
		rankedAttributes = new double[selected][2];
		for (int i = 0; i < selected; i++) {
			rankedAttributes[i][0] = attributeIndices[i];
			rankedAttributes[i][1] = attributeScores[i];
		}
		return rankedAttributes;
	}

	/**
	 * Whether to rank the selected attributes. This class will always rank the
	 * selected attributes, and so this method does nothing.
	 */
	public void setGenerateRanking(boolean doRanking) {
	}

	/** The number of attributes to select. if -1 then select all */
	public void setNumToSelect(int numToSelect) {
		this.numToSelect = numToSelect;
	}

	/**
	 * The number of attributes to preselect using IG. if =0 then do not
	 * preselect.
	 */
	public void setNumToPreSelect(int numToPreSelect) {
		this.numToPreSelect = numToPreSelect;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getEpochLimit() {
		return epochLimit;
	}

	/** The maximum number of training cycles that TPP should run for */
	public void setEpochLimit(int epochLimit) {
		this.epochLimit = epochLimit;
	}

	public double getConvergenceLimit() {
		return convergenceLimit;
	}

	/** The limit at which TPP is treated as converged */
	public void setConvergenceLimit(double convergenceLimit) {
		this.convergenceLimit = convergenceLimit;
	}

	/**
	 * The number of output dimensions (default=2)
	 */
	public int getNumOutputDimensions() {
		return numOutputDimensions;
	}

	/**
	 * The number of output dimensions (default=2)
	 */
	public void setNumOutputDimensions(int numOutputDimensions) {
		this.numOutputDimensions = numOutputDimensions;
	}

	// == OptionHandler methods =============================

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {
		Vector<Option> newVector = new Vector<Option>(2);
		newVector.addElement(new Option(
				"\tSpecify the number of attributes to select.", "N", 1,
				"-N <number of attributes>"));
		newVector.addElement(new Option(
				"\tThe limit at which the search is taken to have converged (default="
						+ CONVERGENCE_LIMIT_DEFAULT + ").", "L", 1,
				"-L <convergence limit>"));
		newVector.addElement(new Option(
				"\tThe maximum number of training cycles (default="
						+ EPOCH_LIMIT_DEFAULT + ").", "C", 1,
				"-C <number of cycles>"));
		newVector.addElement(new Option(
				"\tThe number of output dimensions (default=2).", "D", 1,
				"-D <number of output dimensions>"));
		newVector
				.addElement(new Option(
						"\tTPP is most efficient as an attribute selection algorithm when selecting from approximately 100 attributes. Set this parameter to, eg 100, to preselect attributes using simple IG and Ranker search.",
						"P", 1, "-P <number of attributes>"));
		newVector
				.addElement(new Option(
						"\tWhether to show the resulting data visualisations (only has effect if number of output dimensions is 2).",
						"V", 1, "-V"));
		return newVector.elements();
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <pre>
	 *                      -N &lt;number of attributes&gt;
	 *                       The number of attributes to select.
	 *                      -P &lt;number of attributes to preselect&gt;
	 *                       The number of attributes to preselect.
	 *                      -V;
	 *                       Show resulting data visualisation.
	 *                      -L &lt;convergence limit&gt;
	 *                      -C &lt;maximum number of training cycles&gt;
	 *                      -D &lt;number of output dimensions&gt;
	 * </pre>
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 * 
	 */
	public void setOptions(String[] options) throws Exception {
		String optionString = Utils.getOption('P', options);
		if (optionString.length() != 0) {
			setNumToPreSelect(Integer.parseInt(optionString));
		}
		optionString = Utils.getOption('N', options);
		if (optionString.length() != 0) {
			setNumToSelect(Integer.parseInt(optionString));
		}
		optionString = Utils.getOption('C', options);
		if (optionString.length() != 0) {
			setEpochLimit(Integer.parseInt(optionString));
		} else {
			setEpochLimit(EPOCH_LIMIT_DEFAULT);
		}
		optionString = Utils.getOption('D', options);
		if (optionString.length() != 0) {
			setNumOutputDimensions(Integer.parseInt(optionString));
		} else {
			setNumOutputDimensions(OUTPUT_DIMENSIONS_DEFAULT);
		}
		optionString = Utils.getOption('L', options);
		if (optionString.length() != 0) {
			setConvergenceLimit(Double.parseDouble(optionString));
		} else {
			setConvergenceLimit(CONVERGENCE_LIMIT_DEFAULT);
		}
		optionString = Utils.getOption('V', options);
		if (optionString.length() != 0) {
			setShowView(true);
		}
	}

	/**
	 * Gets the current settings of attributes.
	 * 
	 * @return an array of strings suitable for passing to setOptions()
	 */
	public String[] getOptions() {
		return new String[] { "-N " + getNumToSelect() + "-P "
				+ getNumToSelect() + (getShowView() ? " -V" : "") + " -L "
				+ getConvergenceLimit() + " -C " + getEpochLimit() + " -D "
				+ getNumOutputDimensions() };
	}

	public String toString() {
		return "TPPAttributeSearch";
	}

	/**
	 * Returns the capabilities of this evaluator.
	 * 
	 * @return the capabilities of this search
	 * @see Capabilities
	 */
	public Capabilities getCapabilities() {
		Capabilities result = new Capabilities(this);
		result.disableAll();
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.NOMINAL_CLASS);
		return result;
	}

	/** Test method */
	public static void main(String[] a) throws Exception {

		// Get a new data file from the file chooser
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(null);
		String instancesFileName = null;
		if (returnVal == JFileChooser.APPROVE_OPTION)
			instancesFileName = chooser.getSelectedFile().getPath();

		// Read data from file
		System.out.println("Reading data from file " + instancesFileName);
		FileReader reader = new FileReader(instancesFileName);
		Instances in = new Instances(reader);
		TPPAttributeSearch search = new TPPAttributeSearch();
		search.setShowView(true);
		int[] results = search.search(new TPPAttributeEvaluation(), in);
		System.out.println("Selected attributes:\n"+MatrixUtils.toString(results));
		System.out.println("Ranked attributes:\n"+MatrixUtils.toString(search.rankedAttributes()));

	}

}
