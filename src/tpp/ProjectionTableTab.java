/* Added by Helen Gibson */
package tpp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class ProjectionTableTab extends JPanel implements ActionListener  {
	
	private ScatterPlotModel spModel;
	private ScatterPlotControlPanel spcp;
	private ProjectionTable projectionTable;
	private JButton removeAttributeButton;
	private JButton undoButton;
	
	public ProjectionTableTab(ScatterPlotControlPanel spcp, ScatterPlotModel spModel) {
		
		super();
		this.spModel = spModel;
		this.spcp = spcp;
		init();
		setVisible(true);
	}
	
	private void init() {
		
		setLayout(new GridBagLayout());
		GridBagConstraints projectionGrid = new GridBagConstraints();
		projectionGrid.fill = GridBagConstraints.BOTH;
		projectionGrid.weightx = 1.0;
		projectionGrid.gridy = 0;
		
		addProjectionTableAndAttributeSelection(this, projectionGrid);
	}
	
	private void addProjectionTableAndAttributeSelection(JPanel panel, GridBagConstraints grid) {
		
		// add projection table
		projectionTable = new ProjectionTable(spModel);
		projectionTable
				.setToolTipText("<html><p width=\"300px\">This table shows the components " +
						"of the linear projection used to produce the view of the data. The x and " +
						"y components of each attribute (axis) are shown, and the 'Significance' column " +
						"shows the overall length. The rows can be ordered by each of the columns (click on " +
						"the header to re-order). By clicking on the 'Significance' column you can find " +
						"which attributes are most significant in producing the view. Values from the table can " +
						"be copied to the clipboard and imported to Excel etc. Or these values can be " +
						"saved from the File menu." +
						"Right click a row to colour the points according to that attribute.</p></html>");
		
		JScrollPane tablePane = new JScrollPane(projectionTable);
		
		grid.gridx = 0;
		grid.gridwidth = 3;
		grid.gridy++;
		grid.weighty = 1;
		
		panel.add(tablePane, grid);

		removeAttributeButton = new JButton("Remove Selected Attributes");
		removeAttributeButton.addActionListener(this);
		removeAttributeButton.setToolTipText("Remove the selected attributes");
		
		grid.gridy++;
		grid.weighty = 0;
		grid.gridwidth = 2;
		
		panel.add(removeAttributeButton, grid);

		undoButton = new JButton("Undo");
		undoButton.addActionListener(this);
		undoButton.setToolTipText("Restore the removed attributes");
		undoButton.setEnabled(spModel.canUndo());
		
		grid.gridx = 2;
		grid.gridwidth = 1;
		
		panel.add(undoButton, grid);
		
		projectionTable.addMouseListener( new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{				
				if ( SwingUtilities.isRightMouseButton( e ))
				{
					// get the coordinates of the mouse click
					java.awt.Point p = e.getPoint();
		 
					// get the row index that contains that coordinate
					int rowNumber = projectionTable.rowAtPoint( p );
		 
					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = projectionTable.getSelectionModel();
		 
					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval( rowNumber, rowNumber );
					
					int attributeIndex = ((ProjectionTableListSelectionModel)model).getAttributeIndex(rowNumber);
					
					spcp.selectCombo.setSelectedIndex(attributeIndex + 1);
					spModel.setSeparationAttribute(spModel.getInstances().attribute(attributeIndex));
					spModel.setColourAttribute(spModel.getInstances().attribute(attributeIndex));
					spcp.separateButton.setEnabled(spModel.getPointModel().getSelectAttribute() != null && spModel.getPointModel().getSelectAttribute().isNominal());

				}
			}
		});

}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		if (event.getSource() == removeAttributeButton
				&& projectionTable.getSelectedAttributeIndices() != null
				&& projectionTable.getSelectedAttributeIndices().length > 0){
			spModel.removeAttributes(projectionTable.getSelectedAttributes());}		

		if (event.getSource() == undoButton)
			spModel.undo();
		
	}
}
