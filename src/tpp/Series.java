package tpp;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Represents a set of series in the data. A series is defined by two
 * attributes: the id attribute which identifies members of the series (which
 * may be nominal or string); and the index attribute which orders them (which
 * may be real or nominal or date). The id attribute may be null, in which case
 * it is assumed that the data comprises a single series ordered by the index
 * attribute.
 */
public class Series {

	private Attribute indexAttribute;

	private Attribute idAttribute;

	private Instances instances;

	private HashMap<String, TreeSet<Instance>> allSeries;

	/**
	 * The series themselves. Each entry in the hashmap is a set of instances,
	 * ordered by the index attribute. The hashmap is keyed by the values of the
	 * id attribute. If there is no id attribute then there will be a single set
	 * of instances in the hashmap which contains all instances in
	 * indexAttribute order, and keyed by "";
	 */
	public HashMap<String, TreeSet<Instance>> getAllSeries() {
		return allSeries;
	}

	/**
	 * Create a series given just the names of the two attributes.
	 *
	 * @throws TPPException
	 *             if indexAttribute cannot be recognised
	 */
	public Series(Instances ins, String indexAttributeName, String idAttributeName) throws TPPException {
		this.instances = ins;
		this.indexAttribute = ins.attribute(indexAttributeName);
		this.idAttribute = ins.attribute(idAttributeName);
		if (indexAttribute == null)
			throw new TPPException("Unidentified index attribute");
		init();
	}

	public Series(Instances ins, Attribute indexAttribute, Attribute idAttribute) {
		this.indexAttribute = indexAttribute;
		this.idAttribute = idAttribute;
		this.instances = ins;
		init();
	}

	private void init() {

		InstanceComparator comp = new InstanceComparator(indexAttribute);

		// Construct one TreeSet of instances per value of the idAttribute
		// Store the sets in a hash map, keyed by the value of the idAttribute
		// Each set is sorted by the index attribute
		allSeries = new HashMap<String, TreeSet<Instance>>();
		if (idAttribute != null) {
			Enumeration values = idAttribute.enumerateValues();
			while (values.hasMoreElements())
				allSeries.put((String) values.nextElement(), new TreeSet<Instance>(comp));
		} else {
			allSeries.put("", new TreeSet<Instance>(comp));
		}

		// Assign each instance to the appropriate set
		for (int i = 0; i < instances.numInstances(); i++) {
			if (idAttribute != null)
				allSeries.get(instances.instance(i).stringValue(idAttribute)).add(instances.instance(i));
			else
				allSeries.get("").add(instances.instance(i));
		}
	}

	public Attribute getIdAttribute() {
		return idAttribute;
	}

	public Attribute getIndexAttribute() {
		return indexAttribute;
	}

	public String toString() {

		StringBuffer s = new StringBuffer("Instances in Series: ");
		Iterator<String> ikeys = allSeries.keySet().iterator();
		Iterator<Instance> iins;
		String key;
		while (ikeys.hasNext()) {
			key = ikeys.next();
			s.append("\nID=").append(key);
			iins = allSeries.get(key).iterator();
			while (iins.hasNext())
				s.append("\t").append(indexOfInstance(iins.next()));
		}
		return s.toString();

	}

	/**
	 * Find the instance before this one in whichever series it is a member of.
	 * Returns null if it is the first member of its series
	 */
	public Instance previous(Instance in) {

		// find the set that contains this instance
		String key = (idAttribute == null ? "" : in.stringValue(idAttribute));
		TreeSet<Instance> set = allSeries.get(key);

		// iterate through until the next instance is this one
		Iterator<Instance> it = set.iterator();
		Instance prev = null;
		Instance current = null;
		while (it.hasNext()) {
			current = it.next();
			if (current.equals(in))
				return prev;
			prev = current;

		}
		return null;
	}

	/**
	 * Find the instance after this one in whichever series it is a member of.
	 * Returns null if it is the last member of its series
	 */
	public Instance next(Instance in) {

		// find the set that contains this instance
		String key = (idAttribute == null ? "" : in.stringValue(idAttribute));
		TreeSet<Instance> set = allSeries.get(key);

		// iterate through until we find this one
		Iterator<Instance> it = set.iterator();
		Instance current = null;
		while (it.hasNext()) {
			current = it.next();
			if (current.equals(in))
				// and return the next (or null)
				return (it.hasNext() ? it.next() : null);
		}
		return null;

	}

	/**
	 * Find the index of the previous instance, given the index of an instance.
	 * Returns -1 if the instance was first in the series.
	 *
	 * @throws TPPException
	 */
	public int previous(int i) {
		Instance previous = previous(instances.instance(i));
		return (previous != null ? indexOfInstance(previous) : -1);
	}

	/**
	 * Find the index of the next instance, given the index of an instance.
	 * Returns -1 if the instance was last in the series.
	 *
	 * @throws TPPException
	 */
	public int next(int i){
		Instance next = next(instances.instance(i));
		return (next != null ? indexOfInstance(next) : -1);
	}

	/**
	 * Returns the index of the particular instance within the original
	 * instances that went into this series. returns -1 if the instance cannot
	 * be found.
	 */
	private int indexOfInstance(Instance in) {
		for (int i = 0; i < instances.numInstances(); i++)
			if (instances.instance(i).equals(in))
				return i;
		return -1;
	}

	/** Compare two instances by the value of the attribute. */
	private class InstanceComparator implements Comparator {

		private static final int O1_LESS_THAN_02 = -1;

		private static final int O1_GREATER_THAN_02 = +1;

		private static final int O1_EQUALS_02 = 0;

		private Attribute at;

		public InstanceComparator(Attribute at) {
			this.at = at;
		}

		public int compare(Object o1, Object o2) {

			if (((Instance) o1).value(at) == ((Instance) o2).value(at))
				return O1_EQUALS_02;

			// if the attribute is numeric or a date, compare numeric values
			// directly
			if (at.isNumeric() || at.isDate())
				return (((Instance) o1).value(at) < ((Instance) o2).value(at) ? O1_LESS_THAN_02 : O1_GREATER_THAN_02);

			// if the attribute is nominal or string, compare string values
			// ignoring case
			else
				return (((Instance) o1).stringValue(at).compareToIgnoreCase(((Instance) o2).stringValue(at)));
		}

	}

}