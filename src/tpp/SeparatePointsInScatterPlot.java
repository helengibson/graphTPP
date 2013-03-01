package tpp;

/**
 * Extension of SeparatePoints for viewing the resulting distribution in a
 * scatter plot.
 */
public class SeparatePointsInScatterPlot extends SeparatePoints {

	public SeparatePointsInScatterPlot(TPPModel model) {
		super(model);
		if (!(model instanceof ScatterPlotModel)) {
			throw new RuntimeException();
			
		}
	}

	/**
	 * Find a view that better separates the points
	 */
	public void pursuePerturbation() throws TPPException {
		super.pursuePerturbation();
		((ScatterPlotModel) model).resizePlot();
	}

}
