/*
Updated by Helen Gibson from original TPP version
*/

package tpp;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/** A panel for performing TPP which does everything except load the data. */
public class TPPPanel extends JPanel implements ActionListener, ComponentListener {

	protected ScatterPlotModel model;
	protected ScatterPlotViewPanel viewPanel;
	protected ScatterPlotControlPanel controlPanel = null;
	private DataViewer dataViewer;
	protected JSplitPane splitPane;
	protected JPanel rhPanel;
	protected JComboBox fileCombo;
	protected JComboBox viewCombo;
	private JButton helpButton;

	public TPPPanel() {
		super();
		addComponentListener(this);

	}

	public TPPPanel(LayoutManager layout) {
		super(layout);
		addComponentListener(this);
	}

	public TPPPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		addComponentListener(this);
	}

	public TPPPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		addComponentListener(this);
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

	public TPPModel getModel() {
		return model;
	}

	public void setInstances(Instances in) {
		try {
			removeAll();
			setLayout(new BorderLayout());
			model = new ScatterPlotModel(2);
			model.setInstances(in, false);
			viewPanel = new ScatterPlotViewPanel();
			viewPanel.setModel(model);
			ScatterPlotViewPanelMouseListener l = new ScatterPlotViewPanelMouseListener(viewPanel, model);
			viewPanel.addMouseListener(l);
			viewPanel.addMouseMotionListener(l);
			controlPanel = new ScatterPlotControlPanel();
			controlPanel.setModel(model);
			rhPanel = new JPanel();
			rhPanel.setLayout(new BoxLayout(rhPanel, BoxLayout.Y_AXIS));
			rhPanel.add(getToolBar());
			rhPanel.add(controlPanel);
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewPanel, rhPanel);
			add(splitPane);
			// try to work out a reasonable initial position for the initial divider location
			if (getParent() != null && getParent().getSize().width > 800)
				splitPane.setDividerLocation(getParent().getSize().width - 250);
			else
				splitPane.setDividerLocation(800);
			splitPane.setResizeWeight(0.8);
			Dimension minimumSize = new Dimension(250, 250);
			viewPanel.setMinimumSize(minimumSize);
			rhPanel.setMinimumSize(minimumSize);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "There was a problem reading that data");
		}
	}

	protected JComponent getToolBar() {
		// we'd like to implement these options as menu items, but you can't add
		// menu bars to a panel
		JPanel toolbar = new JPanel();
		toolbar.setLayout(new GridLayout(1, 3, 6, 2));
		toolbar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
		fileCombo = new WideComboBox(new String[] { "File", " Save projection", " Save view",
				" Save view as EPS image", " Save view as SVG image" });
		viewCombo = new WideComboBox(new String[] { "View", " Project onto first two principal components",
				" Resize view to fit to window (right mouse button)", " Show/Hide axes", " Random projection",
				" Dark background", " Light background" });
		helpButton = new JButton("Help");
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseHelp();
			}
		});
		Dimension min = new Dimension(80, 20);
		fileCombo.setMinimumSize(min);
		viewCombo.setMinimumSize(min);
		helpButton.setMinimumSize(min);
		toolbar.add(fileCombo);
		toolbar.add(viewCombo);
		toolbar.add(helpButton);
		fileCombo.addActionListener(this);
		viewCombo.addActionListener(this);
		helpButton.addActionListener(this);
		return toolbar;
	}

	public void componentResized(ComponentEvent e) {
		repaint();
	}

	public void componentMoved(ComponentEvent e) {
		repaint();
	}

	public void componentShown(ComponentEvent e) {
		// rebuild the entire panel every tiem it is reshown, in case there have
		// been any changes to the data
		model.fireModelChanged(TPPModelEvent.DATA_SET_CHANGED);
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fileCombo) {
			switch (fileCombo.getSelectedIndex()) {
			case 1:
				Exporter.saveCurrentProjection(model, null);
				break;
			case 2:
				Exporter.saveCurrentViewDataAsTSV(model, null);
				break;
			case 3:
				Exporter.saveViewAsEPSImage(viewPanel, model, null);
				break;
			case 4:
				Exporter.saveViewAsSVGImage(viewPanel, model, null);
				break;
			}
			fileCombo.setSelectedIndex(0);
		}
		if (e.getSource() == viewCombo) {
			switch (viewCombo.getSelectedIndex()) {
			case 1:
				model.PCA();
				model.resizePlot();
				break;
			case 2:
				model.resizePlot();
				break;
			case 3:
				model.setShowAxes(!(model.showAxes()));
				break;
			case 4:
				model.randomProjection();
				model.resizePlot();
				break;
			case 5:
				model.setColours(ColourScheme.DARK);
				break;
			case 6:
				model.setColours(ColourScheme.LIGHT);
				break;
			}
			viewCombo.setSelectedIndex(0);
		}
	}

	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation info = new TechnicalInformation(Type.ARTICLE);
		info.setValue(Field.AUTHOR, "Faith,J");
		info.setValue(Field.YEAR, "2007");
		info.setValue(Field.TITLE,
				"Targeted Projection Pursuit for Interactive Exploration of High-Dimensional Data Sets");
		info.setValue(Field.JOURNAL, "Proceedings of 11th International Conference on Information Visualisation (IV07)");
		return info;
	}

	// A combobox whose menu is wider than its label
	// http://www.jroller.com/santhosh/entry/make_jcombobox_popup_wide_enough
	// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4618607
	class WideComboBox extends JComboBox {

		public WideComboBox(final Object items[]) {
			super(items);
		}

		private boolean layingOut = false;

		public void doLayout() {
			try {
				layingOut = true;
				super.doLayout();
			} finally {
				layingOut = false;
			}
		}

		public Dimension getSize() {
			Dimension dim = super.getSize();
			if (!layingOut)
				dim.width = Math.max(dim.width, getPreferredSize().width);
			return dim;
		}
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

	/** Test method */
	public static void main(String[] args) {
		JFrame frame = new JFrame("Test TPPPanel");
		TPPPanel panel = new TPPPanel();
		frame.add(panel);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		panel.setInstances(new ARFFImporter().importData());
		frame.setVisible(true);
		frame.repaint();
	}

}
