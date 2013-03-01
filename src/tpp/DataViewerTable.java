package tpp;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import weka.core.Attribute;

public class DataViewerTable extends JTable {
	private static final Color SELECTED_COLOR = Color.cyan;
	private ScatterPlotModel spModel;
	private ColourScheme colors;

	public DataViewerTable(ScatterPlotModel spModel) {
		this.spModel = spModel;
		this.colors = spModel.getColours();
	}

	public Component prepareRenderer(TableCellRenderer renderer, int row,
			int col) {
		Component component = super.prepareRenderer(renderer, row, col);
		int a = convertTableColumnToAxisIndex(col);

		// is this point / axis selected
		if (spModel.isPointSelected(row)
				|| (a >= 0 && spModel.isAxisSelected(a))) {
			component.setBackground(SELECTED_COLOR);
			return component;
		}

		// if its numeric, color the cell by value
		if (a >= 0) {
			component.setBackground(colors.getColorFromSpectrum(spModel
					.getData().get(row, a), 0, 1));
			return component;
		}

		return component;
	}

	/**
	 * Find the numeric axis corresponding to the attribute for this table
	 * column, or -1 if the attribute is not numeric.
	 */
	private int convertTableColumnToAxisIndex(int c) {
		return spModel.getNumericAttributes().indexOf(
				spModel.getInstances().attribute(c));
	}
}
