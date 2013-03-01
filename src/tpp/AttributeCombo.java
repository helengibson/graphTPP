package tpp;

import java.util.Vector;

import javax.swing.JComboBox;

import weka.core.Attribute;

/**
 * A combo box that allows the user to select an attribute -- either a numeric
 * attribute, or a nominal attribute, or any attribute
 * 
 */
public class AttributeCombo extends JComboBox {

	public static final int NUMERIC_ATTRIBUTES = 0;

	public static final int NOMINAL_ATTRIBUTES = 1;

	public static final int ALL_ATTRIBUTES = 2;

	private Vector<Attribute> attributes;

	private AttributeCombo(Vector<String> labels, Vector<Attribute> ats) {
		super(labels);
		this.attributes = ats;
	}

	/**
	 * build a combo box containing attributes from the model
	 * 
	 * @param tpp
	 * @param type
	 *            whether to include NUMERIC_ or NOMINAL_ or ALL_ATTRIBUTES
	 * @param addNull
	 *            whether to add a blank line allowing the user to select no
	 *            attribute at all
	 * @return
	 */
	public static AttributeCombo buildCombo(TPPModel tpp, int type,
			boolean addNull) {

		// find attributes
		Vector<Attribute> ats = new Vector<Attribute>();
		if (addNull)
			ats.add(null);
		if (type == NUMERIC_ATTRIBUTES || type == ALL_ATTRIBUTES)
			ats.addAll(tpp.getNumericAttributes());
		if (type == NOMINAL_ATTRIBUTES || type == ALL_ATTRIBUTES)
			ats.addAll(tpp.getNominalAttributes());

		// create labels
		Vector<String> labels = new Vector<String>();
		for (Attribute at : ats)
			labels.add(at == null ? "None" : at.name());

		return new AttributeCombo(labels, ats);
	}

	public Attribute getSelectedAttribute() {
		if (getSelectedIndex() == -1)
			return null;
		else
			return attributes.get(getSelectedIndex());
	}

	public void setSelectedAttribute(Attribute at) {
		setSelectedIndex(attributes.indexOf(at));
		repaint();
	}
}
