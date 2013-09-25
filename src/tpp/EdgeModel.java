package tpp;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Enumeration;

import processing.core.PVector;

import weka.core.Attribute;
import weka.core.Instances;

/**
 * @author Helen
 *
 */
public class EdgeModel {
	
	private ScatterPlotModel spModel;
	private Graph graph;
	
	// An array which holds the number of edges already drawn between each cluster
	private double[][] clusterEdgesDrawn; 
	private double bundleControl = 1;
	private double bundleSpacing;
	
	private int lowEdgeWeightValue;
	private int upperEdgeWeightRange;
	
	private boolean incomingEdges = true;
	private boolean outgoingEdges = true;
	
	private boolean showSourceEdgeColor;
	private boolean showTargetEdgeColor;
	private boolean showDefaultEdgeColor = true;
	private boolean showMixedEdgeColor;
	
	private Attribute currentBundledAttribute;
	private boolean straightEdges = true;
	private float beizerCurviness = 0.1f;
	private boolean bezierEdges;
	private boolean bundledEdges;
	private boolean fannedEdges;
	private boolean intelligentEdges;
	private boolean directed;
	private boolean filterAllEdges;
	private boolean filterEdgesByWeight;
	private boolean viewEdgeWeights;
	
	private ArrayList<Point2D> centroids;
	private PVector[][] vectors;
	private PVector[][] midPoints;
	private double[] centroidRadii;
	
	public EdgeModel(ScatterPlotModel spModel) {
		this.spModel = spModel;
	}
	
	public void initialise() {
		clusterEdgesDrawn = null;
		
		if (bundledEdges() || intelligentEdges()) {
			calculateCentroids();
			calculateVectors();

			centroids = getCentroids();
			vectors = getVectors();

							
			clusterEdgesDrawn = new double[centroids.size()][centroids.size()];
			for (int p = 0; p < centroids.size(); p++) {
				for (int q = 0; q < centroids.size(); q++) {
					clusterEdgesDrawn[p][q] = 0.0;
				}
			}
			
			bundleSpacing = (spModel.getGraph().getAllConnections().size()/centroids.size())/getBundleControl();
			
			System.out.println("Offset: " + bundleSpacing);

			if (intelligentEdges()) {
				calculateCentroidRadius();
				calculateBezierCentroidMidPoints();
				centroidRadii = getCentroidRadii();
				getBezierMidPoints();
			}
		}
	}
	
	public void setBundleControl(double bt) {
		bundleControl = bt;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}
	
	public double getBundleControl(){
		return bundleControl;
	}
	
	public double getBundleSpacing(){
		return bundleSpacing;
	}
	
	public double[][] getClusterEdgesDrawn(){
		return clusterEdgesDrawn;
	}
	
	/** Show the incoming edges of selected nodes(s) */
	public void showIncomingEdges(boolean b) {
		incomingEdges = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean incomingEdges() {
		return incomingEdges;
	}

	/** Show the outgoing edges of selected nodes(s) */
	public void showOutgoingEdges(boolean b) {
		outgoingEdges = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean outgoingEdges() {
		return outgoingEdges;
	}
	
	/** Whether to color the edges the same as the source node */
	public void setSourceColorEdges(boolean b) {
		showSourceEdgeColor = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean sourceColorEdges() {
		return showSourceEdgeColor;
	}

	/** Whether to color the edges the same as the source node */
	public void setTargetColorEdges(boolean b) {
		showTargetEdgeColor = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean targetColorEdges() {
		return showTargetEdgeColor;
	}

	/** Whether to color all the edges the same */
	public void setDefaultColorEdges(boolean b) {
		showDefaultEdgeColor = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean defaultColorEdges() {
		return showDefaultEdgeColor;
	}

	/** Whether to color the edges the same as the source node */
	public void setMixedColorEdges(boolean b) {
		showMixedEdgeColor = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean mixedColorEdges() {
		return showMixedEdgeColor;
	}

	/** Whether to color the edges the same as the source node */
	public void setStraightEdges(boolean b) {
		straightEdges = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean straightEdges() {
		return straightEdges;
	}

	public float getBeizerCurviness() {
		return beizerCurviness;
	}

	public void setBeizerCurviness(float t) {
		beizerCurviness = t;
		spModel.fireModelChanged(TPPModelEvent.RETINAL_ATTRIBUTE_CHANGED);
	}

	/** Whether to shape the edges as bezier curves */
	public void setBezierEdges(boolean b) {
		bezierEdges = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean bezierEdges() {
		return bezierEdges;
	}

	public void setBundledEdges(boolean b) {
		bundledEdges = b;
		currentBundledAttribute = spModel.getSeparationAttribute();
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}
	
	public Attribute getCurrentBundledAttribute() {
		return currentBundledAttribute;
	}

	public boolean bundledEdges() {
		return bundledEdges;
	}

	public void setFannedEdges(boolean b) {
		fannedEdges = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean fannedEdges() {
		return fannedEdges;
	}

	public void setIntelligentEdges(boolean b) {
		intelligentEdges = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean intelligentEdges() {
		return intelligentEdges;
	}

	/** Whether to add an arrow to each edge to indicate direction */
	public void setDirected(boolean b) {
		directed = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean directed() {
		return directed;
	}

	/** Filter all edges from view until nodes are selected */
	public void setFilterAllEdges(boolean b) {
		filterAllEdges = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}
	
	public boolean filterAllEdges() {
		return filterAllEdges;
	}
	
	/** Filter edges based on their weight */
	public void setFilterEdgesByWeight(boolean b) {
		filterEdgesByWeight = b;
		spModel.fireModelChanged(TPPModelEvent.CONTROL_PANEL_UPDATE);
	}

	public boolean filterEdgesByWeight() {
		return filterEdgesByWeight;
	}
	
	/** Reflect the edge weights in the thickness of the edges */
	public void setViewEdgeWeights(boolean b) {
		viewEdgeWeights = b;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}

	public boolean viewEdgeWeights() {
		return viewEdgeWeights;
	}

	public double getMinEdgeWeight() {
		ArrayList<Connection> allConnections = spModel.getGraph().getAllConnections();
		double min = allConnections.get(0).getEdgeWeight();
		for (int i = 1; i < allConnections.size(); i++){
			if (allConnections.get(i).getEdgeWeight() < min)
				min = allConnections.get(i).getEdgeWeight();
		}
		return min;
	}
	
	public double getMaxEdgeWeight() {
		ArrayList<Connection> allConnections = spModel.getGraph().getAllConnections();
		double max = allConnections.get(0).getEdgeWeight();
		for (int i = 1; i < allConnections.size(); i++){
			if (allConnections.get(i).getEdgeWeight() > max)
				max = allConnections.get(i).getEdgeWeight();
		}
		return max;
	}
	
	public void setLowerEdgeWeightRange(int lowerValue){
		lowEdgeWeightValue = lowerValue;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}
	
	public void setUpperEdgeWeightRange(int upperValue){
		upperEdgeWeightRange = upperValue;
		spModel.fireModelChanged(TPPModelEvent.DECORATION_CHANGED);
	}
	
	public int getLowerEdgeWeightRange(){
		return lowEdgeWeightValue;
	}
	
	public int getUpperEdgeWeightRange(){
		return upperEdgeWeightRange;
	}
	
	public void calculateCentroids() {

		Attribute sepAtt = currentBundledAttribute;
		Instances instances = spModel.getInstances();

		centroids = new ArrayList<Point2D>();

		if (sepAtt.isNominal()) {
			Enumeration classValues = sepAtt.enumerateValues();
			int i = 0;
			String classValue;
			while (classValues.hasMoreElements()) {
				classValue = (String) classValues.nextElement();
//				System.out.println(classValue);
				int k = 0;
				double xcoordsum = 0;
				double ycoordsum = 0;
				for (int j = 0; j < spModel.getNumDataPoints(); j++) {
					if (instances.get(j).toString(sepAtt)
							.replaceAll("^\'|\'$", "").equals(classValue)) {
						xcoordsum += spModel.getTarget().get(j, 0);
						ycoordsum += spModel.getTarget().get(j, 1);
						k++;
					}
				}
				centroids.add(new Point2D.Double(xcoordsum / k, ycoordsum / k));
				i++;
			}
		}
	}
	
	/**
	 * Calculate the vectors in between each pair of clusters
	 */
	public void calculateVectors() {
		int cs = centroids.size();
		vectors = new PVector[cs][cs];
		for (int i = 0; i < cs; i++) {
			for (int j = 0; j < cs; j++) {
				PVector p = new PVector((float) centroids.get(i).getX(),
						(float) centroids.get(i).getY());
				p.sub(new PVector((float) centroids.get(j).getX(),
						(float) centroids.get(j).getY()));
				// p.mult(0.75f);
				p.normalize();
				vectors[i][j] = p;
			}
		}
	}


	public ArrayList<Point2D> getCentroids() {
		return centroids;
	}
	
	public PVector[][] getVectors() {
		return vectors;
	}
	
	public double[] getCentroidRadii() {
		return centroidRadii;
	}
	
	public void calculateCentroidRadius() {
		
		Attribute sepAtt = currentBundledAttribute;
		Instances instances = spModel.getInstances();
		centroidRadii =  new double[sepAtt.numValues()];
		
		if (sepAtt.isNominal()) {
			Enumeration classValues = sepAtt.enumerateValues();
			String classValue;
			int i = 0;
			while (classValues.hasMoreElements()) {
				classValue = (String) classValues.nextElement();
				int k = 0;
				double maxRadius = 0;
				for (int j = 0; j < spModel.getNumDataPoints(); j++) {
					// System.out.println(instances.get(j).toString(sepAtt) +
					// " : " + classValue);
					if (instances.get(j).toString(sepAtt)
							.replaceAll("^\'|\'$", "").equals(classValue)) {
						double xcoord = spModel.getTarget().get(j, 0);
						double ycoord = spModel.getTarget().get(j, 1);
						double radius = Math.sqrt(Math.pow(xcoord, 2)+ Math.pow(ycoord, 2));
						if (radius > maxRadius)
							maxRadius = radius;
					}
				}
				centroidRadii[i] = maxRadius;
				i++;
			}
		}
	}
	
	public void calculateBezierCentroidMidPoints() {
		int cs = centroids.size();
		midPoints = new PVector[cs][cs];
		for (int i = 0; i < cs; i++) {
			for (int j = 0; j < cs; j++) {

				Point2D sourceCentroid = centroids.get(i);
				Point2D targetCentroid = centroids.get(j);

				double scx = sourceCentroid.getX();
				double scy = sourceCentroid.getY();

				double tcx = targetCentroid.getX();
				double tcy = targetCentroid.getY();
							 
				// find the midpoint of the bezier curve between the two centroids
				PVector p2 = new PVector((float) tcx, (float) tcy); // first centroid to mid point of centroids
				p2.sub(new PVector((float) scx, (float) scy));
				float lengthp2 = p2.mag();
				p2.normalize();

				float factorp2 = 0.1f * lengthp2;

				// normal vector to the edge
				PVector n2 = new PVector(p2.y, -p2.x);
				n2.mult(factorp2);

				// first control point
				PVector c1p2 = new PVector(p2.x, p2.y);
				c1p2.mult(factorp2);
				c1p2.add(new PVector((float) scx, (float) scy));
				c1p2.add(n2);

				// second control point
				PVector c2p2 = new PVector(p2.x, p2.y);
				c2p2.mult(-factorp2);
				c2p2.add(new PVector((float) tcx, (float) tcy));
				c2p2.add(n2);
				 
				double bmpx = (0.125 * (scx + tcx)) + (0.375 * (c1p2.x + c2p2.x));
				double bmpy = (0.125 * (scy + tcy)) + (0.375 * (c1p2.y + c2p2.y));
				 
				midPoints[i][j] = new PVector((float)bmpx, (float)bmpy);
				
			}
		}
				
	}
	
	public PVector[][] getBezierMidPoints() {
		return midPoints;
	}
}
