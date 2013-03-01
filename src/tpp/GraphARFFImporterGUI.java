package tpp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class GraphARFFImporterGUI extends JFrame implements ActionListener{
	
	private TPPFrame parent;
	
	private static final ARFFFileFilter ARFF_FILE_FILTER = new ARFFFileFilter();
	
	private JButton browseButton;
	private JTextField fileLocation;
	private JButton cancelButton;
	private JButton OKButton;

	private JRadioButton edgesProjectionRadio;

	private JRadioButton noEdgesProjectionRadio;
	
	public GraphARFFImporterGUI(TPPFrame tppFrame) {
		super("Import ARFF file with graph component.");
		this.parent = tppFrame;
		setup();
		initialize();
		this .setVisible(true);
	}

	private void setup() {
		this.setSize(300, 175);
		this.setLocation(200, 200);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}

	private void initialize() {
		JLabel fileChooserLabel = new JLabel("Choose the location of the GraphARFF file");
		JLabel edgeProjectionLabel = new JLabel("Include edges in the projection?");
		
		fileLocation = new JTextField();
		browseButton  = new JButton("Browse");
		
		edgesProjectionRadio = new JRadioButton("Yes", true);
		noEdgesProjectionRadio = new JRadioButton("No");
		
		ButtonGroup edgesProjectionGroup = new ButtonGroup();
		edgesProjectionGroup.add(edgesProjectionRadio);
		edgesProjectionGroup.add(noEdgesProjectionRadio);
		
		OKButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		
		setLayout(new GridBagLayout());
		GridBagConstraints grid = new GridBagConstraints();
		
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.weightx = 1.0;
		grid.insets = new Insets(2,2,2,2);
		grid.ipady = 3;
		
		grid.weightx = 0.0;	
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(fileChooserLabel, grid);
		
		grid.weightx = 1.0;
		grid.gridwidth = 2;
		add(fileLocation, grid);
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(browseButton, grid);
		
		grid.weightx = 0.0;
		add(edgeProjectionLabel, grid);
		
		grid.weightx = 1.0;
		grid.gridwidth = 1;
		add(edgesProjectionRadio, grid);
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(noEdgesProjectionRadio, grid);
		
		grid.anchor = GridBagConstraints.SOUTHEAST;
		grid.weightx = 1.0;
		grid.gridwidth = 1;
		add(OKButton, grid);
		add(cancelButton, grid);
		
		browseButton.addActionListener(this);
		OKButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
	}
	
	private String getFile() {
		
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setFileFilter(ARFF_FILE_FILTER);
		
		int returnVal = chooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile().getPath();
		else
			return null;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
			if(e.getSource() == browseButton) {
				String filePath = getFile();
				fileLocation.setText(filePath);
			}
			if(e.getSource() == OKButton) {
				
				String filePath = fileLocation.getText();
				
				parent.setData(new GraphARFFImporter().importProjectionData(filePath, edgesProjectionRadio.isSelected()));
			((ScatterPlotModel) parent.getModel())
					.loadGraph(new GraphImporter().importGraph(filePath, parent
							.getModel().getInstances()));
				
				this.dispose();
			}
			if(e.getSource() == cancelButton) {
				this.dispose();
				
			}
			
		}
		
	}
