package tpp;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComboBox;

import weka.core.Attribute;

public class SeparatePointsButton extends JButton implements MouseListener {

	/** The thread */
	PerturbationPursuitThread thread;

	private JComboBox separateCombo;

	private ScatterPlotModel spModel;

	public SeparatePointsButton(ScatterPlotModel spModel, JComboBox separateAttributeSelectorCombo) {
		this.spModel = spModel;
		this.separateCombo = separateAttributeSelectorCombo;
		addMouseListener(this);
	}

	public void mouseClicked(MouseEvent e) {

		// kill any old threads
		if (thread != null)
			thread.stopPerturbationPursuit();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		// kill any old threads
		if (thread != null)
			thread.stopPerturbationPursuit();
	}

	public void mousePressed(MouseEvent e) {
		// kill any old threads
		if (thread != null)
			thread.stopPerturbationPursuit();

		// create and start separator thread
		if (isEnabled()) {
			Attribute sep = spModel.getAttributeByName((String) separateCombo.getSelectedItem());
			spModel.setSeparationAttribute(sep);
			SeparatePointsInScatterPlot separator = new SeparatePointsInScatterPlot(spModel);
			thread = new PerturbationPursuitThread(separator);
			thread.start();
		}
	}

}
