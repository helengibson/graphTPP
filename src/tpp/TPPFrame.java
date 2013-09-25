package tpp;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import weka.core.Instances;

/**
 * A Frame that allows the user to manipulate views of a (categorised) data set
 * loaded from a data file.
 */
public class TPPFrame extends JFrame implements ActionListener {

	private static final String FRAME_TITLE = "Targeted Projection Pursuit";

	// TODO the default dimension should be determined by the size of the demo
	// slides
	private static final Dimension DEFAULT_DIMENSION = new Dimension(1024, 700);

	static final int NO_CLASSIFICATION = -1;

	private ScatterPlotModel model;

	private JMenuBar bar = null;

	private JMenu viewMenu = null;
	private JMenuItem rescaleMenuItem = null;
	private JMenu fileMenu = null;

	private JMenuItem openARFFFileMenuItem = null;
	private JMenuItem openGraphARFFFileMenuItem = null;
	private JMenuItem openCSVFileMenuItem = null;
	private JMenuItem openALNFileMenuItem = null;
	private JMenuItem openFASTAFileMenuItem = null;
	private JMenuItem saveNormalisedViewMenuItem = null;
	private JMenuItem saveProjectionMenuItem = null;
	private JMenuItem saveViewDataMenuItem = null;
	private JMenuItem saveSVGMenuItem, saveEPSMenuItem, savePNGMenuItem;

	private JMenuItem pcaViewMenuItem = null;
	private JMenuItem randomViewMenuItem = null;
	private JRadioButtonMenuItem showAxesMenuItem = null;
	private JRadioButtonMenuItem showHierarchicalClusteringMenuItem = null;
	private JRadioButtonMenuItem showTargetMenuItem;
	private JMenuItem addNoiseMenuItem;
	private JMenuItem showDataViewerMenuItem;
	private DataViewer dataViewer;

	private JMenu DBItem;
	private JMenuItem DBConnectionItem;
	private JMenuItem DBSelectionItem;

	private JMenuItem helpMenuItem;

	ScatterPlotViewPanel viewPanel;
	ScatterPlotControlPanel controlPanel = null;

	public TPPFrame() {
		super(FRAME_TITLE);
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			System.out.println(e);
		}
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setJMenuBar(getBar());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(
				(int) (screenSize.getWidth() - DEFAULT_DIMENSION.getWidth()) / 2,
				(int) (screenSize.getHeight() - DEFAULT_DIMENSION.getHeight()) / 2);
		this.setSize(DEFAULT_DIMENSION);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * This method initializes bar
	 * 
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getBar() {
		if (bar == null) {
			bar = new JMenuBar();
			bar.add(getFileMenu());
			bar.add(getViewMenu());
			bar.add(getHelpMenuItem());
		}
		return bar;
	}

	private JMenuItem getHelpMenuItem() {
		if (helpMenuItem == null) {
			helpMenuItem = new JMenuItem();
			helpMenuItem.setText("Help");
			helpMenuItem.addActionListener(this);
		}
		return helpMenuItem;

	}

	/**
	 * This method initializes fileMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("  File  ");
			fileMenu.add(getOpenARFFFileMenuItem());
			fileMenu.add(getOpenGraphARFFFileMenuItem());
			fileMenu.add(getOpenCSVFileMenuItem());
			fileMenu.add(getOpenFASTAFileMenuItem());
			fileMenu.add(getOpenALNFileMenuItem());
			fileMenu.add(getSaveNormalisedViewMenuItem());
			fileMenu.add(getSaveProjectionMenuItem());
			fileMenu.add(getSaveViewDataMenuItem());
			fileMenu.add(getSaveSVGMenuItem());
			fileMenu.add(getSaveEPSMenuItem());
			fileMenu.add(getSavePNGMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenARFFFileMenuItem() {
		if (openARFFFileMenuItem == null) {
			openARFFFileMenuItem = new JMenuItem();
			openARFFFileMenuItem.setText("Load data from ARFF file");
			openARFFFileMenuItem.addActionListener(this);
		}
		return openARFFFileMenuItem;
	}

	private JMenuItem getOpenGraphARFFFileMenuItem() {
		if (openGraphARFFFileMenuItem == null) {
			openGraphARFFFileMenuItem = new JMenuItem();
			openGraphARFFFileMenuItem.setText("Load data from GraphARFF file");
			openGraphARFFFileMenuItem.addActionListener(this);
		}
		return openGraphARFFFileMenuItem;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenCSVFileMenuItem() {
		if (openCSVFileMenuItem == null) {
			openCSVFileMenuItem = new JMenuItem();
			openCSVFileMenuItem.setText("Load data from CSV file");
			openCSVFileMenuItem.addActionListener(this);
		}
		return openCSVFileMenuItem;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenFASTAFileMenuItem() {
		if (openFASTAFileMenuItem == null) {
			openFASTAFileMenuItem = new JMenuItem();
			openFASTAFileMenuItem.setText("Load data from FASTA file");
			openFASTAFileMenuItem.addActionListener(this);
		}
		return openFASTAFileMenuItem;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenALNFileMenuItem() {
		if (openALNFileMenuItem == null) {
			openALNFileMenuItem = new JMenuItem();
			openALNFileMenuItem.setText("Load data from CLUSTAL (.aln) file");
			openALNFileMenuItem.addActionListener(this);
		}
		return openALNFileMenuItem;
	}

	/**
	 * This method initializes saveNormalisedViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveNormalisedViewMenuItem() {
		if (saveNormalisedViewMenuItem == null) {
			saveNormalisedViewMenuItem = new JMenuItem();
			saveNormalisedViewMenuItem.setText("Save normalised data");
			saveNormalisedViewMenuItem.setEnabled(false);
			saveNormalisedViewMenuItem.addActionListener(this);
		}
		return saveNormalisedViewMenuItem;
	}

	/**
	 * This method initializes saveProjectionMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveProjectionMenuItem() {
		if (saveProjectionMenuItem == null) {
			saveProjectionMenuItem = new JMenuItem();
			saveProjectionMenuItem.setText("Save current projection");
			saveProjectionMenuItem.setEnabled(false);
			saveProjectionMenuItem.addActionListener(this);
		}
		return saveProjectionMenuItem;
	}

	/**
	 * This method initializes saveViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveViewDataMenuItem() {
		if (saveViewDataMenuItem == null) {
			saveViewDataMenuItem = new JMenuItem();
			saveViewDataMenuItem.setText("Save current view data");
			saveViewDataMenuItem.setEnabled(false);
			saveViewDataMenuItem.addActionListener(this);
		}
		return saveViewDataMenuItem;
	}

	/**
	 * This method initializes saveViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveSVGMenuItem() {
		if (saveSVGMenuItem == null) {
			saveSVGMenuItem = new JMenuItem();
			saveSVGMenuItem.setText("Save current view as SVG image");
			saveSVGMenuItem.setEnabled(false);
			saveSVGMenuItem.addActionListener(this);
		}
		return saveSVGMenuItem;
	}

	/**
	 * This method initializes saveViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveEPSMenuItem() {
		if (saveEPSMenuItem == null) {
			saveEPSMenuItem = new JMenuItem();
			saveEPSMenuItem.setText("Save current view as EPS image");
			saveEPSMenuItem.setEnabled(false);
			saveEPSMenuItem.addActionListener(this);
		}
		return saveEPSMenuItem;
	}

	/**
	 * This method initializes savePNGMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSavePNGMenuItem() {
		if (savePNGMenuItem == null) {
			savePNGMenuItem = new JMenuItem();
			savePNGMenuItem.setText("Save current view as PNG image");
			savePNGMenuItem.setEnabled(false);
			savePNGMenuItem.addActionListener(this);
		}
		return savePNGMenuItem;
	}

	private JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu();
			viewMenu.setText("View");
			viewMenu.add(getShowDataViewerMenuItem());
			viewMenu.add(getRescaleMenuItem());
			viewMenu.add(getPCAMenuItem());
			viewMenu.add(getRandomMenuItem());
			viewMenu.add(getShowAxesMenuItem());
			viewMenu.add(getShowHierarchicalClusteringMenuItem());
			viewMenu.add(getAddNoiseMenuItem());
			viewMenu.add(getShowTargetMenuItem());
			viewMenu.add(getDBMenuItems());
		}
		return viewMenu;
	}

	private JMenuItem getShowHierarchicalClusteringMenuItem() {
		if (showHierarchicalClusteringMenuItem == null) {
			showHierarchicalClusteringMenuItem = new JRadioButtonMenuItem();
			showHierarchicalClusteringMenuItem
					.setText("Show HierarchicalClustering");
			showHierarchicalClusteringMenuItem.setEnabled(false);
			showHierarchicalClusteringMenuItem.setSelected(false);
			showHierarchicalClusteringMenuItem.addActionListener(this);
		}
		return showHierarchicalClusteringMenuItem;
	}

	private JMenuItem getShowAxesMenuItem() {
		if (showAxesMenuItem == null) {
			showAxesMenuItem = new JRadioButtonMenuItem();
			showAxesMenuItem.setText("Show Axes");
			showAxesMenuItem.setEnabled(false);
			showAxesMenuItem.setSelected(false);
			showAxesMenuItem.addActionListener(this);
		}
		return showAxesMenuItem;
	}

	private JMenuItem getShowDataViewerMenuItem() {
		if (showDataViewerMenuItem == null) {
			showDataViewerMenuItem = new JMenuItem();
			showDataViewerMenuItem.setText("Show Data Viewer");
			showDataViewerMenuItem.setEnabled(false);
			showDataViewerMenuItem.addActionListener(this);
		}
		return showDataViewerMenuItem;
	}

	private JMenuItem getShowTargetMenuItem() {
		if (showTargetMenuItem == null) {
			showTargetMenuItem = new JRadioButtonMenuItem();
			showTargetMenuItem.setText("Show Target");
			showTargetMenuItem.setEnabled(false);
			showTargetMenuItem.setSelected(false);
			showTargetMenuItem.addActionListener(this);
		}
		return showTargetMenuItem;
	}

	private JMenuItem getAddNoiseMenuItem() {
		if (addNoiseMenuItem == null) {
			addNoiseMenuItem = new JRadioButtonMenuItem();
			addNoiseMenuItem.setText("Add noise to view");
			addNoiseMenuItem.setEnabled(false);
			addNoiseMenuItem.setSelected(false);
			addNoiseMenuItem.addActionListener(this);
		}
		return addNoiseMenuItem;
	}

	private JMenuItem getPCAMenuItem() {
		if (pcaViewMenuItem == null) {
			pcaViewMenuItem = new JMenuItem();
			pcaViewMenuItem.setText("Principal Components View (X=PC1,Y=PC2)");
			pcaViewMenuItem.setEnabled(false);
			pcaViewMenuItem.addActionListener(this);
		}
		return pcaViewMenuItem;
	}

	private JMenuItem getRandomMenuItem() {
		if (randomViewMenuItem == null) {
			randomViewMenuItem = new JMenuItem();
			randomViewMenuItem.setText("Randomise View");
			randomViewMenuItem.setEnabled(false);
			randomViewMenuItem.addActionListener(this);
		}
		return randomViewMenuItem;
	}

	private JMenuItem getRescaleMenuItem() {
		if (rescaleMenuItem == null) {
			rescaleMenuItem = new JMenuItem();
			rescaleMenuItem
					.setText("Fit points to window (Shift + right button)");
			rescaleMenuItem.setEnabled(false);
			rescaleMenuItem.addActionListener(this);
		}
		return rescaleMenuItem;
	}

	private JMenu getDBMenuItems() {
		if (DBItem == null) {
			DBItem = new JMenu();
			DBItem.setText("Database Actions");
			DBItem.setEnabled(false);

			DBConnectionItem = new JMenuItem();
			DBConnectionItem.setText("Database connection");
			DBConnectionItem.addActionListener(this);

			DBSelectionItem = new JMenuItem();
			DBSelectionItem.setText("View in database");
			DBSelectionItem.addActionListener(this);

			DBItem.add(DBConnectionItem);
			DBItem.add(DBSelectionItem);
		}
		return DBItem;
	}

	/** Enable those menu items that rely on a data file being currently loaded. */
	private void enableViewMenuItems() {
		getSaveNormalisedViewMenuItem().setEnabled(true);
		getSaveProjectionMenuItem().setEnabled(true);
		getRescaleMenuItem().setEnabled(true);
		getPCAMenuItem().setEnabled(true);
		getSaveViewDataMenuItem().setEnabled(true);
		getSaveSVGMenuItem().setEnabled(true);
		getSaveEPSMenuItem().setEnabled(true);
		getSavePNGMenuItem().setEnabled(true);
		getRandomMenuItem().setEnabled(true);
		getShowAxesMenuItem().setEnabled(true);
		getShowHierarchicalClusteringMenuItem().setEnabled(true);
		getShowAxesMenuItem().setSelected(false);
		getShowAxesMenuItem().setEnabled(true);
		getShowTargetMenuItem().setEnabled(true);
		getShowTargetMenuItem().setSelected(false);
		getShowDataViewerMenuItem().setEnabled(true);
		getAddNoiseMenuItem().setEnabled(true);
		getAddNoiseMenuItem().setSelected(false);
		getDBMenuItems().setEnabled(true);

	}

	/**
	 * Disable those menu items that rely on a data file being currently loaded.
	 */
	private void disableViewMenuItems() {
		getSaveNormalisedViewMenuItem().setEnabled(false);
		getSaveProjectionMenuItem().setEnabled(false);
		getSaveViewDataMenuItem().setEnabled(false);
		getSaveSVGMenuItem().setEnabled(false);
		getSaveEPSMenuItem().setEnabled(false);
		getSavePNGMenuItem().setEnabled(false);
		getRescaleMenuItem().setEnabled(false);
		getPCAMenuItem().setEnabled(false);
		getRandomMenuItem().setEnabled(false);
		getShowAxesMenuItem().setEnabled(false);
		getShowHierarchicalClusteringMenuItem().setEnabled(false);
		getShowTargetMenuItem().setEnabled(false);
		getAddNoiseMenuItem().setEnabled(false);
		getShowDataViewerMenuItem().setEnabled(false);
		getDBMenuItems().setEnabled(false);
	}

	public void actionPerformed(ActionEvent action) {
		try {
			
			if (action.getSource() == getOpenARFFFileMenuItem())
				setData(new ARFFImporter().importData());
			
			if (action.getSource() == getOpenGraphARFFFileMenuItem())
				new GraphARFFImporterGUI(this);
			
			if (action.getSource() == getOpenCSVFileMenuItem())
				setData(new CSVDataImporter().importData());
			
		} catch (Exception e) {
			
			System.out.println(e);
			
		}
		
		if (action.getSource() == getHelpMenuItem())
			browseHelp();

		if (action.getSource() == getSaveNormalisedViewMenuItem())
			Exporter.saveNormalisedData(model, null);

		if (action.getSource() == getSaveProjectionMenuItem())
			Exporter.saveCurrentProjection(model, null);

		if (action.getSource() == getSaveViewDataMenuItem())
			Exporter.saveCurrentViewDataAsTSV(model, null);

		if (action.getSource() == getSaveSVGMenuItem())
			Exporter.saveViewAsSVGImage(viewPanel.canvas, model, null);

		if (action.getSource() == getSaveEPSMenuItem())
			Exporter.saveViewAsEPSImage(viewPanel.canvas, model, null);

		if (action.getSource() == getSavePNGMenuItem())
			Exporter.saveViewAsPNGImage(viewPanel.canvas, model, null);

		if (action.getSource() == getRescaleMenuItem())
			model.resizePlot();

		if (action.getSource() == getPCAMenuItem())
			model.PCA();

		if (action.getSource() == getRandomMenuItem())
			model.randomProjection();

		if (action.getSource() == getShowAxesMenuItem())
			model.setShowAxes(getShowAxesMenuItem().isSelected());

		if (action.getSource() == getShowHierarchicalClusteringMenuItem()) {
			model.createHierarchicalClustering();
			model.setShowHierarchicalClustering(getShowHierarchicalClusteringMenuItem()
					.isSelected());
		}

		if (action.getSource() == getShowTargetMenuItem())
			model.setShowTarget(getShowTargetMenuItem().isSelected());

		if (action.getSource() == getAddNoiseMenuItem())
			viewPanel.addNoise(getAddNoiseMenuItem().isSelected());

		if (action.getSource() == getShowDataViewerMenuItem())
			showDataViewer(getShowDataViewerMenuItem().isEnabled());

		if (action.getSource() == DBConnectionItem)
			new DBConnectionGUI(this);

		if (action.getSource() == DBSelectionItem) {
			try {
				model.runQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addDatabaseConnection(String username, String password,
			String database, String table) {
		model.addDatabaseConnection(username, password, database, table);
	}

	private static void browseHelp() {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(TargetedProjectionPursuit.HELP_URL));
			} catch (Exception e) {
				// TODO: error handling
			}
		} else {
			// TODO: error handling
		}
	}

	private void showDataViewer(boolean show) {
		if (show) {
			dataViewer = new DataViewer(model);
		} else {
			if (dataViewer != null) {
				dataViewer.setVisible(false);
				dataViewer.dispose();
			}
		}

	}

	/** Set the model that this window is used to visualise */
	void setData(Instances in) {
		if (in != null) {
			try {
				model = new ScatterPlotModel(2);
				model.setInstances(in, false);
				model.setDeepInstances(in);
				viewPanel = new ScatterPlotViewPanel();
				controlPanel = new ScatterPlotControlPanel();
				controlPanel.setModel(model);
				setTitle(model.getInstances().relationName());
				enableViewMenuItems();
				JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
						viewPanel, controlPanel);
				split.setResizeWeight(0.8);
				setContentPane(split);
				setVisible(true);
				split.setDividerLocation(split.getSize().width - 350);
				Dimension minimumSize = new Dimension(0, 0);
				viewPanel.setMinimumSize(minimumSize);
				controlPanel.setMinimumSize(minimumSize);
				viewPanel.setModel(model);
				ScatterPlotViewPanelMouseListener l = new ScatterPlotViewPanelMouseListener(
						viewPanel.canvas, model);
				viewPanel.canvas.addMouseListener(l);
				viewPanel.canvas.addMouseMotionListener(l);
				viewPanel.repaint();

			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"There was a problem reading that data");
			}
		}
	}

	public TPPModel getModel() {
		return model;
	}

}