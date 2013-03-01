package tpp;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.matrix.Matrix;

/**
 * Class for performing HAC using the S. C. Johnson (1967):
 * "Hierarchical Clustering Schemes" Psychometrika, 2:241-254. Different methods
 * use different distance measure to determine the distance between clusters. <br>
 * see http://home.dei.polimi.it/matteucc/Clustering/tutorial_html/
 * hierarchical.html#johnson for more information
 */
public class HierarchicalAgglomerativeClustering {

	TPPModel model;
	
	/**
	 * HAC using single linkage clustering
	 */
	public HierarchicalCluster singleLinkage(TPPModel model) {
		this.model=model;
		return agglomerativeClustering(model, new SingleLinkage());
	}

	/**
	 * HAC using the given distance measure
	 */
	public HierarchicalCluster agglomerativeClustering(TPPModel model, ClusterDistanceCalculator cdc) {
		// the 'root' cluster containing all others
		HierarchicalCluster root = new HierarchicalCluster(model);

		// create new 'leaf' clusters, each containing a single instance
		HierarchicalCluster leaf;
		for (int i = 0; i < model.getNumDataPoints(); i++) {
			leaf = new HierarchicalCluster(model);
			leaf.add(model.instances.instance(i));
			root.add(leaf);
		}

		// until there is a single root cluster
		while (root.size() > 1) {

			// compute distances between clusters and record the most nearest
			// pair
			double nearestDistance = -1, distance;
			HierarchicalCluster[] nearestPair = null;
			HierarchicalCluster cluster1, cluster2;
			for (int i = 0; i < root.size(); i++) {
				for (int j = 0; j < i; j++) {
					cluster1 = (HierarchicalCluster) root.get(i);
					cluster2 = (HierarchicalCluster) root.get(j);
					distance = cdc.distance(cluster1, cluster2);
					if (nearestDistance < 0 || (distance < nearestDistance)) {
						nearestPair = new HierarchicalCluster[] { cluster1, cluster2 };
						nearestDistance = distance;
					}
				}
			}

			// create a new cluster containing the nearest pair and remove the
			// old two
			HierarchicalCluster newCluster = new HierarchicalCluster(model);
			newCluster.add(nearestPair[0]);
			newCluster.add(nearestPair[1]);
			root.remove(nearestPair[0]);
			root.remove(nearestPair[1]);
			root.add(newCluster);

		}

		System.out.println("Clustering: " + root);

		return root;
	}

	/**
	 * Class for calculating distances between clusters. Difference
	 * implementations can be used to perform different types of clustering.
	 */
	private abstract class ClusterDistanceCalculator {

		abstract double distance(HierarchicalCluster c1, HierarchicalCluster c2);

		protected double euclideanDistance(Instance in1, Instance in2) {
			int i1 = model.indexOf(in1);
			int i2 = model.indexOf(in2);
			Matrix m1 = model.data.getMatrix(i1, i1, 0, model.data.getColumnDimension()-1);
			Matrix m2 = model.data.getMatrix(i2, i2, 0, model.data.getColumnDimension()-1);
			double distance = m1.minus(m2).normF();
			return distance;
		}
		
	}

	private class SingleLinkage extends ClusterDistanceCalculator {


		public double distance(HierarchicalCluster c1, HierarchicalCluster c2) {
			double distance, minDistance = -1;

			// search through the instances in each of the clusters
			for (Instance i1 : c1.getInstances())
				for (Instance i2 : c2.getInstances()) {
					distance = euclideanDistance(i1, i2);
					if (minDistance < 0 || minDistance < distance)
						minDistance = distance;
				}

			return minDistance;
		}
	}

}
