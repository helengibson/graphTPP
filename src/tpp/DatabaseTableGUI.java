
/*
Added by Helen Gibson 
*/

package tpp;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class DatabaseTableGUI extends JFrame {
	
	private JTable table;
	
	public DatabaseTableGUI(ResultSet rs) throws SQLException{
		setup();
		initialise(rs);
		
	}

	private void initialise(ResultSet rs) throws SQLException {
		System.out.println(rs.toString());
		table.setModel(new DatabaseTableModel(rs));
		this.setVisible(true);	
	}

	private void setup() {
		setTitle("Results of database query");
		this.setSize(1024, 640);
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(true);
		add(new JScrollPane(table));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
	}

}
