package tpp;

import javax.swing.table.AbstractTableModel;

import weka.core.Attribute;

class DataViewerTableModel extends AbstractTableModel {
	
	private int originalNumAttributes;
	private ScatterPlotModel spModel;

	DataViewerTableModel(ScatterPlotModel spModel){
		super();
		this.spModel=spModel;
		originalNumAttributes = spModel.getInstances().numAttributes()-1;
	}

	public Object getValueAt(int row, int col) {
		Attribute at = spModel.getInstances().attribute(col);
		if (at.isNumeric())
			return new Double(spModel.getInstances().instance(row)
					.value(col));
		else
			return spModel.getInstances().instance(row).stringValue(col);
	}

	public String getColumnName(int c) {
		return spModel.getInstances().attribute(c).name();
	}

	public int getColumnCount() {
		return originalNumAttributes;
	}

	public int getRowCount() {
		return spModel.getInstances().numInstances();
	}
}