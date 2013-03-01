package tpp;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

public class SmoothButton extends JButton implements MouseListener {

	/** The thread */
	PerturbationPursuitThread thread;

	private ScatterPlotModel spmodel;

	public SmoothButton(ScatterPlotModel spmodel) {
		this.spmodel=spmodel;
		addMouseListener(this);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		if (thread != null)
			thread.stopPerturbationPursuit();
	}

	public void mousePressed(MouseEvent e) {
		if (thread!=null) thread.stopPerturbationPursuit();
		SmoothSeries smoother = new SmoothSeries(spmodel);
		thread = new PerturbationPursuitThread(smoother);
		thread.start();
	}

}
