package tpp;

import weka.core.matrix.Matrix;

/** Smooth series in a scatter plot */
public class SmoothSeries implements PerturbationPursuit {

	private ScatterPlotModel model;

	public SmoothSeries(ScatterPlotModel model) {
		this.model = model;
	}

	/**
	 * Find the perturbations necessary to untangle the currently viewed series
	 * by moving each point nearer to any connected ones.
	 * 
	 * @throws TPPException
	 */
	public void pursuePerturbation() throws TPPException {
		if (model.getNumViewDimensions() == 2) {
			Matrix newTarget = new Matrix(model.getNumDataPoints(), 2);
			int previous, next, numPoints = 0;
			double totaly, totalx;
			// Set teh target fr each position to be the mean of the current,
			// the
			// next, and the previous points
			for (int p = 0; p < model.getNumDataPoints(); p++) {
				// find the next and prev points
				previous = model.getSeries().previous(p);
				next = model.getSeries().next(p);
				numPoints = 1;
				totalx = model.getView().get(p, 0);
				totaly = model.getView().get(p, 1);
				if (previous != -1) {
					totalx += model.getView().get(previous, 0);
					totaly += model.getView().get(previous, 1);
					numPoints++;
				}
				if (next != -1) {
					totalx += model.getView().get(next, 0);
					totaly += model.getView().get(next, 1);
					numPoints++;
				}
				newTarget.set(p, 0, totalx / numPoints);
				newTarget.set(p, 1, totaly / numPoints);
			}
			model.setTarget(newTarget);
			model.pursueTarget();

		} else
			throw new TPPException("2D smoother can only be applied to a 2D target");

	}

	public void setModel(TPPModel model) {
		this.model = (ScatterPlotModel) model;
	}

}
