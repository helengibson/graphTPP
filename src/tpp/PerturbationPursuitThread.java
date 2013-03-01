package tpp;

import java.io.Serializable;

/**
 * A thread that will continuously apply a perturbation target, pursue it, and
 * redisplay the results
 * 
 * @author Joe
 * 
 */
public class PerturbationPursuitThread extends Thread implements Serializable {

	boolean pursue;

	private PerturbationPursuit perturbation;

	public PerturbationPursuitThread(PerturbationPursuit perturbation) {
		this.perturbation = perturbation;
	}

	public void setPerturbation(PerturbationPursuit perturbation) {
		this.perturbation = perturbation;
	}

	public void run() {
//		System.out.println("Starting perturbation pursuit of " + perturbation);
		pursue = true;
		if (perturbation != null)
			while (pursue)
				try {
					perturbation.pursuePerturbation();
				} catch (TPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	/**
	 * Calling this method pauses the pertubation pursuit whilst preserving the
	 * life of the thread. Call start() to restart it.
	 */
	public void stopPerturbationPursuit() {
//		System.out.println("Stopping perturbation pursuit");
//		new Exception().printStackTrace();
		pursue = false;
	}

}
