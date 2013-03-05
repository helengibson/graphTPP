package tpp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import org.w3c.dom.Attr;

import weka.classifiers.Classifier;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AddClassification;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import org.apache.commons.collections.CollectionUtils; 

/**
 * A class that represents the TPP model: ie the original data, the current
 * target and view, and the associated methods for manipulating these. This
 * abstract base class does everything except initialise the projection.
 * 
 */
public class TPPModel implements Serializable, Cloneable {

	/**
	 * The minimum value allowed when computing PCAs. NB could probably be set
	 * less than this, but use this value for safety. For more information see
	 * http://www.netlib.org/na-digest-html/90/v90n18.html#5
	 */
	private static final double MIN_VALUE = 1E-100;

	private static final double TRAIN = 0d;

	private static final double TEST = 1d;

	/**
	 * The value of the class attribute that indicates that the class of this
	 * instance is unknown.
	 */
	public static final String CLASS_UNKNOWN = "UNKNOWN";

	/** The current view of the data */
	protected Matrix view;

	/** The current target view of the data */
	protected Matrix target;

	/** The underlying data. */
	protected Instances instances;
	
	/** Deep copy of the instances data */
	protected Instances instancesCopy;
	
	/** The numeric, normalised, filtered data */
	protected Matrix data;

	/** The projection from the higher dimensional data to the view. */
	protected LinearProjection projection;

	protected Matrix distances;

	/** The attribute that indicates which instances are in the test set, if any */
	private Attribute test;

	private ProjectionConstraint constraint;

	/** Whichever attribute used to separate points by */
	protected Attribute separationAttribute;

	/** A hierarchical clustering of the instances */
	private HierarchicalCluster hierarchicalClustering;

	/**
	 * A boolean array that indicates which points have been selected
	 */
	protected boolean[] selectedPoints;

	/** The number of view dimensions */
	protected int numViewDimensions = -1;

	/**
	 * The centroids of the classes, keyed by the attribute that defines the
	 * classes that those centroids were defined over
	 */
	private HashMap<Attribute, Matrix> allCentroids;

	protected Graph graph;

	private Vector<TPPModelEventListener> listeners;

	/* The numeric attributes -- doesn't include date attributes */
	private Vector<Attribute> numericAttributes;

	private Attribute descriptionAttribute;
	
	protected double[] attributeMeans; 

	/** Get the number of output dimensions */
	public int getNumViewDimensions() {
		return numViewDimensions;
	}

	/**
	 * Initialise a projection projecting onto an n-dimensional space
	 * 
	 * @throws Exception
	 *             if number of dimensions is not set
	 */
	protected void initialiseProjection() throws Exception {
		if (numViewDimensions <= 0)
			throw new Exception("number of output dimensions not set");
		projection = new LinearProjection(data.getColumnDimension(), numViewDimensions);
	}

	/** Construct a new TPP model with the given output dimension */
	public TPPModel(int n) {
		this.numViewDimensions = n;
	}

	/*
	 * Set the data for this model. The projection will default to PCA (if there
	 * are sufficient resources to compute this) or a random projection
	 */
	public void setInstances(Instances in, boolean filtered) throws Exception {
		initialise(in, false);
		fireModelChanged(TPPModelEvent.DATA_SET_CHANGED);
	}

	/**
	 * 
	 * @param filtered TODO
	 * @throws Exception
	 */
	protected void initialise(Instances ins, boolean filtered) throws Exception {
		
		Instances deepInstances = getDeepInstances();
		Instances currentInstances = getInstances();

		// Replace missing values
		ReplaceMissingValues replace = new ReplaceMissingValues();
		replace.setInputFormat(ins);
		instances = Filter.useFilter(ins, replace);

		// Chose to start off by classifying and selecting points according to
		// the first nominal
		// attribute
		Vector<Attribute> nom = getNominalAttributes();
		if (nom.size() > 0) {
			instances.setClass(nom.get(0));
			setSeparationAttribute(instances.classAttribute());
		}

		// See if there is a string attribute we could use for describing the
		// points
		descriptionAttribute = null;
		for (int a = 0; a < instances.numAttributes(); a++)
			if (instances.attribute(a).isString())
				descriptionAttribute = instances.attribute(a);

		extractNumericData();
		normalizeDataBipolarHomogenous();

		// Initially all points are unselected
		selectedPoints = new boolean[instances.numInstances()];
		
		// Initally no points are hovered
		hoverPoints = new boolean[instances.numInstances()];
		
		if (filtered){
			
			ArrayList<Attribute> deepAttributes = new ArrayList<Attribute>();
			ArrayList<Attribute> currentAttributes = new ArrayList<Attribute>();
			ArrayList<Attribute> filteredAttributes = new ArrayList<Attribute>();
			
			HashSet<String> deepAttributeNames = new HashSet<String>();
			HashSet<String> currentAttributeNames = new HashSet<String>();
			HashSet<String> filteredAttributeNames = new HashSet<String>();
			
			Enumeration<Attribute> deepEnumAtts = deepInstances.enumerateAttributes();
			while(deepEnumAtts.hasMoreElements()){
				Attribute a = deepEnumAtts.nextElement();
				if (a.isNumeric()){
					deepAttributes.add(a);
					deepAttributeNames.add(a.name());
				}
			}
			
			Enumeration<Attribute> currentEnumAtts = currentInstances.enumerateAttributes();
			while(currentEnumAtts.hasMoreElements()){
				Attribute a = currentEnumAtts.nextElement();
				if (a.isNumeric()){
					currentAttributes.add(a);
					currentAttributeNames.add(a.name());
				}
			}
			
			Enumeration<Attribute> filteredEnumAtts = ins.enumerateAttributes();
			while(filteredEnumAtts.hasMoreElements()){
				Attribute a = filteredEnumAtts.nextElement();
				if (a.isNumeric()) {
					filteredAttributes.add(a);
					filteredAttributeNames.add(a.name());
				}
			}
			
			// want to know how many of the numeric attributes are shared by the projection the 
			// user is currently seeing and the one which they are now requesting

			if (CollectionUtils.isEqualCollection(currentAttributeNames, filteredAttributeNames)) {
				project();
			} else if (CollectionUtils.isProperSubCollection(filteredAttributeNames,currentAttributeNames)){
				int[] cols = {0,1};
				int[] filteredIndices = new int[filteredAttributes.size()];
				int i = 0;
				for(Attribute at: filteredAttributes){
					Attribute curAtt = currentInstances.attribute(at.name());
					filteredIndices[i] = currentAttributes.indexOf(curAtt);
					i++;
				}
				projection = new LinearProjection(projection.getMatrix(filteredIndices, cols));
				project();
			} else if (CollectionUtils.containsAny(currentAttributeNames, filteredAttributeNames)){
				double[][] newProjection = new double[filteredAttributes.size()][2];
				int i = 0;
				for(Attribute at: filteredAttributes){
					if (currentAttributeNames.contains(at.name())) {
						Attribute curAtt = currentInstances.attribute(at.name());
						int index = currentAttributes.indexOf(curAtt);
						newProjection[i][0] = projection.get(index, 0);
						newProjection[i][1] = projection.get(index, 1);
					} else {
						newProjection[i][0] = 0.0;
						newProjection[i][1] = 0.0;
					}
					i++;
				}
					Matrix projectionMatrix = new Matrix(newProjection);
					projection = new LinearProjection(projectionMatrix);
					project();
					
			} else if (CollectionUtils.intersection(currentAttributeNames, filteredAttributeNames).isEmpty()) {
				initialiseProjection();
				project();
				PCA();
			}
		} else {
			// Create a new projection and view
			initialiseProjection();
			project();

			// if its a huge data set don't bother trying to find PCA
			if (getNumDataDimensions() * getNumDataPoints() < 1E6)
				PCA();
			else {
				System.out.println("Data too large to calculate PCA. Defaulting to a random projection.");
				randomProjection();
			}
		}

		

		// initialise centroids
		allCentroids = new HashMap<Attribute, Matrix>();
		
	}
		
	public void setDeepInstances(Instances data) {
		instancesCopy = new Instances(instances);
	}
	
	public Instances getDeepInstances() {
		return instancesCopy;
	}

	/**
	 * Scale the numeric data from the filtered instances to [-1,1] (ie the
	 * maximum absolute value of each attribute will become 1). Attributes are
	 * assumed to be homogenous ie all attributes are divided by the same
	 * scaling factor
	 */
	protected void normalizeDataBipolarHomogenous() {
		data.timesEquals(1 / MatrixUtils.maxAbsValue(data));
	}

	/**
	 * Normalize the numeric data from the filtered instances to the range [0,1]
	 * (ie the minimum data value will be reset to 0 and maximum to 1.
	 * 
	 * @throws Exception
	 */
	public void normalizeDataUnit() throws Exception {
		Filter normalize = new weka.filters.unsupervised.attribute.Normalize();
		normalize.setInputFormat(instances);
		instances = Filter.useFilter(instances, normalize);
		extractNumericData();

	}

	/**
	 * Extract the data from the numeric attributes of the given instances
	 * 
	 * @throws TPPException
	 */
	protected void extractNumericData() throws TPPException {

		// Extract a matrix of numeric data from the instances
		// First find the numeric data attributes
		numericAttributes = new Vector<Attribute>();
		for (int i = 0; i < instances.numAttributes(); i++)
			if (instances.attribute(i).isNumeric() && !instances.attribute(i).isDate())
				numericAttributes.add(instances.attribute(i));
		if (numericAttributes.size() < 2)
			throw new TPPException("Insufficient numeric attributes");
		// create a new matrix
		data = new Matrix(instances.numInstances(), numericAttributes.size());
		// and copy all the numeric data into the matrix
		for (int c = 0; c < numericAttributes.size(); c++)
			for (int r = 0; r < instances.numInstances(); r++)
				data.set(r, c, instances.instance(r).value(numericAttributes.get(c)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.TPPModelInterface#getAttributeByName(java.lang.String)
	 */
	public Attribute getAttributeByName(String name) {
		for (int a = 0; a < instances.numAttributes(); a++)
			if (instances.attribute(a).name().equals(name))
				return instances.attribute(a);
		return null;
	}

	/** Convert a nominal attribute into N x binary numeric attributes */
	public void nominalToBinary(Attribute nominal) throws Exception {

		if (nominal != null && nominal.isNominal()) {
			Attribute binary;
			Enumeration values;
			String value, name;
			name = nominal.name();
			values = nominal.enumerateValues();

			// System.out.println("Converting attribute " + nominal.name() + "
			// from nominal to binary");

			// create a new binary attribute for each value for this
			// attribute
			while (values.hasMoreElements()) {
				value = (String) values.nextElement();
				binary = new Attribute(name + "_" + value);
				System.out.println("Creating attribute " + binary.name());
				addAttribute(binary, null);

				// for each instance, set the value of the attribute
				double d;
				for (int i = 0; i < instances.numInstances(); i++) {
					d = instances.instance(i).stringValue(nominal).equals(value) ? 1.0d : 0.0d;
					instances.instance(i).setValue(binary, d);
				}
			}

			// and remove the original nominal attribute
			removeAttribute(nominal);

			// Reinitialise on the new numeric data
			initialise(instances, false);
		}
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
	}

	public String getDescriptionOfInstance(int[] is) {
		String description = "";
		for (int i : is)
			description += ", point #"
					+ (i + 1)
					+ (descriptionAttribute == null ? "" : " (" + instances.instance(i).toString(descriptionAttribute)
							+ ")");
		return description.substring(2);
	}
	
	public String getDescriptionOfInstanceOnly(int i) {
		String description = "";
			description = (descriptionAttribute == null ? "" : instances.instance(i).toString(descriptionAttribute));
		return description;
	}

	public String getDescriptionOfAttributes(int[] as) {
		String description = "";
		for (int a : as)
			description += ", " + numericAttributes.get(a).name();
		return description.substring(2);
	}

	private Series series;

	/** Get the series defined within the data */
	public Series getSeries() {
		return series;
	}

	public void removeSeries() {
		series = null;
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
	}

	/**
	 * Create a series in the instances using the given index and id attributes
	 * (specified by name). Any existing series are removed.
	 * 
	 * @throws Exception
	 */
	public void createSeries(Attribute indexAttribute, Attribute idAttribute) {
		removeSeries();
		if (indexAttribute != null) {
			series = new Series(instances, indexAttribute, idAttribute);
		}
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
	}

	/**
	 * Create a test set comprising 1/k'th of the instances. If there are no
	 * series then the test set is chosen at random. If there are series defined
	 * the the test set is the last 1/kth of each series Any existing test set
	 * is replaced.
	 * 
	 * @param k
	 * @return the attribute that defines the test set
	 * @throws Exception
	 */
	public Attribute createTestSet(int k) {

		removeTestSet();

		// Create nominal attribute
		FastVector values = new FastVector(2);
		values.addElement("train");
		values.addElement("test");
		test = new Attribute("Test Set", values);
		test = addAttribute(test, null);

		if (series == null) {
			// there are no series so pick test set at random
			// for each instance, set the value of the attribute
			Random r = new Random(System.currentTimeMillis());
			for (int i = 0; i < instances.numInstances(); i++)
				instances.instance(i).setValue(test, r.nextDouble() > 1d / k ? TRAIN : TEST);

		} else {
			// there are series, so the test set are the final points in the
			// series

			// First set each instance to be in the testing set
			for (int p = 0; p < instances.numInstances(); p++)
				instances.instance(p).setValue(test, TEST);

			// then, for each series
			TreeSet<Instance> nextSeries;
			Iterator<Instance> itNextSeries;
			Iterator<TreeSet<Instance>> allSeries = series.getAllSeries().values().iterator();
			int numPointsInSeries, numPointsInTrainingSet;
			while (allSeries.hasNext()) {

				// find out how many points in the series and the number in the
				// training set
				nextSeries = allSeries.next();
				numPointsInSeries = nextSeries.size();
				numPointsInTrainingSet = (int) Math.round((double) numPointsInSeries * (double) (k - 1) / (double) k);

				// and set the training set
				itNextSeries = nextSeries.iterator();
				for (int p = 0; p < numPointsInTrainingSet; p++)
					itNextSeries.next().setValue(test, TRAIN);
			}
		}
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
		return test;
	}

	/** Return the attribute that defines the test/prediction set. */
	public Attribute getTestAttribute() {
		return test;
	}

	public void removeTestSet() {
		if (test != null)
			removeAttribute(test);
		test = null;
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
	}

	/**
	 * Return a boolean array indicating whether each instance is a member of
	 * the training (true) or test (false) set. Returns null if no test set is
	 * defined.
	 */
	public boolean[] getPointsInTrainingSet() {
		if (test == null)
			return null;
		boolean[] isTrain = new boolean[instances.numInstances()];
		for (int p = 0; p < instances.numInstances(); p++)
			isTrain[p] = instances.instance(p).value(test) == TRAIN;
		return isTrain;
	}

	/**
	 * Is a specific point in the testing set or is the classification of it
	 * unknown. Returns false if there is no testing set.
	 */
	public boolean isPointInTestingSet(int p) {
		if (instances.classIndex() >= 0
				&& instances.instance(p).stringValue(instances.classAttribute()) == CLASS_UNKNOWN)
			return true;
		if (test == null)
			return false;
		else
			return instances.instance(p).value(test) == TEST;
	}

	/**
	 * Get the index of the attribute within the instances (starting at 0;
	 * returns -1 if not found.
	 */
	public int indexOf(Attribute att) {
		for (int a = 0; a < instances.numAttributes(); a++)
			if (instances.attribute(a).equals(att))
				return a;
		return -1;
	}

	/**
	 * returns -1 if not found. Get the index of the instance within the
	 * instances (starting at 0;
	 */
	public int indexOf(Instance in) {
		for (int i = 0; i < instances.numInstances(); i++)
			if (instances.instance(i).equals(in)){
				// System.out.println(instances.instance(i));
				// System.out.println(in);
				return i;}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.TPPModelInterface#normalizeProjection()
	 */
	public void normalizeProjection() {
		projection.normalise();
		project();
		target = view;
		//System.out.println("Projection normalized");
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.TPPModelInterface#randomProjection()
	 */
	public void randomProjection() {
		projection.randomize();
		normalizeProjection();
		project();
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.TPPModelInterface#PCA()
	 */
	public void PCA() {
		projection.PCA(data);
		normalizeProjection();
		project();
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	protected double abs(double d) {
		return (d > 0 ? d : -d);
	}

	/** Get the underlying instances */
	public Instances getInstances() {
		return instances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.TPPModelInterface#getNormalisedDistances()
	 */
	public Matrix getNormalisedDistances() {
		return distances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.TPPModelInterface#getTarget()
	 */
	public Matrix getTarget() {
		return target;
	}

	/** The current projection */
	public Matrix getProjection() {
		return projection;
	}

	/** The view of the data */
	public Matrix getView() {
		return view;
	}

	public String getViewAsString() {
		String s = "";
		Attribute classAttribute = instances.classAttribute();
		for (int r = 0; r < view.getRowDimension(); r++) {
			if (getStringAttributes().size() > 0)
				s += instances.instance(r).stringValue(getStringAttributes().get(0)) + ",";
			if (classAttribute != null)
				s += instances.instance(r).stringValue(classAttribute) + ",";
			for (int c = 0; c < view.getColumnDimension(); c++)
				s += view.get(r, c) + ",";
			s += "\n";
		}
		return s;
	}

	/** The normalised numeric data */
	public Matrix getData() {
		return data;
	}

	/** The number of points */
	public int getNumDataPoints() {
		return data.getRowDimension();
	}

	/**
	 * The number of dimensions in the numeric data
	 * 
	 * @return
	 */
	public int getNumDataDimensions() {
		return data.getColumnDimension();
	}

	/** Get the string attributes. Returns an empty vector if there are none. */
	public Vector<Attribute> getStringAttributes() {
		Vector<Attribute> ats = new Vector<Attribute>();
		for (int i = 0; i < instances.numAttributes(); i++)
			if (instances.attribute(i).isString()) {
				ats.add(instances.attribute(i));
				// System.out.println("Attribute "+instances.attribute(i)+" is string");
			}
		return ats;
	}
	
	public Vector<Attribute> getAllAttributes() {
		Vector<Attribute> ats = new Vector<Attribute>();
		for (int i = 0; i < instances.numAttributes(); i++)
				ats.add(instances.attribute(i));
		return ats;
	}

	public Vector<Attribute> getNominalAttributes() {
		Vector<Attribute> ats = new Vector<Attribute>();
		for (int i = 0; i < instances.numAttributes(); i++)
			if (instances.attribute(i).isNominal())
				ats.add(instances.attribute(i));
		return ats;
	}
	
	public Vector<Attribute> getNominalAttributes(Instances in) {
		Vector<Attribute> ats = new Vector<Attribute>();
		for (int i = 0; i < in.numAttributes(); i++)
			if (in.attribute(i).isNominal())
				ats.add(in.attribute(i));
		return ats;
	}

	public Vector<Attribute> getNumericAttributes() {
		return numericAttributes;
	}

	public Vector<Attribute> getDateAttributes() {
		Vector<Attribute> ats = new Vector<Attribute>();
		for (int i = 0; i < instances.numAttributes(); i++)
			if (instances.attribute(i).isDate())
				ats.add(instances.attribute(i));
		return ats;

	}

	/** Set the target that the projection is pursuing */
	public void setTarget(Matrix matrix) {
		target = matrix;
	}

	/** Project the data to produce a new view. */
	public void project() {
		view = data.times(projection);
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/**
	 * Pursue a projection that will map the target onto the projection until
	 * convergence.
	 * 
	 * @throws TPPException
	 */
	public void pursueTarget() throws TPPException {
		double error = projection.pursueTarget(data, target, getPointsInTrainingSet());
		if (getProjectionConstraint() != null)
			projection = (LinearProjection) getProjectionConstraint().findNearestValid(projection);
		view = projection.project(data);
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/**
	 * Pursue a projection that will map the target onto the projection, but
	 * just take a single step. Constraints are ignored
	 * 
	 * @throws TPPException
	 */
	public void pursueTargetSingleShot() throws TPPException {
		double error = projection.pursueTargetSingleShot(data, target, getPointsInTrainingSet());
		view = projection.project(data);
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/**
	 * Return the constraint that any projection must meet. returns null if no
	 * constraint has been defined.
	 */
	public ProjectionConstraint getProjectionConstraint() {
		return constraint;
	}

	/**
	 * Set a constraint on what constitutes a valid projection. If the
	 * projection is non null, then the nearest valid projection is found to the
	 * current one
	 */
	public void setProjectionConstraint(ProjectionConstraint constraint) {
		this.constraint = constraint;
		if (this.constraint != null)
			constraint.findNearestValid(projection);
		project();
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/**
	 * Create a new attribute that clusters the original data. KMeans is used by
	 * default. returns the new attribute
	 */
	public Attribute cluster(int numClusters, Instances in, String attributesUsed) {
		try {
			Instances numericInstances;

			// ignore any non-numeric attributes before clustering
			Remove removeClassification = new Remove();
			String indices = "";
			if (getNominalAttributes(in).size() > 0) {
				for (int a = 0; a < in.numAttributes(); a++)
					if (!in.attribute(a).isNumeric())
						indices = indices + (a + 1) + ",";
				indices = indices.substring(0, indices.length() - 1);
			}
			removeClassification.setAttributeIndices(indices);
			removeClassification.setInputFormat(in);
			numericInstances = Filter.useFilter(in, removeClassification);

			// build a clusterer
			SimpleKMeans clusterer = new SimpleKMeans();
			
			clusterer.setNumClusters(numClusters);
			clusterer.buildClusterer(numericInstances);

			// use the clustering of the unclassified instances to add an
			// attribute to the filtered instances
			// It would be nice to use the AddCluster filter to do this, but we
			// are going to use the clustering of one set of instances to
			// classify another

			// first check there is no attribute with this name
			String atName = attributesUsed+ ": Cluster " + numClusters;
			while (in.attribute(atName) != null)
				atName += "'";

			FastVector values = new FastVector(numClusters);
			for (int v = 0; v < numClusters; v++)
				values.addElement("cluster " + (v + 1) + "/" + numClusters);
			Attribute clustering = addAttribute(new Attribute(atName, values), null);

			int c;
			for (int i = 0; i < instances.numInstances(); i++) {
				c = clusterer.clusterInstance(numericInstances.instance(i));
				instances.instance(i).setValue(clustering, (String) values.elementAt(c));
			}

			// make this clustering the current classification
			instances.setClass(clustering);
			fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);

			return clustering;

		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace(System.out);
			return null;
		}
	}

	public String toString(int point) {
		if (getStringAttributes().size() > 0)
			return instances.instance(point).stringValue(getStringAttributes().get(0));
		else
			return "Instance number " + (point + 1) + " of " + getNumDataPoints();
	}

	/**
	 * Set the input from the selected attributes to zero -- ie remove them
	 * temporarily from teh projection
	 */
	public void zeroProjectionAttributes(int[] ats) {
		if (ats != null)
			for (int r = 0; r < ats.length; r++)
				for (int c = 0; c < projection.getColumnDimension(); c++)
					projection.set(ats[r], c, 0);
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);

	}

	/**
	 * The attribute used to separate points
	 */
	public void setSeparationAttribute(Attribute at) {
		separationAttribute = at;
	}

	public Attribute getSeparationAttribute() {
		return separationAttribute;
	}

	public void createHierarchicalClustering() {
		HierarchicalAgglomerativeClustering hac = new HierarchicalAgglomerativeClustering();
		this.hierarchicalClustering = hac.singleLinkage(this);
	}

	public HierarchicalCluster getHierarchicalCluster() {
		return hierarchicalClustering;
	}

	public boolean isPointSelected(int p) {
		try {
			return selectedPoints[p];
		}
		catch (ArrayIndexOutOfBoundsException e){
			return false;
		} //stupid hack so doesn't fail when importing non existent edges
	}

	/** Are any points selected? */
	public boolean arePointsSelected() {
		for (int p = 0; p < getNumDataPoints(); p++)
			if (isPointSelected(p))
				return true;
		return false;
	}

	public void unselectPoints() {
		for (int p = 0; p < getNumDataPoints(); p++)
			selectedPoints[p] = false;
		fireModelChanged(TPPModelEvent.POINT_SELECTION_CHANGED);
	}

	public void selectPoint(int p) {
		selectedPoints[p] = true;
		fireModelChanged(TPPModelEvent.POINT_SELECTION_CHANGED);
	}

	public void selectPoints(int[] p) {
		for (int i : p)
			selectPoint(i);
	}

	/** How many points are currently selected */
	public int numPointsSelected() {
		int total = 0;
		for (int p = 0; p < getNumDataPoints(); p++)
			if (isPointSelected(p))
				total++;
		return total;
	}

	/**
	 * Perform 10-fold cross validation for the given attribute. Returns the
	 * attributes containing the new classification and the error
	 * 
	 * @param cls
	 */
	public Attribute[] createCrossValidation(Attribute at, Classifier cls) {

		int folds = 10;
		long seed = System.currentTimeMillis();
		Instances train, test;

		try {
			instances.setClass(at);

			// perform cross-validation and add predictions
			Instances predictedData = null;
			for (int n = 0; n < folds; n++) {
				train = instances.trainCV(folds, n);
				test = instances.testCV(folds, n);

				// add predictions
				AddClassification filter = new AddClassification();
				filter.setClassifier(cls);
				filter.setOutputClassification(true);
				filter.setOutputDistribution(false);
				filter.setOutputErrorFlag(true);
				filter.setInputFormat(train);
				Filter.useFilter(train, filter);
				Instances predictions = Filter.useFilter(test, filter);
				if (predictedData == null)
					predictedData = new Instances(predictions, 0);
				for (int j = 0; j < predictions.numInstances(); j++)
					predictedData.add(predictions.instance(j));
			}

			instances = predictedData;

			// change the names of the classification and error attributes
			Attribute errorAttribute = instances.attribute(instances.numAttributes() - 1);
			Attribute classificationAttribute = instances.attribute(instances.numAttributes() - 2);
			errorAttribute = renameAttribute(errorAttribute, cls.getClass().getSimpleName() + " error");
			classificationAttribute = renameAttribute(classificationAttribute, cls.getClass().getSimpleName()
					+ " classification");
			// instances.setClass(classificationAttribute);

			fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
			return new Attribute[] { instances.attribute(instances.numAttributes() - 1),
					instances.attribute(instances.numAttributes() - 2) };

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * add an attribute to the instances, setting the new values to those given
	 * (or to 'missing', if values=null)
	 * 
	 * @param at
	 * @param values
	 *            the values to set the new attribute to, or null
	 * @return
	 */
	private Attribute addAttribute(Attribute at, double[] values) {

		// add it to the instances -- this creates a shallow
		// copy of the attribute so we have to retrieve the copy
		instances.insertAttributeAt(at, instances.numAttributes());
		at = instances.attribute(instances.numAttributes() - 1);

		if (values != null)
			for (int i = 0; i < values.length; i++)
				instances.instance(i).setValue(at, values[i]);

		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
		return at;
	}

	/** Remove an attribute, returning the values */
	public double[] removeAttribute(Attribute at) {
		double[] values = new double[instances.numInstances()];
		for (int i = 0; i < values.length; i++)
			values[i] = instances.instance(i).value(at);
		Remove remove = new Remove();
		remove.setAttributeIndices("" + (indexOf(at) + 1));
		try {
			remove.setInputFormat(instances);
			instances = Filter.useFilter(instances, remove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
		return values;
	}

	private Attribute renameAttribute(Attribute at, String newName) {
		Attribute copy = at.copy(newName);
		double[] values = removeAttribute(at);
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
		return addAttribute(copy, values);

	}

	/**
	 * Select a single numeric attribute
	 * 
	 * @throws Exception
	 */
	public void selectSingleAttribute(Attribute at) throws Exception {
		if (getNumViewDimensions() != 1)
			throw new Exception("Method only applies to 1D projections");
		projection = new LinearProjection(data.getColumnDimension(), 1);
		projection.set(getNumericAttributes().indexOf(at), 0, 1);
		project();
		target = view;
		fireModelChanged(TPPModelEvent.AXIS_SELECTION_CHANGED);
	}

	/**
	 * Create uniform projection in which each attribute has unit weight.
	 * 
	 * @throws Exception
	 */
	public void uniformProjection() throws Exception {
		if (getNumViewDimensions() != 1)
			throw new Exception("Method only applies to 1D projections");
		projection = new LinearProjection(data.getColumnDimension(), 1);
		for (int i = 0; i < projection.getRowDimension(); i++)
			projection.set(i, 0, 1);
		project();
		target = view;
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/**
	 * Get the centroids of the data points for the classes defined by the given
	 * attribute
	 * 
	 * @throws Exception
	 */
	public Matrix getCentroids(Attribute classification) throws Exception {

		if (!classification.isNominal())
			throw new Exception("Can only find centroids over nominal attributes");

		if (allCentroids.get(classification) == null) {
			// first total up the positions for each class
			double[][] centroids = new double[classification.numValues()][getNumViewDimensions()];
			double[] numPoints = new double[classification.numValues()];
			int c, p, od;
			for (p = 0; p < getNumDataPoints(); p++) {
				c = (int) getInstances().instance(p).value(classification);
				for (od = 0; od < getNumViewDimensions(); od++)
					centroids[c][od] += getData().get(p, od);
				numPoints[c]++;
			}
			// then divide by the number of points in each class (if this class
			// is not empty)
			for (c = 0; c < classification.numValues(); c++) {
				if (numPoints[c] > 0) {
					for (od = 0; od < getNumViewDimensions(); od++)
						centroids[c][od] /= numPoints[c];
				}
			}
			allCentroids.put(classification, new Matrix(centroids));

		}
		return allCentroids.get(classification);
	}

	public void setProjectionValues(double[][] values) {
		((LinearProjection) this.projection).setValues(values);
		view = projection.project(data);
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);

	}

	/**
	 * Create an array of descriptions of each point, based on any string
	 * attributes
	 */
	public String[] getPointDescriptions() {
		String[] descriptions = new String[getNumDataPoints()];
		Vector<Attribute> sats = getStringAttributes();
		// if there are string attributes then add them together to get a
		// description of the point
		if (sats != null && sats.size() > 0) {
			for (int p = 0; p < getNumDataPoints(); p++) {
				descriptions[p] = "";
				for (int s = 0; s < sats.size(); s++) {
					try {
						descriptions[p] += instances.instance(p).stringValue(sats.get(s)) + " ";
					} catch (Exception e) {
						descriptions[p] += "? ";
					}
				}
				if (descriptions[p].length() > 40)
					descriptions[p] = descriptions[p].substring(0, 40);
			}
		} else {
			for (int p = 0; p < getNumDataPoints(); p++)
				descriptions[p] = "point " + p;
		}
		return descriptions;
	}

	public void loadGraph(Graph graph) {
		removeGraph();
		this.graph = graph;
		
	}

	public void removeGraph() {
		graph = null;
	}

	public Graph getGraph() {
		return graph;
	}

	public Enumeration<Attribute> getAttributes() {
		return instances.enumerateAttributes();
	}

	private Vector<TPPModelEventListener> getListeners() {
		if (listeners == null)
			listeners = new Vector<TPPModelEventListener>();
		return listeners;
	}

	public void addListener(TPPModelEventListener l) {
		getListeners().add(l);
		// tell every new listener that it has a new model
		l.modelChanged(new TPPModelEvent(this, TPPModelEvent.DATA_SET_CHANGED));
	}

	protected void fireModelChanged(int type) {
		TPPModelEvent e = new TPPModelEvent(this, type);
		// System.out.println(e);
		Iterator<TPPModelEventListener> it = getListeners().iterator();
		while (it.hasNext())
			it.next().modelChanged(e);
	}

	public TPPModel clone() {
		TPPModel clone = new TPPModel(numViewDimensions);
		Instances cloneInstances = new Instances(instances);
		try {
			clone.setInstances(cloneInstances, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clone.projection = new LinearProjection(projection);
		clone.project();
		return clone;
	}
	
	public void setAttributeMeans() {
		int p;
		int numPointsSelected = numPointsSelected();
		double[] relativeMeanForSelected = null;
		Matrix attributeMean = null;
		if ((numPointsSelected > 0)) {
			Matrix mPointsSelected = new Matrix(1, getNumDataPoints());
			for (p = 0; p < getNumDataPoints(); p++)
				if (isPointSelected(p))
					mPointsSelected.set(0, p, 1);
			Matrix attributeMeanSelected = mPointsSelected.times(getData()).times(
					1d / numPointsSelected());
			attributeMean = MatrixUtils.columnMeans(getData());
			relativeMeanForSelected = MatrixUtils.logistic(attributeMeanSelected.minus(attributeMean)
					.arrayRightDivide(attributeMeanSelected).getArray()[0]);
			attributeMeans = relativeMeanForSelected;
			}
		
	}
	
	public double[] getAttributeMeans() {
		return attributeMeans;
	}
	
	public int[] getOccurences() {
		int numNumericAttributes = getNumericAttributes().size();
		
		double[] occurenceValue = new double[numNumericAttributes];
		int[] occurenceTotal = new int[numNumericAttributes];
		int i;
		int k = 0;
		for (i = 0; i < instances.numAttributes(); i++) {
			if (instances.attribute(i).isNumeric() && !instances.attribute(i).isDate()) {
				occurenceTotal[k] = 0;
				for(int j = 0; j < instances.numInstances(); j++) {
					if (instances.instance(j).value(instances.attribute(i))!= 0){
						occurenceTotal[k]++;
						}
				}
				occurenceValue[k] = (double) occurenceTotal[k]/ (double) numNumericAttributes;
				k++;
			}
		}
		//return occurenceValue;
		return occurenceTotal;
	}
	
	public boolean[] hoverPoints;

	protected int edgeAttIndex;

	private String edgeAttString = "NodeId";

	public void setHoverPoints(int[] hover) {
		removeHoverPoints();
		for (int i = 0; i < hover.length; i++) {
			hoverPoints[hover[i]] = true;
		}
		fireModelChanged(TPPModelEvent.POINT_SELECTION_CHANGED);
	}
	
	public boolean isPointHovered(int p) {
		return hoverPoints[p];
	}

	/** Are any points selected? */
	public boolean arePointsHovered() {
		for (int p = 0; p < getNumDataPoints(); p++)
			if (isPointHovered(p))
				return true;
		return false;
	}

	public void removeHoverPoints() {
		for (int p = 0; p < getNumDataPoints(); p++)
			hoverPoints[p] = false;
		fireModelChanged(TPPModelEvent.POINT_SELECTION_CHANGED);
	}
	
	public void setGraphEdgeAttribute(String edgeAttribute) {
		Attribute edgeAtt = instances.attribute(edgeAttribute);
		edgeAttIndex = edgeAtt.index();
		edgeAttString = edgeAttribute;
	}
	
	public int getEdgeAttributeIndex() {
		return edgeAttIndex;
	}
	
	public String getEdgeAttributeString() {
		return edgeAttString;
	}

}
