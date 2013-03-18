package tpp;

import java.awt.geom.AffineTransform;

import weka.core.Attribute;

public class PointModel {
	
	private ScatterPlotModel spModel;
		
	private boolean labels = false;
	private boolean highlightedLabels = false;
	private boolean hoverLabels = false;
	private boolean selectedLabels = false;
	private boolean nodeLabelColor = false;
	private double labelSize = 0.25;
	
	/**
	 * The default size of the markers to display (as a proportion of screen
	 * size).
	 */
	static final double MARKER_DEFAULT = 0.01;
	private double markerSize = MARKER_DEFAULT;
	private double scaledMarkerSize;
	private double markerRange;
	private double minMarkerSize;
	private double markerRadius;
	
	private Attribute sizeAttribute;
	private Attribute fillAttribute;
	private Attribute selectAttribute;
	private Attribute shapeAttribute;
	double sizeAttributeLowerBound;
	double sizeAttributeUpperBound;

	private boolean sizeLabels;
	private boolean filterLabels;

	double labelSizeLowerBound;
	double labelSizeUpperBound;

	private int lowerLabelFilterDegreeBound;

	private int upperLabelFilterDegreeBound;

	
	public PointModel(ScatterPlotModel spModel) {
		this.spModel = spModel;
	}
	
	public void initialise() {
		
	}
	
	public void initiatePointAttributes(){
		shapeAttribute = null;
		sizeAttribute = null;
		fillAttribute = null;
		selectAttribute = spModel.getColourAttribute();
	}
	
	public void removePointAttributes(Attribute at){
		if (getShapeAttribute() == at)
			setShapeAttribute(null);
		if (getSizeAttribute() == at)
			setSizeAttribute(null);
		if (getFillAttribute() == at)
			setFillAttribute(null);
		if (getSelectAttribute() == at)
			setSelectAttribute(null);
	}
	
	public void setMarkerSize(double d) {
		markerSize = d;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}

	public double getMarkerSize() {
		return markerSize;
	}
	
	public void setScaledMarkerSize(double screenWidth) {
		scaledMarkerSize = markerSize * screenWidth;
//		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}

	public double getScaledMarkerSize() {
		return scaledMarkerSize;
	}
	
	public void setMarkerRange(){
		markerRange = scaledMarkerSize * 2;
	}
	
	public double getMarkerRange() {
		return markerRange;
	}
	
	public void setMinMarkerSize() {
		minMarkerSize = scaledMarkerSize * 0.5;
	}
	
	public double getMinMarkerSize() {
		return minMarkerSize;
	}
	
	public void setMarkerRadius(AffineTransform transform) {
		markerRadius = scaledMarkerSize / transform.getScaleX();
	}
	
	public double getMarkerRadius() {
		return markerRadius;
	}
				
	public void showLabels(boolean b) {
		labels = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean labels() {
		return labels;
	}
	
	public void showHightlightedLabels(boolean b) {
		highlightedLabels = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean highlightedLabels() {
		return highlightedLabels;
	}
	
	public void showHoverLabels(boolean b) {
		hoverLabels = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean hoverLabels() {
		return hoverLabels;
	}
	public void showSelectedLabels(boolean b) {
		selectedLabels = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean selectedLabels() {
		return selectedLabels;
	}
	
	public void showNodeLabelColor(boolean b) {
		nodeLabelColor = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean nodeLabelColor() {
		return nodeLabelColor;
	}
	
	public double getLabelSize() {
		return labelSize;
	}

	public void setLabelSize(double t) {
		labelSize = t;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
	
	/** Use the degree to size the labels */
	public boolean sizeLabels() {
		return sizeLabels;
	}
	
	public void setSizeLabels(boolean b){
		sizeLabels = b;
	}
	
	/** Use the degree to filter the view of the labels */
	public boolean filterLabels() {
		return filterLabels;
	}
	
	public void setFilterLabels(boolean b){
		filterLabels = b;
	}
		
	public Attribute getShapeAttribute() {
		return shapeAttribute;
	}

	public Attribute getFillAttribute() {
		return fillAttribute;
	}

	public Attribute getSelectAttribute() {
		return selectAttribute;
	}

	public Attribute getSizeAttribute() {
		return sizeAttribute;
	}
	
	public void setSelectAttribute(Attribute selectAttribute) {
		this.selectAttribute = selectAttribute;
	}
	
	/*
	 * Set which attribute will be used to set the point size
	 */
	public void setSizeAttribute(Attribute at) {
		this.sizeAttribute = at;

		// if the size attribute is numeric then find its range
		if (at != null && at.isNumeric()) {
			double v;
			sizeAttributeLowerBound = spModel.getInstances().instance(0).value(at);
			sizeAttributeUpperBound = spModel.getInstances().instance(0).value(at);
			for (int i = 1; i < spModel.getInstances().numInstances(); i++) {
				v = spModel.getInstances().instance(i).value(at);
				if (v > sizeAttributeUpperBound)
					sizeAttributeUpperBound = v;
				if (v < sizeAttributeLowerBound)
					sizeAttributeLowerBound = v;
			}
		}
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
	
	public void setSizeOnDegree(int[] degree, double lowest, double highest) {
		// assign these values to the sizing bounds
		double v;
		sizeAttributeLowerBound = lowest;
		sizeAttributeUpperBound = highest;
		for (int j = 0; j < spModel.getInstances().numInstances(); j++) {
			v = degree[j];
			if (v > sizeAttributeUpperBound)
				sizeAttributeUpperBound = v;
			if (v < sizeAttributeLowerBound)
				sizeAttributeLowerBound = v;
		}
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
	
	public void resetSizes(Attribute sizeAttribute) {
		this.sizeAttribute = sizeAttribute;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
	
	public void setShapeAttribute(Attribute shapeAttribute) {
		this.shapeAttribute = shapeAttribute;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}

	public void setFillAttribute(Attribute fillAttribute) {
		this.fillAttribute = fillAttribute;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
	
	public void setLabelSizeOnDegree(int[] degree, double lowest, double highest) {
		// assign these values to the sizing bounds
		double v;
		labelSizeLowerBound = lowest;
		labelSizeUpperBound = highest;
		for (int j = 0; j < spModel.getInstances().numInstances(); j++) {
			v = degree[j];
			if (v > labelSizeUpperBound)
				sizeAttributeUpperBound = v;
			if (v < labelSizeLowerBound)
				sizeAttributeLowerBound = v;
		}
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}

	public void setLowerFilterDegreeRange(int lowerValue) {
		lowerLabelFilterDegreeBound = lowerValue;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}

	public void setUpperFilterDegreeRange(int upperValue) {
		upperLabelFilterDegreeBound = upperValue;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}
	
	public int getLowerLabelFilterDegreeBound() {
		return lowerLabelFilterDegreeBound;
	}
	
	public int getUpperLabelFilterDegreeBound() {
		return upperLabelFilterDegreeBound;
	}

}
