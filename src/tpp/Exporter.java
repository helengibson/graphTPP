package tpp;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import tpp.ScatterPlotViewPanel.ScatterPlotViewPanelCanvas;

/**
 * Utility class for exporting aspects of a TPP Model in various formats. In
 * each case, if the file is null then the user is prompted to choose a file.
 */
public class Exporter {

	private static final int DEFAULT_IMAGE_SIZE = 1000;

	/** The default directory for file operations */
	private static final String DEFAULT_DIRECTORY = ".";

	private static final String DATA_FILE_DELIMITER = "\t";

	/** Save the current view as a PNG image file. If the file is null then the user is prompted to choose one. */
	static void saveViewAsPNGImage(ScatterPlotViewPanelCanvas viewPanel, ScatterPlotModel model,
			File file) {
		if (file == null) {
			JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile();
		}
		if (file == null)
			return;
		try {

			BufferedImage image = new BufferedImage(DEFAULT_IMAGE_SIZE,
					DEFAULT_IMAGE_SIZE, ColorSpace.TYPE_RGB);
			viewPanel.setZPTransform((Graphics2D) image.getGraphics());
			viewPanel.paintView((Graphics2D) image.getGraphics(),
					model.getTransform(viewPanel.getWidth(), viewPanel.getHeight()),
					viewPanel.getWidth(), viewPanel.getHeight());
			ImageIO.write(image, "png", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Save the current view as a SVG image file. If the file is null then the user is prompted to choose one. */
	static void saveViewAsSVGImage(ScatterPlotViewPanelCanvas viewPanel, ScatterPlotModel model,
			File file) {
		if (file == null) {
			JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile();
		}
		if (file == null)
			return;
		// write the view panel to a new SVG document
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		viewPanel.setZPTransform(svgGenerator);
		viewPanel.paintView(svgGenerator,
				model.getTransform(viewPanel.getWidth(), viewPanel.getHeight()),
				viewPanel.getWidth(), viewPanel.getHeight());
		// TestSVG(svgGenerator);

		// and write the SVG to the file
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(file));
			svgGenerator.stream(out, true);
			out.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/** Save the current view as a EPS image file. If the file is null then the user is prompted to choose one. */
	static void saveViewAsEPSImage(ScatterPlotViewPanelCanvas viewPanel, ScatterPlotModel model,
			File file) {
		if (file == null) {
			JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile();
		}
		if (file == null)
			return;

		// output view to file
		OutputStream out = null;
		try {
			EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(false);
			g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
			out = new java.io.FileOutputStream(file);
			out = new java.io.BufferedOutputStream(out);
			g2d.setupDocument(out, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);
			viewPanel.setZPTransform(g2d);
			viewPanel.paintView(g2d,
					model.getTransform(DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE),
					DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);
			g2d.finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/** Save the current projection matrix. If the file is null then the user is prompted to choose one. */
	static void saveCurrentProjection(TPPModel model, File file) {
		if (file == null) {
			JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile();
		}
		if (file == null)
			return;

		try {
			FileWriter out = new FileWriter(file);
			out.write(model.getProjection().toString());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Save the current view matrix. If the file is null then the user is prompted to choose one. */
	static void saveCurrentViewDataAsTSV(TPPModel model, File file) {
		if (file == null) {
			JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile();
		}
		if (file == null)
			return;
		try {
			FileWriter out = new FileWriter(file);
			out.write(model.getViewAsString());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Save the current normalised data. If the file is null then the user is prompted to choose one. */
	static void saveNormalisedData(TPPModel model, File file) {
		if (file == null) {
			JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile();
		}
		if (file == null)
			return;
		try {
			FileWriter out = new FileWriter(file);

			StringBuffer sb = new StringBuffer();
			for (int row = 0; row < model.getData().getRowDimension(); row++) {
				for (int col = 0; col < model.getData().getColumnDimension(); col++) {
					sb.append(model.getData().get(row, col)).append(
							DATA_FILE_DELIMITER);
				}
				sb.append("\n");
			}
			out.write(sb.toString());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
