package tpp;

/** Clamp the weights of the specified attributes to zero */
public class ZeroConstraint implements ProjectionConstraint {
	private final double MIN = 1E-100;
	private int[] attributes;

	/** Specify which attributes to clamp at zero */
	public ZeroConstraint(int[] attributes) {
		this.attributes = attributes;
	}

	public Projection findNearestValid(Projection p) {
		LinearProjection lp = (LinearProjection) p;
		for (int r : attributes)
			for (int c = 0; c < lp.getColumnDimension(); c++)
				lp.set(r, c, 0);
		return lp;
	}

	public boolean isValid(Projection p) {
		LinearProjection lp = (LinearProjection) p;
		for (int r : attributes)
			for (int c = 0; c < lp.getColumnDimension(); c++)
				if (notZero(lp.get(r, c)))
					return false;
		return true;
	}

	private boolean notZero(double d) {
		return (d > MIN || d < -MIN);
	}
}
