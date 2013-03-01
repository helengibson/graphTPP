package tpp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class DBConnectionGUI extends JFrame implements ActionListener {
	
	private DBConnection dbConnection;
	private JButton connectButton;
	private JButton cancelButton;
	private JTextField usernameTF;
	private JTextField passwordTF;
	private JTextField databaseTF;
	private JTextField tableTF;
	
	private TPPFrame parent;
	
	public DBConnectionGUI(TPPFrame parent) {
		super("Add a database connection");
		this.parent = parent;
		setup();
		initalize();
		this.setVisible(true);
	}

	private void initalize() {
		
		JLabel usernameLabel = new JLabel("Username: ");
		JLabel passwordLabel = new JLabel("Password: ");
		JLabel databaseLabel = new JLabel("Database Name: ");
		JLabel tableLabel = new JLabel("Table Name: ");
		
		usernameTF = new JTextField();
		passwordTF = new JTextField();
		databaseTF = new JTextField();
		tableTF = new JTextField();
		
		connectButton = new JButton("Connect");
		cancelButton = new JButton("Cancel");
		
		connectButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		setLayout(new GridBagLayout());
		GridBagConstraints grid = new GridBagConstraints();
		
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 0, 0, 0);
		grid.gridy = 0;
		grid.gridx = 0;
		
		add(usernameLabel, grid);
		grid.gridx++;
		add(usernameTF, grid);
		
		grid.gridy++;
		grid.gridx = 0;
		
		add(passwordLabel, grid);
		grid.gridx++;
		add(passwordTF, grid);
		
		grid.gridy++;
		grid.gridx = 0;
		
		add(databaseLabel, grid);
		grid.gridx++;
		add(databaseTF, grid);
		
		grid.gridy++;
		grid.gridx = 0;
		
		add(tableLabel, grid);
		grid.gridx++;
		add(tableTF, grid);
		
		grid.gridy++;
		grid.gridx = 0;
		
		add(connectButton, grid);
		grid.gridx++;
		add(cancelButton, grid);
		
	}

	private void setup() {
		this.setSize(400, 200);
		this.setLocation(400, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		if(action.getSource() == connectButton) {
			System.out.println("Connect button pressed");
			parent.addDatabaseConnection(usernameTF.getText(), passwordTF.getText(), 
					databaseTF.getText(), tableTF.getText());
			this.dispose();
		}
		else
			this.dispose();
	}
	

}
