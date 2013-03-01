package tpp;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.matrix.Matrix;

import processing.core.PGraphics;
import processing.core.PVector;

public class ScatterPlotViewPanel2 extends JPanel implements
		TPPModelEventListener, ComponentListener {

	protected ScatterPlotModel spModel = null;

	protected static final double LINE_WIDTH = 0.7;

	/** Noise added to the view to better separate the points */
	private Matrix noise;

	/** whether to add noise to the view */
	private boolean showNoise = false;


	ScatterPlotViewPanelCanvas canvas;

	public ScatterPlotViewPanel() {
		super();

		initialize();
		canvas = new ScatterPlotViewPanelCanvas();
		PanningHandler panner = new PanningHandler();
		canvas.addMouseListener(panner);
		canvas.addMouseMotionListener(panner);

		ScaleHandler scaler = new ScaleHandler();
		canvas.addMouseListener(scaler);
		canvas.addMouseWheelListener(scaler);

		this.add(canvas);
	}

	/**
	 *
	 */
	private void initialize() {
		addComponentListener(this);
	}

	/**
	 * Load the data panel with the instances to be displayed
	 */
	public void setModel(ScatterPlotModel spModel) {
		this.spModel = spModel;
		if (spModel == null)
			removeAll();
		else {
			spModel.addListener(this);
			spModel.setColours(ColourScheme.LIGHT);
			noise = new Matrix(spModel.getNumDataPoints(),
					spModel.getNumDataDimensions());
			spModel.initRetinalAttributes();
			spModel.resizePlot(canvas.getWidth(), canvas.getHeight());
		}

	}

	class ScatterPlotViewPanelCanvas extends JComponent {

		double translateX;
		double translateY;
		double scale;

		double mouseX;
		double mouseY;

		private AffineTransform at;
		private AffineTransform saveTransform;
		private AffineTransform currentZPTransform;

		ScatterPlotViewPanelCanvas() {
			translateX = 0;
			translateY = 0;
			scale = 1;
		}

		public void paintComponent(Graphics g) {

			// add a transform so that we can specify coordinates in data space
			// rather than device space
			Graphics2D g2 = (Graphics2D) g;
			g2 = setZPTransform(g2);
			paintView(g2, at, getHeight(), getWidth());

			// make sure you restore the original transform or else the drawing
			// of borders and other components might be messed up
			g2.setTransform(saveTransform);
		}

		public Graphics2D setZPTransform(Graphics2D g2) {
			saveTransform = g2.getTransform();

			// blank the screen. If we do not call super.paintComponent, then
			// we need to blank it ourselves
			g2.setColor(spModel.getColours().getBackgroundColor());
			g2.fillRect(0, 0, getWidth(), getHeight());

			// We need to add new transforms to the existing
			// transform, rather than creating a new transform from scratch.
			// If we create a transform from scratch, we will
			// will start from the upper left of a JFrame,
			// rather than from the upper left of our component
			at = new AffineTransform(saveTransform);

			// The zooming transformation. Notice that it will be performed
			// after the panning transformation, zooming the panned scene,
			// rather than the original scene
			at.translate(mouseX, mouseY);
			at.scale(scale, scale);
			// at.translate(-getWidth()/2, -getHeight()/2);
			at.translate(-mouseX, -mouseY);

			// The panning transformation
			at.translate(translateX, translateY);
			// System.out.println("panning transformation set");

			if (spModel != null)
				at.concatenate(spModel.getTransform());

			g2.setTransform(at);

			return g2;
		}

		public AffineTransform getTransform() {
			return at;
		}

		public Dimension getPreferredSize() {
			return getParent().getSize();

		}

		/**
		 * Find the indices of the nearest points to the given coordinates in
		 * data space. If no point is found then zero length array returned
		 */
		public int[] findNearestPoints(Point2D.Double pt) {
			double margin = spModel.markerSize * getWidth()
					/ spModel.getTransform().getScaleX();
			double distance;
			Vector<Integer> points = new Vector<Integer>();
			for (int i = 0; i < spModel.getNumDataPoints(); i++) {
				distance = pt.distance(new Point2D.Double(spModel.getView()
						.get(i, 0), spModel.getView().get(i, 1)));
				if (distance < margin)
					points.add(new Integer(i));
			}
			int[] aPoints = new int[points.size()];
			for (int i = 0; i < points.size(); i++)
				aPoints[i] = points.get(i).intValue();
			return aPoints;

		}

		/**
		 * Find the indices of the nearest axes to the given coordinates in data
		 * space. If no axis is found then zero length array returned
		 */
		public int[] findNearestAxes(java.awt.geom.Point2D.Double pt) {
			double margin = spModel.markerSize * getWidth()
					/ spModel.getTransform().getScaleX();
			double distance;
			Vector<Integer> axes = new Vector<Integer>();
			for (int i = 0; i < spModel.getNumDataDimensions(); i++) {
				distance = pt.distance(new Point2D.Double(spModel
						.getProjection().get(i, 0), spModel.getProjection()
						.get(i, 1)));
				if (distance < margin)
					axes.add(new Integer(i));
			}
			int[] aAxes = new int[axes.size()];
			for (int i = 0; i < axes.size(); i++)
				aAxes[i] = axes.get(i).intValue();
			return aAxes;
		}

		/**
		 * Paint the scatter plot to the given Graphics, using the given mapping
		 * from data space to device space, and with markers of the given size
		 * (in pixels) If transform is null then use the default one. If
		 * markerSize=0 use the default size.
		 */
		public void paintView(Graphics2D g2, AffineTransform transform,
				int width, int height) {

			double scaledMarkerSize = (spModel.markerSize * width);

			/**
			 * The difference between the maximum and minimum marker size (as a
			 * proportion of screen size).
			 */
			double markerRange = scaledMarkerSize * 2;

			/**
			 * The minimum size of the markers to display. (as a proportion of
			 * screen size).
			 */
			double markerMin = scaledMarkerSize * 0.5;

			double origin = scaledMarkerSize * 2;

			if (spModel != null && spModel.getData() != null) {

				// if a transform is specified then use it, saving the original
				AffineTransform saveAT = null;
				if (transform != null) {
					saveAT = g2.getTransform();
					g2.setTransform(transform);
				} else {
					transform = g2.getTransform();
				}

				BasicStroke strokeWidth = new BasicStroke(
						(float) (LINE_WIDTH / transform.getScaleX()));
				g2.setStroke(strokeWidth);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				// find out how big the markers need to be in data space in
				// order to
				// appear the right size in device space
				// nb this assumes that the same scale is used for both x and y
				double markerRadius = scaledMarkerSize / transform.getScaleX();

				int transparency;
				double labelSize = spModel.getLabelSize();
				Color c;

				// set the transparency level for when the graph is shown
				if (spModel.arePointsSelected())
					transparency = spModel.getTransparencyLevel();
				else
					transparency = 255;

				// If the axes are shown and there are points currently selected
				// then calculate mean attribute values for those selected
				// points,
				// compared to the overall attribute means
				// This is then used to color the axes
				//
				// relativeMeanForSelected = L( ats-at / at )
				// where ats = mean of this attribute for the selected points
				// and at = mean of this attribute for all points
				// L = logistic squashing function

				int numPointsSelected = spModel.numPointsSelected();
				double[] relativeMeanForSelected = null;

				//
				if (spModel.showAxes() && (numPointsSelected > 0))
					relativeMeanForSelected = spModel.getAttributeMeans();

				// draw series lines;
				if (spModel.showSeries() && (spModel.getSeries() != null)) {
					drawSeries(g2, markerRadius);
				}

				// draw the edges
				if (spModel.showGraph()) {
					ArrayList<Connection> allConnections = spModel.getGraph()
							.getAllConnections();
										
//					double[] k = null;
//					double curveFactor = 1.0;
//					
//					if (spModel.bundledEdges() || spModel.intelligentEdges()) {
//						spModel.calculateCentroids();
//
//						centroids = spModel.getCentroids();
//						// magnitudes = spModel.getMagnitudes();
//						vectors = spModel.getVectors();
//
//						k = new double[(int) Math.pow(centroids.size(), 2)];
//						for (double d : k) {
//							d = 0.0; // initialise entries in the array.
//						}
//						System.out.println("k in paint view is: "
//								+ k.toString());
//
//						curveFactor = (allConnections.size() / spModel.instances
//								.numInstances()) * 100;
//
//						if (spModel.intelligentEdges()) {
//							spModel.calculateCentroidRadius();
//							spModel.calculateBezierCentroidMidPoints();
//							centroidRadii = spModel.getCentroidRadii();
//							bezierMidPoints = spModel.getBezierMidPoints();
//						}
					}

					DrawEdge edge = new DrawEdge(this, spModel);
					for (int i = 0; i < allConnections.size(); i++) {
						edge.drawEdge(allConnections.get(i));
					}
//						Connection e = allConnections.get(i);
//
//						if (spModel.filterEdgesByWeight()) {
//							System.out.println("lower: "
//									+ spModel.getLowerEdgeWeightRange() + " : "
//									+ e.getEdgeWeight());
//							System.out.println("upper: "
//									+ spModel.getUpperEdgeWeightRange() + " : "
//									+ e.getEdgeWeight());
//							if ((e.getEdgeWeight() >= spModel
//									.getLowerEdgeWeightRange())
//									&& (e.getEdgeWeight() <= spModel
//											.getUpperEdgeWeightRange()))
//								drawEdge(e, g2, markerRadius, transparency,
//										markerRange, markerMin, k, curveFactor,
//										strokeWidth.getLineWidth());
//						} else
//							drawEdge(e, g2, markerRadius, transparency,
//									markerRange, markerMin, k, curveFactor,
//									strokeWidth.getLineWidth());
//					}
				}

				// draw clustering
				if (spModel.showHierarchicalClustering())
					drawClustering(g2);

				// draw the target
				if (spModel.showTarget())
					drawTarget(g2, markerRadius);

				// draw the points so that selected points appear on top
				// long startTimeA = System.currentTimeMillis();

				g2.setStroke(new BasicStroke(
						(float) (LINE_WIDTH * 2 / transform.getScaleX())));

				if (spModel.showGraph()) {
					ArrayList<Integer> selectedPoints = new ArrayList<Integer>();
					ArrayList<Integer> linkedPoints = new ArrayList<Integer>();
					ArrayList<Integer> backgroundPoints = new ArrayList<Integer>();
					for (int i = 0; i < spModel.getNumDataPoints(); i++) {
						if (spModel.isPointSelected(i))
							selectedPoints.add(i);
						else if (spModel.neighbourSelected(i)
								|| spModel.isPointHovered(i))
							linkedPoints.add(i);
						else
							backgroundPoints.add(i);
					}
					ArrayList<Integer> allPoints = new ArrayList<Integer>();
					allPoints.addAll(backgroundPoints);
					allPoints.addAll(linkedPoints);
					allPoints.addAll(selectedPoints);

					System.out.println("Drawing points with a graph");

					for (int i : allPoints) {
						drawPoint(g2, transform, markerRange, markerMin,
								markerRadius, i, transparency);
					}

				} else {
					System.out.println("Drawing points without graph");
					for (int i = 0; i < spModel.getNumDataPoints(); i++) {
						drawPoint(g2, transform, markerRange, markerMin,
								markerRadius, i, transparency);
					}
				}
				// long endTimeA = System.currentTimeMillis();
				// System.out.println("Total elapsed time in execution of drawing points is :"+
				// (endTimeA-startTimeA));

				// add labels to the points on the graph
				if (spModel.labels())
					drawLabels(g2, transform, markerRadius, labelSize);

				// plot the axes or just the origin
				if (spModel.showAxes())
					drawAxes(g2, markerRadius, numPointsSelected,
							relativeMeanForSelected);
				else {
					double originSize = origin / transform.getScaleX();
					g2.setColor(spModel.getColours().getAxesColor());
					g2.draw(new Line2D.Double(-originSize, 0, originSize, 0));
					g2.draw(new Line2D.Double(0, -originSize, 0, originSize));
				}

				// draw the rectangle?
				if (spModel.rectangle != null)
					spModel.rectangle.draw(g2);

				// restore original transform
				if (saveAT != null)
					g2.setTransform(saveAT);

				if (showNoise)
					updateNoise();
			}
		}
	}

	private void drawPoint(Graphics2D g2, AffineTransform transform,
			double markerRange, double markerMin, double markerRadius, int i,
			int transparency) {
		double x;
		double y;
		double size;
		Shape marker;
		Color c;
		{
			// first set the colour
			c = pointColor(g2, i, transparency);
			// if the graph isn't loaded or no points are selected
			// or the selected points leave colors as they are
			if (!spModel.showGraph()
					|| (spModel.isPointSelected(i) || !spModel
							.arePointsSelected()))
				g2.setColor(c);
			// if a neighbour is selected or a point is being hovered over
			// do something to indicate this (what? - making colours brighter
			// etc doesn't really work
			// for now keep it the same color and update the stroke to give the
			// selected effect
			else if (spModel.neighbourSelected(i) || spModel.isPointHovered(i))
				// g2.setColor(c.brighter());
				g2.setColor(c);
			// if a point is neither, selected, a neighbour or hovered then make
			// it transparent
			else
				g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
						transparency));

			// Size of the marker depends on size attribute
			if (spModel.getSizeAttribute() == null
					&& spModel.getDegree() == null) {
				size = Math.sqrt(markerRadius / Math.PI);
			} else if (spModel.getDegree() != null) {
				double area = (markerMin + markerRange
						* (spModel.getDegree()[i] - spModel.sizeAttributeLowerBound)
						/ (spModel.sizeAttributeUpperBound - spModel.sizeAttributeLowerBound))
						/ transform.getScaleX();
				size = Math.sqrt(area / Math.PI);
			} else {
				double area = (markerMin + markerRange
						* (spModel.getInstances().instance(i)
								.value(spModel.getSizeAttribute()) - spModel.sizeAttributeLowerBound)
						/ (spModel.sizeAttributeUpperBound - spModel.sizeAttributeLowerBound))
						/ transform.getScaleX();
				size = Math.sqrt(area / Math.PI);
			}

			// shape/fill of the marker depends on respective attributes
			x = spModel.getView().get(i, 0) + noise.get(i, 0);
			y = spModel.getView().get(i, 1) + noise.get(i, 1);

			if (spModel.getShapeAttribute() == null)
				marker = MarkerFactory.buildMarker(0, x, y, size);
			else {
				marker = MarkerFactory.buildMarker((int) spModel.instances
						.instance(i).value(spModel.getShapeAttribute()), x, y,
						size);
			}

			if (spModel.isPointSelected(i)) {
				g2.draw(new Line2D.Double(x - size, y, x + size, y));
				g2.draw(new Line2D.Double(x, y - size, x, y + size));
				if (spModel.graphLoaded()) {
					g2.fill(marker);

					if (spModel.getColours().getBackgroundColor() == Color.BLACK)
						g2.setColor(Color.WHITE);
					if (spModel.getColours().getBackgroundColor() == Color.WHITE)
						g2.setColor(Color.BLACK);
					g2.draw(marker);

				}
			} else {

				if (spModel.getFillAttribute() == null) {
					g2.fill(marker);
					// if (spModel.getColours().getBackgroundColor() ==
					// Color.BLACK) {
					// if (!spModel.showGraph()
					// || !spModel.arePointsSelected()
					// || (spModel.showGraph() && spModel
					// .neighbourSelected(i))
					// || (spModel.showGraph() && (spModel
					// .isPointSelected(i)))
					// || (spModel.showGraph() && (spModel
					// .isPointHovered(i))))
					// g2.setColor(Color.WHITE);
					// else
					// g2.setColor(new Color(255, 255, 255, transparency));
					//
					// } else if (spModel.getColours().getBackgroundColor() ==
					// Color.WHITE) {
					// if (!spModel.showGraph()
					// || !spModel.arePointsSelected()
					// || (spModel.showGraph() && spModel
					// .neighbourSelected(i))
					// || (spModel.showGraph() && (spModel
					// .isPointSelected(i)))
					// || (spModel.showGraph() && (spModel
					// .isPointHovered(i))))
					// g2.setColor(Color.BLACK);
					// else
					// g2.setColor(new Color(0, 0, 0, transparency));
					// }
					if (spModel.getColours().getBackgroundColor() == Color.BLACK) {
						if (!spModel.showGraph()
								|| !spModel.arePointsSelected()
								|| (spModel.showGraph() && (spModel
										.isPointSelected(i))))
							g2.setColor(Color.WHITE);
						else if ((spModel.showGraph() && spModel
								.neighbourSelected(i))
								|| (spModel.showGraph() && spModel
										.isPointHovered(i)))
							// g2.setColor(c.darker());
							g2.setColor(c.brighter());
						else
							g2.setColor(new Color(255, 255, 255, transparency));

					} else if (spModel.getColours().getBackgroundColor() == Color.WHITE) {
						if (!spModel.showGraph()
								|| !spModel.arePointsSelected()
								|| (spModel.showGraph() && (spModel
										.isPointSelected(i))))
							g2.setColor(Color.BLACK);
						else if ((spModel.showGraph() && spModel
								.neighbourSelected(i))
								|| (spModel.showGraph() && spModel
										.isPointHovered(i)))
							// g2.setColor(c.darker());
							g2.setColor(c.brighter());
						else
							g2.setColor(new Color(0, 0, 0, transparency));
					}
					g2.draw(marker);
				} else {
					switch ((int) spModel.instances.instance(i).value(
							spModel.getFillAttribute())) {
					case 0: {
						g2.fill(marker);
						break;
					}
					case 1: {
						g2.draw(marker);
						break;
					}
					default: {
						// TODO add more textures for filling points (shaded
						// lines etc)
						g2.draw(marker);
					}
					}
				}
			}
		}
	}

	private void drawLabels(Graphics2D g2, AffineTransform transform,
			double markerRadius, double labelSize) {
		double x;
		double y;
		int i;
		Color c;
		{

			for (i = 0; i < spModel.getNumDataPoints(); i++) {

				// Get the coordinates of the node that we want to label
				x = spModel.getView().get(i, 0) + noise.get(i, 0);
				y = spModel.getView().get(i, 1) + noise.get(i, 1);

				// pick the colour of the label
				c = spModel.getColours().getForegroundColor();
				Color b = spModel.getColours().getBackgroundColor();
				if (spModel.nodeLabelColor()) {
					c = pointColor(g2, i, 255);
				}

				// and what it will say
				String nodeLabel = "";
				if (!spModel.getDescriptionOfInstanceOnly(i).equals(""))
					nodeLabel += spModel.getDescriptionOfInstanceOnly(i);
				else
					nodeLabel += i + 1;

				// save the current transform
				AffineTransform saveXform = g2.getTransform();

				TextLayout textTl = new TextLayout(nodeLabel, new Font(
						"SansSerif", Font.PLAIN, 1), new FontRenderContext(
						transform, false, false));

				AffineTransform nodePosition = new AffineTransform();
				nodePosition.concatenate(transform);
				nodePosition.scale(labelSize, labelSize);
				g2.setTransform(nodePosition);

				Rectangle2D r = textTl.getBounds();
				r.setRect(
						r.getX() + (x / labelSize - (r.getWidth() / 2)),
						r.getY()
								+ (y / labelSize + 2 * markerRadius / labelSize + r
										.getHeight()), r.getWidth(),
						r.getHeight());

				if (spModel.showGraph()) {
					if ((spModel.highlightedLabels() && (spModel
							.isPointSelected(i) || spModel.neighbourSelected(i)))
							|| (spModel.hoverLabels() && spModel
									.isPointHovered(i))
							|| (spModel.selectedLabels() && spModel
									.isPointSelected(i))) {
						g2.setColor(b);
						g2.fill(r);
						g2.setColor(c);
					} else
						g2.setColor(new Color(c.getRed(), c.getGreen(), c
								.getBlue(), 0));

					if (!spModel.highlightedLabels() && !spModel.hoverLabels()
							&& !spModel.selectedLabels()) {
						g2.setColor(b);
						g2.fill(r);
						g2.setColor(c);
					}
				} else {
					g2.setColor(b);
					g2.fill(r);
					g2.setColor(c);
				}

				textTl.draw(
						g2,
						(float) (x / labelSize - (r.getWidth() / 2)),
						(float) (y / labelSize + 2 * markerRadius / labelSize + r
								.getHeight()));
				g2.setTransform(saveXform);
			}
		}
	}

	private void drawAxes(Graphics2D g2, double markerRadius,
			int numPointsSelected, double[] relativeMeanForSelected) {
		int i;
		{
			for (i = 0; i < spModel.getNumDataDimensions(); i++) {

				// If there are any point(s) selected then color the axes by
				// their (average) weight with the selected point(s)
				if (numPointsSelected > 0)
					g2.setColor(spModel.getColours().getColorFromSpectrum(
							relativeMeanForSelected[i], 0, 1));
				// otherwise highlight the axis if it is selected
				else
					g2.setColor((spModel.isAxisSelected(i) ? spModel
							.getColours().getForegroundColor() : spModel
							.getColours().getAxesColor()));
				g2.draw(new Line2D.Double(0, 0, spModel.getProjection().get(i,
						0), spModel.getProjection().get(i, 1)));
				if (spModel.isAxisSelected(i)) {
					g2.fill(new Ellipse2D.Double(spModel.getProjection().get(i,
							0)
							- markerRadius, spModel.getProjection().get(i, 1)
							- markerRadius, markerRadius * 2, markerRadius * 2));
				}
			}
		}
	}

	private void drawTarget(Graphics2D g2, double markerRadius) {
		double x;
		double y;
		Shape circle;
		int i;
		{
			g2.setColor(spModel.getColours().getAxesColor());
			for (i = 0; i < spModel.getNumDataPoints(); i++) {
				x = spModel.getTarget().get(i, 0);
				y = spModel.getTarget().get(i, 1);
				circle = new Ellipse2D.Double(x - markerRadius, y
						- markerRadius, markerRadius * 2, markerRadius * 2);
				g2.draw(circle);
			}
		}
	}

	private void drawClustering(Graphics2D g2) {
		// recursively draw lines between the centroids of each cluster
		// nb this assumes the this is a binary HC -- ie that each
		// cluster contains two members
		g2.setColor(spModel.getColours().getAxesColor());
		HierarchicalCluster cluster = spModel.getHierarchicalCluster();
		drawClusterArc(cluster, g2);
	}

	private void drawEdge(Connection cnxn, Graphics2D g2, double markerRadius,
			int transparency, double markerRange, double markerMin, double[] k,
			double curveFactor, float lineWidth) {
		double x1;
		double y1;
		double x2;
		double y2;
		CubicCurve2D beizerLine;
		Line2D line;
		int i;
		int j;
		Color c;
		{
			Instance source = cnxn.getSourceInstance();
			Instance target = cnxn.getTargetInstance();
			double edgeWeight = cnxn.getEdgeWeight();

			if (source != null && target != null) {
				// i = spModel.indexOf(source);
				i = cnxn.getSourceIndex();

				x1 = spModel.getView().get(i, 0) + noise.get(i, 0);
				y1 = spModel.getView().get(i, 1) + noise.get(i, 1);

				// j = spModel.indexOf(target);
				j = cnxn.getTargetIndex();
				// System.out.print("Target is " + j);
				x2 = spModel.getView().get(j, 0) + noise.get(j, 0);
				y2 = spModel.getView().get(j, 1) + noise.get(j, 1);

				if (!spModel.filterAllEdges()
						|| (spModel.filterAllEdges() && (spModel
								.isPointSelected(i) || spModel
								.isPointSelected(j)))) {

					// Color the edges of the graph
					if (spModel.sourceColorEdges())
						c = setColor(g2, i);
					else if (spModel.targetColorEdges())
						c = setColor(g2, j);
					else if (spModel.mixedColorEdges())
						c = addColors(setColor(g2, i), setColor(g2, j));
					else if (spModel.defaultColorEdges())
						c = spModel.getColours().getGraphColor();
					else
						c = spModel.getColours().getGraphColor();

					evaluateEdgeColorOptions(g2, i, j, transparency, c);

					// TODO if edge weights are added to the model reflect in
					// strokewidth
					if (spModel.viewEdgeWeights())
						g2.setStroke(new BasicStroke(
								(float) (lineWidth * edgeWeight)));

					if (spModel.bezierEdges()) {
						// Code from gephi
						// TODO Add proper attribution
						// (preview.plugin.renderers.edgerender.java

						drawBezierEdge(g2, x1, y1, x2, y2);

					} else if (spModel.bundledEdges()) {

						drawBundledEdges(i, j, x1, y1, x2, y2, g2, k,
								curveFactor);

					} else if (spModel.fannedEdges()) {

						drawFannedEdges(i, j, x1, y1, x2, y2, g2);

					} else if (spModel.intelligentEdges()) {
						double radius = 0.5;
						int w = 0;
						for (double d : centroidRadii) {
							Point2D p = centroids.get(w);
							double q = centroidRadii[w];

							Ellipse2D.Double e = new Ellipse2D.Double(
									p.getX() - 2.5, p.getY() - 2.5, 5, 5);
							// g2.setColor(Color.black);
							g2.draw(e);
							w++;
						}

						drawIntelligentBundledEdges(i, j, x1, y1, x2, y2, g2,
								k, curveFactor);

					} else {
						line = new Line2D.Double(x1, y1, x2, y2);
						g2.draw(line);

						if (spModel.arrowedEdges()) {
							AffineTransform transform = spModel.getTransform();
							double size;
							if (spModel.getSizeAttribute() == null
									&& spModel.getDegree() == null) {
								size = Math.sqrt(markerRadius / Math.PI);
							} else if (spModel.getDegree() != null) {
								double area = (markerMin + markerRange
										* (spModel.getDegree()[j] - spModel.sizeAttributeLowerBound)
										/ (spModel.sizeAttributeUpperBound - spModel.sizeAttributeLowerBound))
										/ transform.getScaleX();
								size = Math.sqrt(area / Math.PI);
							} else {
								double area = (markerMin + markerRange
										* (spModel
												.getInstances()
												.instance(j)
												.value(spModel
														.getSizeAttribute()) - spModel.sizeAttributeLowerBound)
										/ (spModel.sizeAttributeUpperBound - spModel.sizeAttributeLowerBound))
										/ transform.getScaleX();
								size = Math.sqrt(area / Math.PI);
							}
							g2.fill(MarkerFactory.buildArrowHead(line, size,
									true));
						}
					}
				}
			}
		}
	}

	private Color pointColor(Graphics2D g2, int i, int transparency) {
		Color c;
		if (!spModel.showGraph() || !spModel.arePointsSelected()) {
			c = setColor(g2, i);
			g2.setColor(c);
		} else {
			if (spModel.arePointsSelected())
				c = setColor(g2, i);
			else
				c = spModel.getColours().getGraphColor();
		}
		return c;
	}

	/**
	 * Set the colours depending on if they are chosen to be viewed by class or
	 * numeric
	 * 
	 * @param g2
	 *            the graphics context
	 * @param i
	 *            the node the colour applies to
	 * @return
	 */
	private Color setColor(Graphics2D g2, int i) {
		Color c = null;
		if (spModel.getColourAttribute() == null) {
			c = spModel.getColours().getForegroundColor();
		} else if (spModel.getColourAttribute().isNominal()) {
			c = spModel.getColours().getClassificationColor(
					(int) spModel.getInstances().instance(i)
							.value(spModel.getColourAttribute()));
		} else if (spModel.getColourAttribute().isNumeric()) {
			c = spModel.getColours().getColorFromSpectrum(
					spModel.getInstances().instance(i)
							.value(spModel.getColourAttribute()),
					spModel.colorAttributeLowerBound,
					spModel.colorAttributeUpperBound);
		}
		return c;
	}

	private void drawSeries(Graphics2D g2, double markerRadius) {
		double x1;
		double y1;
		double x2;
		double y2;
		Line2D line;
		int i;
		g2.setColor(spModel.getColours().getAxesColor());
		Iterator<TreeSet<Instance>> allSeries = spModel.getSeries()
				.getAllSeries().values().iterator();
		Iterator<Instance> nextSeries;
		// for all the series
		while (allSeries.hasNext()) {
			nextSeries = allSeries.next().iterator();
			if (nextSeries.hasNext()) {

				// find the start point
				i = spModel.indexOf(nextSeries.next());
				x1 = spModel.getView().get(i, 0) + noise.get(i, 0);
				y1 = spModel.getView().get(i, 1) + noise.get(i, 1);
				while (nextSeries.hasNext()) {

					// and draw a line and arrow head to the next point
					i = spModel.indexOf(nextSeries.next());
					x2 = spModel.getView().get(i, 0) + noise.get(i, 0);
					y2 = spModel.getView().get(i, 1) + noise.get(i, 1);
					line = new Line2D.Double(x1, y1, x2, y2);
					g2.draw(line);
					g2.fill(MarkerFactory.buildArrowHead(line, markerRadius,
							true));

					x1 = x2;
					y1 = y2;
				}
			}
		}
	}

	/**
	 * 
	 * @param g2
	 *            The Graphics2D environment being used
	 * @param i
	 *            the source node
	 * @param j
	 *            the target node
	 * @param transparency
	 *            how transparent non-selected nodes should be
	 * @param c
	 *            the colour of the edge
	 */
	private void evaluateEdgeColorOptions(Graphics2D g2, int i, int j,
			int transparency, Color c) {

		if ((spModel.isPointSelected(i) && spModel.isPointSelected(j)))
			g2.setColor(c);
		else if ((spModel.isPointSelected(i) || spModel.isPointSelected(j))
				&& (spModel.incomingEdges() && spModel.outgoingEdges()))
			g2.setColor(c);
		else if (spModel.isPointSelected(i)
				&& (spModel.incomingEdges() && !spModel.outgoingEdges()))
			g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
					transparency));
		else if (spModel.isPointSelected(i)
				&& (!spModel.incomingEdges() && spModel.outgoingEdges()))
			g2.setColor(c);
		else if (spModel.isPointSelected(j)
				&& (spModel.incomingEdges() && !spModel.outgoingEdges()))
			g2.setColor(c);
		else if (spModel.isPointSelected(j)
				&& (!spModel.incomingEdges() && spModel.outgoingEdges()))
			g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
					transparency));
		else
			g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
					transparency));
	}

	/**
	 * If the edge should be a mixture of source and target node colour add them
	 * together
	 * 
	 * @param c1
	 *            source node colour
	 * @param c2
	 *            target node colour
	 * @return mixed colour
	 */
	private Color addColors(Color c1, Color c2) {
		int a1 = c1.getAlpha();
		int r1 = c1.getRed();
		int g1 = c1.getGreen();
		int b1 = c1.getBlue();
		int a2 = c2.getAlpha();
		int r2 = c2.getRed();
		int g2 = c2.getGreen();
		int b2 = c2.getBlue();
		int ax = (a1 + a2) / 2;
		int rx = (r1 + r2) / 2;
		int gx = (g1 + g2) / 2;
		int bx = (b1 + b2) / 2;
		return new Color(rx, gx, bx, ax);
	}

	/**
	 * Recursively draw an arc between the centroids of the members of this
	 * (binary) cluster
	 * 
	 * @param g2
	 */
	private void drawClusterArc(HierarchicalCluster cluster, Graphics2D g2) {

		// if this cluster just contains another cluster then draw that
		if (cluster.size() == 1
				&& cluster.get(0) instanceof HierarchicalCluster)
			drawClusterArc((HierarchicalCluster) cluster.get(0), g2);

		// if this cluster contains two subclusters then draw arc between their
		// centroids
		if (cluster.size() == 2) {
			HierarchicalCluster c0, c1;
			Matrix p0, p1;
			c0 = (HierarchicalCluster) cluster.get(0);
			c1 = (HierarchicalCluster) cluster.get(1);
			p0 = spModel.projection.project(c0.getCentroid());
			p1 = spModel.projection.project(c1.getCentroid());
			Double line = new Line2D.Double(p0.get(0, 0), p0.get(0, 1), p1.get(
					0, 0), p1.get(0, 1));
			g2.draw(line);
			drawClusterArc(c0, g2);
			drawClusterArc(c1, g2);
		}

	}

	/** Whether to add noise to the current view */
	public void addNoise(boolean showNoise) {
		this.showNoise = showNoise;
		if (showNoise) {
			updateNoise();
		} else
			// reset noise to null
			noise = new Matrix(spModel.getNumDataPoints(),
					spModel.getNumDataDimensions());

	}

	/** Change the noise */
	private void updateNoise() {

		double scale = spModel.getTransform().getScaleX();
		Random ran = new Random();
		for (int i = 0; i < noise.getRowDimension(); i++)
			for (int j = 0; j < noise.getColumnDimension(); j++)
				noise.set(i, j, (ran.nextDouble() - 0.5d) * 20d / scale);

	}

	public void modelChanged(TPPModelEvent e) {
		repaint();
	}

	public void componentHidden(ComponentEvent e) {

	}

	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e) {
		spModel.resizePlot(getWidth(), getHeight());
	}

	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	class PanningHandler implements MouseListener, MouseMotionListener {
		int referenceX;
		int referenceY;

		public void mousePressed(MouseEvent e) {
			// capture the starting point
			if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
				referenceX = e.getX();
				referenceY = e.getY();
			}
		}

		public void mouseDragged(MouseEvent e) {

			if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
				// if(e.isShiftDown()) {

				// the size of the pan translations
				// are defined by the current mouse location subtracted
				// from the reference location
				int deltaX = e.getX() - referenceX;
				int deltaY = e.getY() - referenceY;

				// make the reference point be the new mouse point.
				referenceX = e.getX();
				referenceY = e.getY();

				canvas.translateX += deltaX;
				canvas.translateY += deltaY;

				// schedule a repaint.
				canvas.repaint();
				// }
			}
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseMoved(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

	class ScaleHandler implements MouseListener, MouseWheelListener {

		int zoomPercent = 100;

		private void resetZoom() {
			zoomPercent = 100;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub

			canvas.mouseX = e.getX();
			canvas.mouseY = e.getY();

			zoomPercent = zoomPercent + e.getWheelRotation();
			canvas.scale = Math.max(0.00001, zoomPercent / 100.0);
			canvas.repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.getButton() == MouseEvent.BUTTON3 && e.isShiftDown())
				zoomPercent = 100;

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}

	