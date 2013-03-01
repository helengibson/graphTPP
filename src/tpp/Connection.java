package tpp;

import weka.core.Instance;
import weka.core.Instances;

public class Connection {
	
	private String sourceNode;
	
	private String targetNode;
	
	private Instance nodeInstance;
	
	private Instance sourceInstance;
	
	private Instance targetInstance;
	
	private int sourceIndex;
	
	private int targetIndex;

	private Double weight;
		
	/**
	 * 
	 * @param ins
	 * @param index - the index of the attribute that is being used as the node identifier
	 * @param sourceNode
	 * @param targetNode
	 */
	public Connection(Instances ins, int index, String sourceNode, String targetNode, Double weight) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
				
		sourceInstance = getNodeInstance(ins, sourceNode, index);
		targetInstance = getNodeInstance(ins, targetNode, index);
		
		sourceIndex = indexOf(ins, sourceInstance);
		targetIndex = indexOf(ins, targetInstance);
		
		this.weight = weight;
		
	}
	
	public String getSourceNode() {
		return sourceNode;
	}

	public String getTargetNode() {
		return targetNode;
	}
	
	public Instance getSourceInstance() {
		return sourceInstance;
	}
	
	public Instance getTargetInstance() {
		return targetInstance;
	}
	
	public int getSourceIndex() {
		return sourceIndex;
	}
	
	public int getTargetIndex() {
		return targetIndex;
	}
	
	public double getEdgeWeight() {
		return weight;
	}
		
	private Instance getNodeInstance(Instances ins, String node, int index) {
			
		for(int i = 0; i < ins.numInstances(); i++) {
			Instance in = ins.instance(i);
			String attVal = in.stringValue(index);
						
			if(attVal.equals(node)) {
				nodeInstance = in;
			}
		}
		
		return nodeInstance;	
		
	}
	
	// Copied from TPPModel
	private int indexOf(Instances ins, Instance in) {
		for (int i = 0; i < ins.numInstances(); i++)
			if (ins.instance(i).equals(in)){
				// System.out.println(instances.instance(i));
				// System.out.println(in);
				return i;}
		return -1;
	}
}
