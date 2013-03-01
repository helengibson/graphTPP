package tpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/** A generic class for reading TSV and CSV files. */
public class TSVFileReader {

	public static final String DEFAULT_DELIMITER = "\t";

	/**
	 * @param file
	 * @param delimiter
	 *            if null then default to the default
	 * @param header
	 *            whether this contains a header row. If so then the first line
	 *            in the file is ignored
	 * @return
	 * @throws IOException
	 */
	public String[][] readStrings(File file, String delimiter, boolean header) throws IOException {
		if (delimiter == null)
			delimiter = DEFAULT_DELIMITER;
		String row;
		String[][] values = null;
		String splitRegex = "[" + delimiter + "]\\s*";
		int cols;
		Vector<String> rows = new Vector<String>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		if (header)
			reader.readLine();
		while ((row = reader.readLine()) != null)
			rows.add(row);
		if (rows.size() > 0) {
			cols = rows.get(0).split(splitRegex).length;
			values = new String[rows.size()][cols];
			int r = 0;
			for (String thisrow : rows)
				values[r++] = thisrow.split(splitRegex);
		}
		return values;
	}

	public double[][] readDoubles(File file, String delimiter, boolean header) throws IOException {
		String[][] strings = readStrings(file, delimiter, header);
		double[][] doubles = new double[strings.length][strings[0].length];
		for (int row=0;row<strings.length;row++)
			for (int col=0;col<strings[0].length;col++)
				doubles[row][col] = Double.parseDouble(strings[row][col]);
		return doubles;
	}
}
