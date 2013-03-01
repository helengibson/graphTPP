package tpp;

import javax.swing.Icon;

import weka.gui.beans.KnowledgeFlowApp;

/**
 * A KnowledgeFlow Perspective for Targeted Projection Pursuit
 */
public class TPPPerspective extends TPPPanel implements KnowledgeFlowApp.KFPerspective {

	/**
	 * Get the title of this perspective
	 * 
	 * @return the title of this perspective
	 */
	public String getPerspectiveTitle() {
		return "Projection Plot";
	}

	/**
	 * Get the tool tip text for this perspective.
	 * 
	 * @return the tool tip text for this perspective
	 */
	public String getPerspectiveTipText() {
		return "Explore data using Targeted Projection Pursuit";
	}

	/**
	 * Get the icon for this perspective.
	 * 
	 * @return the Icon for this perspective (or null if the perspective does
	 *         not have an icon)
	 */
	public Icon getPerspectiveIcon() {
		java.awt.Image pic = null;
		System.out.println("Class loader root at " + this.getClass().getClassLoader().getResource("."));
		java.net.URL imageURL = this.getClass().getClassLoader().getResource("tpp/tpp.png");
		if (imageURL == null) {
			System.out.println("failed to find icon for TPP Perspective at " + imageURL);
			return null;
		} else {
			pic = java.awt.Toolkit.getDefaultToolkit().getImage(imageURL);
			return new javax.swing.ImageIcon(pic);
		}
	}

	/**
	 * Make this perspective the active (visible) one in the KF
	 * 
	 * @param active
	 *            true if this perspective is the currently active one
	 */
	public void setActive(boolean active) {
	}

	/**
	 * Tell this perspective whether or not it is part of the users perspectives
	 * toolbar in the KnowledgeFlow. If not part of the current set of
	 * perspectives, then we can free some resources.
	 * 
	 * @param loaded
	 *            true if this perspective is part of the user-selected
	 *            perspectives in the KnowledgeFlow
	 */
	public void setLoaded(boolean loaded) {
	}

	/**
	 * Set a reference to the main KnowledgeFlow perspective - i.e. the
	 * perspective that manages flow layouts.
	 * 
	 * @param main
	 *            the main KnowledgeFlow perspective.
	 */
	public void setMainKFPerspective(KnowledgeFlowApp.MainKFPerspective main) {
	}

	/**
	 * Returns true if this perspective accepts instances
	 * 
	 * @return true if this perspective can accept instances
	 */
	public boolean acceptsInstances() {
		return true;
	}

}