package tpp;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.matrix.Matrix;

public class SelectedAttributesModel extends AbstractTableModel {
	
	private static final int COLS = 4;

	public static final int SEQ_COL = 0;

	public static final int NAME_COL = 1;

	public static final int MEAN_ATTRIBUTE = 2;
	
	public static final int OCCURENCE_COL = 3;

	private static final String[] COLUMN_NAMES = { " ", "Attribute",
			"Mean Attribute Value", "Occurence" };

	int numAttributeRows;

	Instances instances = null;
	
	private Double attributeMeans;
	
	private int totalAttribute;
	
	private int attribute_occurence;

	/** The names of the attributes. */
	protected String[] attributeNames;

	protected TPPModel tpp;
	public SelectedAttributesModel(ScatterPlotModel tpp) {
		super();
		setModel(tpp);
	}

	public void setModel(TPPModel tpp) {
		this.tpp = tpp;
		Vector<Attribute> numerics = tpp.getNumericAttributes();
		attributeNames = new String[numerics.size()];
		for (int a = 0; a < numerics.size(); a++)
			attributeNames[a] = numerics.get(a).name();
		numAttributeRows = numerics.size();
	}

	public int getRowCount() {
		return numAttributeRows;
	}

	public Object getValueAt(int row, int col) {
		switch (col) {
		case SEQ_COL:
			return row + 1;
		case NAME_COL:
			return attributeNames[row];
		case MEAN_ATTRIBUTE:
			if (tpp.getAttributeMeans() != null)
				return new Double(tpp.getAttributeMeans()[row]);
			else 
				// zero is just a placeholder here until something is selected.
				return 0.0;
		case OCCURENCE_COL:
			return tpp.getOccurences()[row];
		default:
			return "#Error#";
		}
	}
	

	public int getColumnCount() {
		return COLS;
	}

	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	public Class getColumnClass(int col) {
		return (col == NAME_COL ? String.class : Double.class);
	}

	/** Should this column be colored along a bipolar spectrum? */
	public boolean bipolarColorColumn(int col) {
		return (col == MEAN_ATTRIBUTE);
	}

	/** Should this column be colored along a unipolar spectrum? */
	public boolean unipolarColorColumn(int col) {
		return false;
	}

}
