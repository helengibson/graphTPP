package tpp;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/** A file filter that only shows files with an .csv .tsv or .dat extension */
public class CSVFileFilter extends FileFilter {

	private static final String FILTER_DESCRIPTION = "CSV data files";

	public boolean accept(File f) {
		if (f.isDirectory())return true;
		String ext = getExtension(f);
		if (ext != null)return (ext.equals("csv") || ext.equals("tsv") || ext.equals("dat"));
		return false;
	}

	public String getDescription() {
		return FILTER_DESCRIPTION;
	}

	private String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}
