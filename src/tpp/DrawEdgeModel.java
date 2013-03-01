package tpp;

public class DrawEdgeModel {
	
	private ScatterPlotModel spModel;
	
	public drawEdgeModel(ScatterPlotModel spModel) {
		
	}

	
	private double[] edgeDrawn = null;
	private double curveFactor = 1.0;
	
	if (spModel.bundledEdges() || spModel.intelligentEdges()) {
		spModel.calculateCentroids();

		centroids = spModel.getCentroids();
		// magnitudes = spModel.getMagnitudes();
		vectors = spModel.getVectors();

		k = new double[(int) Math.pow(centroids.size(), 2)];
		for (double d : k) {
			d = 0.0; // initialise entries in the array.
		}
		System.out.println("k in paint view is: "
				+ k.toString());

		curveFactor = (allConnections.size() / spModel.instances
				.numInstances()) * 100;

		if (spModel.intelligentEdges()) {
			spModel.calculateCentroidRadius();
			spModel.calculateBezierCentroidMidPoints();
			centroidRadii = spModel.getCentroidRadii();
			bezierMidPoints = spModel.getBezierMidPoints();
		}
}
