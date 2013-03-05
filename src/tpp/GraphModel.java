package tpp;

import java.util.Iterator;

import weka.core.Instance;
import weka.core.Instances;

public class GraphModel {
	
	private Graph graph;
	private ScatterPlotModel spModel;
	private int[] nodeDegree;
	private int[] inDegree;
	private int[] outDegree;
	int[] degree;
	int edgeAttributeIndex;

	public GraphModel(Graph graph, ScatterPlotModel spModel){
		this.graph = graph;
		this.spModel = spModel;
		edgeAttributeIndex = spModel.getEdgeAttributeIndex();
	}
	
	private int[] calculateNodeDegree() {
			
		Instances ins = spModel.getInstances();
		int numberInstances = ins.numInstances();
		Connection cnxn = null;
		nodeDegree = new int[numberInstances];

		for (int i = 0; i < numberInstances; i++){
			
			Iterator<Connection> allConnections = spModel.getGraph().getAllConnections().iterator();
			Instance currentNode = ins.instance(i);
			int currentDegree = 0;
								
			while (allConnections.hasNext()) {
				cnxn = allConnections.next();
				if(cnxn.getSourceInstance()== currentNode)
					currentDegree++;
				if(cnxn.getTargetInstance() == currentNode)
					currentDegree++;
			} 
			System.out.println(i + " : "+ currentDegree);
			nodeDegree[i] = currentDegree;
		}
		return nodeDegree;	
	}
	
	private int[] calculateNodeInDegree() {	
		
		Instances ins = spModel.getInstances();
		int numberInstances = ins.numInstances();
		Connection cnxn = null;
		inDegree = new int[numberInstances];
		
		for (int i = 0; i < numberInstances; i++){
			
			Iterator<Connection> allConnections = spModel.getGraph().getAllConnections().iterator();
			Instance currentNode = ins.instance(i);
			int currentInDegree = 0;
			
					
			while (allConnections.hasNext()) {
				cnxn = allConnections.next();
				if(cnxn.getTargetInstance() == currentNode)
					currentInDegree++;
			}
			inDegree[i] = currentInDegree;
			
		}
	return inDegree;	
	}
	
	private int[] calculateNodeOutDegree() {
		
		Instances ins = spModel.getInstances();
		int numberInstances = ins.numInstances();
		Connection cnxn = null;
		outDegree = new int[numberInstances];

		for (int i = 0; i < numberInstances; i++){
			
			Iterator<Connection> allConnections = spModel.getGraph().getAllConnections().iterator();
			Instance currentNode = ins.instance(i);
			int currentOutDegree = 0;
			
			while (allConnections.hasNext()) {
				cnxn = allConnections.next();
				if(cnxn.getSourceInstance() == currentNode)
					currentOutDegree++;
			}
			outDegree[i] = currentOutDegree;
		}
	return outDegree;	
	}
	
	public boolean neighbourSelected(int i) {
		// get all neighbours of a node
		
		Instances instances = spModel.getInstances();
		EdgeModel edgeModel = spModel.getEdgeModel();
		
		int idIndex =  instances.attribute(spModel.getEdgeAttributeString()).index(); 
		Iterator<Connection> nbs = spModel.getGraph().findNeighbours(
				instances.instance(i).stringValue(idIndex)).iterator();
		boolean result = false;
		while (nbs.hasNext()) {
			Connection nextnbr;
			int j;
			nextnbr = nbs.next();
			// check if the node i is the source node in this connection and
			// that we are wanting to display
			// outgoing edges
			if (nextnbr.getSourceNode().equals(instances.instance(i).stringValue(idIndex)) ){
				// if it is get the instance this target node belongs to
				j = nextnbr.getTargetIndex();
				// then check if this instance is in the list of selected nodes
				if (spModel.isPointSelected(j) && edgeModel.incomingEdges()) {
					result = true;
				}
			} else if (nextnbr.getTargetNode().equals(instances.instance(i).stringValue(idIndex))) {
				j = nextnbr.getSourceIndex();
				if (spModel.isPointSelected(j) && edgeModel.outgoingEdges()) {
					result = true;
				}
			} else {
				result = false;
			}
		}
		return result;
	}
	
	public int[] getDegree() {
		System.out.println("Degree is : " + degree);
		return degree;
	}
	
	/** Set the size attribute to be node degree */
	public void setGraphSizeAttribute(int index) {
		System.out.println("index is: "+index);
		// create an array with the chosen degree of each node
		switch (index) {
		case 0:
			degree = null;
			break;
		case 1:
			degree = calculateNodeDegree();
			break;
		case 2:
			degree = calculateNodeInDegree();
			break;
		case 3:
			degree = calculateNodeOutDegree();
			break;
		}

		// check that the degree isn't null
		if (degree != null) {
			// find the maximum and minimum degree values
			int length = degree.length;
			int i;
			int lowest = degree[0];
			int highest = degree[0];

			for (i = 1; i < length; i++) {
				if (degree[i] < lowest)
					lowest = degree[i];
				if (degree[i] > highest)
					highest = degree[i];
			}
			spModel.updateGraphModel(this);
			spModel.getPointModel().setSizeOnDegree(degree, lowest, highest);
			
		}
	}
	
	

	
}
