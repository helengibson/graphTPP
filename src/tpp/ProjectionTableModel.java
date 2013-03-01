package tpp;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import weka.core.Attribute;
import weka.core.Instances;

public class ProjectionTableModel extends AbstractTableModel {

	private static final int COLS = 5;

	public static final int SEQ_COL = 0;

	public static final int NAME_COL = 1;

	public static final int X_COL = 2;

	public static final int Y_COL = 3;

	public static final int SIG_COL = 4;

	private static final String[] COLUMN_NAMES = { "Index", "Attribute",
			"X \n Weight", "Y \n Weight", "Significance" };

	int numAttributeRows;

	Instances instances = null;

	/** The names of the attributes. */
	protected String[] attributeNames;

	protected TPPModel tpp;
	public ProjectionTableModel(ScatterPlotModel tpp) {
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
		case X_COL:
			return new Double(tpp.getProjection().get(row, 0));
		case Y_COL:
			return new Double(tpp.getProjection().get(row, 1));
		case SIG_COL:
			return new Double(XY(row));
		default:
			return "#Error#";
		}
	}

	public void setValueAt(Object value, int row, int col) {
		Double d = Double.valueOf(value.toString());
		if (col == X_COL)
			tpp.getProjection().set(row, 0, d.doubleValue());
		if (col == Y_COL)
			tpp.getProjection().set(row, 1, d.doubleValue());
		tpp.project();
		fireTableCellUpdated(row, col); //added
	}

	public int getColumnCount() {
		return COLS;
	}

	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	/**
	 * The sum of the absolute values of X and Y components of the projection.
	 * ie |X|+|Y|
	 */
	protected double XY(int row) {
		return (tpp.getProjection().get(row, 0) * tpp.getProjection().get(row,
				0))
				+ (+(tpp.getProjection().get(row, 1) * tpp.getProjection().get(
						row, 1)));
	}

	public boolean isCellEditable(int row, int col) {
		return ((col == X_COL) || (col == Y_COL));
	}

	public Class getColumnClass(int col) {
		return (col == NAME_COL ? String.class : Double.class);
	}

	/** Should this column be colored along a bipolar spectrum? */
	public boolean bipolarColorColumn(int col) {
		return (col == X_COL || col == Y_COL || col == SIG_COL);
	}

	/** Should this column be colored along a unipolar spectrum? */
	public boolean unipolarColorColumn(int col) {
		return false;
	}

}
