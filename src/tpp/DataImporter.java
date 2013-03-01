package tpp;

import weka.core.Instances;

/** A class capable of importing a set of instances from a source. */
public interface DataImporter {

	public abstract Instances importData() throws Exception;

}