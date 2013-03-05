package tpp;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import weka.core.Attribute;

@SuppressWarnings("serial")
public class GraphOptionsTab extends JPanel implements ActionListener,
		ItemListener, ChangeListener {

	ScatterPlotModel spModel;
	private Graph graph;
	private EdgeModel edgeModel;
	private PointModel pointModel;
	private GraphModel graphModel;

	private JButton loadGraphButton;
	private JCheckBox directedCheckBox;
	private JCheckBox filterEdgeCheckBox;
	private JCheckBox edgeWeightsCheckBox;

	private JRadioButton sourceRB;
	private JRadioButton targetRB;
	private JRadioButton mixedRB;

	private JRadioButton noneRB;
	private JRadioButton straightRB;
	private JRadioButton curvedRB;
	private JRadioButton hideRB;
	private JRadioButton bundledRB;
	private JRadioButton fannedRB;

	private JCheckBox incomingCB;
	private JCheckBox outgoingCB;

	private JSlider transparencySlider;
	private JComboBox graphSizeCombo;

	private JCheckBox showLabelsCheckBox;
	private JCheckBox showHighlightedLabels;
	private JCheckBox showHoverLabels;
	private JCheckBox showSelectedLabels;
	private JCheckBox nodeColorCB;
	private JSlider labelSlider;

	private Border raisedetched = BorderFactory
			.createEtchedBorder(EtchedBorder.RAISED);
	private Dimension min = new Dimension(100, 20);
	private JCheckBox edgeWeightFilterCB;


	public GraphOptionsTab(ScatterPlotModel spModel) {

		super();
		this.spModel = spModel;
		edgeModel = spModel.getEdgeModel();
		pointModel = spModel.getPointModel();
		graphModel = spModel.getGraphModel();
		init();
		setVisible(true);
		
	}

	private void init() {

		setLayout(new GridBagLayout());
		GridBagConstraints graphGrid = new GridBagConstraints();

		graphGrid.fill = GridBagConstraints.BOTH;
		graphGrid.weightx = 1.0;
		graphGrid.weighty = 1.0;

		addGraphOptionsPanel(this, graphGrid);

		// if(spModel.graphLoaded())
		enableGraphButtons();

	}

	private void addGraphOptionsPanel(JPanel graphPanel,
			GridBagConstraints graphGrid) {

		graphGrid.fill = GridBagConstraints.HORIZONTAL;
		graphGrid.weightx = 1.0;
		graphGrid.gridy = 0;
		graphGrid.gridx = 0;
		graphGrid.insets = new Insets(1, 1, 1, 1);

		addLoadGraph(graphPanel, graphGrid);

		graphGrid.gridx = 0;
		graphGrid.gridy++;

		addColorEdges(graphPanel, graphGrid);

		graphGrid.gridx = 0;
		graphGrid.gridy++;

		addEdgeType(graphPanel, graphGrid);

		graphGrid.gridx = 0;
		graphGrid.gridy++;

		chooseVisibleEdges(graphPanel, graphGrid);

		graphGrid.gridx = 0;
		graphGrid.gridy++;

		addSliders(graphPanel, graphGrid);

		graphGrid.gridx = 0;
		graphGrid.gridy++;

		addLabelsPanel(graphPanel, graphGrid);

		graphGrid.gridx = 0;
		graphGrid.gridy++;

		addEdgeFilterPanel(graphPanel, graphGrid);
	}

	private void addLoadGraph(JPanel panel, GridBagConstraints grid) {

		JPanel loadGraphPanel = new JPanel();
		loadGraphPanel.setLayout(new GridBagLayout());
		GridBagConstraints loadGraphGrid = new GridBagConstraints();

		loadGraphGrid.fill = GridBagConstraints.BOTH;
		loadGraphGrid.weightx = 1.0;
		loadGraphGrid.gridy = 0;
		loadGraphGrid.insets = new Insets(0, 0, 0, 0);

		TitledBorder border = BorderFactory.createTitledBorder(raisedetched,
				"Load graph options:");
		loadGraphPanel.setBorder(border);

		loadGraphButton = new JButton("Load Graph");

		directedCheckBox = new JCheckBox("Show Arrows");
		directedCheckBox.setEnabled(false);
		directedCheckBox.setSelected(edgeModel.arrowedEdges());

		filterEdgeCheckBox = new JCheckBox("Filter Edges");
		filterEdgeCheckBox.setEnabled(false);
		filterEdgeCheckBox.setSelected(edgeModel.filterAllEdges());

		edgeWeightsCheckBox = new JCheckBox("Weight Edges");
		edgeWeightsCheckBox.setEnabled(false);
		edgeWeightsCheckBox.setSelected(edgeModel.viewEdgeWeights());

		loadGraphButton.addActionListener(this);
		directedCheckBox.addActionListener(this);
		filterEdgeCheckBox.addActionListener(this);
		edgeWeightsCheckBox.addActionListener(this);

		loadGraphButton.setToolTipText("Load a new graph from a csv file");
		directedCheckBox
				.setToolTipText("Indicate if the graph is directed or not");
		filterEdgeCheckBox
				.setToolTipText("Show only the edges of selected nodes");

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

		loadGraphGrid.gridx = 3;
		loadGraphGrid.gridwidth = 1;

		loadGraphPanel.add(edgeWeightsCheckBox, loadGraphGrid);

		panel.add(loadGraphPanel, grid);
	}

	private void addColorEdges(JPanel viewOptionsPanel,
			GridBagConstraints viewOptionsGrid) {

		JPanel edgeColorPanel = new JPanel();
		edgeColorPanel.setLayout(new GridBagLayout());
		GridBagConstraints edgeColorGrid = new GridBagConstraints();

		edgeColorGrid.fill = GridBagConstraints.BOTH;
		edgeColorGrid.weightx = 1.0;
		edgeColorGrid.gridy = 0;
		edgeColorGrid.insets = new Insets(0, 0, 0, 0);

		TitledBorder border = BorderFactory.createTitledBorder(raisedetched,
				"Pick an edge colour scheme:");
		edgeColorPanel.setBorder(border);

		sourceRB = new JRadioButton("Source");
		targetRB = new JRadioButton("Target");
		mixedRB = new JRadioButton("Mixed");
		noneRB = new JRadioButton("None");

		sourceRB.setSelected(edgeModel.sourceColorEdges());
		targetRB.setSelected(edgeModel.targetColorEdges());
		mixedRB.setSelected(edgeModel.mixedColorEdges());
		noneRB.setSelected(edgeModel.defaultColorEdges());

		sourceRB.setEnabled(false);
		targetRB.setEnabled(false);
		mixedRB.setEnabled(false);
		noneRB.setEnabled(false);

		ButtonGroup group = new ButtonGroup();
		group.add(noneRB);
		group.add(mixedRB);
		group.add(sourceRB);
		group.add(targetRB);

		sourceRB.addItemListener(this);
		targetRB.addItemListener(this);
		mixedRB.addItemListener(this);
		noneRB.addItemListener(this);

		noneRB.setToolTipText("Color all edges grey");
		mixedRB.setToolTipText("Color edges based on a combination of their source and target nodes");
		sourceRB.setToolTipText("Color edge based on source node color");
		targetRB.setToolTipText("Color edges based on their target node color");

		edgeColorGrid.gridx = 0;
		edgeColorGrid.gridwidth = 1;

		edgeColorPanel.add(noneRB, edgeColorGrid);

		edgeColorGrid.gridx = 1;
		edgeColorGrid.gridwidth = 1;

		edgeColorPanel.add(sourceRB, edgeColorGrid);

		edgeColorGrid.gridx = 2;
		edgeColorGrid.gridwidth = 1;

		edgeColorPanel.add(targetRB, edgeColorGrid);

		edgeColorGrid.gridx = 3;
		edgeColorGrid.gridwidth = 1;

		edgeColorPanel.add(mixedRB, edgeColorGrid);

		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridy++;

		viewOptionsPanel.add(edgeColorPanel, viewOptionsGrid);

	}

	private void addEdgeType(JPanel viewOptionsPanel,
			GridBagConstraints viewOptionsGrid) {

		JPanel edgeStylePanel = new JPanel();
		edgeStylePanel.setLayout(new GridBagLayout());
		GridBagConstraints edgeStyleGrid = new GridBagConstraints();

		edgeStyleGrid.fill = GridBagConstraints.BOTH;
		edgeStyleGrid.weightx = 1.0;
		edgeStyleGrid.gridy = 0;
		edgeStyleGrid.insets = new Insets(0, 0, 0, 0);

		TitledBorder border = BorderFactory.createTitledBorder(raisedetched,
				"Pick an edge style:");
		edgeStylePanel.setBorder(border);

		straightRB = new JRadioButton("Straight");
		curvedRB = new JRadioButton("Curved");
		hideRB = new JRadioButton("Hide");
		bundledRB = new JRadioButton("Bundle");
		fannedRB = new JRadioButton("Fan");

		straightRB.setSelected(edgeModel.straightEdges());
		curvedRB.setSelected(edgeModel.bezierEdges());
		bundledRB.setSelected(edgeModel.bundledEdges());
		fannedRB.setSelected(edgeModel.fannedEdges());
		hideRB.setSelected(!spModel.showGraph());

		straightRB.setEnabled(false);
		curvedRB.setEnabled(false);
		hideRB.setEnabled(false);
		bundledRB.setEnabled(false);
		fannedRB.setEnabled(false);

		ButtonGroup group = new ButtonGroup();
		group.add(straightRB);
		group.add(curvedRB);
		group.add(hideRB);
		group.add(bundledRB);
		group.add(fannedRB);

		straightRB.addItemListener(this);
		curvedRB.addItemListener(this);
		hideRB.addItemListener(this);
		bundledRB.addItemListener(this);
		fannedRB.addItemListener(this);

		straightRB.setToolTipText("Use straight edges");
		curvedRB.setToolTipText("Use curved edges");
		hideRB.setToolTipText("Hide all edges");
		bundledRB.setToolTipText("Bundle edges from centroids");
		fannedRB.setToolTipText("Fan edges");

		edgeStyleGrid.gridx = 0;
		edgeStyleGrid.gridwidth = 1;

		edgeStylePanel.add(straightRB, edgeStyleGrid);

		edgeStyleGrid.gridx = 1;
		edgeStyleGrid.gridwidth = 1;

		edgeStylePanel.add(curvedRB, edgeStyleGrid);

		edgeStyleGrid.gridx = 2;
		edgeStyleGrid.gridwidth = 1;

		edgeStylePanel.add(bundledRB, edgeStyleGrid);

		edgeStyleGrid.gridx = 3;
		edgeStyleGrid.gridwidth = 1;

		edgeStylePanel.add(fannedRB, edgeStyleGrid);

		edgeStyleGrid.gridx = 4;
		edgeStyleGrid.gridwidth = 1;

		edgeStylePanel.add(hideRB, edgeStyleGrid);

		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridy++;

		viewOptionsPanel.add(edgeStylePanel, viewOptionsGrid);

	}

	private void chooseVisibleEdges(JPanel panel, GridBagConstraints grid) {

		JPanel edgeDirectionPanel = new JPanel();
		edgeDirectionPanel.setLayout(new GridBagLayout());
		GridBagConstraints edgeDirectionGrid = new GridBagConstraints();

		edgeDirectionGrid.fill = GridBagConstraints.BOTH;
		edgeDirectionGrid.weightx = 1.0;
		edgeDirectionGrid.gridy = 0;
		edgeDirectionGrid.insets = new Insets(0, 0, 0, 0);

		TitledBorder border = BorderFactory.createTitledBorder(raisedetched,
				"Set which edges are visible:");
		edgeDirectionPanel.setBorder(border);

		incomingCB = new JCheckBox("Incoming");
		outgoingCB = new JCheckBox("Outgoing");

		incomingCB.setSelected(edgeModel.incomingEdges());
		outgoingCB.setSelected(edgeModel.outgoingEdges());

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

		panel.add(edgeDirectionPanel, grid);

	}

	private void addSliders(JPanel viewOptionsPanel,
			GridBagConstraints viewOptionsGrid) {

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridBagLayout());
		GridBagConstraints sliderGrid = new GridBagConstraints();

		sliderGrid.fill = GridBagConstraints.BOTH;
		sliderGrid.weightx = 1.0;
		sliderGrid.gridy = 0;
		sliderGrid.insets = new Insets(0, 0, 0, 0);

		TitledBorder border = BorderFactory.createTitledBorder(raisedetched,
				"Select transparency and node size:");
		sliderPanel.setBorder(border);

		JLabel transparencyLabel = new JLabel("Transparency: ", JLabel.LEFT);

		sliderGrid.gridy = 0;
		sliderGrid.gridx = 0;
		sliderGrid.gridwidth = 1;

		sliderPanel.add(transparencyLabel, sliderGrid);

		transparencySlider = new JSlider(0, 255, spModel.getTransparencyLevel());
		transparencySlider.setEnabled(false);
		transparencySlider.setValue((int) (spModel.getTransparencyLevel()));
		transparencySlider.addChangeListener(this);
		transparencySlider
				.setToolTipText("Change the amount of transparency for the non-highlighted point and edges");

		sliderGrid.gridx = 1;
		sliderGrid.gridwidth = 2;
		sliderPanel.add(transparencySlider, sliderGrid);

		addGraphSizeSelector(sliderPanel, sliderGrid);

		viewOptionsPanel.add(sliderPanel, viewOptionsGrid);

	}

	private void addGraphSizeSelector(JPanel panel, GridBagConstraints grid) {

		String[] degreeOptions = { "None", "Degree", "In Degree", "Out Degree" };
		graphSizeCombo = new JComboBox(degreeOptions);
		graphSizeCombo.setEnabled(false);

		graphSizeCombo.setMinimumSize(min);
		graphSizeCombo.setSelectedIndex(0);
		graphSizeCombo.addActionListener(this);
		graphSizeCombo
				.setToolTipText("Choose which graph metric is used to determine the size of each point");

		JLabel sizeAttributeSelectorLabel = new JLabel("Size points by: ",
				JLabel.RIGHT);

		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;

		panel.add(sizeAttributeSelectorLabel, grid);

		grid.gridx = 1;
		grid.gridwidth = 2;

		panel.add(graphSizeCombo, grid);
	}

	private void addLabelsPanel(JPanel viewOptionsPanel,
			GridBagConstraints viewOptionsGrid) {

		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new GridBagLayout());
		GridBagConstraints labelGrid = new GridBagConstraints();

		labelGrid.fill = GridBagConstraints.BOTH;
		labelGrid.weightx = 1.0;
		labelGrid.gridy = 0;
		labelGrid.insets = new Insets(0, 0, 0, 0);

		TitledBorder border = BorderFactory.createTitledBorder(raisedetched,
				"Show the node labels:");
		labelPanel.setBorder(border);

		showLabelsCheckBox = new JCheckBox("Show Labels");
		showHighlightedLabels = new JCheckBox("Highlighted Only");
		showHoverLabels = new JCheckBox("Hover");
		showSelectedLabels = new JCheckBox("Selected");
		nodeColorCB = new JCheckBox("Node Color");

		showLabelsCheckBox.setSelected(pointModel.labels());
		showHighlightedLabels.setSelected(pointModel.highlightedLabels());
		showHoverLabels.setSelected(pointModel.hoverLabels());
		showSelectedLabels.setSelected(pointModel.selectedLabels());
		nodeColorCB.setSelected(pointModel.nodeLabelColor());

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
		showHighlightedLabels
				.setToolTipText("Only show labels of those nodes currently in focus");
		nodeColorCB
				.setToolTipText("Color labels the same as their node's colour");

		labelGrid.gridx = 0;
		labelPanel.add(showLabelsCheckBox, labelGrid);

		labelGrid.gridx = 1;
		labelPanel.add(showHighlightedLabels, labelGrid);

		labelGrid.gridx = 2;
		labelPanel.add(showHoverLabels, labelGrid);

		labelGrid.gridy++;
		labelGrid.gridx = 0;

		labelPanel.add(nodeColorCB, labelGrid);

		labelGrid.gridx = 1;
		labelPanel.add(showSelectedLabels, labelGrid);

		JLabel labelSizeLabel = new JLabel("Label Size: ", JLabel.RIGHT);

		labelGrid.gridx = 2;

		labelPanel.add(labelSizeLabel, labelGrid);

		labelSlider = new JSlider(1, 100, (int) (pointModel.getLabelSize() * 100));
		labelSlider.setValue((int) (pointModel.getLabelSize() * 100));
		labelSlider.addChangeListener(this);
		labelSlider.setToolTipText("Change the size of the labels");
		labelSlider.setEnabled(false);

		labelGrid.gridx = 3;
		labelGrid.gridwidth = GridBagConstraints.REMAINDER;
		labelPanel.add(labelSlider, labelGrid);

		viewOptionsPanel.add(labelPanel, viewOptionsGrid);
	}

	private void addEdgeFilterPanel(JPanel viewOptionsPanel,
			GridBagConstraints viewOptionsGrid) {

		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new GridBagLayout());
		GridBagConstraints filterGrid = new GridBagConstraints();

		filterGrid.fill = GridBagConstraints.BOTH;
		filterGrid.weightx = 1.0;
		filterGrid.gridy = 0;
		filterGrid.insets = new Insets(0, 0, 0, 0);

		TitledBorder border = BorderFactory.createTitledBorder(raisedetched,
				"Edge weight filter");
		filterPanel.setBorder(border);

		// JLabel edgeWeightFilterLabel = new
		// JLabel("Filter edges according to their weight");
		edgeWeightFilterCB = new JCheckBox(
				"Filter edges according to their weight");
		edgeWeightFilterCB.setEnabled(false);
		edgeWeightFilterCB.setSelected(edgeModel.filterEdgesByWeight());
		edgeWeightFilterCB.addActionListener(this);

		JLabel rangeSliderLowerLabel = new JLabel("Lower value");
		final JLabel rangeSliderLowerValue = new JLabel();
		JLabel rangeSliderUpperLabel = new JLabel("Upper value");
		final JLabel rangeSliderUpperValue = new JLabel();

		RangeSlider rangeSlider = new RangeSlider();

		if (spModel.showGraph()) {
			rangeSlider
					.setMinimum((int) Math.floor(edgeModel.getMinEdgeWeight()));
			rangeSlider.setMaximum((int) Math.ceil(edgeModel.getMaxEdgeWeight()));

			rangeSlider.setValue((int) Math.floor(edgeModel.getMinEdgeWeight()));
			rangeSlider.setUpperValue((int) Math.ceil(edgeModel
					.getMaxEdgeWeight()));
		} else {
			rangeSlider.setMinimum(0);
			rangeSlider.setMaximum(0);

			rangeSlider.setValue(0);
			rangeSlider.setUpperValue(0);
		}

		// Initialize value display.
		rangeSliderLowerValue.setText(String.valueOf(rangeSlider.getValue()));
		rangeSliderUpperValue.setText(String.valueOf(rangeSlider
				.getUpperValue()));

		rangeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				RangeSlider slider = (RangeSlider) e.getSource();
				rangeSliderLowerValue.setText(String.valueOf(slider.getValue()));
				rangeSliderUpperValue.setText(String.valueOf(slider
						.getUpperValue()));
				edgeModel.setLowerEdgeWeightRange(slider.getValue());
				edgeModel.setUpperEdgeWeightRange(slider.getUpperValue());
			}
		});

		filterGrid.gridy = 0;
		filterGrid.gridx = 0;
		filterPanel.add(edgeWeightFilterCB, filterGrid);

		filterGrid.gridy++;
		filterGrid.gridx = 0;
		filterPanel.add(rangeSliderLowerLabel, filterGrid);

		filterGrid.gridx++;
		filterPanel.add(rangeSliderLowerValue, filterGrid);

		filterGrid.gridx = 0;
		filterGrid.gridy++;
		filterPanel.add(rangeSliderUpperLabel, filterGrid);

		filterGrid.gridx++;
		filterPanel.add(rangeSliderUpperValue, filterGrid);

		filterGrid.gridy++;
		filterGrid.gridx = 0;
		filterPanel.add(rangeSlider);

		viewOptionsPanel.add(filterPanel, viewOptionsGrid);

	}

	private void enableGraphButtons() {
		directedCheckBox.setEnabled(true);
		filterEdgeCheckBox.setEnabled(true);
		edgeWeightsCheckBox.setEnabled(true);
		noneRB.setEnabled(true);
		sourceRB.setEnabled(true);
		targetRB.setEnabled(true);
		mixedRB.setEnabled(true);
		straightRB.setEnabled(true);
		curvedRB.setEnabled(true);
		hideRB.setEnabled(true);
		bundledRB.setEnabled(true);
		fannedRB.setEnabled(true);
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
		edgeWeightFilterCB.setEnabled(true);

	}

	private void selectGraphImportProperties() {

		String[] stringAttributes = new String[spModel.getStringAttributes()
				.size()];

		Vector<Attribute> vStringAttributes = spModel.getStringAttributes();
		for (int i = 0; i < vStringAttributes.size(); i++) {
			stringAttributes[i] = vStringAttributes.get(i).name();
		}

		GraphImportGUI graphImportGUI = new GraphImportGUI(this,
				stringAttributes);
		enableGraphButtons();

	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == loadGraphButton)
			try {
				selectGraphImportProperties();
			} catch (Exception e) {
				e.printStackTrace();
			}

		if (event.getSource() == filterEdgeCheckBox) {
			edgeModel.setFilterAllEdges(filterEdgeCheckBox.isSelected());
		}

		if (event.getSource() == directedCheckBox) {
			edgeModel.setArrowedEdges(directedCheckBox.isSelected());
		}

		if (event.getSource() == edgeWeightsCheckBox) {
			edgeModel.setViewEdgeWeights(edgeWeightsCheckBox.isSelected());
		}

		if (event.getSource() == incomingCB) {
			edgeModel.showIncomingEdges(incomingCB.isSelected());
		}

		if (event.getSource() == outgoingCB) {
			edgeModel.showOutgoingEdges(outgoingCB.isSelected());
		}

		if (event.getSource() == graphSizeCombo) {
			// sizeCombo.setSelectedIndex(0);
			pointModel.setSizeAttribute(null);
			graphModel.setGraphSizeAttribute(graph,
					graphSizeCombo.getSelectedIndex());
		}

		if (event.getSource() == showLabelsCheckBox) {
			pointModel.showLabels(showLabelsCheckBox.isSelected());
		}

		if (event.getSource() == showHighlightedLabels) {
			pointModel.showHightlightedLabels(showHighlightedLabels.isSelected());
		}

		if (event.getSource() == showHoverLabels) {
			pointModel.showHoverLabels(showHoverLabels.isSelected());
		}

		if (event.getSource() == showSelectedLabels) {
			pointModel.showSelectedLabels(showSelectedLabels.isSelected());
		}

		if (event.getSource() == nodeColorCB) {
			pointModel.showNodeLabelColor(nodeColorCB.isSelected());
		}

		if (event.getSource() == edgeWeightFilterCB) {
			edgeModel.setFilterEdgesByWeight(edgeWeightFilterCB.isSelected());
		}

	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() == noneRB)
			edgeModel.setDefaultColorEdges(noneRB.isSelected());

		if (event.getSource() == sourceRB)
			edgeModel.setSourceColorEdges(sourceRB.isSelected());

		if (event.getSource() == targetRB)
			edgeModel.setTargetColorEdges(targetRB.isSelected());

		if (event.getSource() == mixedRB)
			edgeModel.setMixedColorEdges(mixedRB.isSelected());

		if (event.getSource() == straightRB)
			edgeModel.setStraightEdges(straightRB.isSelected());

		if (event.getSource() == curvedRB)
			edgeModel.setBezierEdges(curvedRB.isSelected());

		if (event.getSource() == bundledRB)
			edgeModel.setBundledEdges(bundledRB.isSelected());

		if (event.getSource() == fannedRB)
			edgeModel.setFannedEdges(fannedRB.isSelected());

		if (event.getSource() == hideRB)
			// spModel.setShowGraph(!hideRB.isSelected());
			edgeModel.setIntelligentEdges(hideRB.isSelected());
	}

	/** Marker slider state has changed */
	public void stateChanged(ChangeEvent e) {

		if (transparencySlider == (JSlider) e.getSource())
			spModel.setTransparencyLevel(transparencySlider.getValue());

		// if (beizerSlider == (JSlider) e.getSource())
		// spModel.setBeizerCurviness((float)beizerSlider.getValue()/100f);

		if (labelSlider == (JSlider) e.getSource())
			pointModel.setLabelSize(labelSlider.getValue() / 100d);
	}

}
