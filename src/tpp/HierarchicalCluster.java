package tpp;

import java.util.Vector;

import weka.core.Instance;
import weka.core.matrix.Matrix;

/** A cluster of instances and or other clusters */
public class HierarchicalCluster {

	private Vector members;

	/**
	 * A 1xN row matrix containing the centroid of the members of this cluster
	 * -- used when drawing cluster arcs
	 */
	private Matrix centroid;

	TPPModel model;
	
	HierarchicalCluster(TPPModel model) {
		members = new Vector();
		this.model = model;
	}

	/** cluster can only contain Instances or other Clusters */
	public void add(Object member) {
		if (member instanceof Instance || member instanceof HierarchicalCluster) {
			members.add(member);
			recalculateCentroid();
		} else
			throw new RuntimeException("HierarchicalCLusters can only contain instances or other Clusters");
	}

	public void remove(Object member) {
		members.remove(member);
	}

	private void recalculateCentroid() {
		centroid = new Matrix(1, model.getNumDataDimensions());
		int i;
		for (Object member : members) {
			if (member instanceof Instance) {
				i = model.indexOf((Instance) member);
				centroid = centroid.plus(model.data.getMatrix(i, i, 0, model.getNumDataDimensions() - 1));
			} else
				centroid = centroid.plus(((HierarchicalCluster) member).centroid);
		}
		centroid = centroid.times(1d / members.size());
	}

	public Matrix getCentroid() {
		return centroid;
	}

	public int size() {
		return members.size();
	}

	/** is this a leaf? ie does it contain just one instance and no sub-clusters */
	public boolean isLeaf() {
		return ((members.size() == 1) && (members.get(0) instanceof Instance));
	}

	public String toString() {
		String s = "[";
		for (Object member : members) {
			if (member instanceof Instance)
				s = s + model.indexOf(((Instance) member)) + " ";
			else
				s = s + member + " ";
		}
		return s.trim() + "]";
	}

	public Object get(int i) {
		return members.get(i);
	}
	public Vector getMembers() {
		return members;
	}

	/** All the instances in all the sub-clusters of this one */
	public Vector<Instance> getInstances() {
		Vector<Instance> vi = new Vector<Instance>();
		for (Object member : getMembers()) {
			if (member instanceof Instance)
				vi.add((Instance) member);
			if (member instanceof HierarchicalCluster)
				vi.addAll(((HierarchicalCluster) member).getInstances());
		}
		return vi;
	}

}
