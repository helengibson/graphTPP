package tpp;

import java.util.ArrayList;
import java.util.Iterator;

import weka.core.Instance;
import weka.core.Instances;

public class Graph {
		
	private ArrayList<Connection> cnxns;
	
	private Instances in;
	
	private int index;
	
	public Graph(){	
		
		cnxns = new ArrayList<Connection>();	
		
	}
	
	public void add(Connection cnxn) {
		cnxns.add(cnxn);				
	}
	
	public Graph getGraph(){
		return this;
	}

	public ArrayList<Connection> getAllConnections() {
		return cnxns;
	}
	
	public ArrayList<Connection> findNeighbours(String nodeId) {
		 //System.out.println(node);
		ArrayList<Connection> neighbours = new ArrayList<Connection>();
		Iterator<Connection> allConnections = getAllConnections().iterator();
		Connection cnxn;
		
		while (allConnections.hasNext()) {
			cnxn = allConnections.next();
			 //System.out.println("connection is: " + cnxn.getSourceNode() + " to " + cnxn.getTargetNode());
			if (nodeId.equals(cnxn.getSourceNode())){
				neighbours.add(cnxn);
			}
			else if (nodeId.equals(cnxn.getTargetNode())){
				neighbours.add(cnxn);
				//System.out.println("added connection: " + cnxn.getSourceNode() + " to " + cnxn.getTargetNode());
			}
		}
		return neighbours;
	}
		
}
