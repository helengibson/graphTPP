package tpp;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;

/**
 * A panel including buttons that allow the user to select points based on the
 * value of the SelectAttribute
 */
public class SelectionPanel extends JPanel implements TPPModelEventListener {

	private Vector<SelectButton> selectButtons;
	private ScatterPlotModel spModel;
	private GridBagConstraints grid;
	private JScrollPane scroller;
	private JPanel buttonPanel;
	private GridBagConstraints buttonGrid;
	private GridBagConstraints bg;

	public SelectionPanel(ScatterPlotModel spModel) {
		super();
		this.spModel = spModel;
		initialiseSelectionButtons();
		//setBorder(BorderFactory.createLineBorder(spModel.getColours().getBackgroundColor(), 3));
		setVisible(true);
	}

	public void initialiseSelectionButtons() {
		selectButtons = new Vector<SelectButton>();

		// initialise layout
		removeAll();
		setLayout(new GridBagLayout());
		
		grid = new GridBagConstraints();
		
		grid.weightx = 1.0;
		grid.fill = GridBagConstraints.BOTH;

		// Create buttons for individual attribute values
		selectButtons.removeAllElements();
		
		// include buttons to allow the user to select a classes in the current
		// classification, or a numeric range
		selectButtons = SelectButton.buildSelectButtons(spModel);
		
		buttonPanel = new JPanel();
		
		buttonPanel.setLayout(new GridBagLayout());	
		buttonGrid = new GridBagConstraints();
		
		buttonGrid.fill = GridBagConstraints.BOTH;
		buttonGrid.gridwidth = 1;
		buttonGrid.weightx = 1.0;
		
		scroller = new JScrollPane(buttonPanel);
						
		buttonGrid.gridy = 0;
		buttonGrid.gridx = 0;
		for (SelectButton button : selectButtons) {
			buttonGrid.gridx = 0;
			buttonGrid.gridwidth = 1;
			buttonPanel.add(button, buttonGrid);
			buttonGrid.gridy++;
		}
		
		scroller.setMinimumSize(new Dimension(210,150));
		scroller.setPreferredSize(new Dimension(210,150));
		
		grid.gridx = 2;
		add(scroller,grid);
		
		revalidate();
		repaint();
	}
	
	public Vector<SelectButton> getSelectButtons() {
		return selectButtons;
	}

	@Override
	public void modelChanged(TPPModelEvent e) {
		
		if(e.getType() == TPPModelEvent.COLOR_SCHEME_CHANGED)
			initialiseSelectionButtons();
		
	}

}
