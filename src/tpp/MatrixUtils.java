package tpp;

import weka.core.Instances;
import weka.core.matrix.Matrix;

/**
 * Matrix Utilities !!TODO make all methods instance rather than static !!TODO
 * change all these methods to just work on Matrix objects rather than double
 * arrays -- and change the rest of the application to use double arrays less.
 * 
 * @author Joe Faith
 */
public class MatrixUtils {

	/**
	 * Construct a matrix from (the numeric attributes of ) a set of instances.
	 * Nominal and string attributes are ignored.
	 */
	public static Matrix numeric2matrix(Instances instances) {

		int numRows = instances.numInstances();
		int numColumns = instances.numAttributes();
		for (int a = 0; a < instances.numAttributes(); a++)
			if (!instances.attribute(a).isNumeric())
				numColumns--;
		Matrix mx = new Matrix(numRows, numColumns);
		int mc = -1;
		for (int ic = 0; ic < instances.numAttributes(); ic++) {
			if (instances.attribute(ic).isNumeric()) {
				mc++;
				for (int r = 0; r < numRows; r++)
					mx.set(r, mc, instances.instance(r).value(ic));

			}
		}
		return mx;
	}

	public static double[][] distances(double[][] a) {
		int points = a.length;
		int dimensions = a[0].length;
		double[][] distances = new double[points][points];
		double distance;
		for (int p1 = 1; p1 < points; p1++) {
			for (int p2 = 0; p2 < p1; p2++) {
				distance = 0;
				for (int c = 0; c < dimensions; c++)
					distance += Math.pow(a[p1][c] - a[p2][c], 2);
				distances[p1][p2] = Math.sqrt(distance);
				distances[p2][p1] = Math.sqrt(distance);
			}
		}
		return distances;
	}

	/** Return an array comprising the Euclidean norm of each row */
	public static double[] rowNorm2(Matrix mx) {

		double[] norms = new double[mx.getRowDimension()];
		double total;
		int r, c;
		for (r = 0; r < mx.getRowDimension(); r++) {
			total = 0;
			for (c = 0; c < mx.getColumnDimension(); c++)
				total += mx.get(r, c) * mx.get(r, c);
			norms[r] = Math.sqrt(total);
		}
		return norms;

	}

	/**
	 * Sort the array of doubles, returning the indices of the doubles in order
	 * of size -- largest first
	 */
	public static int[] rank(double[] data) {

		// initialise the ranks
		int[] rank = new int[data.length];
		for (int i = 0; i < data.length; i++)
			rank[i] = i;

		// bubble sort the data, sorting the ranks accordingly
		boolean swapped;
		double dtmp;
		int i, j, itmp;
		for (i = 0; i < data.length - 1; i++) {
			swapped = false;
			for (j = 0; j < data.length - 1 - i; j++) {
				if (data[j] < data[j + 1]) {
					// swap the data
					dtmp = data[j];
					data[j] = data[j + 1];
					data[j + 1] = dtmp;
					// swap the corresponding rank
					itmp = rank[j];
					rank[j] = rank[j + 1];
					rank[j + 1] = itmp;
					swapped = true;
				}
			}
		}

		return rank;
	}

	/**
	 * Create a new matrix, the result of multiplying each column of the given
	 * matrix by the corresponding scalar value in the array.
	 */
	public static Matrix columnarTimes(Matrix mx, double[] scalars) {
		Matrix result = (Matrix) mx.clone();
		double scalar;
		int col, row;
		for (col = 0; col < mx.getColumnDimension(); col++) {
			scalar = scalars[col];
			for (row = 0; row < mx.getRowDimension(); row++)
				result.set(row, col, mx.get(row, col) * scalar);
		}
		return result;
	}

	public static String array2String(double[][] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++)
				sb.append(array[i][j]).append("\t");
			sb.append("\n");
		}
		return sb.toString();
	}

	/** The maximum absolute value of components in the matrix. */
	public static double maxAbsValue(Matrix mx) {
		double max = 0;
		int col, row;
		for (col = 0; col < mx.getColumnDimension(); col++) {
			for (row = 0; row < mx.getRowDimension(); row++)
				if (max < abs(mx.get(row, col)))
					max = abs(mx.get(row, col));
		}
		return max;
	}

	/** The maximum absolute value of components in each column of the matrix. */
	public static double[] maxAbsValueCol(Matrix mx) {
		double[] maxs = new double[mx.getColumnDimension()];
		double max = 0;
		int col, row;
		for (col = 0; col < mx.getColumnDimension(); col++) {
			max = 0;
			for (row = 0; row < mx.getRowDimension(); row++) {
				if (max < abs(mx.get(row, col)))
					max = abs(mx.get(row, col));
			}
			maxs[col] = max;
		}
		return maxs;

	}

	/** Find the stdev of an array of doubles. */
	public static double stdev(double[] x) {
		double n = x.length;
		double sum = 0;
		double mean = mean(x);

		// calculate variance
		sum = 0;
		for (int i = 0; i < n; i++)
			sum += square(x[i] - mean);

		// calculate stdev
		double sigma = Math.sqrt(sum / n);
		return sigma;

	}

	/** Find the mean of an array of doubles. */
	public static double mean(double[] x) {
		double sum = 0;

		// calculate mean
		for (int i = 0; i < x.length; i++)
			sum += x[i];
		return sum / x.length;

	}

	/**
	 * Find the local mean -- ie the mean of the n neighbours on either side.
	 */
	public static double[] localMean(double[] x, int neighbours) {

		double localTotal, localMean;
		double[] mean = new double[x.length];

		// for each point (excluding boundary cases)
		for (int i = neighbours; i < x.length - neighbours; i++) {
			// calculate local mean
			localTotal = 0;
			for (int j = i - neighbours; j < i + neighbours; j++)
				localTotal += x[j];
			mean[i] = localTotal / (2 * neighbours + 1);
		}

		// boundary cases
		for (int i = 0; i < neighbours; i++) {
			mean[i] = x[i];
			mean[x.length - i - 1] = x[x.length - i - 1];
		}

		return mean;
	}

	/**
	 * Find the local volatility -- ie the root sum square distance from the
	 * local mean. Local mean is formed from the n neighbours on either side.
	 * Boundary cases are ignored
	 */
	public static double localVolatility(double[] x, int neighbours) {

		double localTotal, localMean, totalLocalVariance = 0;

		// for each point (excluding boundary cases)
		for (int i = neighbours; i < x.length - neighbours; i++) {
			// calculate local mean
			localTotal = 0;
			for (int j = i - neighbours; j < i + neighbours; j++)
				localTotal += x[j];
			localMean = localTotal / (2 * neighbours + 1);
			// calculate total distance from local mean
			totalLocalVariance += square(x[i] - localMean);
		}
		return Math.sqrt(totalLocalVariance / (x.length - 2 * neighbours));
	}

	/** Normalise by dividing by root mean square */
	public static double[] normalise(double[] x) {
		double sum = 0;
		for (int i = 0; i < x.length; i++)
			sum += square(x[i]);
		double rms = Math.sqrt(sum / x.length);
		double norm[] = new double[x.length];
		for (int i = 0; i < x.length; i++)
			norm[i] = x[i] / rms;
		return norm;

	}

	public static double[][] removeRows(double[][] m, int[] rowsToRemove) {
		double[][] result = new double[m.length - rowsToRemove.length][m[0].length];
		int resultRow = 0;
		for (int sourceRow = 0; sourceRow < m.length; sourceRow++)
			if (!in(sourceRow, rowsToRemove)) {
				for (int col = 0; col < m[0].length; col++)
					result[resultRow][col] = m[sourceRow][col];
				resultRow++;
			}
		return result;
	}

	private static boolean in(int value, int[] values) {
		for (int i = 0; i < values.length; i++)
			if (values[i] == value)
				return true;
		return false;
	}

	public static double[] getColumn(Matrix m, int col) {
		double[] column = new double[m.getRowDimension()];
		for (int row = 0; row < m.getRowDimension(); row++)
			column[row] = m.get(row, col);
		return column;
	}

	public static double[] getColumn(double[][] m, int col) {
		double[] column = new double[m.length];
		for (int row = 0; row < m.length; row++)
			column[row] = m[row][col];
		return column;
	}

	public static void setColumn(Matrix m, int col, double[] values) {
		for (int row = 0; row < m.getRowDimension(); row++)
			m.set(row, col, values[row]);
	}

	public static void setColumn(Matrix m, int col, double value) {
		for (int row = 0; row < m.getRowDimension(); row++)
			m.set(row, col, value);
	}

	public static double[] getRow(Matrix m, int r) {
		double[] row = new double[m.getRowDimension()];
		for (int c = 0; c < m.getColumnDimension(); c++)
			row[c] = m.get(r, c);
		return row;
	}

	public static void setRow(Matrix m, int row, double[] values) {
		for (int col = 0; col < m.getColumnDimension(); col++)
			m.set(row, col, values[col]);
	}

	public static double[] abs(double[] x) {
		double[] abs = new double[x.length];
		for (int i = 0; i < x.length; i++)
			abs[i] = abs(x[i]);
		return abs;
	}

	private static double square(double d) {
		return d * d;
	}

	private static double abs(double d) {
		return (d < 0 ? -d : d);
	}

	public static String toString(double[] x) {
		String s = "";
		for (int i = 0; i < x.length; i++)
			s += x[i] + "\t";
		return s;
	}

	public static String toString(int[] x) {
		String s = "";
		for (int i = 0; i < x.length; i++)
			s += x[i] + "\n";
		return s;
	}

	/** add a1 to a2. return the result */
	public static double[] add(double[] a1, double[] a2) {
		double[] a = new double[a1.length];
		for (int i = 0; i < a.length; i++)
			a[i] = a1[i] + a2[i];
		return a;
	}

	/** times a by d. return teh result */
	public static double[] times(double[] a, double d) {
		double[] a1 = new double[a.length];
		for (int i = 0; i < a.length; i++)
			a1[i] = a[i] * d;
		return a1;
	}

	/** matrix multiplication */
	public static double[][] times(double[][] a, double[][] b) {
		int row, col, i;
		double value;
		double[][] result = new double[a.length][b[0].length];
		for (row = 0; row < a.length; row++) {
			for (col = 0; col < b[0].length; col++) {
				value = 0;
				for (i = 0; i < b.length; i++)
					value += a[row][i] * b[i][col];
				result[row][col] = value;
			}
		}
		return result;
	}

	/** Multiply a row by a matrix */
	public static double[] times(double[] a, double[][] b) {
		int col, i;
		double value;
		double[] result = new double[a.length];
		for (col = 0; col < a.length; col++) {
			value = 0;
			for (i = 0; i < b.length; i++)
				value += a[i] * b[i][col];
			result[col] = value;
		}
		return result;
	}

	public static String toString(double[][] a) {
		String s = "";
		for (int r = 0; r < a.length; r++) {
			for (int c = 0; c < a[0].length; c++) {
				s += a[r][c] + "\t";
			}
			s += "\n";
		}
		return s;

	}

	/**
	 * Insert a row of values into the bottom of the given matrix. If matrix is
	 * null then construct a new one
	 */
	public static Matrix insertRow(Matrix m, double[] row) {

		if (m == null)
			return new Matrix(row, 1);

		Matrix m1 = new Matrix(m.getRowDimension() + 1, m.getColumnDimension());
		m1.setMatrix(0, m.getRowDimension() - 1, 0, m.getColumnDimension() - 1,
				m);
		for (int j = 0; j < m1.getColumnDimension(); j++)
			m1.set(m1.getRowDimension() - 1, j, row[j]);
		return m1;

	}

	/** The means of each column */
	public static Matrix columnMeans(Matrix m) {
		Matrix means = new Matrix(1, m.getColumnDimension());
		for (int col = 0; col < means.getColumnDimension(); col++)
			means.set(0, col, MatrixUtils.mean(MatrixUtils.getColumn(m, col)));
		return means;
	}

	/** The means of each column */
	public static double[] columnMeans(double[][] m) {
		double[] means = new double[m[0].length];
		for (int col = 0; col < means.length; col++)
			means[col] = MatrixUtils.mean(MatrixUtils.getColumn(m, col));
		return means;
	}

	/** The stdev of each column */
	public static double[] columnStdev(Matrix m) {
		double[] stdevs = new double[m.getColumnDimension()];
		for (int col = 0; col < stdevs.length; col++)
			stdevs[col] = MatrixUtils.stdev(MatrixUtils.getColumn(m, col));
		return stdevs;
	}

	/** The stdev of each column */
	public static double[] columnStdev(double[][] m) {
		double[] stdevs = new double[m[0].length];
		for (int col = 0; col < stdevs.length; col++)
			stdevs[col] = MatrixUtils.stdev(MatrixUtils.getColumn(m, col));
		return stdevs;
	}

	/** Euclidean length of a row vector */
	public static double length(double[] a) {
		double l = 0;
		for (int i = 0; i < a.length; i++)
			l += (a[i] * a[i]);
		return Math.sqrt(l);

	}

	/** The size of the difference between two arrays */
	public static double dissimilarity(double[][] a, double[][] b) {
		double diss = 0;
		for (int row = 0; row < a.length; row++)
			for (int col = 0; col < a[0].length; col++)
				diss += Math.pow(a[row][col] - b[row][col], 2);
		return Math.sqrt(diss);
	}

	public static void main(String[] args) {
		double[][] a = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };
		double[][] b = new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		System.out.println(MatrixUtils.toString(MatrixUtils.times(a, b)));

	}

	public static double[][] transpose(double[][] a) {
		double[][] t = new double[a[0].length][a.length];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[0].length; j++)
				t[j][i] = a[i][j];
		return t;
	}

	/** The min and max of a set of numbers */
	public static double[] minMax(double[] a) {
		double max = -1;
		double min = -1;
		for (int i = 0; i < a.length; i++) {
			if (a[i] > max)
				max = a[i];
			if (min == -1 || a[i] < min)
				min = a[i];
		}
		return new double[] { min, max };

	}

	/** The logistic function of a double L(x) = 1 / (1+exp(-x) */
	public static double logistic(double x) {
		return 1d / (1 + Math.exp(-x));
	}

	/** The logistic function of an array of doubles */
	public static double[] logistic(double[] x) {
		double[] logs = new double[x.length];
		for (int i = 0; i < x.length; i++)
			logs[i] = logistic(x[i]);
		return logs;
	}
}
