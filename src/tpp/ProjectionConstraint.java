package tpp;

/**
 * A constraint on what constitutes a valid projection.
 *
 * @author Joe
 *
 */
public interface ProjectionConstraint {

	/**
	 * Whether or not the given projection meets the constraint
	 */
	public boolean isValid(Projection p);

	/**
	 * Find the nearest projection to that given that meets the constraint
	 */
	public Projection findNearestValid(Projection p);

}
