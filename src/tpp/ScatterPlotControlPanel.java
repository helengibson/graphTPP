package tpp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.svggen.font.Point;
import org.apache.commons.math.optimization.fitting.CurveFitter;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;
import weka.gui.explorer.ExplorerDefaults;

/*
 * Created on 23-May-2006
 *
 */

/**
 * An extension of JPanel that includes the controls and projections for a data
 * set
 * 
 * @author Joe
 * 
 */
public class ScatterPlotControlPanel extends JPanel implements
		TPPModelEventListener, ActionListener {

	private JButton clusterButton;
	private JComboBox clusterNumberCombo;
	private JComboBox clusterOptionsCB;

	private JLabel projectionOptionsLabel;
	private JComboBox projectionOptions;
	private JButton projectionButton;

	String[] attributeSelectionOptions = { "All", "Attributes", "Edges",
			"Custom" };

	private SelectionPanel selectionPanel;
	AttributeCombo selectCombo;
	SeparatePointsButton separateButton;

	private JComboBox pointSelectorCombo;
	private JButton pointSelectorButton;
	protected ProjectionTable projectionTable = null;

	private Dimension min = new Dimension(100, 20);
	private JTabbedPane tabbedPane;
	protected ScatterPlotModel spModel;
	private JButton dbConnectButton;
	private JButton viewInDBButton;
	private JSplitPane splitter;
	private JPanel topSSPanel;
	private Graph graph;

	private int currentTabIndex = 0;
	private int[] selectedIndices;
	private PointModel pointModel;

	public ScatterPlotControlPanel() {
		super();
	}

	public void setModel(ScatterPlotModel tpp) throws TPPException {
		this.spModel = tpp;
		pointModel = spModel.getPointModel();
		graph = spModel.getGraph();
		spModel.addListener(this);
		init();
	}

	/**
	 * Initialise this panel, creating the controls necessary to manipulate this
	 * scatter plot
	 * 
	 * @throws TPPException
	 */
	public void init() {
		removeAll();
		revalidate();

		setLayout(new BorderLayout(5, 5));

		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		add(splitter, BorderLayout.CENTER);

		splitter.setOneTouchExpandable(true);
		// splitter.setDividerLocation(300);
		splitter.setResizeWeight(0.8);
		splitter.setDividerLocation(splitter.getSize().width - 350);

		// Provide a preferred size for the split pane.
		splitter.setPreferredSize(new Dimension(200, 350));

		addSelectionPanel();

		addTabbedPane(currentTabIndex);

		revalidate();

	}

	private void addTabbedPane(int currentIndex) {

		JPanel topViewOptionsPanel = new JPanel(new BorderLayout(5, 5));
		JPanel topGraphPanel = new JPanel(new BorderLayout(5, 5));

		ProjectionTableTab projectionPanel = new ProjectionTableTab(this,
				spModel);

		SelectedAttributesTableTab selectedPanel = new SelectedAttributesTableTab(
				this, spModel);

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("View Options", topViewOptionsPanel);
		tabbedPane.addTab("Graph Options", topGraphPanel);
		tabbedPane.addTab("Significance", projectionPanel);
		tabbedPane.addTab("Selected Attributes", selectedPanel);

		tabbedPane.setSelectedIndex(currentIndex);

		splitter.setBottomComponent(tabbedPane);

		// //////////////////////////////////////////////

		ViewOptionsTab viewOptionsPanel = new ViewOptionsTab(spModel);
		AdvancedOptionsTab advancedOptionsPanel = new AdvancedOptionsTab(
				spModel, pointModel);
		topViewOptionsPanel.add(viewOptionsPanel, BorderLayout.NORTH);
		// topViewOptionsPanel.add(advancedOptionsPanel, BorderLayout.CENTER);

		// ////////////////////////////////////////////

		GraphOptionsTab graphPanel = new GraphOptionsTab(spModel);
		topGraphPanel.add(graphPanel, BorderLayout.NORTH);

		// ////////////////////////////////////////////

		// listen for tab changes and update the currently viewed tab index

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
						.getSource();
				currentTabIndex = sourceTabbedPane.getSelectedIndex();
				System.out.println("Tab changed to: "
						+ sourceTabbedPane.getTitleAt(currentTabIndex));
			}
		};
		tabbedPane.addChangeListener(changeListener);

	}

	private void addSelectionPanel() {
		// add selection attribute selector, with the current selection
		// attribute shown

		topSSPanel = new JPanel(new BorderLayout(5, 5));
		splitter.setTopComponent(topSSPanel);

		JPanel selectSeparatePanel = new JPanel();
		selectSeparatePanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));

		selectSeparatePanel.setLayout(new GridBagLayout());
		GridBagConstraints selectSeparateGrid = new GridBagConstraints();

		selectSeparateGrid.fill = GridBagConstraints.BOTH;
		selectSeparateGrid.weightx = 1.0;
		selectSeparateGrid.gridy = 0;
		selectSeparateGrid.insets = new Insets(1, 2, 1, 2);

		selectCombo = AttributeCombo.buildCombo(spModel,
				AttributeCombo.ALL_ATTRIBUTES, true);
		selectCombo.setMinimumSize(min);
		selectCombo.setSelectedAttribute(pointModel.getSelectAttribute());
		selectCombo.addActionListener(this);
		selectCombo
				.setToolTipText("Choose which attribute is used to color and select points");
		JLabel selectAttributeSelectorLabel = new JLabel("Color points by: ",
				JLabel.RIGHT);

		selectCombo.setEditable(true);


		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;

		selectSeparatePanel.add(selectAttributeSelectorLabel,
				selectSeparateGrid);

		selectSeparateGrid.gridx++;
		selectSeparateGrid.gridwidth = 1;
		selectSeparatePanel.add(selectCombo, selectSeparateGrid);

		separateButton = new SeparatePointsButton(spModel, selectCombo);
		separateButton.setText("Separate points");
		separateButton
				.setToolTipText("<html><p width=\"300px\">Try to find "
						+ "a projection in which the points are separated on the basis of "
						+ "the chosen attribute. If the attribute is nominal then each of the "
						+ "classes will be separated as far as possible. If the attribute is numeric "
						+ "then the distance between points in will try to approximate to the difference "
						+ "in value of this attribute</p></html>");

		// can only separate points by a nominal attribute
		// (SeparatePoints contains code to separate by numerical attributes as
		// well but it doesn't work very well)

		separateButton.setEnabled(selectCombo.getSelectedAttribute() != null
				&& selectCombo.getSelectedAttribute().isNominal());

		selectSeparateGrid.gridx++;
		selectSeparateGrid.gridwidth = 1;
		selectSeparatePanel.add(separateButton, selectSeparateGrid);

		addClustererButton(selectSeparatePanel, selectSeparateGrid);

		// ////////////////////////////////////////////////////////////////

		projectionOptionsLabel = new JLabel("Choose projection attributes:",
				JLabel.RIGHT);

		projectionOptions = new JComboBox(attributeSelectionOptions);
		projectionOptions.addActionListener(this);

		projectionButton = new JButton("Update");
		projectionButton.addActionListener(this);
		projectionButton
				.setToolTipText("Choose the attributes to include in the projection");

		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;

		selectSeparatePanel.add(projectionOptionsLabel, selectSeparateGrid);

		selectSeparateGrid.gridx = 1;
		selectSeparateGrid.gridwidth = 1;

		selectSeparatePanel.add(projectionOptions, selectSeparateGrid);

		selectSeparateGrid.gridx = 2;
		selectSeparateGrid.gridwidth = 1;

		selectSeparatePanel.add(projectionButton, selectSeparateGrid);

		// ////////////////////////////////////////////////////////////////

		pointSelectorCombo = new JComboBox(spModel.getPointDescriptions());
		pointSelectorCombo.setEditable(true);

		pointSelectorButton = new JButton("Add point to selection: ");
		pointSelectorButton.addActionListener(this);
		pointSelectorButton
				.setToolTipText("Add another point to the selection");

		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;

		selectSeparatePanel.add(pointSelectorButton, selectSeparateGrid);

		selectSeparateGrid.gridx = 1;
		selectSeparateGrid.gridwidth = 2;

		selectSeparatePanel.add(pointSelectorCombo, selectSeparateGrid);

		// add selection panel
		selectionPanel = new SelectionPanel(spModel);

		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 3;
		selectSeparateGrid.fill = GridBagConstraints.BOTH;

		selectSeparatePanel.add(selectionPanel, selectSeparateGrid);

		topSSPanel.add(selectSeparatePanel, BorderLayout.PAGE_START);

	}

	private void addClustererButton(JPanel panel, GridBagConstraints grid) {
		// add clusterer button

		Vector<String> clusters = new Vector<String>();

		for (int k = 2; k < 20; k++)
			clusters.add(" N=" + k);

		clusterNumberCombo = new JComboBox(clusters);
		clusterNumberCombo
				.setToolTipText("Choose the number of clusters to find in the data");

		clusterOptionsCB = new JComboBox(attributeSelectionOptions);
		clusterOptionsCB.addActionListener(this);
		clusterOptionsCB.setToolTipText("Choose the attributes that will be used in the projection");

		clusterButton = new JButton("Create clusters:");
		clusterButton.addActionListener(this);
		clusterButton
				.setToolTipText("<html><p width=\"300px\">Use an unsupervised clustering algorithm (K-means) to divide the points into clusters based on the value of the numeric attributes</p></html>");

		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;

		panel.add(clusterNumberCombo, grid);

		grid.gridx = 1;
		grid.gridwidth = 1;

		panel.add(clusterOptionsCB, grid);

		grid.gridx = 2;
		grid.gridwidth = 1;

		panel.add(clusterButton, grid);

	}

	public void actionPerformed(ActionEvent event) {
		
		if (event.getSource() == clusterOptionsCB) {
			if (clusterOptionsCB.getSelectedItem().equals("Custom")) {
				AttributeSelector selector = new AttributeSelector(
						spModel, this);
			}
//			currentClusterSelection = clusterOptionsCB.getSelectedItem();
		}

		if (event.getSource() == clusterButton) {
			Instances in = spModel.getInstances();
			Instances inCopy = new Instances(in);
			
			boolean attributesExist = false;
			String attributesUsed = (String) clusterOptionsCB.getSelectedItem();

			if (!attributesUsed.equals("All")) {
				try {
					Remove remove = new Remove();
					String indices = "";
					if (attributesUsed.equals("Attributes")) {
						for (int a = 0; a < inCopy.numAttributes(); a++) {
							String name = inCopy.attribute(a).name();
							if (!name.startsWith("_")) {
								indices = indices + (a + 1) + ",";
								attributesExist = true;
							}
						}
					} else if (attributesUsed.equals(
							"Edges")) {
						for (int a = 0; a < inCopy.numAttributes(); a++) {
							String name = inCopy.attribute(a).name();
							if (name.startsWith("_")) {
								indices = indices + (a + 1) + ",";
								attributesExist = true;
							}
						}
					} else if (attributesUsed.equals(
							"Custom")) {
						int[] numIndices = getSelectedIndices();
						System.out.println(numIndices);
						for (int i : numIndices) {
							indices = indices + (i + 1) + ",";
							attributesExist = true;
						}
					}
					if(attributesExist){
						indices = indices.substring(0, indices.length() - 1);
					
						remove.setAttributeIndices(indices);
						remove.setInvertSelection(true);
						remove.setInputFormat(inCopy);
						inCopy = Filter.useFilter(inCopy, remove);
					} else {
						JOptionPane.showMessageDialog(this, "No attributes available to project on, \n" +
								"defaulting to using all attributes.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Attribute cluster = spModel.cluster(
					((JComboBox) clusterNumberCombo).getSelectedIndex() + 2,
					inCopy, attributesUsed);
			pointModel.setSelectAttribute(cluster);
			spModel.setColourAttribute(cluster);
			init();
		}
		
		if (event.getSource() == projectionOptions) {
			if (projectionOptions.getSelectedItem().equals("Custom")) {
				System.out.println("custom projection options chosen");
				AttributeSelector selector = new AttributeSelector(
						spModel, this);
			}
		}
		
		if (event.getSource() == projectionButton){
			
			Instances deepInstances = spModel.getDeepInstances();
			Instances currentInstances = spModel.getInstances();
			Instances filteredInstances = new Instances(deepInstances);
			
			boolean attributesExist = false;
			String projectionAttributeOption = (String) projectionOptions.getSelectedItem();
			
			try {
				
				// Create a set of Instances that holds only the non numeric attributes. 
				// This is so we don't lose any of the cluster data etc. 
				String nonNumericAttributes = "";
				for (int i = 0; i < currentInstances.numAttributes(); i++){
					if(currentInstances.attribute(i).isNominal() || currentInstances.attribute(i).isString()){
						nonNumericAttributes = nonNumericAttributes + (i + 1) + ",";
					}
				}
				
				nonNumericAttributes = nonNumericAttributes.substring(0, nonNumericAttributes.length() - 1);
				Remove removeCurrentNumeric = new Remove();
				removeCurrentNumeric.setAttributeIndices(nonNumericAttributes);
				removeCurrentNumeric.setInvertSelection(true);
				removeCurrentNumeric.setInputFormat(currentInstances);
				Instances nonNumericInstances = Filter.useFilter(currentInstances, removeCurrentNumeric);	
				
				Remove remove = new Remove();
				String indices = "";
				if(!projectionAttributeOption.equals("All")){
					if (projectionAttributeOption.equals("Attributes")){
						for (int a = 0; a < filteredInstances.numAttributes(); a++) {
							String name = filteredInstances.attribute(a).name();
							if (!name.startsWith("_")) {  // want attributes so don't include edges
								if (filteredInstances.attribute(a).isNumeric()){ // want numeric so dont include the non-numeric
									indices = indices + (a + 1) + ","; // we'll invert later so it should be the opposite
								}
								attributesExist = true;
							} 
						}
					} else if (projectionAttributeOption.equals("Edges")){
						for (int a = 0; a < filteredInstances.numAttributes(); a++) {
							String name = filteredInstances.attribute(a).name();
							if (name.startsWith("_")) {
								indices = indices + (a + 1) + ",";
								attributesExist = true;
							} 
						}
					} else if (projectionAttributeOption.equals("Custom")) {
						int[] numIndices = getSelectedIndices();
						for (int i : numIndices) {
							if (filteredInstances.attribute(i).isNumeric()){
								indices = indices + (i + 1) + ",";
								attributesExist = true;
							}
						} 
					}
				} else if (projectionAttributeOption.equals("All")) {
					for (int a = 0; a < filteredInstances.numAttributes(); a++) {
						if (filteredInstances.attribute(a).isNumeric()){ // want numeric so dont include the non-numeric
							indices = indices + (a + 1) + ","; // we'll invert later so it should be the opposite
						}
						attributesExist = true;
					} 
					
				} else {
					JOptionPane.showMessageDialog(this, "No attributes available to project on, \n" +
							"defaulting to using all attributes.");
					}
				
				if(attributesExist){
					indices = indices.substring(0, indices.length() - 1);
					remove.setAttributeIndices(indices);
					remove.setInvertSelection(true);
					remove.setInputFormat(filteredInstances);
					filteredInstances = Filter.useFilter(filteredInstances, remove);
					filteredInstances = Instances.mergeInstances(filteredInstances, nonNumericInstances);
				}		
				
				spModel.setInstances(filteredInstances, true);
				System.out.println("filter zero instances: " +spModel.zeroInstances());
				if(spModel.zeroInstances())
					JOptionPane.showMessageDialog(this,"Data has zero instances:  TPP may to fail. \n" +
							"Ensure each instance has at least one non-zero numeric attribute");
				init();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (event.getSource() == selectCombo) {
			// by default we should color the points by the same attribute we
			// are selecting by
			pointModel.setSelectAttribute(selectCombo.getSelectedAttribute());
			spModel.setColourAttribute(selectCombo.getSelectedAttribute());
			selectionPanel.initialiseSelectionButtons();
			separateButton.setEnabled(pointModel.getSelectAttribute() != null
					&& pointModel.getSelectAttribute().isNominal());
			revalidate();
			repaint();
		}

		if (event.getSource() == pointSelectorButton) {
			spModel.selectPoint(pointSelectorCombo.getSelectedIndex());
			spModel.drawRectangleAroundSelectedPoints();
		}

	}

	public void setSelectedIndices(int[] indices) {
		this.selectedIndices = indices;
	}

	public int[] getSelectedIndices() {
		return selectedIndices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.ControlPanel#getClassificationPanel()
	 */
	public SelectionPanel getClassificationPanel() {
		return selectionPanel;
	}

	public void setColours(ColourScheme colours) {
		selectionPanel = new SelectionPanel(spModel);
	}

	public void modelChanged(TPPModelEvent e) {
		switch (e.getType()) {
		case (TPPModelEvent.DATA_SET_CHANGED):
			init();
			break;
		case (TPPModelEvent.DATA_STRUCTURE_CHANGED):
			init();
			break;
		case (TPPModelEvent.COLOR_SCHEME_CHANGED):
			init();
			break;
		case (TPPModelEvent.CONTROL_PANEL_UPDATE):
			init();
			break;
		default:
			repaint();
		}
	}

	public ProjectionTable getProjectionTable() {
		return projectionTable;
	}

}