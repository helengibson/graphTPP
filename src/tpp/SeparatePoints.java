package tpp;

import weka.core.Attribute;
import weka.core.matrix.Matrix;

/**
 * A TargetPerturbation that will perturb the target so that the distribution of
 * points better reflects the value of the separation attribute defined on the
 * TPPModel. If the separation attribute is nominal then this produces a
 * distribution of points that maximises the separation between classes
 */
public class SeparatePoints implements PerturbationPursuit {

	private static final int X = 0;

	private static final int Y = 1;

	/** The attribute that is used for classification */
	private Attribute separationAttribute;

	protected TPPModel model;

	public SeparatePoints(TPPModel model) {
		this.model = model;
	};

	/**
	 * Find a view that better separates the points
	 */
	public void pursuePerturbation() throws TPPException {
		separationAttribute = model.getSeparationAttribute();
		if (separationAttribute == null)
			unsupervisedAttractionRepulsion();
			else {
				if (separationAttribute.isNominal())
					separateByClassification();
				else {
					if (separationAttribute.isNumeric())
						separateByNumeric();
				}
		}

	}

	/**
	 * Perform unsupervised feature selection of the data using an
	 * attraction-repulsion model
	 * 
	 * @throws TPPException
	 */
	private void unsupervisedAttractionRepulsion() throws TPPException {

		if (model.getNumViewDimensions() != 2)
			throw new TPPException("this method is only implented for 2-dimensional output spaces");

		int n = model.getNumDataPoints();
		double[][] aView = model.getView().getArray();
		double[][] movement = new double[model.getNumDataPoints()][2];
		double d;

		// find the mean distance between points
		double total = 0;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < i; j++)
				total += Math.sqrt(Math.pow(aView[i][X] - aView[j][X], 2) + Math.pow(aView[i][Y] - aView[j][Y], 2));

		double mean = total / (0.5 * n * (n - 1));
		double force;

		// attract/repulse each point from each other
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					d = Math.sqrt(Math.pow(aView[i][X] - aView[j][X], 2) + Math.pow(aView[i][Y] - aView[j][Y], 2));
					force = (mean - d) / mean;
					movement[i][X] += (aView[j][X] - aView[i][X]) * force;
					movement[i][Y] += (aView[j][Y] - aView[i][Y]) * force;
				}
			}
		}
		model.setTarget(model.getView().plus(new Matrix(movement)));
		model.pursueTarget();
	}
	private void separateByNumeric() throws TPPException {

		if (model.getNumViewDimensions() != 2)
			throw new TPPException("this method is only implented for 2-dimensional output spaces");

		Matrix perturbation = new Matrix(model.getNumDataPoints(), 2);

		double dValue, dValueMean;
		int n = model.getNumDataPoints();
		System.out.println("Variance of separation attribute = " + model.instances.variance(separationAttribute));

		/**
		 * fairly arbitrary number for reducing the size of teh perturbation.
		 * Adjust at will.
		 */
		double velocity = model.instances.variance(separationAttribute);

		double dX, dY, dXY;
		double dXNorm, dYNorm;
		double[] dTarget = new double[2];

		// For each point (x_i)
		for (int i = 0; i < n; i++) {

			// Find the mean difference in value between this point and all
			// others (mean|f_i-f_j|)
			dValue = 0;
			for (int j = 0; j < n; j++)
				dValue += abs(model.instances.instance(i).value(separationAttribute)
						- model.instances.instance(j).value(separationAttribute));
			dValueMean = dValue / n;

			// For every other point (x_j)
			dTarget[X] = 0;
			dTarget[Y] = 0;
			for (int j = 0; j < n; j++) {
				if (i != j) {
					// find the unit vector pointing away from that other point
					// (x_i-x_j)/|x_i-x_j|
					dX = model.getView().get(i, X) - model.getView().get(j, X);
					dY = model.getView().get(i, Y) - model.getView().get(j, Y);
					dXY = Math.sqrt((dX * dX) + (dY * dY));
					dXNorm = dX / dXY;
					dYNorm = dY / dXY;

					// and move the point away or towards it, depending on the
					// difference in value: the bigger the difference in value,
					// the greater the movement away form the other objects
					dValue = model.instances.instance(i).value(separationAttribute)
							- model.instances.instance(j).value(separationAttribute);
					dTarget[X] += dXNorm * (dValue - dValueMean);
					dTarget[Y] += dYNorm * (dValue - dValueMean);
				}
			}

			// And normalise the movement to get the perturbation of the target
			perturbation.set(i, X, dTarget[X] / velocity);
			perturbation.set(i, Y, dTarget[Y] / velocity);

		}

		model.getTarget().plusEquals(perturbation);
		model.pursueTarget();

	}

	private void separateByClassification() throws TPPException {

		Matrix perturbation = new Matrix(model.getNumDataPoints(), model.getNumViewDimensions());

		// 1. Find the centroids of the classes:
		// TODO we don't need to calculate the centroids in the view each time.
		// just calculate the centroids in the data and then project
		double[][] centroids = new double[separationAttribute.numValues()][model.getNumViewDimensions()];
		// first total up the positions for each class
		double[] numPoints = new double[separationAttribute.numValues()];
		int c, p, od;
		final int numOutputDimensions = model.getNumViewDimensions();
		final int numDataPoints = model.getNumDataPoints();
		for (p = 0; p < numDataPoints; p++) {
			c = (int) model.getInstances().instance(p).value(separationAttribute);
			for (od = 0; od < numOutputDimensions; od++)
				centroids[c][od] += model.getView().get(p, od);
			numPoints[c]++;
		}
		// then divide by the number of points in each class (if this class
		// is not empty)
		for (c = 0; c < separationAttribute.numValues(); c++) {
			if (numPoints[c] > 0) {
				for (od = 0; od < numOutputDimensions; od++)
					centroids[c][od] /= numPoints[c];
			}
		}

		// 2. Move centroids away from other classes (with velocity
		// inversely proportional to distance)
		double distance;
		// first find aggregate movements
		double[][] dCentroid = new double[separationAttribute.numValues()][model.getNumViewDimensions()];
		for (c = 0; c < separationAttribute.numValues(); c++) {
			if (numPoints[c] > 0) {
				for (int c1 = 0; c1 < separationAttribute.numValues(); c1++) {
					// can't move away from self (or empty classes)
					if ((c != c1) && (numPoints[c1] > 0)) {
						distance = 0;
						for (od = 0; od < numOutputDimensions; od++)
							distance += Math.pow(centroids[c][od] - centroids[c1][od], 2);
						for (od = 0; od < numOutputDimensions; od++)
							dCentroid[c][od] += (centroids[c][od] - centroids[c1][od]) / distance;
					}
				}
			}
		}
		// then add aggregate movements to centroids
		for (c = 0; c < separationAttribute.numValues(); c++) {
			if (numPoints[c] > 0) {
				for (od = 0; od < numOutputDimensions; od++)
					centroids[c][od] += dCentroid[c][od];
			}
		}

		// 3. Move points towards their newly separated centroids
		for (p = 0; p < numDataPoints; p++) {
			c = (int) model.getInstances().instance(p).value(separationAttribute);
			for (od = 0; od < numOutputDimensions; od++)
				perturbation.set(p, od, (centroids[c][od] - model.getView().get(p, od)));
		}

		model.setTarget(model.getView().plus(perturbation));
		model.pursueTarget();

	}

	private double abs(double d) {
		return (d < 0 ? -d : d);
	}
}
