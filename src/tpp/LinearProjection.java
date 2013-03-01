package tpp;

import java.util.ArrayList;
import java.util.Random;

import weka.core.matrix.Matrix;
import weka.core.matrix.SingularValueDecomposition;

/**
 * A linear projection. the projection is defined by a matrix and projection
 * pursuit is achieved by application of the perceptron learning rule. In other
 * words we treat the projection as a bunch of linear neurons.
 */
public class LinearProjection extends Matrix implements Projection {

	/**
	 *
	 */
	private static final long serialVersionUID = 1343300372331771870L;

	/**
	 * The minimum value allowed when computing PCAs. NB could probably be set
	 * less than this, but use this value for safety. For more information see
	 * http://www.netlib.org/na-digest-html/90/v90n18.html#5
	 */
	private static final double MIN_VALUE = 1E-100;

	/** The allowed margin of error between the current view and the target. */
	private static final double TRAINING_ERROR_MARGIN = 1E-5;

	/**
	 * The default rate for training the neural network. The actual rate used
	 * may be less than this if the network starts to diverge.
	 */
	private static final double TRAINING_RATE = 0.2d;

	/**
	 * Stop training the neural network when the error is less than
	 * TRAINING_ERROR_MARGIN, or the error is not improving by at least this
	 * much ie a value of 0.01 means the error must be improving by one percent
	 * per cycle.
	 */
	private static final double TRAINING_CONVERGENCE_MARGIN = 0.001;

	private static final double TRAINING_MOMENTUM = 0.5d;

	/** The maximum number of epochs to train for */
	private static final int TRAINING_EPOCH_LIMIT = 500;

	private double[][] previousWeights;

	private double rate = -1d;

	private ArrayList<Integer> zeroInstancesList;

	private boolean zeroInstance;

	public LinearProjection(int inputDimensions, int outputDimensions) {
		super(inputDimensions, outputDimensions);
	}

	/** Construct a LinearProjection from a matrix */
	public LinearProjection(Matrix m) {
		super(m.getArray().clone());
	}

	public Matrix project(Matrix data) {
		return data.times(this);
	}

	public double pursueTarget(Matrix data, Matrix target) {
		return pursueTarget(data, target, null);
	}

	public double pursueTargetSingleShot(Matrix data, Matrix target,
			boolean[] inTrainingSet) {
		train(data, target, inTrainingSet, previousWeights, 4d);
		double currentError = project(data).minus(target).normF()
				/ target.normF();
		// System.out.println(currentError);
		return currentError;
	}

	/**
	 * Try to find a projection that best maps the data to the target.
	 * 
	 * @param data
	 * @param target
	 * @param inTrainingSet
	 *            which points have been selected -- we ignore the other ones.
	 *            (if ==null then we use all the points)
	 * @return the resulting error (norm_frobenius(error) /
	 *         norm_frobenius(target))
	 */
	public double pursueTarget(Matrix data, Matrix target,
			boolean[] inTrainingSet) {

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double previousError, currentError = 0;
		rate = TRAINING_RATE / data.getRowDimension();
		double normTarget = normF(target, inTrainingSet);
		if (normTarget < MIN_VALUE)
			throw new RuntimeException("norm of target is zero");
		Matrix currentOutput = project(data);
		if (previousWeights == null)
			previousWeights = getValues(new double[getRowDimension()][getColumnDimension()]);
		previousError = normF(currentOutput.minus(target), inTrainingSet);

		// System.out.println("Initial error: "+previousError);

		int epoch = 0;
		while (++epoch < TRAINING_EPOCH_LIMIT) {

			// if the error is small enough then quit
			if (previousError < TRAINING_ERROR_MARGIN)
				return previousError;

			// else train
			train(data, target, inTrainingSet, previousWeights, rate);

			// find the current error
			currentOutput = project(data);
			currentError = normF(currentOutput.minus(target), inTrainingSet)
					/ normTarget;

			// System.out.println("epoch=" + epoch + "\tRate: " + rate +
			// "\tError: " + currentError);

			// if network has converged then quit
			if (abs(currentError - previousError) / previousError < TRAINING_CONVERGENCE_MARGIN)
				return currentError;

			// if the net diverged by more than 5%then reduce the rate and reset
			// values to
			// previous
			if (currentError > (previousError * 1.05d)) {
				// if we cannot reduce the rate any further then quit
				if (rate < 1E-10)
					return previousError;
				rate = rate * .5d;
				setValues(previousWeights);
			} else {
				previousError = currentError;
				previousWeights = getValues(previousWeights);
			}

		}

		return currentError;
	}

	// Batch train the projection to produce the target when applied to the
	// selected points in the data
	public void train(Matrix data, Matrix target, boolean[] selected,
			double[][] previousWeights, double rate) {
		int i, j, p;

		// use native arrays rather than Matrix classes, to make access faster
		double[][] aData = data.getArray(), aTarget = target.getArray(), aOutput = project(
				data).getArray();

		// the cumulative weight difference for this unit (ie column of the
		// projection)
		double[] cumWeightDiff;

		// the error for this unit applied to a single point (ie row in the
		// data)
		double error;

		// for each unit (ie column of the projection)
		for (j = 0; j < getColumnDimension(); j++) {

			// cumulate a weight difference for all points
			cumWeightDiff = new double[getRowDimension()];
			for (p = 0; p < data.getRowDimension(); p++) {

				if (selected == null || selected[p]) {

					// first find the error for this point
					error = aTarget[p][j] - aOutput[p][j];

					// then use this to calculate a weight difference which is
					// added to the cumulative total
					for (i = 0; i < getRowDimension(); i++)
						cumWeightDiff[i] += error * rate * aData[p][i];
				}
			}

			// then add the cumulative weight difference to the weights
			if (previousWeights != null)
				for (i = 0; i < getRowDimension(); i++)
					set(i, j, A[i][j] + cumWeightDiff[i] + TRAINING_MOMENTUM
							* (A[i][j] - previousWeights[i][j]));
			else
				for (i = 0; i < getRowDimension(); i++)
					set(i, j, A[i][j] + cumWeightDiff[i]);

		}
	}

	/** Calculate the Frobenius norm of the selected points of the matrix */
	private double normF(Matrix mx, boolean[] selected) {
		double n = 0;
		for (int i = 0; i < mx.getRowDimension(); i++)
			if (selected == null || selected[i])
				for (int j = 0; j < mx.getColumnDimension(); j++)
					n += (mx.get(i, j) * mx.get(i, j));
		return n;
	}

	void setValues(double[][] values) {
		A = values;
	}

	/**
	 * Get the current values of the projection, and put them in the supplied
	 * array
	 */
	private double[][] getValues(double[][] values) {
		for (int i = 0; i < values.length; i++)
			for (int j = 0; j < values[0].length; j++)
				values[i][j] = get(i, j);
		return values;
	}

	/** Set all projection values to a random values in [-1,1] */
	public void randomize() {
		Random r = new Random();
		for (int row = 0; row < getRowDimension(); row++)
			for (int col = 0; col < getColumnDimension(); col++)
				set(row, col, r.nextDouble() * 2 - 1);
	}

	/** Scale the projection so that all values are in the range [-1,1]. */
	public void normalise() {
		timesEquals(1 / MatrixUtils.maxAbsValue(this));
	}

	private double abs(double d) {
		return (d > 0 ? d : -d);
	}

	/** Find the principle components of the given data. */
	public void PCA(Matrix data) {

		// The LINPACK SVD algorithm fails if any of the instances are zero
		// see http://www.netlib.org/na-digest-html/90/v90n18.html#5
		// so we remove any zero instances before finding the pca projection;
		// First, copy all non-zero instances into a temporary matrix
		Matrix nonZeroData = new Matrix(data.getRowDimension(),
				data.getColumnDimension());
		zeroInstance= false;
		int zeroInstances = 0;
		zeroInstancesList = new ArrayList();
		for (int i = 0; i < data.getRowDimension(); i++) {
			// assume this is a zero instance until we find a non-zero value
			zeroInstance = true;
			zeroInstances++;
			for (int j = 0; j < data.getColumnDimension(); j++)
				if (abs(data.get(i, j)) > MIN_VALUE) {
					zeroInstance = false;
					zeroInstances--;
					break;
				}
			if (!zeroInstance)
				for (int j = 0; j < data.getColumnDimension(); j++)
					nonZeroData.set(i - zeroInstances, j, data.get(i, j));
			else {
				System.out.println(i + " is a zero instance" + zeroInstance);
				zeroInstancesList.add(i);
			}
		}
		nonZeroData = nonZeroData.getMatrix(0, data.getRowDimension()
				- zeroInstances - 1, 0, data.getColumnDimension() - 1);
		SingularValueDecomposition svd = new SingularValueDecomposition(
				nonZeroData);
		setMatrix(0, getRowDimension() - 1, 0, getColumnDimension() - 1,
				svd.getV());
	}

	/** Create a new projection by removing a single attribute (row) */
	public LinearProjection removeAttribute(int i) {
		LinearProjection newProjection = new LinearProjection(
				getRowDimension() - 1, getColumnDimension());
		int newRow = 0;
		int col;
		for (int oldRow = 0; oldRow < getRowDimension(); oldRow++) {
			if (oldRow != i) {
				for (col = 0; col < getColumnDimension(); col++)
					newProjection.set(newRow, col, this.get(oldRow, col));
				newRow++;
			}
		}
		return newProjection;
	}
	
	public ArrayList<Integer>getZeroInstances() {
		return zeroInstancesList;
	}
	
	public boolean zeroInstances() {
		return zeroInstance;
	}
}
