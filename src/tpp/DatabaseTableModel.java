/*
Updated by Helen Gibson from original TPP version
*/

package tpp;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class DatabaseTableModel implements TableModel {
	
	private ResultSet rs;
	private ResultSetMetaData metadata;
	private int numcols;
	private int numrows;

	public DatabaseTableModel(ResultSet rs) throws SQLException {
		this.rs = rs;
		metadata = rs.getMetaData();       		// Get metadata on them
		numcols = metadata.getColumnCount();    // How many columns?
		rs.last();                         		// Move to last row
		numrows = rs.getRow();             		// How many rows?
	}

	   /** 
     * Call this when done with the table model.  It closes the ResultSet and
     * the Statement object used to create it.
     **/
    public void close() {
	try { rs.getStatement().close(); }
	catch(SQLException e) {};
    }

    /** Automatically close when we're garbage collected */
    protected void finalize() { close(); }

    // These two TableModel methods return the size of the table
    public int getColumnCount() { return numcols; }
    public int getRowCount() { return numrows; }

    // This TableModel method returns columns names from the ResultSetMetaData
    public String getColumnName(int column) {
	try {
	    return metadata.getColumnLabel(column+1);
	} catch (SQLException e) { return e.toString(); }
    }

    // This TableModel method specifies the data type for each column.  
    // We could map SQL types to Java types, but for this example, we'll just
    // convert all the returned data to strings.
    public Class getColumnClass(int column) { return String.class; }
    
    /**
     * This is the key method of TableModel: it returns the value at each cell
     * of the table.  We use strings in this case.  If anything goes wrong, we
     * return the exception as a string, so it will be displayed in the table.
     * Note that SQL row and column numbers start at 1, but TableModel column
     * numbers start at 0.
     **/
    public Object getValueAt(int row, int column) {
	try {
	    rs.absolute(row+1);                // Go to the specified row
	    Object o = rs.getObject(column+1); // Get value of the column
	    if (o == null) return null;       
	    else return o.toString();               // Convert it to a string
	} catch (SQLException e) { return e.toString(); }
    }

    // Our table isn't editable
    public boolean isCellEditable(int row, int column) { return false; } 

    // Since its not editable, we don't need to implement these methods
    public void setValueAt(Object value, int row, int column) {}
    public void addTableModelListener(TableModelListener l) {}
    public void removeTableModelListener(TableModelListener l) {}
}
