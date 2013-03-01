package tpp;

import javax.swing.DefaultListSelectionModel;
import javax.swing.RowSorter;

/** Select rows in the Projection Table based on the selection in the TPP Model */
public class ProjectionTableListSelectionModel extends
		DefaultListSelectionModel {

	// TODO known bug: when the projection tabled is re-ordered, the
	// setSelection method is called with wrong parameters. As a result the
	// selection changes. Not sure why. more investigation needed

	private ScatterPlotModel model;
	private RowSorter<ProjectionTableModel> sorter;

	public ProjectionTableListSelectionModel(ScatterPlotModel tpp,
			RowSorter<ProjectionTableModel> sorter) {
		this.model = tpp;
		this.sorter = sorter;
		
	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		for (int i = index0; i <= index1; i++) {
			model.selectAxis(sorter.convertRowIndexToModel(i));
		}
		setAnchorSelectionIndex(index0);
		setLeadSelectionIndex(index1);
		model.drawRectangleAroundSelectedAxes();
		fireValueChanged(false);
	}

	@Override
	public void clearSelection() {
		model.unselectAxes();
		fireValueChanged(false);
	}

	@Override
	public void removeIndexInterval(int index0, int index1) {
		for (int i = index0; i <= index1; i++)
			model.unselectAxis(sorter.convertRowIndexToModel(i));
		setAnchorSelectionIndex(index0);
		setLeadSelectionIndex(index1);
		fireValueChanged(false);
	}

	@Override
	public void setSelectionInterval(int index0, int index1) {
		model.unselectAxes();
		addSelectionInterval(index0, index1);
	}

	@Override
	public int getMaxSelectionIndex() {
		for (int i = model.getNumDataDimensions() - 1; i >= 0; i--)
			if (model.isAxisSelected(sorter.convertRowIndexToModel(i)))
				return i;
		return -1;
	}

	@Override
	public int getMinSelectionIndex() {
		for (int i = 0; i < model.getNumDataDimensions(); i++)
			if (model.isAxisSelected(sorter.convertRowIndexToModel(i)))
				return i;
		return -1;
	}

//	Removing this seems to eliminate the problem with the axis not highlighting  correctly when sorted
//	but I don''t know why but it messes up the remove attributes action. 
//  Edit two: returned to previous implementation and added setUpdateSelectionOnSort(false) to the projection table 
	@Override
	public boolean isSelectedIndex(int index) {
////		System.out.println("is index " + index + " selected?");
////		System.out.println("this is " + sorter.convertRowIndexToView(index) + " in the model");
////		return model.isAxisSelected(sorter.convertRowIndexToModel(index));
		return model.isAxisSelected(sorter.convertRowIndexToView(index));
//			return super.isSelectedIndex(index);
	}

	@Override
	public boolean isSelectionEmpty() {
		return !model.areAxesSelected();
	}
	
	public int getAttributeIndex(int i ){
		return sorter.convertRowIndexToModel(i);
	}
		
}
