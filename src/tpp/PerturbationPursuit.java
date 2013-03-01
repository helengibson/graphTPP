package tpp;

import java.io.Serializable;

/**
 * A concrete instance of this class is capable of perturbing the target and
 * pursuing it - ie finding a projection that minimises the difference between
 * the view of the data resulting from teh projection and the revised target and.
 * 
 * @author Joe
 * 
 */
public interface PerturbationPursuit extends Serializable {

	public void pursuePerturbation() throws TPPException;

}
