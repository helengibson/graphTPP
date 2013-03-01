package tpp.protein;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/** A class capable of reading a collection of protein sequences from a stream. */
public interface SequenceLoader {
	public Vector<Protein> readSequences(InputStream in) throws IOException;
}
