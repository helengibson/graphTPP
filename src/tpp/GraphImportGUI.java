package tpp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class GraphImportGUI extends JFrame implements ActionListener {
	
	private GraphOptionsTab parent;

	private JComboBox<String> nodeSelector;
	private JTextField fileLocation;
	private JButton browseButton;
	private JCheckBox headerCB;
	private JCheckBox weightCB;
	
	private ButtonGroup delimiterGroup;
	private JRadioButton commaRadio;
	private JRadioButton semicolonRadio;
	private JRadioButton spaceRadio;
	private JRadioButton tabRadio;
	private JRadioButton barRadio;
	
	private ButtonGroup directionGroup;
	private JRadioButton undirectedRadio;
	private JRadioButton directedRadio;
	
	private JButton OKButton;
	private JButton cancelButton;

	private Object[] nodeIds;

	private String edgeAttribute = null;

	private String filePath = null;
	
	public GraphImportGUI(GraphOptionsTab graphOptionsTab, String[] nodeIds) {
		super("Import graph from delimited file");
		this.parent = graphOptionsTab;
		this.nodeIds = nodeIds;
		setup();
		initalize();
		this.setVisible(true);
	}

	private void setup() {
		this.setSize(425, 325);
		this.setLocation(200, 200);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}

	private void initalize() {
		// TODO Auto-generated method stub
		JLabel attributeLabel = new JLabel("Choose the attribute that uniquely identifies the node:");
		JLabel fileChooserLabel = new JLabel("Choose the location of the edgelist file:");
		JLabel delimiterLabel = new JLabel("Choose the delimiter used in the file:");
		JLabel directedLabel = new JLabel("Is the graph undirected or directed?");
		
		nodeSelector = new JComboBox(nodeIds);
		fileLocation = new JTextField();
		browseButton  = new JButton("Browse");
		headerCB = new JCheckBox("Ignore the header in the file");
		weightCB = new JCheckBox("Use edge weights");
		
		commaRadio = new JRadioButton("Comma", true);
		semicolonRadio = new JRadioButton("Semi-colon");
		tabRadio = new JRadioButton("Tab");
		spaceRadio = new JRadioButton("Space");
		barRadio = new JRadioButton("Bar");
		
		delimiterGroup = new ButtonGroup();
		delimiterGroup.add(commaRadio);
		delimiterGroup.add(semicolonRadio);
		delimiterGroup.add(tabRadio);
		delimiterGroup.add(spaceRadio);
		delimiterGroup.add(barRadio);
				
		undirectedRadio =  new JRadioButton("undirected", true);
		directedRadio = new JRadioButton("directed");
		
		directionGroup = new ButtonGroup();
		directionGroup.add(undirectedRadio);
		directionGroup.add(directedRadio);
		
		OKButton = new JButton("OK");
		cancelButton = new JButton("cancel");
		
		setLayout(new GridBagLayout());
		GridBagConstraints grid = new GridBagConstraints();
		
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.weightx = 1.0;
		grid.insets = new Insets(2,2,2,2);
		grid.ipady = 3;
		
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(attributeLabel, grid);

		grid.weightx = 0.0;	
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(nodeSelector, grid);
		
		grid.weightx = 0.0;	
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(fileChooserLabel, grid);
		
		grid.weightx = 1.0;
		grid.gridwidth = GridBagConstraints.RELATIVE;
		add(fileLocation, grid);
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(browseButton, grid);
		
		grid.weightx = 0.0;	
		add(headerCB, grid);
		
		grid.weightx = 0.0;	
		add(weightCB, grid);
		
		grid.weightx = 0.0;	
		add(delimiterLabel, grid);
		
		grid.weightx = 1.0;	
		grid.gridwidth = 1;
		add(commaRadio, grid);
		add(semicolonRadio, grid);
		add(tabRadio, grid);
		add(spaceRadio, grid);
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(barRadio, grid);
		
		grid.weightx = 0.0;
		add(directedLabel, grid);
		
		grid.weightx = 1.0;
		grid.gridwidth = 1;
		add(undirectedRadio, grid);
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(directedRadio, grid);
		
		grid.anchor = GridBagConstraints.SOUTHEAST;
		grid.weightx = 1.0;
		grid.gridwidth = 1;
		add(OKButton, grid);
		add(cancelButton, grid);
		
		nodeSelector.setSelectedIndex(0);
		browseButton.addActionListener(this);
		OKButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == browseButton) {
			String filePath = getFile();
			fileLocation.setText(filePath);
		}
		if(e.getSource() == OKButton) {
			
			String edgeAtt = (String)nodeSelector.getSelectedItem();
			parent.spModel.setGraphEdgeAttribute(edgeAtt);
			
			String filePath = fileLocation.getText();
			String delimiter = getDelimiter();
			boolean header = headerCB.isSelected();
			boolean weight = weightCB.isSelected();
			
			try {
				parent.spModel.loadGraph(new GraphImporter().importGraph(
						parent.spModel.getInstances(), 
						parent.spModel.getEdgeAttributeIndex(), filePath, delimiter, header, weight));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			this.dispose();
		}
		if(e.getSource() == cancelButton) {
			this.dispose();
			
		}
		
	}

	private String getDelimiter() {
		if(commaRadio.isSelected())
			return ",";
		else if(semicolonRadio.isSelected())
			return ";";
		else if(tabRadio.isSelected())
			return "\t";
		else if(spaceRadio.isSelected())
			return " ";
		else if(barRadio.isSelected())
			return "|";
		else
			return ",";
	}


	private String getFile() {
		
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		//chooser.setFileFilter(FILE_FILTER);
		
		int returnVal = chooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile().getPath();
		else
			return null;
		
	}
	 

}
