package tpp;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JOptionPane;

import processing.core.PVector;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * A TPPModel with added members for allowing it to be presented and manipulated
 * using via a Scatter Plot GUI
 * Updated by 
 * @author Helen
 * from orignal TPP version
 *
 */
public class ScatterPlotModel extends TPPModel implements Cloneable {
	
	/** The filtered instances */
	private Instances filteredInstances;
	private boolean useFilteredInstances = false;
	private boolean zeroInstances = false;
	
	/** Noise added to the view to better separate the points */
	private Matrix noise;
	private Stack<ScatterPlotModel> snapshots;
	
	private PointModel pointModel;
	private EdgeModel edgeModel;
	private GraphModel graphModel;
	
	/**
	 * How big is the margin round the points as a proportion of the overall
	 * window
	 */
	private static final double PANEL_MARGIN = .1d; // margin around points as a proportion of screen size
	private AffineTransform transform; // Affine transform from device space to screen space
	private int width; // Width of device space
	private int height; // Height of device space
	
	protected boolean showAxes = false;
	protected boolean[] isAxisSelected;
	
	protected boolean showTarget;
	
	protected boolean showSeries = false;
	protected boolean showHierarchicalClustering;
	
	protected boolean showGraph = false;
	protected boolean graphLoaded = false;
	
	protected Rectangle rectangle;
	private static final double MARGIN = 0.1;

	protected int transparencyLevel = 25;
	protected int currentTransparency;
	
	protected ColourScheme colors = ColourScheme.DARK;
	private String spectrumColor = "Default";
	private String classColor = "Default";
	private String bgColor = "Light";
	private Attribute colourAttribute;
	double colorAttributeLowerBound;
	double colorAttributeUpperBound;
	
	private DBConnection dbConnection;
	
	// == Initialisation ================

	public ScatterPlotModel(int n) {
		super(n);
		edgeModel = new EdgeModel(this);
		pointModel =  new PointModel(this);
		graphModel = new GraphModel(null, this);
		pointModel.initialise();
	}

	/*
	 * 
	 */
	public void setInstances(Instances data, boolean filtered) throws Exception {
		initialise(data, filtered);
	}
		
	protected void initialise(Instances ins, boolean filtered) throws Exception {
		super.initialise(ins, filtered);
		initRetinalAttributes();
		isAxisSelected = new boolean[getNumDataDimensions()];
//		if(!projection.getZeroInstances().isEmpty()) {
//			zeroInstances = true;
//		}
	}
	
	public boolean zeroInstances(){
		return zeroInstances;
	}
	
	public EdgeModel getEdgeModel() {
		return edgeModel;
	}
	
	public PointModel getPointModel() {
		return pointModel;
	}

	// == POINT SELECTION ===========================
	
	/**
	 * Select the points whose value of the selection attribute is equal to the
	 * given value
	 */
	public void selectPointsByClassValue(String value,
			boolean addToExistingSelection) {
		if (!addToExistingSelection)
			unselectPoints();
		for (int i = 0; i < getNumDataPoints(); i++)
			if (getInstances().instance(i).stringValue(pointModel.getSelectAttribute())
					.equals(value))
				selectPoint(i);
		fireModelChanged(TPPModelEvent.POINT_SELECTION_CHANGED);
	}

	/**
	 * Select the points whose value of the selection attribute is in the given
	 * range.
	 */
	public void selectPointsByNumericRange(double min, double max,
			boolean addToExistingSelection) {
		if (!addToExistingSelection)
			unselectPoints();
		for (int i = 0; i < getNumDataPoints(); i++)
			if (getInstances().instance(i).value(pointModel.getSelectAttribute()) >= min
					&& getInstances().instance(i).value(pointModel.getSelectAttribute()) <= max)
				selectPoint(i);
		fireModelChanged(TPPModelEvent.POINT_SELECTION_CHANGED);
	}
	
	
	// == AXIS SELECTION==================

	/** Unselect the axes */
	public void unselectAxes() {
		isAxisSelected = new boolean[getNumDataDimensions()];
		fireModelChanged(TPPModelEvent.AXIS_SELECTION_CHANGED);
	}

	/** Add a axis to the selection */
	public void selectAxis(int axis) {
		isAxisSelected[axis] = true;
		fireModelChanged(TPPModelEvent.AXIS_SELECTION_CHANGED);
	}

	/** Add axes to the selection */
	public void selectAxes(int[] axes) {
		for (int a : axes)
			isAxisSelected[a] = true;
		fireModelChanged(TPPModelEvent.AXIS_SELECTION_CHANGED);
	}

	/** Are any axes selected? */
	public boolean areAxesSelected() {
		for (int a = 0; a < getNumDataDimensions(); a++)
			if (isAxisSelected[a])
				return true;
		return false;
	}

	public boolean isAxisSelected(int a) {
		return isAxisSelected[a];
	}

	public void unselectAxis(int i) {
		isAxisSelected[i] = false;
		fireModelChanged(TPPModelEvent.AXIS_SELECTION_CHANGED);
	}

	/** Move the selected axes by the (dx,dy) */
	public void moveSelectedAxes(double dx, double dy) {
		for (int a = 0; a < getNumDataDimensions(); a++) {
			if (isAxisSelected(a)) {
				getProjection().set(a, 0, getProjection().get(a, 0) + dx);
				getProjection().set(a, 1, getProjection().get(a, 1) + dy);
			}
		}
		project();
	}

	// == COLOR SCHEME ===============================

	/** Get the color scheme */
	public ColourScheme getColours() {
		return colors;
	}

	/** Set the color scheme */
	public void setColours(ColourScheme colours) {
		colors = colours;
		fireModelChanged(TPPModelEvent.COLOR_SCHEME_CHANGED);
	}
	
	/** Set the background colour */
	public void setBGColor(String selectedItem) {
		bgColor = selectedItem;

	}

	/** Set the colour to differentiate classes by */
	public void setClassColor(String selectedItem) {
		classColor = selectedItem;
	}

	/** Set the colour spectrum to see the difference in numerical sequences*/
	public void setSpectrumColor(String selectedItem) {
		spectrumColor = selectedItem;
	}

	/** Get the background colour */
	public String getBGColor() {
		return bgColor;
	}

	/** Get the class colour */
	public String getClassColor() {
		return classColor;
	}

	/** Get the spectrum colour */
	public String getSpectrumColor() {
		return spectrumColor;
	}

	/**
	 * Set the colours depending on if they are chosen to be viewed by class or
	 * numeric
	 * @param i
	 *            the node the colour applies to
	 * @return
	 */
	Color setColor(int i) {
		Color c = null;
		if (getColourAttribute() == null) {
			c = getColours().getForegroundColor();
		} else if (getColourAttribute().isNominal()) {
			c = getColours().getClassificationColor(
					(int) getInstances().instance(i)
							.value(getColourAttribute()));
		} else if (getColourAttribute().isNumeric()) {
			c = getColours().getColorFromSpectrum(
					getInstances().instance(i)
							.value(getColourAttribute()),
					colorAttributeLowerBound,
					colorAttributeUpperBound);
		}
		return c;
	}

	// == RETINAL ATTRIBUTES ===========================

	/** The color attributes, as shape, fill and size are specific to the points
	 * they are controlled by the pointmodel whilst colour affects both
	 * points and edges and so is controlled at a higher level here
	 */
	public Attribute getColourAttribute() {
		return colourAttribute;
	}
	
	public void setColourAttribute(Attribute at) {
		this.colourAttribute = at;

		// if the color attribute is numeric then find its range
		if (at != null && at.isNumeric()) {
			double v;
			colorAttributeLowerBound = getInstances().instance(0).value(at);
			colorAttributeUpperBound = getInstances().instance(0).value(at);
			for (int i = 1; i < getInstances().numInstances(); i++) {
				v = getInstances().instance(i).value(at);
				if (v > colorAttributeUpperBound)
					colorAttributeUpperBound = v;
				if (v < colorAttributeLowerBound)
					colorAttributeLowerBound = v;
			}
		}
		fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
	
	public int getTransparencyLevel() {
		return transparencyLevel;
	}

	public void setTransparencyLevel(int t) {
		transparencyLevel = t;
		fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
		
	public int getTransparency() {
		return currentTransparency;
	}

	public void setTransparency(int t) {
		currentTransparency = t;
	}

	void initRetinalAttributes() {
		if (getInstances() != null && getInstances().classIndex() >= 0)
			colourAttribute = getInstances().classAttribute();
		else
			colourAttribute = null;
		pointModel.initiatePointAttributes();
		fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}

	/** Remove an attribute, returning the values */
	public double[] removeAttribute(Attribute at) {
		// before removing an attribute from a scatter plot first check whether
		// it is currently being used to draw the markers
		removeRetinalAttribute(at);
		return super.removeAttribute(at);
	}

	/** remove multiple attributes */
	public void removeAttributes(Vector<Attribute> attributes) {
		takeSnapshot();
		for (Attribute at : attributes)
			removeRetinalAttribute(at);
		unselectAxes();
		int[] atx = new int[attributes.size()];
		for (int a = 0; a < atx.length; a++)
			atx[a] = indexOf(attributes.get(a));
		Remove remove = new Remove();
		remove.setAttributeIndicesArray(atx);
		try {
			remove.setInputFormat(instances);
			instances = Filter.useFilter(instances, remove);
			// reinitialise the projection and data etc, preserving the
			// projection
			// NB we have to do this since the Remove filter messes up
			// references to color attributes etc
			double[][] oldProjectionValues = getProjection().copy().getArray();
			initialise(instances, false);
			// copy back the values of the projection (except those from the
			// removed attributes)
			((LinearProjection) getProjection()).setValues(MatrixUtils
					.removeRows(oldProjectionValues, atx));
			project();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
	}

	/**
	 * If this attribute is being used as a retinal attribute, then set that
	 * attribute to null
	 */
	private void removeRetinalAttribute(Attribute at) {
		pointModel.removePointAttributes(at);
		if (getColourAttribute() == at)
			setColourAttribute(null);
	}
	
	// == Selection rectangle =========================

	/** Initialise a rectangle with corners at the given points */
	public void initRectangle(Point2D p1, Point2D p2) {
		rectangle = new Rectangle(p1, p2);
	}

	/** Create a rectangle that includes all selected points */
	public void drawRectangleAroundSelectedPoints() {
		double xMin = 0, xMax = 0, yMin = 0, yMax = 0;
		boolean firstSelectedPoint = true;
		for (int i = 0; i < getNumDataPoints(); i++) {
			if (isPointSelected(i)) {
				if (xMin > getView().get(i, 0) || firstSelectedPoint)
					xMin = getView().get(i, 0);
				if (xMax < getView().get(i, 0) || firstSelectedPoint)
					xMax = getView().get(i, 0);
				if (yMin > getView().get(i, 1) || firstSelectedPoint)
					yMin = getView().get(i, 1);
				if (yMax < getView().get(i, 1) || firstSelectedPoint)
					yMax = getView().get(i, 1);
				firstSelectedPoint = false;
			}
		}
		// recall that minY is the top of the rectangle and maxY is the bottom
		// margin is a percentage of the maximum dimension
		double margin = max(xMax - xMin, yMax - yMin) * MARGIN;
		rectangle = new Rectangle(xMin - margin, yMin - margin, xMax + margin,
				yMax + margin);
		setAttributeMeans();
	}

	/** Create a rectangle that includes all selected axes */
	public void drawRectangleAroundSelectedAxes() {
		double xMin = 0, xMax = 0, yMin = 0, yMax = 0;
		boolean firstSelectedAxis = true;
		for (int i = 0; i < getNumDataDimensions(); i++) {
			if (isAxisSelected(i)) {
				if (xMin > getProjection().get(i, 0) || firstSelectedAxis)
					xMin = getProjection().get(i, 0);
				if (xMax < getProjection().get(i, 0) || firstSelectedAxis)
					xMax = getProjection().get(i, 0);
				if (yMin > getProjection().get(i, 1) || firstSelectedAxis)
					yMin = getProjection().get(i, 1);
				if (yMax < getProjection().get(i, 1) || firstSelectedAxis)
					yMax = getProjection().get(i, 1);
				firstSelectedAxis = false;
			}
		}
		// recall that minY is the top of the rectangle and maxY is the bottom
		// margin is a percentage of the maximum dimension
		double margin = max(xMax - xMin, yMax - yMin) * MARGIN;
		rectangle = new Rectangle(xMin - margin, yMin - margin, xMax + margin,
				yMax + margin);
	}

	/** Select any points within the rectangle */
	public void selectPointsByRectangle() {
		if (rectangle != null) {
			for (int i = 0; i < getNumDataPoints(); i++)
				selectedPoints[i] = rectangle.contains(getView().get(i, 0),
						getView().get(i, 1));
			setAttributeMeans();
			fireModelChanged(TPPModelEvent.POINT_SELECTION_CHANGED);
		}
	}

	/** Select any axes within the rectangle */
	public void selectAxesByRectangle() {
		if (rectangle != null) {
			for (int a = 0; a < getNumDataDimensions(); a++)
				isAxisSelected[a] = rectangle.contains(getProjection()
						.get(a, 0), getProjection().get(a, 1));
			fireModelChanged(TPPModelEvent.AXIS_SELECTION_CHANGED);
		}
	}

	private double max(double a, double b) {
		return (a < b ? b : a);
	}

	// == Axes =======================

	/** Whether to show axes */
	public void setShowAxes(boolean b) {
		showAxes = b;
		fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean showAxes() {
		return showAxes;
	}

	// == Target =======================
	
	/** Whether to show the target currently being pursued */
	public boolean showTarget() {
		return showTarget;
	}

	public void setShowTarget(boolean showTarget) {
		this.showTarget = showTarget;
		fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	// == Series =======================
	
	public void createSeries(Attribute indexAttribute, Attribute idAttribute) {
		super.createSeries(indexAttribute, idAttribute);
		setShowSeries(true);
	}

	public void removeSeries() {
		super.removeSeries();
		setShowSeries(false);
	}

	/** whether to show series */
	public boolean showSeries() {
		return showSeries;
	}

	/** Show any series in the data */
	public void setShowSeries(boolean show) {
		showSeries = show;
		fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	// == Hierarchical clustering =======================
	
	public void setShowHierarchicalClustering(boolean show) {
		showHierarchicalClustering = show;
		fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean showHierarchicalClustering() {
		return showHierarchicalClustering;
	}

	// == Test set  =======================
	
	public Attribute createTestSet(int k) {
		super.createTestSet(k);
		pointModel.setFillAttribute(getTestAttribute());
		return getTestAttribute();
	}

	// == Transforming date space into device space ===================

	/**
	 * Resize the scatter plot so that it fits in the new window size
	 * 
	 * @return
	 */
	public void resizePlot(int width, int height) {
		this.width = width;
		this.height = height;
		transform = getTransform(width, height);
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/** Rescale the scatter plot so that it fits into the existing window */
	public void resizePlot() {
		transform = getTransform(width, height);
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
	}

	/**
	 * Return a transform that will map data space onto device space of the
	 * given width and height, so that the points (and axes, if shown) fit
	 * snugly into the panel
	 * 
	 */
	private AffineTransform fitPointsToWindowAtCurrentProjection(double width,
			double height) {

		// Make sure the origin is included in the range of points
		double xmax = 0;
		double ymax = 0;
		double xmin = 0;
		double ymin = 0;

		// find range of values in the current view
		// (both points and axes -- if shown)
		for (int row = 0; row < getNumDataPoints(); row++) {
			if (getView().get(row, 0) > xmax)
				xmax = getView().get(row, 0);
			if (getView().get(row, 1) > ymax)
				ymax = getView().get(row, 1);
			if (getView().get(row, 0) < xmin)
				xmin = getView().get(row, 0);
			if (getView().get(row, 1) < ymin)
				ymin = getView().get(row, 1);
		}

		if (showAxes())
			for (int axis = 0; axis < getNumDataDimensions(); axis++) {
				if (getProjection().get(axis, 0) > xmax)
					xmax = getProjection().get(axis, 0);
				if (getProjection().get(axis, 0) < xmin)
					xmin = getProjection().get(axis, 0);
				if (getProjection().get(axis, 1) > ymax)
					ymax = getProjection().get(axis, 1);
				if (getProjection().get(axis, 1) < ymin)
					ymin = getProjection().get(axis, 1);
			}

		// The same scaling factor is used on both x and y axes (so that the
		// overall proportions of the selected view are preserved)
		// So find what scaling factor will give the best fit.
		// NB this can end up with large scaling factors which can cause
		// problems:
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6294396
		int MARGIN = (int) ((width < height ? width : height) * PANEL_MARGIN);
		double xRange = xmax - xmin;
		double yRange = ymax - ymin;
		double scaleX = (width - (2 * MARGIN)) / xRange;
		double scaleY = (height - (2 * MARGIN)) / yRange;
		double scale = (scaleX > scaleY ? scaleY : scaleX);
		// System.out.println("scaleX=" + scaleX + "\tscaleY=" + scaleY +
		// "\tscale=" + scale);
		double translationX = (-xmin * scale) + MARGIN;
		double translationY = (-ymin * scale) + MARGIN;
		AffineTransform transform = new AffineTransform();
		transform.setTransform(scale, 0, 0, scale, translationX, translationY);
		return transform;
	}

	/**
	 * Return a transform that will map data space onto device space of the
	 * given width and height, so that the points (and axes, if shown) fit
	 * snugly into the panel, adjusting the projection so that the scale of the
	 * transform is within reasonable limits. <br>
	 * (See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6294396 and
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4982427 for more on
	 * reasonable limits for AffineTransforms)
	 * 
	 */
	public AffineTransform getTransform(double width, double height) {
		AffineTransform transform = null;

		// only rescale if the panel is not zero
		if (width > 0 && height > 0) {

			// first scale the projection so that it fits into the current
			// window
			transform = fitPointsToWindowAtCurrentProjection(width, height);
			// NB this can produce large/small scaling factors which can
			// cause problems, so adjust the scale so that it is within
			// reasonable limits
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6294396
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4982427

			while (transform.getScaleX() > 100) {
				projection.timesEquals(2);
				project();
				transform = fitPointsToWindowAtCurrentProjection(width, height);
			}
			while (transform.getScaleX() < 1) {
				projection.timesEquals(0.5);
				project();
				transform = fitPointsToWindowAtCurrentProjection(width, height);
			}
		}
		// System.out.println("transform changed");
		fireModelChanged(TPPModelEvent.PROJECTION_CHANGED);
		return transform;
	}

	public AffineTransform getTransform() {
		return transform;
	}

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
	}

	public ScatterPlotModel clone() {
		ScatterPlotModel clone = new ScatterPlotModel(numViewDimensions);
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

	/** Take a snapshot of the current state of the model */
	private void takeSnapshot() {
		if (snapshots == null)
			snapshots = new Stack<ScatterPlotModel>();
		snapshots.push(this.clone());
	}

	/** Undo any changes back to the previous snapshot */
	public void undo() {
		if (snapshots != null && snapshots.size() > 0) {
			ScatterPlotModel previous = snapshots.pop();
			try {
				initialise(previous.instances, false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			projection = previous.projection;
			project();
			fireModelChanged(TPPModelEvent.DATA_STRUCTURE_CHANGED);
		}
	}

	/** Whether there is a snapshot to undo to. */
	public boolean canUndo() {
		return (snapshots != null && snapshots.size() > 0);
	}

	// ---- Anything to do with the Graph/Network----------------

	// Load the Graph
	public void loadGraph(Graph graph) {
		super.loadGraph(graph);
		setShowGraph(true);
		setGraphLoaded(true);
		graphModel = new GraphModel(graph, this);
	}
	
	public GraphModel getGraphModel(){
		return graphModel;
	}
	
	public void updateGraphModel(GraphModel updatedGraphModel) {
		graphModel = updatedGraphModel;
	}

	// Remove the graph from the model
	public void removeGraph() {
		super.removeGraph();
		setShowGraph(false);
	}

	public void setShowGraph(boolean show) {
		showGraph = show;
		fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean showGraph() {
		return showGraph;
	}

	public void setGraphLoaded(boolean b) {
		graphLoaded = b;
	}

	public boolean graphLoaded() {
		return graphLoaded;
	}

	public void addDatabaseConnection(String username, String password,
			String database, String table) {
		dbConnection = new DBConnection(username, password, database, table);
	}

	public void runQuery() throws SQLException {
		ResultSet rs = null;
		Statement statement = null;
		System.out.println(dbConnection);
		com.mysql.jdbc.Connection db = dbConnection.getConnection();
		String query = "select * from " + dbConnection.getTable() + " where ";
		int j = 0;
		for (int i = 0; i < getInstances().numInstances(); i++) {
			if (isPointSelected(i)) {
				if (j > 0) {
					query += " or ";
				}
				String node = instances.instance(i).stringValue(edgeAttIndex);
				query += " sourceip = '" + node + "' or destip = '" + node
						+ "'";
				j++;
			}

		}
		statement = db.createStatement();
		System.out.println(query);
		rs = statement.executeQuery(query);

		if (rs != null)
			System.out.println("the result set is" + rs.toString());
		else
			System.out.println("result set is null");

		while (rs.next()) {
			System.out.println(rs.getString(2));
		}

		DatabaseTableGUI dbTableGUI = new DatabaseTableGUI(rs);

	}
	
	public void setNoise(){
		noise = new Matrix(getNumDataPoints(),
				getNumDataDimensions());
	}
	
	/** Change the noise */
	public void updateNoise() {
		double scale = getTransform().getScaleX();
		Random ran = new Random();
		for (int i = 0; i < noise.getRowDimension(); i++)
			for (int j = 0; j < noise.getColumnDimension(); j++)
				noise.set(i, j, (ran.nextDouble() - 0.5d) * 20d / scale);
	}
	
	public Matrix getNoise(){
		return noise;
	}
	


	

}
