package tpp;

import weka.core.matrix.Matrix;


/**
 * Responds to movements of a rectangular selection of points by using projection pursuit
 */
public class RectangleMovementListener {

	// OK its not really a listener but it fulfills a similar function. 
	
	/**
	 * Coefficient of elasiticity: ie how much connected points are dragged with
	 * each other
	 */
	private static final double ELASTICITY = 0.5;

	/** how to update the target in response to translations */
	private Matrix dTarget;
	
	/** How to update the target in response to scalings */
	private Matrix zTarget;

	private ScatterPlotModel model;

	private Rectangle rectangle;


	/**
	 * Create perturbations that describe how incremental changes to the
	 * selected points by the user will alter the target view in a scatter plot
	 *
	 * @throws TPPException
	 */
	public RectangleMovementListener(ScatterPlotModel model) {

		this.model=model;
		this.rectangle=model.rectangle;

		// create a new target view
		model.setTarget((Matrix) model.getView().clone());

		// These are the translations to the target view that would be the
		// result of moving the selection by a single pixel x and y in the device space
		// (recall that a mouse movement down (dy>0) corresponds to a negative
		// movement in the target (dyTarget<0))
		dTarget = new Matrix(model.getNumDataPoints(), 2);
		double dx=1,dy=1;
		for (int p = 0; p < model.getNumDataPoints(); p++) {
			if (model.isPointSelected(p) && !model.isPointInTestingSet(p)) {
				dTarget.set(p, 0, dx);
				dTarget.set(p, 1, dy);
			}
		}

		// The translations to the target view that would be result
		// of expanding the selection by a single pixel x and y
		zTarget = new Matrix(model.getNumDataPoints(), 2);

		// For zoom, we need to know the original offsets from the rectangle
		// center
		double width = rectangle.width() / 2;
		double height = rectangle.height() / 2;
		// and center of rectangle
		double cx = rectangle.centerX();
		double cy = rectangle.centerY();

		// the position of the point under consideration
		double x, y;
		for (int p = 0; p < model.getNumDataPoints(); p++) {
			if (model.isPointSelected(p) && !model.isPointInTestingSet(p)) {
				x = model.getView().get(p, 0);
				y = model.getView().get(p, 1);
				// (recall that a mouse movement down (dy>0) corresponds to a
				// negative movement in the target (zTarget.y<0));
				zTarget.set(p, 0, (x - cx) / width);
				zTarget.set(p, 1, (cy - y) / height);
			}
		}

		// If there are series currently defined then we also need to include
		// the effect of 'elasticity'
		// in which movements to selected points drag connected points with them
		if (model.showSeries() && model.getSeries() != null)
			for (int p = 0; p < model.getNumDataPoints(); p++)
				if (model.isPointSelected(p) && !model.isPointInTestingSet(p)) {
					dragPrevious(p);
					dragNext(p);
				}

	}

	/** Drag the point previous to the current one. */
	private void dragPrevious(int p) {

		// find previous point
		int previous = -1;
		previous = model.getSeries().previous(p);

		// if we haven't reached the end of the series, or another selected
		// point
		if (previous != -1 && !model.isPointSelected(previous)) {
			// then add a proportion of the movement of the selected point to
			// the connected one
			dTarget.set(previous, 0, dTarget.get(previous, 0) + (dTarget.get(p, 0) * ELASTICITY));
			dTarget.set(previous, 1, dTarget.get(previous, 1) + (dTarget.get(p, 1) * ELASTICITY));

			// and drag any point previous connected to it
			dragPrevious(previous);
		}
	}

	/** Drag the point next to the current one.
	 * @param model
	 * @param panel */
	private void dragNext(int p) {

		// find next point
		int next = -1;
		next = model.getSeries().next(p);

		// if we haven't reached the end of the series, or another selected
		// point
		if (next != -1 && !model.isPointSelected(next)) {
			// then add a proportion of the movement of the selected point to
			// the connected one
			dTarget.set(next, 0, dTarget.get(next, 0) + (dTarget.get(p, 0) * ELASTICITY));
			dTarget.set(next, 1, dTarget.get(next, 1) + (dTarget.get(p, 1) * ELASTICITY));

			// and drag any point next connected to it
			dragNext(next);
		}
	}

	/** Move the rectangle by the given increments */
	public void rectangleTranslated(double dx, double dy) {
		model.getTarget().plusEquals(MatrixUtils.columnarTimes(dTarget, new double[] { dx, dy }));
		try {
			model.pursueTarget();
		} catch (TPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/** The rectangle has been scaled in the x and y directions */
	public void rectangleScaled(double zx, double zy) {
		model.getTarget().plusEquals(MatrixUtils.columnarTimes(zTarget, new double[] { zx, zy }));
		try {
			model.pursueTarget();
		} catch (TPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
