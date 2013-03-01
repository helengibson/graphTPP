package tpp;

import weka.core.matrix.Matrix;

/** A projection from a data-space to a view-space.*/
public interface Projection {

	/** Apply the projection to the given data. */
	public Matrix project(Matrix data);

	/** Find the projection that best maps the data to the target. Return the error. */
	public double pursueTarget(Matrix data, Matrix target);


}
