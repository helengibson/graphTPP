package tpp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;
import weka.gui.explorer.ExplorerDefaults;

@SuppressWarnings("serial")
public class ViewOptionsTab extends JPanel implements ActionListener, ChangeListener {
	
	private ScatterPlotModel spModel;
	private PointModel pointModel;
	
	private AttributeCombo sizeCombo;
	private AttributeCombo fillCombo;
	private AttributeCombo shapeCombo;
	
	private JSlider markerSlider;
	
	private String[] background;
	private String[] classColorSchemes;
	private String[] diveringColorSchemes;
	
	private JComboBox<String> backgroundCB;
	private JComboBox<String> classCB;
	private JComboBox<String> spectrumCB;
	
	private Dimension min = new Dimension(100, 20);

	public ViewOptionsTab(ScatterPlotModel spModel) {
		
		super();
		this.spModel = spModel;
		pointModel = spModel.getPointModel();
		init();
		setVisible(true);
		
	}
	
	private void init() {
		
		setLayout(new GridBagLayout());
		GridBagConstraints viewOptionsGrid = new GridBagConstraints();
		
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.fill = GridBagConstraints.HORIZONTAL;
		viewOptionsGrid.weightx = 1.0;
		viewOptionsGrid.gridy = 0;
		
		viewOptionsGrid.insets = new Insets(2,2,2,2);
		
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				
		addSizeAttributeSelector(this, viewOptionsGrid);
		addFillAttributeSelector(this, viewOptionsGrid);
		addShapeAttributeSelector(this, viewOptionsGrid);
		addMarkerSizeSlider(this, viewOptionsGrid);
		addColourOptions(this, viewOptionsGrid);
		
	}
	
	private void addSizeAttributeSelector(JPanel panel, GridBagConstraints grid) {
		
		// add size attribute selector
		sizeCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NUMERIC_ATTRIBUTES, true);
		sizeCombo.setMinimumSize(min);
		sizeCombo.setSelectedAttribute(pointModel.getSizeAttribute());
		sizeCombo.addActionListener(this);
		sizeCombo.setToolTipText("Choose which attribute is used to determine the size of each point");
		
		JLabel sizeAttributeSelectorLabel = new JLabel("Size points by: ", JLabel.LEFT);
		
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(sizeAttributeSelectorLabel, grid);
		
		grid.gridx = 1;
		grid.gridwidth = 2;
		
		panel.add(sizeCombo, grid);
	}
	
	private void addFillAttributeSelector(JPanel panel, GridBagConstraints grid) {
		// add fill attribute selector
		fillCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NOMINAL_ATTRIBUTES, true);
		fillCombo.setMinimumSize(min);
		fillCombo.setSelectedAttribute(pointModel.getFillAttribute());
		fillCombo.addActionListener(this);
		fillCombo.setToolTipText("Choose which attribute is used to determine whether each point is filled");
		
		JLabel fillAttributeSelectorLabel = new JLabel("Fill points by: ", JLabel.LEFT);
		
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(fillAttributeSelectorLabel, grid);
		
		grid.gridx = 1;
		grid.gridwidth = 2;
		
		panel.add(fillCombo, grid);
	}
	
	private void addShapeAttributeSelector(JPanel panel, GridBagConstraints grid) {
		// add shape attribute selector
		
		shapeCombo = AttributeCombo.buildCombo(spModel, AttributeCombo.NOMINAL_ATTRIBUTES, true);
		shapeCombo.setMinimumSize(min);
		shapeCombo.setSelectedAttribute(pointModel.getShapeAttribute());
		shapeCombo.addActionListener(this);
		shapeCombo.setToolTipText("Choose which attribute is used to determine the shape of each point");
		
		JLabel shapeAttributeSelectorLabel = new JLabel("Shape points by: ", JLabel.LEFT);
		
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(shapeAttributeSelectorLabel, grid);
		
		grid.gridx = 1;
		grid.gridwidth = 2;
		
		panel.add(shapeCombo, grid);
	}
	
	private void addMarkerSizeSlider(JPanel panel, GridBagConstraints grid) {
		// add marker slider
		
		JLabel markerSizeLabel = new JLabel("Marker size: ", JLabel.LEFT);
		
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(markerSizeLabel, grid);
		markerSlider = new JSlider(1, (int) (PointModel.MARKER_DEFAULT * 2000), (int) (PointModel.MARKER_DEFAULT * 1000));
		markerSlider.setValue((int) (spModel.getPointModel().getMarkerSize() * 1000));
		markerSlider.addChangeListener(this);
		markerSlider.setToolTipText("Change the average size of the points");
		
		grid.gridx = 1;
		grid.gridwidth = 2;
		panel.add(markerSlider, grid);
	}
	
	private void addColourOptions(JPanel panel,	GridBagConstraints grid) {
		
		background = new String[]{"Dark", "Light"};
		classColorSchemes = new String[]{"Default", "Custom", "Set2", "Accent", "Set1", "Set3", "Dark2", "Paired", "Pastel2", "Pastel1", "GnBu"};
		diveringColorSchemes = new String[]{"Default", "Spectral", "Red-Yellow-Blue", "Red-Grey", "Red-Blue", "Purple-Orange", "Purple-Green", "Pink-Green", "Brown-Blue"};  
			
		JLabel backgroundLabel = new JLabel("Background: ", JLabel.LEFT);
		JLabel classLabel = new JLabel("Classification: ", JLabel.LEFT);
		JLabel spectrumLabel = new JLabel("Spectrum: ", JLabel.LEFT);
		
		backgroundCB = new JComboBox<String>(background);
		classCB = new JComboBox<String>(classColorSchemes);
		spectrumCB = new JComboBox<String>(diveringColorSchemes);
		
		backgroundCB.setSelectedItem(spModel.getBGColor());
		classCB.setSelectedItem(spModel.getClassColor());
		spectrumCB.setSelectedItem(spModel.getSpectrumColor());
		
		backgroundCB.addActionListener(this);
		classCB.addActionListener(this);
		spectrumCB.addActionListener(this);;
							
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(backgroundLabel, grid);
		grid.gridx++;
		grid.gridwidth = 2;
		panel.add(backgroundCB, grid);
		
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(classLabel, grid);
		grid.gridx++;
		grid.gridwidth = 2;
		panel.add(classCB, grid);
		
		grid.gridy++;
		grid.gridx = 0;
		grid.gridwidth = 1;
		
		panel.add(spectrumLabel, grid);
		grid.gridx++;
		grid.gridwidth = 2;
		panel.add(spectrumCB, grid);
						
	}

	private void applyColorScheme(ScatterPlotModel spModel, String bg) {
		
		if(bg.equals("Dark"))
			spModel.setColours(ColourScheme.createDarkColorScheme(setSpectrumColours((String)spectrumCB.getSelectedItem()), 
					setClassColours((String)classCB.getSelectedItem())));
		
		if(bg.equals("Light"))
			spModel.setColours(ColourScheme.createLightColorScheme(setSpectrumColours((String)spectrumCB.getSelectedItem()), 
					setClassColours((String)classCB.getSelectedItem())));
		
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
			if(s.equals("GnBu")){
				Attribute at = pointModel.getSelectAttribute();
				Enumeration classValues = at.enumerateValues();
				int b = 0;
				while(classValues.hasMoreElements()) {
					b++;
				}
				classColors = ColourScheme.getGnBu(b);
			}
			if(s.equals("Custom"))
				classColors = ColourScheme.getCustom();
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
			if(s.equals("Red-Blue") || s.equals("Default"))
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
			
	@Override
	public void actionPerformed(ActionEvent event) {
		
		if (event.getSource() == sizeCombo)
			pointModel.setSizeAttribute(sizeCombo.getSelectedAttribute());
		
		if (event.getSource() == fillCombo)
			pointModel.setFillAttribute(fillCombo.getSelectedAttribute());

		if (event.getSource() == shapeCombo)
			pointModel.setShapeAttribute(shapeCombo.getSelectedAttribute());
				
		if ((event.getSource() == backgroundCB) || (event.getSource() == spectrumCB) || (event.getSource() == classCB)) {
			applyColorScheme(spModel, (String)backgroundCB.getSelectedItem());
			
			if(event.getSource() == backgroundCB)
				spModel.setBGColor((String)backgroundCB.getSelectedItem());
			
			if(event.getSource() == classCB)
				spModel.setClassColor((String)classCB.getSelectedItem());
			
			if(event.getSource() == spectrumCB)
				spModel.setSpectrumColor((String)spectrumCB.getSelectedItem());
		}
		
		spModel.fireModelChanged(TPPModelEvent.COLOR_SCHEME_CHANGED);
	}


	@Override
	public void stateChanged(ChangeEvent event) {
		
		if (markerSlider == (JSlider) event.getSource())
			spModel.getPointModel().setMarkerSize(markerSlider.getValue() / 1000d);
		
	}

}
