package tpp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;


public class DataViewer extends JFrame implements TPPModelEventListener {

	private static final Dimension DEFAULT_DIMENSION = new Dimension(1024, 640);
	private static final int DEFAULT_COLUMN_WIDTH = 75;
	ScatterPlotModel spModel;
	private JTable table;

	public DataViewer(ScatterPlotModel model) {
		this.spModel = model;
		model.addListener(this);
		init();
	}

	@Override
	public void modelChanged(TPPModelEvent e) {
		switch (e.getType()) {
		case TPPModelEvent.DATA_SET_CHANGED:
			init();
			break;
		case TPPModelEvent.DATA_STRUCTURE_CHANGED:
			init();
			break;
		case TPPModelEvent.POINT_SELECTION_CHANGED:
			((DataViewerTableModel) table.getModel()).fireTableDataChanged();
			repaint();
			break;
		case TPPModelEvent.AXIS_SELECTION_CHANGED:
			((DataViewerTableModel) table.getModel()).fireTableDataChanged();
			break;
		}
	}

	private void init() {
		setTitle(spModel.getInstances().relationName());
		setSize(DEFAULT_DIMENSION);
		table = new DataViewerTable(spModel);
		table.setModel(new DataViewerTableModel(spModel));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int c = 0; c < table.getColumnCount(); c++)
			table.getColumnModel().getColumn(c)
					.setPreferredWidth(DEFAULT_COLUMN_WIDTH);
		JScrollPane scrollPane = new JScrollPane(table);
		getContentPane().removeAll();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		setVisible(true);
	}


}
