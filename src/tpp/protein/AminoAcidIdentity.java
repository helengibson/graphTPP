package tpp.protein;

import java.util.HashMap;
import java.util.Vector;

import weka.core.Attribute;

/**
 * The identity property: each individual residue is represented by a 20 digit
 * binary sequence of 1s and 0s as used in Casari et al (1995), 'A method to
 * predict functional residues', Nature Structural Biology
 * 
 * @author Joe
 * 
 */
public class AminoAcidIdentity extends AminoAcidProperty {

	private static final double[] NULL_SEQUENCE = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0 };

	private HashMap<String, double[]> sequences;

	/** The name of this property */
	public String getName() {
		return "Identity";
	}

	public AminoAcidIdentity() {
		super();
		name = "Identity";
		sequences = new HashMap<String, double[]>();
		// Copy values into hash and calculate mean value
		sequences.put("A", new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("B", new double[] { 0, 0, 0.5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.5, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("C", new double[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("D", new double[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("E", new double[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("F", new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("G", new double[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("H", new double[] { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("I", new double[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("K", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("L", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("M", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("N", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("P", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 });
		sequences.put("Q", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 });
		sequences.put("R", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 });
		sequences.put("S", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 });
		sequences.put("T", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 });
		sequences.put("V", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 });
		sequences.put("W", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 });
		sequences.put("Y", new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
		sequences.put("Z", new double[] { 0, 0, 0, 0.5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.5, 0, 0, 0, 0, 0, 0 });
		sequences.put("X", new double[] { 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
				0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05 });
		sequences.put("-", new double[] { 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
				0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05 });
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
		double[] valueSequence = new double[sequence.length() * 20];

		// the way we deal with insertions depends on the gapping strategy.
		if (gappingStrategy == GAPPING_STRATEGY_MEAN_OF_ALL_RESIDUES)
			for (int i = 0; i < sequence.length(); i++)
				insertArray(valueSequence, i * 20, getSequence(sequence.substring(i, i + 1)));

		if (gappingStrategy == GAPPING_STRATEGY_MEAN_OF_NEIGHBOURS) {

			// first convert all the non-insertions
			for (int i = 0; i < sequence.length(); i++)
				if (!sequence.substring(i, i + 1).equals(INSERTION))
					insertArray(valueSequence, i * 20, getSequence(sequence.substring(i, i + 1)));

			// then convert the insertions to be equal to the bordering values
			boolean isInsertionAtStart, isInsertionAtEnd;
			double[] previousSequence, nextSequence;
			for (int i = 0; i < sequence.length(); i++)
				if (sequence.substring(i, i + 1).equals(INSERTION)) {

					// find the previous value
					isInsertionAtStart = true;
					previousSequence = NULL_SEQUENCE;
					for (int p = i; p >= 0; p--)
						if (!sequence.substring(p, p + 1).equals(INSERTION)) {
							isInsertionAtStart = false;
							previousSequence = getSequence(sequence.substring(p, p + 1));
						}

					// find the next value
					isInsertionAtEnd = true;
					nextSequence = NULL_SEQUENCE;
					for (int p = i; p < sequence.length(); p++)
						if (!sequence.substring(p, p + 1).equals(INSERTION)) {
							isInsertionAtEnd = false;
							nextSequence = getSequence(sequence.substring(p, p + 1));
						}

					// if the insertion is neither at the end or the beginning
					// then
					// the values is the average of the previous and next
					if (!isInsertionAtEnd && !isInsertionAtStart)
						insertArray(valueSequence, i * 20, meanArray(previousSequence, nextSequence));
					if (isInsertionAtEnd)
						insertArray(valueSequence, i * 20, previousSequence);
					if (isInsertionAtStart)
						insertArray(valueSequence, i * 20, nextSequence);
				}
		}
		return valueSequence;
	}

	/** Return a new array that is the mean of the two other arrays */
	private double[] meanArray(double[] array1, double[] array2) {
		double[] mean = new double[array1.length];
		for (int i = 0; i < mean.length; i++)
			mean[i] = (array1[i] + array2[i]) / 2;
		return mean;
	}

	/** Insert an array of doubles into another array, starting at position i */
	private double[] insertArray(double[] array, int position, double[] insertion) {
		for (int i = 0; i < insertion.length; i++)
			array[position + i] = insertion[i];
		return array;
	}

	/** This method is not valid for this subclass */
	public double getValue(String residue) {
		throw new RuntimeException("each residue is represented by a sequence of binary digits in this class "
				+ this.getClass().getName());
	}

	/** Get attributes suitable for holding the values for this sequence. */
	public Vector<Attribute> getAttributesForSequence(String sequence) {
		Vector<Attribute> va = new Vector<Attribute>();
			for (int p = 0; p < sequence.length(); p++)
				for (String aa : RESIDUES)
				va.addElement(new Attribute("p" + p + "." + aa));
		return va;
	}

	/** get the sequence for a particular residue */
	private double[] getSequence(String residue) {
		if (sequences.containsKey(residue))
			return ((double[]) sequences.get(residue));
		else
			throw new RuntimeException("Unknown " + getName() + " for residue " + residue);
	}

}
