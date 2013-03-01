package tpp.protein;

/** Represents a protein primary sequence */
public class Protein {

	private String label;

	private String sequence;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence.toUpperCase();
	}

	public void appendSequence(String fragment) {
		if (sequence == null)
			sequence = fragment;
		else
			sequence = sequence + fragment;
	}

	public String toString() {
		return label + "\t\t" + sequence;
	}
}
