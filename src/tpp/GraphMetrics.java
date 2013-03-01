package tpp;

import java.util.Iterator;

import weka.core.Instance;
import weka.core.Instances;

public class GraphMetrics {
	
	private int[] nodeDegree;
	private Graph graph;
	private ScatterPlotModel spModel;
	private int[] inDegree;
	private int[] outDegree;
	int edgeAttributeIndex;
		
	public GraphMetrics(Graph graph, ScatterPlotModel spModel){
		this.graph = graph;
		this.spModel = spModel;
		edgeAttributeIndex = spModel.getEdgeAttributeIndex();
	}
	
	public int[] calculateNodeDegree() {
		System.out.println("Calculating total degree");
			
		Instances ins = spModel.getInstances();
		
		int numberInstances = ins.numInstances();

		Connection cnxn = null;
		
		nodeDegree = new int[numberInstances];
				
		int i;
		for (i = 0; i < numberInstances; i++){
			
			Iterator<Connection> allConnections = spModel.getGraph().getAllConnections().iterator();
			
//			String currentNode = ins.instance(i).toString(edgeAttributeIndex);
			Instance currentNode = ins.instance(i);
//			System.out.println(currentNode);
			int currentDegree = 0;
								
			while (allConnections.hasNext()) {
				cnxn = allConnections.next();
				if(cnxn.getSourceInstance()== currentNode){
//					System.out.println(cnxn.getSourceNode());
					currentDegree++;
				}
				if(cnxn.getTargetInstance() == currentNode){
//					System.out.println(cnxn.getTargetNode());
					currentDegree++;
				}
			}
			nodeDegree[i] = currentDegree;
			
		}
	return nodeDegree;	
	}
	
	public int[] calculateNodeInDegree() {
		System.out.println("Calculating in degree");
		
		Instances ins = spModel.getInstances();
		
		int numberInstances = ins.numInstances();

		Connection cnxn = null;
		
		inDegree = new int[numberInstances];
		
		int i;
		for (i = 0; i < numberInstances; i++){
			
			Iterator<Connection> allConnections = spModel.getGraph().getAllConnections().iterator();
			
			//String currentNode = ins.instance(i).toString(edgeAttributeIndex);
			Instance currentNode = ins.instance(i);
			//System.out.println(currentNode);
			int currentInDegree = 0;
			
					
			while (allConnections.hasNext()) {
				cnxn = allConnections.next();
				if(cnxn.getTargetInstance() == currentNode){
					//System.out.println(cnxn.getTargetNode());
					currentInDegree++;
				}
			}
			inDegree[i] = currentInDegree;
			
		}
	return inDegree;	
	}
	
	public int[] calculateNodeOutDegree() {
		System.out.println("Calculating out degree");
		
		Instances ins = spModel.getInstances();
		
		int numberInstances = ins.numInstances();

		Connection cnxn = null;
		
		outDegree = new int[numberInstances];
		
		int i;
		for (i = 0; i < numberInstances; i++){
			
			Iterator<Connection> allConnections = spModel.getGraph().getAllConnections().iterator();
			
			//String currentNode = ins.instance(i).toString(edgeAttributeIndex);
			Instance currentNode = ins.instance(i);
			//System.out.println(currentNode);
			int currentOutDegree = 0;
			
					
			while (allConnections.hasNext()) {
				cnxn = allConnections.next();
				if(cnxn.getSourceInstance() == currentNode){
					//System.out.println(cnxn.getSourceNode());
					currentOutDegree++;
				}
				
			}
			outDegree[i] = currentOutDegree;
			
		}
	return outDegree;	
	}
	
	
	
	

	
}
