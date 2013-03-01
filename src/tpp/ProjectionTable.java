package tpp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import weka.core.Attribute;

/**
 * The extensions to JTable deal with color rendering, changing values and
 * selections, and adding the table sorter and copy/paste functionality.
 */
public class ProjectionTable extends JTable {

	private static final Color SELECTED_COLOR = Color.gray;

	ColourScheme colors;

	private ScatterPlotModel spModel;

	private ProjectionTableModel tableModel;

	public ProjectionTable(ScatterPlotModel tpp) {
		this.spModel = tpp;
		tableModel = new ProjectionTableModel(tpp);
		setModel(tableModel);
		RowSorter<ProjectionTableModel> sorter = new TableRowSorter<ProjectionTableModel>(
				tableModel);
		setRowSorter(sorter);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);
		setUpdateSelectionOnSort(false);
		ProjectionTableListSelectionModel lsm = new ProjectionTableListSelectionModel(
				tpp, sorter);
		setSelectionModel(lsm);
		ExcelAdapter xls = new ExcelAdapter(this);
		colors = tpp.getColours();
		setMinimumSize(new Dimension(20, 20));
		
	}

	// try using a custom renderer instead
	// http://www.exampledepot.com/egs/javax.swing.table/CustRend.html
	public Component prepareRenderer(TableCellRenderer renderer, int row,
			int col) {
		Component component = super.prepareRenderer(renderer, row, col);
		
		// without this any rows between the row last selection and 
		// the new one have their foreground set to white
		// this forces it to stay black
		component.setForeground(Color.BLACK);

		if (spModel.isAxisSelected(getRowSorter().convertRowIndexToModel(row))) {
			component.setBackground(SELECTED_COLOR);
			return component;
		}

		if (tableModel.bipolarColorColumn(col)) {
			component.setBackground(bipolarColor(getModel().getValueAt(
					getRowSorter().convertRowIndexToModel(row), col)));
			return component;
		}

		else {
			if (tableModel.unipolarColorColumn(col))
				component.setBackground(unipolarColor(getModel().getValueAt(
						getRowSorter().convertRowIndexToModel(row), col)));
			else
				component.setBackground(Color.WHITE);
			return component;
		}
	}

	private Color bipolarColor(Object o) {
		double value = ((Double) o).doubleValue();
		return colors.getColorFromSpectrum(value, -1, 1);
	}

	private Color unipolarColor(Object o) {
		double value = ((Double) o).doubleValue();
		return colors.getColorFromSpectrum(value, 0, 2);
	}

	public void fireTableDataChanged() {
		((ProjectionTableModel) getModel()).fireTableDataChanged();
	}

	public void selectAttribute(int attribute) {
		int row = getRowSorter().convertRowIndexToView(attribute);

		// Select a single attribute row
		clearSelection();
		setRowSelectionInterval(row, row);
		repaint();
	}

	/**
	 * Get the selected attributes. Returns null if no rows are selected.
	 */
	public Vector<Attribute> getSelectedAttributes() {
		int[] rows = getSelectedRows();
		if (rows != null && rows.length > 0) {
			Vector<Attribute> ats = new Vector<Attribute>();
			for (int i = 0; i < rows.length; i++)
				ats.add(spModel.getNumericAttributes().get(
						getRowSorter().convertRowIndexToModel(rows[i])));
			return ats;
		} else
			return null;
	}

	/**
	 * Get the indices of the selected attributes. Returns null if no rows are
	 * selected.
	 */
	public int[] getSelectedAttributeIndices() {
		int[] rows = getSelectedRows();
//		for (int row : rows){
			System.out.println("selected rows are: "+ rows.length);
//		}
		if (rows != null && rows.length > 0) {
			for (int i = 0; i < rows.length; i++)
				rows[i] = getRowSorter().convertRowIndexToModel(rows[i]);
			return rows;
		} else
			return null;
	}
	
	

}
