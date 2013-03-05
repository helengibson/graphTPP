package tpp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class SelectedAttributesTableTab extends JPanel {

	private ScatterPlotModel spModel;
	private ScatterPlotControlPanel spcp;
	private SelectedAttributesTable selectionTable = null;
	private PointModel pointModel;

	public SelectedAttributesTableTab(ScatterPlotControlPanel spcp,
			ScatterPlotModel spModel) {
		
		super();
		this.spModel = spModel;
		this.spcp = spcp;
		pointModel = spModel.getPointModel();
		init();
		setVisible(true);
		
	}

	private void init() {
		
		setLayout(new GridBagLayout());
		GridBagConstraints selectedGrid = new GridBagConstraints();
		selectedGrid.fill = GridBagConstraints.BOTH;
		selectedGrid.weightx = 1.0;
		selectedGrid.gridy = 0;
		
		addSelectedAttributesTable(this, selectedGrid);
	}
	
	private void addSelectedAttributesTable(JPanel panel, GridBagConstraints grid) {
		// add selection table
		selectionTable = new SelectedAttributesTable(spModel);
		
		selectionTable.setToolTipText("<html><p width=\"300px\">When you select a " +
				"number of point in the view with the rectangel this table updates to " +
				"show the relative mean values of each the attributes the points selected. " +
				"Right click a row to colour the points according to that attribute.</p></html>");
		
		JScrollPane tablePane = new JScrollPane(selectionTable);
		
		grid.gridx = 0;
		grid.gridwidth = 3;
		grid.gridy++;
		grid.weighty = 1;
		
		panel.add(tablePane, grid);
		
		selectionTable.addMouseListener( new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				if ( SwingUtilities.isRightMouseButton( e ))
				{
					// get the coordinates of the mouse click
					java.awt.Point p = e.getPoint();
		 
					// get the row index that contains that coordinate
					int rowNumber = selectionTable.rowAtPoint( p );
		 
					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = selectionTable.getSelectionModel();
		 
					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval( rowNumber, rowNumber );
					
					int attributeIndex = ((SelectedAttributesListSelectionModel)model).getAttributeIndex(rowNumber);
					
					spcp.selectCombo.setSelectedIndex(attributeIndex + 1);
					spModel.setSeparationAttribute(spModel.getInstances().attribute(attributeIndex));
					spModel.setColourAttribute(spModel.getInstances().attribute(attributeIndex));
					spcp.separateButton.setEnabled(pointModel.getSelectAttribute() != null && pointModel.getSelectAttribute().isNominal());
					revalidate();
					repaint();
				}
			}
		});
	}
	

}
