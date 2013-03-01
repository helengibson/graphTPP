package tpp;

/**
 * filter an array using a 1D Gaussian filter, sd=1.0
 */
public class Gaussian {

	/** Square root of 2 x pi. */
	private static final float R2PI = 2.5066282f;

	private static final double[] kernel = new double[] { 0.006, 0.061, 0.242, 0.383, 0.242, 0.061, 0.006 };

	public double[] filter(double[] x) {

		double[] y = new double[x.length];

		if (x.length > 6)
			for (int i = 3; i < x.length - 3; i++)
				for (int j = -3; j < 4; j++)
					y[i] += x[i + j] * kernel[j + 3];

		for (int i = 0; i < 3; i++) {
			y[i] = x[i];
			y[y.length - 1 - i] = x[x.length - 1 - i];
		}

		return y;

	}

	public static void main(String[] args) {
		Gaussian filter = new Gaussian();
		System.out.println(MatrixUtils.toString(filter.filter(new double[] { 1, 1, 1, 2, 2, 2, 2, 1, 1, 1 })));
	}

}