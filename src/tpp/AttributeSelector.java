package tpp;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import weka.core.Attribute;
import weka.core.Instances;

/**
 * 
 * @author Helen Uses code from AttributeCombo to load a Multiple interval
 *         selection JList so that the user can select the attributes they want
 *         to include in the projection. Ultimately any non-numeric attributes
 *         will be excluded anyway
 */

public class AttributeSelector extends JFrame implements ActionListener, ListSelectionListener {
	
	private ScatterPlotControlPanel cp;
	private DefaultListModel attributeListModel;
	private ScatterPlotModel model;
	
	private JList attributeList;
	private JTable attributeTable;
	private JButton OKButton;
	private int[] selectedIndices;
//	private JButton CancelButton;
	private AttributeTableModel attributeTableModel;

	public AttributeSelector(ScatterPlotModel model, ScatterPlotControlPanel cp) {
		super("Select attributes");
		this.cp = cp;
		this.model = model;
		setup();
		initialize();
		this.setVisible(true);
	}

	private void setup() {
		this.setSize(200, 400);
		this.setLocation(600, 200);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
	}

	private void initialize() {
		
//		attributeListModel =  new DefaultListModel();
//		
//		Vector<Attribute> ats = new Vector<Attribute>();
//		ats = model.getAllAttributes();
//		
//		// create labels
//		Vector<String> labels = new Vector<String>();
//		for (Attribute at : ats) 
//			labels.add(at == null ? "None" : at.name());
//			
//		
//		for (String label : labels)
//			attributeListModel.addElement(label);
//		
//		attributeList  = new JList(attributeListModel);
//		attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		attributeList.addListSelectionListener(this);
//		JScrollPane listScrollPane = new JScrollPane(attributeList);
//		listScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				
		attributeTableModel = new AttributeTableModel(model);
		attributeTable  = new JTable(attributeTableModel);
		
		RowSorter<AttributeTableModel> sorter = new TableRowSorter<AttributeTableModel>(attributeTableModel);
		attributeTable.setRowSorter(sorter);
		
		attributeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		attributeTable.setRowSelectionAllowed(true);
		
		JScrollPane listScrollPane = new JScrollPane(attributeTable);
		
		OKButton = new JButton("OK");
//		CancelButton = new JButton("Cancel");
		
		OKButton.addActionListener(this);
//		CancelButton.addActionListener(this);
			
		setLayout(new BorderLayout());
		
		add(listScrollPane, BorderLayout.CENTER);
		
		add(OKButton, BorderLayout.PAGE_END);				
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == OKButton) {
//			int[] indices = attributeList.getSelectedIndices();
			int[] indices = attributeTable.getSelectedRows();
			int[] modelIndices = new int[indices.length];
			for (int i = 0; i < indices.length; i++){
				modelIndices[i] = attributeTable.getRowSorter().convertRowIndexToModel(indices[i]);
			}
			System.out.println(modelIndices);
			cp.setSelectedIndices(modelIndices);
		}
		this.dispose();

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	class AttributeTableModel extends AbstractTableModel {
		
		private static final int COLS = 3;

		public static final int SEQ_COL = 0;

		public static final int ATT_COL = 1;

		public static final int OCC_COL = 2;
				
		private String[] columnNames = {" ","Attribute", "Occurences"};
//		private Object[][] columnData;
		
		private ScatterPlotModel model;

		private Vector<String> labels;

		private Vector<Integer> occurrences;
		
		public AttributeTableModel(ScatterPlotModel model){
			this.model = model;	
			
			Vector<Attribute> ats = new Vector<Attribute>();
			
//			ats = model.getAllAttributes();
//			columnData = new Object[ats.size()][3];
			Instances d = model.getDeepInstances();
			for (int i = 0; i < d.numAttributes(); i++)
				ats.add(d.attribute(i));
			
			// create labels
			labels = new Vector<String>();
			for (Attribute at : ats) 
				labels.add(at == null ? "None" : at.name());
			
			// create occurrence values
			occurrences = new Vector<Integer>();
			occurrences = getOccurences(d);
			
				
//			
//			for (String label : labels)
//				attributeListModel.addElement(label);
			
			// For this to work all numeric attributes MUST be listed first
//			int i = 0;
//			for(Attribute at : ats){
//				String attName = at.name();
//				int attOccurence;
//				if (at.isNumeric())
//					attOccurence = model.getOccurences()[i];
//				else
//					attOccurence = model.getInstances().size();
//				columnData[i][0] = i;
//				columnData[i][1] = attName;
//				columnData[i][2] = attOccurence;
//				i++;
//			}
			
		}
		
		private Vector<Integer> getOccurences(Instances ins) {
						
			Vector<Integer> occurenceTotal = new Vector<Integer>();
			
			for (int i = 0; i < ins.numAttributes(); i++) {
				int k = 0;
				if (ins.attribute(i).isNumeric() && !ins.attribute(i).isDate()) {
					for(int j = 0; j < ins.numInstances(); j++) {
						if (ins.instance(j).value(ins.attribute(i))!= 0){
							k++;
						}
					}
				} else
					k = ins.size();
				System.out.println(k);
				occurenceTotal.add(k);
			}
			return occurenceTotal;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}
		
		public Class getColumnClass(int col) {
			return (col == ATT_COL ? String.class : Integer.class);
		}

		@Override
		public int getRowCount() {
			return labels.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case SEQ_COL:
				return row + 1;
			case ATT_COL:
				return new String(labels.get(row));
			case OCC_COL:
				return new Integer(occurrences.get(row));
			default:
				return "#Error#";
			}
		}
		
	}

}
