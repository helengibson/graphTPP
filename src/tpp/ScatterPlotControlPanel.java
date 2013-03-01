package tpp;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.svggen.font.Point;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.AttributeStats;
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
		TPPModelEventListener, ActionListener, ChangeListener, ItemListener {

	private JButton clusterButton;
	private JComboBox clusterNumberCombo;
	private SelectionPanel selectionPanel;
	private AttributeCombo seriesIdCombo;
	private AttributeCombo sizeCombo;
	private AttributeCombo selectCombo;
	private AttributeCombo seriesIndexCombo;
	private JButton createSeriesButton;
	private SmoothButton smoothSeriesButton;
	private JButton createTestSetButton;
	private JComboBox createTestSetKCombo;
	private JButton removeTestSetButton;
	private SeparatePointsButton separateButton;
	private JButton removeAttributeButton;
	private AttributeCombo fillCombo;
	private AttributeCombo shapeCombo;
	private AbstractButton applyClassifierButton;
	private AttributeCombo classificationTargetCombo;
	// private JComboBox classificationCombo;
	private JSlider markerSlider;
	private JComboBox pointSelectorCombo;
	private JButton pointSelectorButton;
	private JButton removeSeriesButton;
	protected ProjectionTable projectionTable = null;
	private GenericObjectEditor classifierChooser;
	private PropertyPanel classifierChooserPanel;
	private Dimension min = new Dimension(100, 20);
	private JButton undoButton;
	private JTabbedPane tabbedPane;
	private SelectedAttributesTable selectionTable = null; 
	private JRadioButton sourceRB;
	private JRadioButton targetRB;
	private JRadioButton mixedRB;
	private JRadioButton noneRB;
	private JRadioButton straightRB;
	private JRadioButton curvedRB;
	private JRadioButton hideRB;
	private JCheckBox incomingCB;
	private JCheckBox outgoingCB;
		
	protected ScatterPlotModel spModel;
	private Graph graph;
	private JSlider transparencySlider;
	private JButton loadGraphButton;
	private JCheckBox directedCheckBox;
	private JButton colorSchemeButton;
	private JSlider beizerSlider;
	private JCheckBox arrowedCB;
	private JCheckBox showLabelsCheckBox;
	private JCheckBox showHighlightedLabels;
	private JSlider labelSlider;
	private JCheckBox nodeColorCB;
	private JComboBox graphSizeCombo;
	private JLabel colorSchemeLabel;
	private JComboBox colorSchemeCB;
	private JPanel viewOptionsPanel;
	private GridBagConstraints viewOptionsGrid;
	private JCheckBox showHoverLabels;
	private JCheckBox showSelectedLabels;
	private String[] background;
	private String[] classColorSchemes;
	private String[] diveringColorSchemes;
	private JComboBox backgroundCB;
	private JComboBox classCB;
	private JComboBox spectrumCB;
	private JCheckBox filterEdgeCheckBox;
	private JButton dbConnectButton;
	private JButton viewInDBButton;
	private DBConnection dbConnection;
	



	public ScatterPlotControlPanel() {
		super();
	}

	public void setModel(ScatterPlotModel tpp) throws TPPException {
		this.spModel = tpp;
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
		setLayout(new GridBagLayout());
		GridBagConstraints grid = new GridBagConstraints();
		
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 0, 0, 0);
		grid.gridy = 0;
		grid.gridx = 0;
			
		addSelectionPanel(grid);
		
		grid.gridy++;
		grid.fill = GridBagConstraints.BOTH;
		grid.weightx = 1.0;
		grid.weighty = 1.0;
		
		addTabbedPane(grid);
		
		// for now have them available all the time - causing a conflict with having a graph loaded and creating 
		// a new cluster
		enableGraphButtons();

		revalidate();

	}
	
	private void addTabbedPane(GridBagConstraints grid) {
		
		viewOptionsPanel = new JPanel();
		JPanel projectionPanel = new JPanel();
		JPanel selectedPanel = new JPanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("View Options", viewOptionsPanel);
		tabbedPane.addTab("Significance", projectionPanel);
		tabbedPane.addTab("Selected Attributes", selectedPanel);
				
		viewOptionsPanel.setLayout(new GridBagLayout());
		viewOptionsGrid = new GridBagConstraints();
		
		viewOptionsGrid.fill = GridBagConstraints.BOTH;
		viewOptionsGrid.weightx = 1.0;
		viewOptionsGrid.weighty = 1.0;
		viewOptionsGrid.gridy = 0;
		
		viewOptionsGrid.insets = new Insets(0, 0, 0, 0);
		
		viewOptionsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		addViewOptionsPanel(viewOptionsPanel, viewOptionsGrid);
		
		projectionPanel.setLayout(new GridBagLayout());
		GridBagConstraints projectionGrid = new GridBagConstraints();
		projectionGrid.fill = GridBagConstraints.BOTH;
		projectionGrid.weightx = 1.0;
		projectionGrid.gridy = 0;
		
		addProjectionTableAndAttributeSelection(projectionPanel, projectionGrid);
		
		selectedPanel.setLayout(new GridBagLayout());
		GridBagConstraints selectedGrid = new GridBagConstraints();
		selectedGrid.fill = GridBagConstraints.BOTH;
		selectedGrid.weightx = 1.0;
		selectedGrid.gridy = 0;

		addSelectedAttributesTable(selectedPanel, selectedGrid);		
		
//		grid.gridy = 0;
//		grid.gridx = 0;
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(tabbedPane, grid);
	}
		
	private void addViewOptionsPanel(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		addAttributePanel(viewOptionsPanel, viewOptionsGrid);
		addClusterPanel(viewOptionsPanel, viewOptionsGrid);
		addLoadGraph(viewOptionsPanel, viewOptionsGrid);
		addColorEdges(viewOptionsPanel, viewOptionsGrid);
		addEdgeType(viewOptionsPanel, viewOptionsGrid);
		addEdgeDirection(viewOptionsPanel, viewOptionsGrid);
		addSliders(viewOptionsPanel, viewOptionsGrid);
		addLabelsPanel(viewOptionsPanel, viewOptionsGrid);
	}
	
	private void addSelectionPanel(GridBagConstraints grid) {
		// add selection attribute selector, with the current selection
		// attribute shown
		
		JPanel selectSeparatePanel = new JPanel();
		selectSeparatePanel.setLayout(new GridBagLayout());
		GridBagConstraints selectSeparateGrid = new GridBagConstraints();
		
		selectSeparateGrid.fill = GridBagConstraints.BOTH;
		selectSeparateGrid.weightx = 1.0;
		selectSeparateGrid.gridy = 0;
		selectSeparateGrid.insets = new Insets(0, 0, 0, 0);
		selectSeparatePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		
		selectCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.ALL_ATTRIBUTES, true);
		selectCombo.setMinimumSize(min);
		selectCombo.setSelectedAttribute(spModel.getSelectAttribute());
		selectCombo.addActionListener(this);
		selectCombo.setToolTipText("Choose which attribute is used to color and select points");
		JLabel selectAttributeSelectorLabel = new JLabel("Color points by: ", JLabel.RIGHT);
		selectCombo.setEditable(true);
		
		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;
		
		selectSeparatePanel.add(selectAttributeSelectorLabel, selectSeparateGrid);
		
		selectSeparateGrid.gridx = 1;
		selectSeparateGrid.gridwidth = 1;
		selectSeparatePanel.add(selectCombo, selectSeparateGrid);
		
		separateButton = new SeparatePointsButton(spModel, selectCombo);
		separateButton.setText("Separate points");
		separateButton.setToolTipText("<html><p width=\"300px\">Try to find " +
				"a projection in which the points are separated on the basis of " +
				"the chosen attribute. If the attribute is nominal then each of the " +
				"classes will be separated as far as possible. If the attribute is numeric " +
				"then the distance between points in will try to approximate to the difference " +
				"in value of this attribute</p></html>");
		
		// can only separate points by a nominal attribute
		// (SeparatePoints contains code to separate by numerical attributes as
		// well but it doesn't work very well)
		
		separateButton.setEnabled(selectCombo.getSelectedAttribute() != null && selectCombo.getSelectedAttribute().isNominal());
		
		selectSeparateGrid.gridx = 2;
		selectSeparateGrid.gridwidth = 1;
		selectSeparatePanel.add(separateButton, selectSeparateGrid);

		// add selection panel
		selectionPanel = new SelectionPanel(spModel);
		
		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = GridBagConstraints.REMAINDER;
		
		selectSeparatePanel.add(selectionPanel, selectSeparateGrid);
		
		background = new String[]{"Dark", "Light"};
		classColorSchemes = new String[]{"Default", "Custom", "Set2", "Accent", "Set1", "Set3", "Dark2", "Paired", "Pastel2", "Pastel1"};
		diveringColorSchemes = new String[]{"Default", "Spectral", "Red-Yellow-Blue", "Red-Grey", "Red-Blue", "Purple-Orange", "Purple-Green", "Pink-Green", "Brown-Blue"};  
		
		JLabel backgroundLabel = new JLabel("Background: ", JLabel.RIGHT);
		JLabel classLabel = new JLabel("Classification: ", JLabel.RIGHT);
		JLabel spectrumLabel = new JLabel("Spectrum: ", JLabel.RIGHT);
		
		backgroundCB = new JComboBox(background);
		classCB = new JComboBox(classColorSchemes);
		spectrumCB = new JComboBox(diveringColorSchemes);
		
		backgroundCB.setSelectedIndex(0);
		classCB.setSelectedIndex(0);
		spectrumCB.setSelectedIndex(0);
		
		backgroundCB.setMinimumSize(min);
		classCB.setMinimumSize(min);
		spectrumCB.setMinimumSize(min);
			
		colorSchemeLabel = new JLabel("Colour Scheme: ", JLabel.RIGHT);	
		colorSchemeButton = new JButton("Apply");
		
		colorSchemeButton.addActionListener(this);
		
		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;
		
		selectSeparatePanel.add(backgroundLabel, selectSeparateGrid);
		selectSeparateGrid.gridx++;
		selectSeparateGrid.gridwidth = 2;
		selectSeparatePanel.add(backgroundCB, selectSeparateGrid);
		
		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;
		
		selectSeparatePanel.add(classLabel, selectSeparateGrid);
		selectSeparateGrid.gridx++;
		selectSeparateGrid.gridwidth = 2;
		selectSeparatePanel.add(classCB, selectSeparateGrid);
		
		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;
		
		selectSeparatePanel.add(spectrumLabel, selectSeparateGrid);
		selectSeparateGrid.gridx++;
		selectSeparateGrid.gridwidth = 2;
		selectSeparatePanel.add(spectrumCB, selectSeparateGrid);
		
		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;
		
		selectSeparatePanel.add(colorSchemeLabel, selectSeparateGrid);
		selectSeparateGrid.gridx++;
		selectSeparateGrid.gridwidth = 2;
		selectSeparatePanel.add(colorSchemeButton, selectSeparateGrid);
		
		selectSeparateGrid.gridy++;
		selectSeparateGrid.gridx = 0;
		selectSeparateGrid.gridwidth = 1;
		
		addPointSelector(selectSeparatePanel, selectSeparateGrid);
		
		grid.gridx = 0;
		grid.gridy = 0;
		
		add(selectSeparatePanel, grid);
	}
	
	private void addAttributePanel(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel attributePanel = new JPanel();
		attributePanel.setLayout(new GridBagLayout());
		GridBagConstraints attributeGrid = new GridBagConstraints();
		
		attributeGrid.fill = GridBagConstraints.BOTH;
		attributeGrid.weightx = 1.0;
		attributeGrid.gridy = 0;
		attributeGrid.insets = new Insets(0, 0, 0, 0);
		attributePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		addSizeAttributeSelector(attributePanel, attributeGrid);
		addFillAttributeSelector(attributePanel, attributeGrid);
		addShapeAttributeSelector(attributePanel, attributeGrid);
		addMarkerSizeSlider(attributePanel, attributeGrid);
				
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridy++;
		viewOptionsPanel.add(attributePanel, viewOptionsGrid);
	}
		
	private void addSizeAttributeSelector(JPanel viewOptionsPanel,GridBagConstraints viewOptionsGrid) {
		// add size attribute selector
		sizeCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NUMERIC_ATTRIBUTES, true);
		sizeCombo.setMinimumSize(min);
		sizeCombo.setSelectedAttribute(spModel.getSizeAttribute());
		sizeCombo.addActionListener(this);
		sizeCombo.setToolTipText("Choose which attribute is used to determine the size of each point");
		
		JLabel sizeAttributeSelectorLabel = new JLabel("Size points by: ", JLabel.RIGHT);
		
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		
		viewOptionsPanel.add(sizeAttributeSelectorLabel, viewOptionsGrid);
		
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		
		viewOptionsPanel.add(sizeCombo, viewOptionsGrid);
	}
	
	private void addFillAttributeSelector(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		// add fill attribute selector
		fillCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NOMINAL_ATTRIBUTES, true);
		fillCombo.setMinimumSize(min);
		fillCombo.setSelectedAttribute(spModel.getFillAttribute());
		fillCombo.addActionListener(this);
		fillCombo.setToolTipText("Choose which attribute is used to determine whether each point is filled");
		
		JLabel fillAttributeSelectorLabel = new JLabel("Fill points by: ", JLabel.RIGHT);
		
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		
		viewOptionsPanel.add(fillAttributeSelectorLabel, viewOptionsGrid);
		
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		
		viewOptionsPanel.add(fillCombo, viewOptionsGrid);
	}
	
	private void addShapeAttributeSelector(JPanel viewOptionsPanel,	GridBagConstraints viewOptionsGrid) {
		// add shape attribute selector
		
		shapeCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NOMINAL_ATTRIBUTES, true);
		shapeCombo.setMinimumSize(min);
		shapeCombo.setSelectedAttribute(spModel.getShapeAttribute());
		shapeCombo.addActionListener(this);
		shapeCombo.setToolTipText("Choose which attribute is used to determine the shape of each point");
		
		JLabel shapeAttributeSelectorLabel = new JLabel("Shape points by: ", JLabel.RIGHT);
		
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		
		viewOptionsPanel.add(shapeAttributeSelectorLabel, viewOptionsGrid);
		
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		
		viewOptionsPanel.add(shapeCombo, viewOptionsGrid);
	}
	
	private void addMarkerSizeSlider(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		// add marker slider
		
		JLabel markerSizeLabel = new JLabel("Marker size: ", JLabel.RIGHT);
		
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		
		viewOptionsPanel.add(markerSizeLabel, viewOptionsGrid);
		
		markerSlider = new JSlider(1, (int) (spModel.MARKER_DEFAULT * 2000), (int) (spModel.MARKER_DEFAULT * 1000));
		markerSlider.setValue((int) (spModel.getMarkerSize() * 1000));
		markerSlider.addChangeListener(this);
		markerSlider.setToolTipText("Change the average size of the points");
		
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		viewOptionsPanel.add(markerSlider, viewOptionsGrid);
	}

	private void addClusterPanel(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel actionsPanel = new JPanel();
		actionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints actionsGrid = new GridBagConstraints();
		
		actionsGrid.fill = GridBagConstraints.BOTH;
		actionsGrid.weightx = 1.0;
		actionsGrid.gridy = 0;
		actionsGrid.insets = new Insets(0, 0, 0, 0);
		actionsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		// remove these (temporarily?) to simplify UI
		// addPointSelector(actionsPanel, actionsGrid);
		addClustererButton(actionsPanel, actionsGrid);
		// addClassificationButton(actionsPanel, actionsGrid);
		// addSeriesCreationButton(actionsPanel, actionsGrid);
		// addTestSetCreationButton(actionsPanel, actionsGrid);

		// and add these options to the control panel
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridy++;
		viewOptionsPanel.add(actionsPanel, viewOptionsGrid);
	}
	
	private void addPointSelector(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// add point selector
		
		pointSelectorCombo = new JComboBox(spModel.getPointDescriptions());
		pointSelectorCombo.setEditable(true);
		
		pointSelectorButton = new JButton("Add point to selection: ");
		pointSelectorButton.addActionListener(this);
		pointSelectorButton.setToolTipText("Add another point to the selection");
		
		dbConnectButton = new JButton("Connect to a database");
		dbConnectButton.addActionListener(this);
		
		viewInDBButton = new JButton("View in DB");
		viewInDBButton.addActionListener(this);
		
		actionsGrid.gridy++;
		actionsGrid.gridx = 0;
		actionsGrid.gridwidth = 1;
		
		actionsPanel.add(pointSelectorButton, actionsGrid);
		
		actionsGrid.gridx = 1;
		actionsGrid.gridwidth = 2;
		
		actionsPanel.add(pointSelectorCombo, actionsGrid);
		
		actionsGrid.gridy++;
		actionsGrid.gridx = 0;
		actionsGrid.gridwidth = 1;
		
		actionsPanel.add(dbConnectButton, actionsGrid);
		
		actionsGrid.gridx = 1;
		actionsGrid.gridwidth = 2;
		
		actionsPanel.add(viewInDBButton, actionsGrid);
		
	}
	
	private void addClustererButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// add clusterer button
		
		Vector<String> clusters = new Vector<String>();
		
		for (int k = 2; k < 20; k++)
			clusters.add(" N=" + k);
		
		clusterNumberCombo = new JComboBox(clusters);
		clusterNumberCombo.setToolTipText("Choose the number of clusters to find in the data");
		
		clusterButton = new JButton("Create clusters:");
		clusterButton.addActionListener(this);
		clusterButton.setToolTipText("<html><p width=\"300px\">Use an unsupervised clustering algorithm (K-means) to divide the points into clusters based on the value of the numeric attributes</p></html>");
		
		actionsGrid.gridy++;
		actionsGrid.gridx = 0;
		actionsGrid.gridwidth = 1;
		
		actionsPanel.add(clusterButton, actionsGrid);
		
		actionsGrid.gridx = 1;
		actionsGrid.gridwidth = 1;
		
		actionsPanel.add(clusterNumberCombo, actionsGrid);
	}
	
	private void addClassificationButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// Add classification crossvalidation, but only if there are any nominal
		// attributes
		if (spModel.getNominalAttributes() != null && spModel.getNominalAttributes().size() > 0) {

			/** Lets the user configure the classifier. */
			classifierChooser = new GenericObjectEditor();

			/** The panel showing the current classifier selection. */
			classifierChooserPanel = new PropertyPanel(classifierChooser);
			
			classifierChooser.setClassType(Classifier.class);
			classifierChooser.setValue(ExplorerDefaults.getClassifier());

			classificationTargetCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NOMINAL_ATTRIBUTES, false);
			classificationTargetCombo.setToolTipText("Choose the target attribute for the classifier");
			
			applyClassifierButton = new JButton("Apply classifier:");
			applyClassifierButton.addActionListener(this);
			applyClassifierButton.setToolTipText("<html><p width=\"300px\">Test the performance of a " +
					"classification algorithm on this data. The chosen classifier is applied using 10-fold " +
					"cross-validation, and the resulting predicted classifications and error are " +
					"shown.</p></html>");
			
			// classificationCombo = new JComboBox(new String[] { "lazy.IBk",
			// "bayes.NaiveBayes", "functions.SMO",
			// "functions.MultilayerPerceptron" });
			// classificationCombo
			
			classifierChooserPanel.setToolTipText("Choose a classifier from the Weka toolkit");
			
			actionsGrid.gridy++;
			actionsGrid.gridx = 0;
			actionsGrid.gridwidth = 1;
			
			actionsPanel.add(applyClassifierButton, actionsGrid);
			
			actionsGrid.gridx = 1;
			
			actionsPanel.add(classifierChooserPanel, actionsGrid);
			
			actionsGrid.gridx = 2;
			
			actionsPanel.add(classificationTargetCombo, actionsGrid);
		}
	}
		
	private void addLoadGraph(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel loadGraphPanel = new JPanel();
		loadGraphPanel.setLayout(new GridBagLayout());
		GridBagConstraints loadGraphGrid = new GridBagConstraints();
		
		loadGraphGrid.fill = GridBagConstraints.BOTH;
		loadGraphGrid.weightx = 1.0;
		loadGraphGrid.gridy = 0;
		loadGraphGrid.insets = new Insets(0, 0, 0, 0);
		loadGraphPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		loadGraphButton = new JButton("Load Graph");
		directedCheckBox = new JCheckBox("Show Arrows", false);
		directedCheckBox.setEnabled(false);
		
		filterEdgeCheckBox = new JCheckBox("Filter Edges", false);
		filterEdgeCheckBox.setEnabled(false);
		
		loadGraphButton.addActionListener(this);
		directedCheckBox.addActionListener(this);
		filterEdgeCheckBox.addActionListener(this);
		
		loadGraphButton.setToolTipText("Load a new graph from a csv file");
		directedCheckBox.setToolTipText("Indicate if the graph is directed or not");
		filterEdgeCheckBox.setToolTipText("Show only the edges of selected nodes");
		
		loadGraphGrid.gridy++;
		loadGraphGrid.gridx = 0;
		loadGraphGrid.gridwidth = 1;
		
		loadGraphPanel.add(loadGraphButton, loadGraphGrid);
		
		loadGraphGrid.gridx = 1;
		loadGraphGrid.gridwidth = 1;
		
		loadGraphPanel.add(directedCheckBox, loadGraphGrid);
		
		loadGraphGrid.gridx = 2;
		loadGraphGrid.gridwidth = 1;
		
		loadGraphPanel.add(filterEdgeCheckBox, loadGraphGrid);
		
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridy++;
		
		viewOptionsPanel.add(loadGraphPanel, viewOptionsGrid);
	}
	
	private void addColorEdges(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel edgeColorPanel = new JPanel();
		edgeColorPanel.setLayout(new GridBagLayout());
		GridBagConstraints edgeColorGrid = new GridBagConstraints();
		
		edgeColorGrid.fill = GridBagConstraints.BOTH;
		edgeColorGrid.weightx = 1.0;
		edgeColorGrid.gridy = 0;
		edgeColorGrid.insets = new Insets(0, 0, 0, 0);
		edgeColorPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		sourceRB = new JRadioButton("Source", false);
		targetRB = new JRadioButton("Target", false);
		mixedRB = new JRadioButton("Mixed", false);
		noneRB = new JRadioButton("None", true);
		
		sourceRB.setEnabled(false);
		targetRB.setEnabled(false);
		mixedRB.setEnabled(false);
		noneRB.setEnabled(false);
		
		ButtonGroup group = new ButtonGroup();
		group.add(noneRB);
		group.add(mixedRB);
		group.add(sourceRB);
		group.add(targetRB);
		
		//if(graph != null) {
		//	sizeCombo.addItem(spModel.getNodeDegree(graph));
		//	}
		sourceRB.addActionListener(this);
		targetRB.addActionListener(this);
		mixedRB.addActionListener(this);
		noneRB.addActionListener(this);
		
		sourceRB.addItemListener(this);
		targetRB.addItemListener(this);
		mixedRB.addItemListener(this);
		noneRB.addItemListener(this);
		
		noneRB.setToolTipText("Color all edges grey");
		mixedRB.setToolTipText("Color edges based on a combination of their source and target nodes");
		sourceRB.setToolTipText("Color edge based on source node color");
		targetRB.setToolTipText("Color edges based on their target node color");
		
		edgeColorGrid.gridy++;
		edgeColorGrid.gridx = 0;
		edgeColorGrid.gridwidth = 1;
		
		edgeColorPanel.add(noneRB, edgeColorGrid);
		
		edgeColorGrid.gridx = 1;
		edgeColorGrid.gridwidth = 1;
		
		edgeColorPanel.add(mixedRB, edgeColorGrid);
		
		edgeColorGrid.gridy++;
		edgeColorGrid.gridx = 0;
		edgeColorGrid.gridwidth = 1;
		
		edgeColorPanel.add(sourceRB, edgeColorGrid);
		
		edgeColorGrid.gridx = 1;
		edgeColorGrid.gridwidth = 1;
		
		edgeColorPanel.add(targetRB, edgeColorGrid);
		
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridy++;
		
		viewOptionsPanel.add(edgeColorPanel, viewOptionsGrid);		
		
	}
	
	private void addEdgeType(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel edgeStylePanel = new JPanel();
		edgeStylePanel.setLayout(new GridBagLayout());
		GridBagConstraints edgeStyleGrid = new GridBagConstraints();
		
		edgeStyleGrid.fill = GridBagConstraints.BOTH;
		edgeStyleGrid.weightx = 1.0;
		edgeStyleGrid.gridy = 0;
		edgeStyleGrid.insets = new Insets(0, 0, 0, 0);
		edgeStylePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		straightRB = new JRadioButton("Straight", true);
		curvedRB = new JRadioButton("Curved", false);
		hideRB = new JRadioButton("Hide", false);
		
		straightRB.setEnabled(false);
		curvedRB.setEnabled(false);
		hideRB.setEnabled(false);
		
		ButtonGroup group = new ButtonGroup();
		group.add(straightRB);
		group.add(curvedRB);
		group.add(hideRB);

		straightRB.addActionListener(this);
		curvedRB.addActionListener(this);
		hideRB.addActionListener(this);
		
		straightRB.setToolTipText("Use straight edges");
		mixedRB.setToolTipText("Use curved edges");
		sourceRB.setToolTipText("Hide all edges");
		
		edgeStyleGrid.gridy++;
		edgeStyleGrid.gridx = 0;
		edgeStyleGrid.gridwidth = 1;
		
		edgeStylePanel.add(straightRB, edgeStyleGrid);
		
		edgeStyleGrid.gridx = 1;
		edgeStyleGrid.gridwidth = 1;
		
		edgeStylePanel.add(curvedRB, edgeStyleGrid);
		
		edgeStyleGrid.gridx = 2;
		edgeStyleGrid.gridwidth = 2;
		
		edgeStylePanel.add(hideRB, edgeStyleGrid);
		
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridy++;
		
		viewOptionsPanel.add(edgeStylePanel, viewOptionsGrid);
		
	}
	
	private void addEdgeDirection(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel edgeDirectionPanel = new JPanel();
		edgeDirectionPanel.setLayout(new GridBagLayout());
		GridBagConstraints edgeDirectionGrid = new GridBagConstraints();
		
		edgeDirectionGrid.fill = GridBagConstraints.BOTH;
		edgeDirectionGrid.weightx = 1.0;
		edgeDirectionGrid.gridy = 0;
		edgeDirectionGrid.insets = new Insets(0, 0, 0, 0);
		edgeDirectionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		incomingCB = new JCheckBox("Incoming", true);
		outgoingCB = new JCheckBox("Outgoing", true);
		
		incomingCB.setEnabled(false);
		outgoingCB.setEnabled(false);
		
		incomingCB.addActionListener(this);
		outgoingCB.addActionListener(this);
		
		incomingCB.setToolTipText("Show a node's incoming links");
		outgoingCB.setToolTipText("Show a node's outgoing links");
		
		
		edgeDirectionGrid.gridy++;
		edgeDirectionGrid.gridx = 0;
		edgeDirectionGrid.gridwidth = 1;
		
		edgeDirectionPanel.add(incomingCB, edgeDirectionGrid);
		
		edgeDirectionGrid.gridx = 1;
		edgeDirectionGrid.gridwidth = 1;
		
		edgeDirectionPanel.add(outgoingCB, edgeDirectionGrid);
		
		viewOptionsGrid.gridy++;
		viewOptionsPanel.add(edgeDirectionPanel, viewOptionsGrid);		
	
	}
	
	private void addSliders(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridBagLayout());
		GridBagConstraints sliderGrid = new GridBagConstraints();
		
		sliderGrid.fill = GridBagConstraints.BOTH;
		sliderGrid.weightx = 1.0;
		sliderGrid.gridy = 0;
		sliderGrid.insets = new Insets(0, 0, 0, 0);
		sliderPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		JLabel transparencyLabel = new JLabel("Transparency: ", JLabel.LEFT);
		
		sliderGrid.gridy = 0;
		sliderGrid.gridx = 0;
		sliderGrid.gridwidth = 1;
		
		sliderPanel.add(transparencyLabel, sliderGrid);
		
		transparencySlider = new JSlider(0,255,spModel.getTransparencyLevel());
		transparencySlider.setEnabled(false);
		transparencySlider.setValue((int) (spModel.getTransparencyLevel()));
		transparencySlider.addChangeListener(this);
		transparencySlider.setToolTipText("Change the amount of transparency for the non-highlighted point and edges");
		
		sliderGrid.gridx = 1;
		sliderGrid.gridwidth = 2;
		sliderPanel.add(transparencySlider, sliderGrid);
		
		addGraphSizeSelector(sliderPanel, sliderGrid);
				
		viewOptionsGrid.gridy++;
		viewOptionsPanel.add(sliderPanel, viewOptionsGrid);		
	
	}
	
	private void addGraphSizeSelector(JPanel viewOptionsPanel,GridBagConstraints viewOptionsGrid) {
		
		String[] degreeOptions = {"None","Degree", "In Degree", "Out Degree"};
		graphSizeCombo = new JComboBox(degreeOptions);
		graphSizeCombo.setEnabled(false);
		
		graphSizeCombo.setMinimumSize(min);
		graphSizeCombo.setSelectedIndex(0);
		graphSizeCombo.addActionListener(this);
		graphSizeCombo.setToolTipText("Choose which graph metric is used to determine the size of each point");
		
		JLabel sizeAttributeSelectorLabel = new JLabel("Size points by: ", JLabel.RIGHT);
		
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		
		viewOptionsPanel.add(sizeAttributeSelectorLabel, viewOptionsGrid);
		
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		
		viewOptionsPanel.add(graphSizeCombo, viewOptionsGrid);
	}
	
	private void addLabelsPanel(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new GridBagLayout());
		GridBagConstraints labelGrid = new GridBagConstraints();
		
		labelGrid.fill = GridBagConstraints.BOTH;
		labelGrid.weightx = 1.0;
		labelGrid.gridy = 0;
		labelGrid.insets = new Insets(0, 0, 0, 0);
		labelPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		showLabelsCheckBox = new JCheckBox("Show Labels", false);
		showHighlightedLabels = new JCheckBox("Highlighted Only", false);
		showHoverLabels = new JCheckBox("Hover", false);
		showSelectedLabels = new JCheckBox("Selected", false);
		nodeColorCB = new JCheckBox("Node Color", false);
		
		showLabelsCheckBox.setEnabled(false);
		showHighlightedLabels.setEnabled(false);
		showHoverLabels.setEnabled(false);
		showSelectedLabels.setEnabled(false);
		nodeColorCB.setEnabled(false);
		
		showLabelsCheckBox.addActionListener(this);
		showHighlightedLabels.addActionListener(this);
		showHoverLabels.addActionListener(this);
		showSelectedLabels.addActionListener(this);
		nodeColorCB.addActionListener(this);
		
		showLabelsCheckBox.setToolTipText("Show the point's label or number");
		showHighlightedLabels.setToolTipText("Only show labels of those nodes currently in focus");
		nodeColorCB.setToolTipText("Color labels the same as their node's colour");
		
		labelGrid.gridx = 0;
		labelPanel.add(showLabelsCheckBox, labelGrid);
		
		labelGrid.gridx = 1;
		labelPanel.add(showHighlightedLabels, labelGrid);
		
		labelGrid.gridx = 2;
		labelPanel.add(showHoverLabels, labelGrid);
		
		labelGrid.gridy++;
		labelGrid.gridx = 0;
		labelGrid.gridwidth = 1;
		
		labelPanel.add(nodeColorCB, labelGrid);
		
		labelGrid.gridx++;
		labelPanel.add(showSelectedLabels, labelGrid);
		
		JLabel labelSizeLabel = new JLabel("Label Size: ", JLabel.RIGHT);
		
		labelGrid.gridy++;
		labelGrid.gridx = 0;
		labelGrid.gridwidth = 1;
		
		labelPanel.add(labelSizeLabel, labelGrid);
		
		labelSlider = new JSlider(1,100,(int) (spModel.getLabelSize()*100));
		labelSlider.setValue((int) (spModel.getLabelSize()*100));
		labelSlider.addChangeListener(this);
		labelSlider.setToolTipText("Change the size of the labels");
		labelSlider.setEnabled(false);
		
		labelGrid.gridx = 1;
		labelGrid.gridwidth = 2;
		labelPanel.add(labelSlider, labelGrid);
				
		viewOptionsGrid.gridy++;
		viewOptionsPanel.add(labelPanel, viewOptionsGrid);
		
	}
	
	private void addTestSetCreationButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// Add test set creation button
		Vector<Integer> testSetKValues = new Vector<Integer>();
		
		for (int k = 2; k < 11; k++)
			testSetKValues.add(new Integer(k));
		
		createTestSetKCombo = new JComboBox(testSetKValues);
		createTestSetKCombo.setToolTipText("<html><p width=\"300px\">What proportion of " +
				"the data will be used as a test set (1/k)</p></html>");
		
		createTestSetButton = new JButton("Create test set");
		createTestSetButton.addActionListener(this);
		createTestSetButton.setToolTipText("<html><p width=\"300px\">Create a test set. " +
						"The points in the test set will not be affected by projection pursuit " +
						"operations (dragging or separating etc), so this shows the generalisability " +
						"and robustness of the projection pursuit operations</p></html>");
		
		removeTestSetButton = new JButton("Remove test set");
		removeTestSetButton.addActionListener(this);
		
		actionsGrid.gridy++;
		actionsGrid.gridx = 0;
		actionsGrid.gridwidth = 1;
		
		actionsPanel.add(createTestSetKCombo, actionsGrid);
		actionsGrid.gridx = 1;
		
		actionsPanel.add(createTestSetButton, actionsGrid);
		
		actionsGrid.gridx = 2;
		
		actionsPanel.add(removeTestSetButton, actionsGrid);
	}

	private void addSeriesCreationButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// !!TODO it should be possible to identify series by string attributes
		if (spModel.getSeries() == null) {

			seriesIdCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NOMINAL_ATTRIBUTES, true);
			seriesIdCombo.setToolTipText("<html><p width=\"300px\">Choose the attribute that " +
					"identifies which series each point is a member of. If no attribute is chosen " +
					"then include all points in a single series</p></html>");
			
			seriesIndexCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NUMERIC_ATTRIBUTES, false);
			seriesIndexCombo.setMinimumSize(min);
			seriesIndexCombo.setToolTipText("<html><p width=\"300px\">Choose the " +
					"attribute used to order points in the series, such as a date.</p></html>");
			seriesIdCombo.setMinimumSize(min);
			
			if (spModel.getSeries() != null) {
				seriesIdCombo.setSelectedAttribute(spModel.getSeries()
						.getIdAttribute());
				seriesIndexCombo.setSelectedAttribute(spModel.getSeries()
						.getIndexAttribute());
			}
			
			createSeriesButton = new JButton("Show series (id & order)");
			createSeriesButton.addActionListener(this);
			createSeriesButton.setToolTipText("Divide the points into series, connected by lines");
			
			actionsGrid.gridy++;
			actionsGrid.gridx = 0;
			actionsGrid.gridwidth = 1;
			
			actionsPanel.add(createSeriesButton, actionsGrid);
			
			actionsGrid.gridx = 1;
			
			actionsPanel.add(seriesIdCombo, actionsGrid);
			
			actionsGrid.gridx = 2;
			
			actionsGrid.gridwidth = GridBagConstraints.REMAINDER;
			
			actionsPanel.add(seriesIndexCombo, actionsGrid);
		}

		// if there are series currently defined then add a button for
		// denoising and removing them
		if (spModel.getSeries() != null) {
			smoothSeriesButton = new SmoothButton(spModel);
			smoothSeriesButton.setText("Smooth Series");
			smoothSeriesButton.setToolTipText("<html><p width=\"300px\">Try to find" +
					" a view of the data that removes low frequency noise and shows longer-term " +
					"evolution of the system</p></html>");
			
			actionsGrid.gridy++;
			actionsGrid.gridx = 0;
			actionsGrid.gridwidth = 1;
			
			actionsPanel.add(smoothSeriesButton, actionsGrid);

			removeSeriesButton = new JButton("Remove Series");
			removeSeriesButton.addActionListener(this);
			removeSeriesButton.setToolTipText("No longer show the series lines");
			
			actionsGrid.gridx = 1;
			actionsGrid.gridwidth = 1;
			
			actionsPanel.add(removeSeriesButton, actionsGrid);
		}
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
					
					selectCombo.setSelectedIndex(attributeIndex + 1);
					spModel.setSeparationAttribute(spModel.getInstances().attribute(attributeIndex));
					spModel.setColourAttribute(spModel.getInstances().attribute(attributeIndex));
					separateButton.setEnabled(spModel.getSelectAttribute() != null && spModel.getSelectAttribute().isNominal());
					revalidate();
					repaint();
				}
			}
		});
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
					
					selectCombo.setSelectedIndex(attributeIndex + 1);
					spModel.setSeparationAttribute(spModel.getInstances().attribute(attributeIndex));
					spModel.setColourAttribute(spModel.getInstances().attribute(attributeIndex));
					separateButton.setEnabled(spModel.getSelectAttribute() != null && spModel.getSelectAttribute().isNominal());
					revalidate();
					repaint();
				}
			}
		});
	}

	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == clusterButton) {
			Attribute cluster = spModel
					.cluster(((JComboBox) clusterNumberCombo)
							.getSelectedIndex() + 2);
			spModel.setSelectAttribute(cluster);
			spModel.setColourAttribute(cluster);
//			selectionPanel.initialiseSelectionButtons();
//			selectionPanel.revalidate();
//			selectionPanel.repaint();
			init();
		}

		if (event.getSource() == createSeriesButton) {
			spModel.createSeries(seriesIndexCombo.getSelectedAttribute(),
					seriesIdCombo.getSelectedAttribute());
		}

		if (event.getSource() == removeSeriesButton) {
			spModel.removeSeries();
		}
		if (event.getSource() == fillCombo)
			spModel.setFillAttribute(fillCombo.getSelectedAttribute());

		if (event.getSource() == shapeCombo)
			spModel.setShapeAttribute(shapeCombo.getSelectedAttribute());

		if (event.getSource() == selectCombo) {
			// by default we should color the points by the same attribute we
			// are selecting by
			spModel.setSelectAttribute(selectCombo.getSelectedAttribute());
			spModel.setColourAttribute(selectCombo.getSelectedAttribute());
			selectionPanel.initialiseSelectionButtons();
			separateButton.setEnabled(spModel.getSelectAttribute() != null && spModel.getSelectAttribute().isNominal());
			revalidate();
			repaint();
		}

		if (event.getSource() == createTestSetButton)
			spModel.createTestSet(((Integer) createTestSetKCombo
					.getSelectedItem()).intValue());

		if (event.getSource() == removeTestSetButton)
			spModel.removeTestSet();

		if (event.getSource() == removeAttributeButton
				&& projectionTable.getSelectedAttributeIndices() != null
				&& projectionTable.getSelectedAttributeIndices().length > 0)
			spModel.removeAttributes(projectionTable.getSelectedAttributes());

		if (event.getSource() == undoButton)
			spModel.undo();

		if (event.getSource() == pointSelectorButton) {
			spModel.selectPoint(pointSelectorCombo.getSelectedIndex());
			spModel.drawRectangleAroundSelectedPoints();
		}

		if (event.getSource() == applyClassifierButton) {
			try {

				Attribute[] classification = spModel.createCrossValidation(
						classificationTargetCombo.getSelectedAttribute(),
						(Classifier) classifierChooser.getValue());
				// and fill the points by the error
				spModel.setColourAttribute(classification[0]);
				spModel.setSelectAttribute(classification[0]);
				spModel.setFillAttribute(classification[1]);
				init();
			//	selectionPanel.initialiseSelectionButtons();
			//	selectionPanel.revalidate();
			//	selectionPanel.repaint();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (event.getSource() == loadGraphButton)
			try {
				selectGraphImportProperties();
//				spModel.loadGraph(new GraphImporter().importGraph(spModel.getInstances(), spModel.getEdgeAttributeIndex()));
//				enableGraphButtons();
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (event.getSource() == sizeCombo){
			spModel.setSizeAttribute(sizeCombo.getSelectedAttribute());
		}
		if (event.getSource() == graphSizeCombo){
			sizeCombo.setSelectedIndex(0);
			spModel.setSizeAttribute(null);
			spModel.setGraphSizeAttribute(graph, graphSizeCombo.getSelectedIndex());
		}
		if (event.getSource() == colorSchemeButton){
			applyColorScheme(spModel, (String)backgroundCB.getSelectedItem());
		}
		if (event.getSource() == straightRB){
			spModel.setShowGraph(straightRB.hasFocus());
			spModel.setBezierEdges(curvedRB.hasFocus());
		}		
		if (event.getSource() == curvedRB) {
			spModel.setShowGraph(curvedRB.hasFocus());
			spModel.setBezierEdges(curvedRB.hasFocus());
		}
		if (event.getSource() == hideRB) {
			spModel.setShowGraph(!hideRB.hasFocus());
			}
		if(event.getSource() == directedCheckBox){
			spModel.setArrowedEdges(directedCheckBox.isSelected());
		}
		if(event.getSource() == filterEdgeCheckBox) {
			spModel.setAllFilterEdges(filterEdgeCheckBox.isSelected());
		}
		if(event.getSource() == incomingCB){
			spModel.showIncomingEdges(incomingCB.isSelected());
		}
		if(event.getSource() == outgoingCB){
			spModel.showOutgoingEdges(outgoingCB.isSelected());
		}
		if(event.getSource() == showLabelsCheckBox){
			spModel.showLabels(showLabelsCheckBox.isSelected());
		}
		if(event.getSource() == showHighlightedLabels) {
			spModel.showHightlightedLabels(showHighlightedLabels.isSelected());
		}
		if(event.getSource() == showHoverLabels){
			spModel.showHoverLabels(showHoverLabels.isSelected());
		}
		if(event.getSource() == showSelectedLabels) {
			spModel.showSelectedLabels(showSelectedLabels.isSelected());
		}
		if(event.getSource() == nodeColorCB) {
			spModel.showNodeLabelColor(nodeColorCB.isSelected());
		}
		if(event.getSource() == dbConnectButton) {
			DBConnectionGUI dbConnectionGUI = new DBConnectionGUI(this);
		}
		if(event.getSource() == viewInDBButton) {
			try {
				spModel.runQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	public void addDatabaseConnection(String username, String password, 
			String database, String table) {
				spModel.addDatabaseConnection(username, password, 
			database, table);
	}

	private void enableGraphButtons() {
		directedCheckBox.setEnabled(true);
		filterEdgeCheckBox.setEnabled(true);
		noneRB.setEnabled(true);
		sourceRB.setEnabled(true);
		targetRB.setEnabled(true);
		mixedRB.setEnabled(true);
		straightRB.setEnabled(true);
		curvedRB.setEnabled(true);
		hideRB.setEnabled(true);
		incomingCB.setEnabled(true);
		outgoingCB.setEnabled(true);
		transparencySlider.setEnabled(true);
		graphSizeCombo.setEnabled(true);
		showLabelsCheckBox.setEnabled(true);
		showHighlightedLabels.setEnabled(true);
		showHoverLabels.setEnabled(true);
		showSelectedLabels.setEnabled(true);
		nodeColorCB.setEnabled(true);
		labelSlider.setEnabled(true);
		
	}
	
	private void applyColorScheme(ScatterPlotModel spModel, String bg) {
		if(bg.equals("Dark"))
			spModel.setColours(ColourScheme.createDarkColorScheme(setSpectrumColours((String)spectrumCB.getSelectedItem()), 
					setClassColours((String)classCB.getSelectedItem())));
		if(bg.equals("Light"))
			spModel.setColours(ColourScheme.createLightColorScheme(setSpectrumColours((String)spectrumCB.getSelectedItem()), 
					setClassColours((String)classCB.getSelectedItem())));
		
		selectionPanel.initialiseSelectionButtons();
//		selectionPanel.revalidate();
		init();
		//revalidate();
//		selectionPanel.repaint();
	}
		
	private Color[] setClassColours(String s) {
		
		Color[] classColors = null;
		
		if(s != null) {
			if(s.equals("Set1") || s.equals("Default"))
				classColors = ColourScheme.getSet1();
			if(s.equals("Set2"))
				classColors = ColourScheme.getSet2();
			if(s.equals("Set3"))
				classColors = ColourScheme.getSet3();
			if(s.equals("Pastel1"))
				classColors = ColourScheme.getPastel1();
			if(s.equals("Pastel2"))
				classColors = ColourScheme.getPastel2();
			if(s.equals("Dark2"))
				classColors = ColourScheme.getDark2();
			if(s.equals("Accent"))
				classColors = ColourScheme.getAccent();
			if(s.equals("Paired"))
				classColors = ColourScheme.getPaired();
			if(s.equals("Custom"))
				classColors = getClassColors();
		}
		return classColors;
	}
	
	private Color[] setSpectrumColours(String s) {
		
		Color[] spectrumColors = null;
		
		if(s != null) {
			if(s.equals("Spectral"))
				spectrumColors = ColourScheme.getSpectral();
			if(s.equals("Red-Yellow-Green"))
				spectrumColors = ColourScheme.getRdYlGn();
			if(s.equals("Red-Yellow-Blue"))
				spectrumColors = ColourScheme.getRdY1Bu();
			if(s.equals("Red-Grey"))
				spectrumColors = ColourScheme.getRdGy();
			if(s.equals("Red-Blue")|| s.equals("Default"))
				spectrumColors = ColourScheme.getRdBu();
			if(s.equals("Purple-Orange"))
				spectrumColors = ColourScheme.getPuOr();
			if(s.equals("Purple-Green"))
				spectrumColors = ColourScheme.getPRGn();
			if(s.equals("Pink-Green"))
				spectrumColors = ColourScheme.getPiYG();
			if(s.equals("Brown-Blue"))
				spectrumColors = ColourScheme.getBrBG();
		}
		return spectrumColors;
	}
	
	private Color[] getClassColors(){
		
		Vector<SelectButton> selectButtons = selectionPanel.getSelectButtons();
		
		Color[] classColors = new Color[selectButtons.size()];
		
		for (int b = 0; b < selectButtons.size(); b++){
			classColors[b] = selectButtons.get(b).getForeground();
		}
		return classColors;
	}

	private void selectGraphImportProperties() {
		
		String[] stringAttributes = new String[spModel.getStringAttributes().size()];
		//spModel.getStringAttributes().toArray(stringAttributes);
		
		Vector<Attribute> vStringAttributes = spModel.getStringAttributes();
		for (int i = 0; i < vStringAttributes.size(); i++) {
			stringAttributes[i] = vStringAttributes.get(i).name();
		}
		
		GraphImportGUI graphImportGUI = new GraphImportGUI(this, stringAttributes);

		enableGraphButtons();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.ControlPanel#getClassificationPanel()
	 */
	public SelectionPanel getClassificationPanel() {
		return selectionPanel;
	}

	/** Marker slider state has changed */
	public void stateChanged(ChangeEvent e) {
		if (markerSlider == (JSlider) e.getSource())
			spModel.setMarkerSize(markerSlider.getValue() / 1000d);
		if (transparencySlider == (JSlider) e.getSource())
			spModel.setTransparencyLevel(transparencySlider.getValue());
		if (beizerSlider == (JSlider) e.getSource())
			spModel.setBeizerCurviness((float)beizerSlider.getValue()/100f);
		if (labelSlider == (JSlider) e.getSource())
			spModel.setLabelSize(labelSlider.getValue() / 100d);
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
		default:
			repaint();
		}
	}

	public ProjectionTable getProjectionTable() {
		return projectionTable;
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if(event.getSource() == noneRB){
			spModel.setDefaultColorEdges(noneRB.isSelected());
		}
		if(event.getSource() == sourceRB) {
			spModel.setSourceColorEdges(sourceRB.isSelected());
		}
		if(event.getSource() == targetRB) {
			spModel.setTargetColorEdges(targetRB.isSelected());
		}
		if(event.getSource() == mixedRB) {
			spModel.setMixedColorEdges(mixedRB.isSelected());
		}
	}

}