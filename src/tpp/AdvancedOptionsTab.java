/* 
Copyright (C) 2015  Helen Gibson
*/
package tpp;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;
import weka.gui.explorer.ExplorerDefaults;

public class AdvancedOptionsTab extends JPanel implements ActionListener {
	
	private ScatterPlotModel spModel;
	private PointModel pointModel;
	
	private AttributeCombo seriesIndexCombo;
	private AttributeCombo seriesIdCombo;
	private JButton createSeriesButton;
	private SmoothButton smoothSeriesButton;
	private JButton removeSeriesButton;
	
	private JButton createTestSetButton;
	private JComboBox<Integer> createTestSetKCombo;
	private JButton removeTestSetButton;
	
	private GenericObjectEditor classifierChooser;
	private PropertyPanel classifierChooserPanel;
	private AbstractButton applyClassifierButton;
	private AttributeCombo classificationTargetCombo;
	
	private Dimension min = new Dimension(100, 20);
	
	private Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
	
	public AdvancedOptionsTab(ScatterPlotModel spModel, PointModel pointModel) {
		super();
		this.spModel = spModel;
		this.pointModel = pointModel;
		init();
		setVisible(true);
	}
	
	private void init() {
		
		setLayout(new GridBagLayout());
		GridBagConstraints advancedOptionsGrid = new GridBagConstraints();
		
		advancedOptionsGrid.gridx = 0;
		advancedOptionsGrid.fill = GridBagConstraints.HORIZONTAL;
		advancedOptionsGrid.weightx = 1.0;
		advancedOptionsGrid.gridy = 0;
		
		advancedOptionsGrid.insets = new Insets(2,2,2,2);
		
		TitledBorder border = BorderFactory.createTitledBorder(raisedetched, "Advanced Options");
		setBorder(border);
		
		addClassificationButton(this, advancedOptionsGrid);
		addTestSetCreationButton(this, advancedOptionsGrid);
		addSeriesCreationButton(this, advancedOptionsGrid);		
	}

	private void addClassificationButton(JPanel panel, GridBagConstraints grid) {
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
			
			grid.anchor = GridBagConstraints.NORTH;
			grid.weighty = 1.0;
						
			panel.add(applyClassifierButton, grid);
			
			grid.gridx = 1;
			
			panel.add(classifierChooserPanel, grid);
			
			grid.gridx = 2;
			
			panel.add(classificationTargetCombo, grid);
		}
	}
				
	private void addTestSetCreationButton(JPanel panel, GridBagConstraints grid) {
		// Add test set creation button
		Vector<Integer> testSetKValues = new Vector<Integer>();
		
		for (int k = 2; k < 11; k++)
			testSetKValues.add(new Integer(k));
		
		createTestSetKCombo = new JComboBox<Integer>(testSetKValues);
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
		
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(createTestSetKCombo, grid);
		grid.gridx = 1;
		
		panel.add(createTestSetButton, grid);
		
		grid.gridx = 2;
		
		panel.add(removeTestSetButton, grid);
	}

	private void addSeriesCreationButton(JPanel panel, GridBagConstraints grid) {
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
			
			grid.gridy++;
			grid.gridx = 0;
			grid.gridwidth = 1;
			
			panel.add(createSeriesButton, grid);
			
			grid.gridx = 1;
			
			panel.add(seriesIdCombo, grid);
			
			grid.gridx = 2;
			
			grid.gridwidth = GridBagConstraints.REMAINDER;
			
			panel.add(seriesIndexCombo, grid);
		}

		// if there are series currently defined then add a button for
		// denoising and removing them
		if (spModel.getSeries() != null) {
			smoothSeriesButton = new SmoothButton(spModel);
			smoothSeriesButton.setText("Smooth Series");
			smoothSeriesButton.setToolTipText("<html><p width=\"300px\">Try to find" +
					" a view of the data that removes low frequency noise and shows longer-term " +
					"evolution of the system</p></html>");
			
			grid.gridy++;
			grid.gridx = 0;
			grid.gridwidth = 1;
			
			panel.add(smoothSeriesButton, grid);

			removeSeriesButton = new JButton("Remove Series");
			removeSeriesButton.addActionListener(this);
			removeSeriesButton.setToolTipText("No longer show the series lines");
			
			grid.gridx = 1;
			grid.gridwidth = 1;
			
			panel.add(removeSeriesButton, grid);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		if (event.getSource() == createSeriesButton) 
			spModel.createSeries(seriesIndexCombo.getSelectedAttribute(),seriesIdCombo.getSelectedAttribute());

		if (event.getSource() == removeSeriesButton) 
			spModel.removeSeries();
		
		if (event.getSource() == createTestSetButton)
			spModel.createTestSet(((Integer) createTestSetKCombo
					.getSelectedItem()).intValue());

		if (event.getSource() == removeTestSetButton)
			spModel.removeTestSet();
		
		if (event.getSource() == applyClassifierButton) {
			try {

				Attribute[] classification = spModel.createCrossValidation(
						classificationTargetCombo.getSelectedAttribute(),
						(Classifier) classifierChooser.getValue());
				// and fill the points by the error
				spModel.setColourAttribute(classification[0]);
				pointModel.setSelectAttribute(classification[0]);
				pointModel.setFillAttribute(classification[1]);
				init();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

}
