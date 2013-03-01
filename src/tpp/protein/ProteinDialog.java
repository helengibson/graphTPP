package tpp.protein;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/** A JDialog for determining how to convert protein sequences to weka instances */
public class ProteinDialog extends JDialog implements ActionListener, ItemListener {

	private JPanel panel = null;

	private JComboBox propertyComboBox = null;

	private JLabel propertyLabel = null;

	private JButton okButton = null;

	private JList classPieceList = null;

	private String exampleHeader;

	private JCheckBox selectClassificationBox = null;

	private JLabel selectClassificationLabel = null;

	private JPanel classificationPanel = null;

	private JComboBox classDelimiterComboBox = null;

	private JLabel selectDelimiterLabel = null;

	private static final String[] DELIMITERS_DISPLAY = new String[]{" |"," :"," _"," ;"};
	private static final String[] DELIMITERS         = new String[]{"\\|",":","_"," ;"};

	/**
	 * This method initializes propertyComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getPropertyComboBox() {
		if (propertyComboBox == null) {
			propertyComboBox = new JComboBox(AminoAcidProperty.ALL_PROPERTIES);
			propertyComboBox.addItem(new AminoAcidIdentity());
			propertyComboBox.addItem(new AminAcidAllProperties());
			propertyComboBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			propertyComboBox.setBounds(new Rectangle(19, 159, 165, 30));
		}
		return propertyComboBox;
	}

	/**
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.setBounds(new Rectangle(286, 158, 60, 30));
			okButton.addActionListener(this);
		}
		return okButton;
	}
	
	public ProteinDialog(JFrame frame, String exampleHeader) {
		super(frame, true);
		this.exampleHeader = exampleHeader;
		initialize();
		this.setVisible(true);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(359, 222);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (panel == null) {
			selectDelimiterLabel = new JLabel();
			selectDelimiterLabel.setText("Select character used to indicate classification");
			selectDelimiterLabel.setBounds(new Rectangle(59, 52, 263, 32));
			selectDelimiterLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			selectClassificationLabel = new JLabel();

			selectClassificationLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			selectClassificationLabel.setBounds(new Rectangle(39, 14, 285, 45));
			selectClassificationLabel
					.setText("<html>Click on the piece of the protein header<p>to classify sequences by</html>");
			propertyLabel = new JLabel();
			propertyLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			propertyLabel.setBounds(new Rectangle(20, 130, 270, 30));
			propertyLabel.setText("Which amino acid residue property to use:");
			panel = new JPanel();
			panel.setLayout(null);
			panel.add(getPropertyComboBox(), null);
			panel.add(propertyLabel, null);
			panel.add(getOkButton(), null);
			panel.add(getClassificationPanel(), null);
		}
		return panel;
	}

	public String getClassDelimiter() {
		if (getSelectClassificationBox().isSelected())
			return DELIMITERS[getClassDelimiterComboBox().getSelectedIndex()];
		else
			return null;
	}

	public int getClassPiece() {
		return classPieceList.getSelectedIndex();
	}

	public AminoAcidProperty getSelectedProperty() {
		return (AminoAcidProperty) propertyComboBox.getSelectedItem();
	}

	/**
	 * This method initializes classPieceList
	 *
	 * @return javax.swing.JList
	 */
	private JList getClassPieceList() {
		if (classPieceList == null) {
			classPieceList = new JList(exampleHeader.split("\\|"));
			classPieceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			classPieceList.setVisibleRowCount(1);
			classPieceList.setLayoutOrientation(classPieceList.HORIZONTAL_WRAP);
			classPieceList.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			classPieceList.setBounds(new Rectangle(10, 95, 300, 20));
			classPieceList.setEnabled(false);
		}
		return classPieceList;
	}

	/**
	 * This method initializes selectClassificationBox
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getSelectClassificationBox() {
		if (selectClassificationBox == null) {
			selectClassificationBox = new JCheckBox();
			selectClassificationBox.addItemListener(this);
			selectClassificationBox.setSelected(false);
			selectClassificationBox.setBounds(new Rectangle(10, 20, 21, 21));
		}
		return selectClassificationBox;
	}

	/**
	 * This method initializes classificationPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getClassificationPanel() {
		if (classificationPanel == null) {
			classificationPanel = new JPanel();
			classificationPanel.setLayout(null);
			classificationPanel.setBorder(BorderFactory.createTitledBorder(null, "Classify Sequences",
					TitledBorder.LEFT, TitledBorder.TOP, new Font("Dialog", Font.PLAIN, 10), new Color(51, 51, 51)));
			classificationPanel.setBounds(new Rectangle(11, 13, 334, 122));
			classificationPanel.add(getClassPieceList(), null);
			classificationPanel.add(getSelectClassificationBox(), null);
			classificationPanel.add(selectClassificationLabel, null);
			classificationPanel.add(getClassDelimiterComboBox(), null);
			classificationPanel.add(selectDelimiterLabel, null);
		}
		return classificationPanel;
	}

	/**
	 * This method initializes classDelimiterComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getClassDelimiterComboBox() {
		if (classDelimiterComboBox == null) {
			classDelimiterComboBox = new JComboBox(DELIMITERS_DISPLAY);
			classDelimiterComboBox.setBounds(new Rectangle(11, 59, 41, 25));
			classDelimiterComboBox.addItemListener(this);
			classDelimiterComboBox.setEnabled(false);
		}
		return classDelimiterComboBox;
	}

	public static void main(String[] args) {
		ProteinDialog pd = new ProteinDialog(null, "oi:misc|ibib|ibib|243rf|");
	}

	public void itemStateChanged(ItemEvent e) {
		// if the select classification checkbox is ticked then allow user to
		// select
		if (e.getSource() == getSelectClassificationBox()) {
			getClassPieceList().setEnabled(e.getStateChange() == e.SELECTED);
			getClassDelimiterComboBox().setEnabled(e.getStateChange() == e.SELECTED);
		}

		// if a new delimiter is chosen, then split the example header using it and display
		if (e.getSource() == getClassDelimiterComboBox()){
			getClassPieceList().setListData(exampleHeader.split(DELIMITERS[getClassDelimiterComboBox().getSelectedIndex()]));
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == getOkButton()) {
			if (getSelectClassificationBox().isSelected()&&getClassPieceList().isSelectionEmpty()) {
				JOptionPane.showMessageDialog(this, "Select which piece of the header denotes the classification of the protein.");
			} else
			setVisible(false);
		}
	}
}  //  @jve:decl-index=0:visual-constraint="152,62"
