package tpp;

import weka.core.Instances;
import weka.core.TechnicalInformationHandler;
import weka.gui.explorer.Explorer;
import weka.gui.explorer.Explorer.ExplorerPanel;

/**
 * An ExplorerPanel for Targeted Projection Pursuit.
 */
public class TPPExplorerPanel extends TPPPanel implements ExplorerPanel,TPPModelEventListener {

	private Explorer explorer;

	boolean dataStructureChangeTriggeredByTPP;

	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public String getTabTitle() {
		return "Projection Plot";
	}

	@Override
	public String getTabTitleToolTip() {
		return "Explore data using Targeted Projection Pursuit";
	}

	@Override
	public void setExplorer(Explorer e) {
		explorer = e;
	}

	@Override
	public void setInstances(Instances in) {
		// only respond to changes that are triggered by other panels
		if (dataStructureChangeTriggeredByTPP)
			return;
		super.setInstances(in);
		model.addListener(this);
	}

	public void modelChanged(TPPModelEvent e) {
		if (e.getType() == TPPModelEvent.DATA_STRUCTURE_CHANGED) {
			// This version of the Panel has to tell the enclosing Explorer that the data may have changed
			// set a flag indicating that the change in data structure comes
			// from the TPP application, to prevent infinite loops
			dataStructureChangeTriggeredByTPP = true;
			getExplorer().getPreprocessPanel().setInstances(model.getInstances());
			dataStructureChangeTriggeredByTPP = false;
		}
	}

}