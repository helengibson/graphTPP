package tpp.protein;

import java.util.HashMap;
import java.util.Vector;

import weka.core.Attribute;

/**
 * A concatenation of all properties. Each individual residue is represented by
 * a sequences of real values: one for each of the properties in
 * AminoAcidProperty.ALL_PROPERTIES
 * 
 * @author Joe
 * 
 */
public class AminAcidAllProperties extends AminoAcidProperty {
	public static final int count = AminoAcidProperty.ALL_PROPERTIES.length;

	/** The name of this property */
	public String getName() {
		return "All_properties";
	}

	public AminAcidAllProperties() {
	}

	/**
	 * Convert a sequence of amino acid residues into an array of values.
	 * 
	 * @param sequence
	 *            the sequence to be converted
	 * @param gappingStrategy
	 *            how to deal with insertions. They can either be converted to a
	 *            fixed value, equal to the average property value of all
	 *            residues; or can be set to the mean of the residues
	 *            neighbouring the insertion.
	 * @return
	 */
	public double[] getValuesForSequence(String sequence, int gappingStrategy) {
		sequence = sequence.toUpperCase();
		VD vd = new VD();
		for (AminoAcidProperty p : AminoAcidProperty.ALL_PROPERTIES)
			vd.append(p.getValuesForSequence(sequence, gappingStrategy));
		return vd.values();
	}

	/**
	 * Convert a sequence of amino acid residues into an array of values using
	 * the default gapping strategy.
	 * 
	 * @param sequence
	 *            the sequence to be converted
	 * @return
	 */
	public double[] getValuesForSequence(String sequence) {
		sequence = sequence.toUpperCase();
		VD vd = new VD();
		for (AminoAcidProperty p : AminoAcidProperty.ALL_PROPERTIES)
			vd.append(p.getValuesForSequence(sequence));
		return vd.values();
	}

	/** Get attributes suitable for holding the values for this sequence. */
	public Vector<Attribute> getAttributesForSequence(String sequence) {
		Vector<Attribute> va = new Vector<Attribute>();
		for (AminoAcidProperty property : AminoAcidProperty.ALL_PROPERTIES)
			for (int p = 0; p < sequence.length(); p++)
				va.addElement(new Attribute("p" + p + "." + property.getName()));
		return va;
	}

	/** This method is not valid for this subclass */
	public double getValue(String residue) {
		throw new RuntimeException("each residue is represented by a real vector in this class "
				+ this.getClass().getName());
	}

	private class VD extends Vector<Double> {

		private void append(double[] da) {
			for (double d : da)
				add(new Double(d));
		}

		private double[] values() {
			double[] da = new double[size()];
			for (int i = 0; i < da.length; i++)
				da[i] = get(i).doubleValue();
			return da;
		}
	}
}
