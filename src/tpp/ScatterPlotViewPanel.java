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

/*
 * Created on 16-Feb-2006
 */

/**
 * Panel with a view of a set of instances that can be manipulated thru
 * projection pursuit
 * 
 * @author Joe Faith
 * updated for graphTPP by  
 * @author Helen Gibson
 *
 */
public class ScatterPlotViewPanel extends JPanel implements
		TPPModelEventListener, ComponentListener {

	protected ScatterPlotModel spModel = null;
	protected static final double LINE_WIDTH = 0.7;

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
			spModel.setNoise();
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
			at.translate(-mouseX, -mouseY);

			// The panning transformation
			at.translate(translateX, translateY);

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
			double margin = spModel.getPointModel().getMarkerSize() * getWidth()
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
			double margin = spModel.getPointModel().getMarkerSize() * getWidth()
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
			
			PointModel pointModel = spModel.getPointModel();
			GraphModel graphModel = spModel.getGraphModel();
			pointModel.setScaledMarkerSize(width);
			pointModel.setMinMarkerSize();
			pointModel.setMarkerRange();
			pointModel.setMarkerRadius(transform);
			
			double origin = pointModel.getScaledMarkerSize();

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

				// set the transparency level for when the graph is shown
				int transparency;
				if (spModel.arePointsSelected())
					transparency = spModel.getTransparencyLevel();
				else
					transparency = 255;
				
				spModel.setTransparency(transparency);

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


				if (spModel.showAxes() && (numPointsSelected > 0))
					relativeMeanForSelected = spModel.getAttributeMeans();
				
				// plot the axes or just the origin
				if (spModel.showAxes())
					drawAxes(g2, spModel.getPointModel().getMarkerRadius(), numPointsSelected,
							relativeMeanForSelected);
				else {
					double originSize = origin / transform.getScaleX();
					g2.setColor(spModel.getColours().getAxesColor());
					g2.draw(new Line2D.Double(-originSize, 0, originSize, 0));
					g2.draw(new Line2D.Double(0, -originSize, 0, originSize));
				}

				if (spModel.showGraph()) {
					ArrayList<Connection> allConnections = spModel.getGraph().getAllConnections();
					EdgeModel edgeModel = spModel.getEdgeModel();
					edgeModel.initialise();
					
					for (int i = 0; i < allConnections.size(); i++) {
						Connection cnxn = allConnections.get(i);
						Edge edge = new Edge(cnxn, edgeModel, spModel);
						edge.drawEdge(g2, strokeWidth.getLineWidth());
					}
					
//					double[][] clusters = edgeModel.getClusterEdgesDrawn();
//					if(clusters != null) {
//						for (int i = 0; i < clusters.length; i++) {
//							for (int j = 0; j < clusters.length; j++) {
//								System.out.print(clusters[i][j] + ", ");
//							}
//							System.out.print("\n");
//						}
//					}
						
				}
					
				
				// draw clustering
				if (spModel.showHierarchicalClustering())
					drawClustering(g2);
				
				// draw series lines;
				if (spModel.showSeries() && (spModel.getSeries() != null)) {
					drawSeries(g2, pointModel.getMarkerRadius());
				}

				// draw the target
				if (spModel.showTarget())
					drawTarget(g2, pointModel.getMarkerRadius());

				g2.setStroke(new BasicStroke((float) (LINE_WIDTH * 2 / transform.getScaleX())));

				// draw the points/nodes
				if (spModel.showGraph()) {
					
					ArrayList<Integer> allPoints = orderPoints(graphModel);
					Point[] newPoints = new Point[allPoints.size()];
					
					for (int p : allPoints) {
						Point point = new Point(spModel, pointModel, p);
						newPoints[p] = point;
						point.drawPoint(g2, transform);
					}
					
					if (pointModel.labels()) {
						for (int p : allPoints) {
							Point point = newPoints[p];
							// add labels to the points on the graph
							if(pointModel.filterLabels()){
								if ((graphModel.getLabelFilterDegree()[p] >= pointModel.getLowerLabelFilterDegreeBound())
										&& (graphModel.getLabelFilterDegree()[p] <= pointModel.getUpperLabelFilterDegreeBound()))
										point.drawLabel(g2, transform);
							} else
								point.drawLabel(g2, transform);
						}
					}
				} else {
					for (int p = 0; p < spModel.getNumDataPoints(); p++) {
						Point point = new Point(spModel, pointModel, p);
						point.drawPoint(g2, transform);
					}
				}

				// draw the rectangle?
				if (spModel.rectangle != null)
					spModel.rectangle.draw(g2);

				// restore original transform
				if (saveAT != null)
					g2.setTransform(saveAT);

				if (showNoise)
					spModel.updateNoise();
			}
		}
	}

		/**
		 * Orders the points so that those nodes that are selected and those nodes connected to
		 * those that are selected in the graph are drawn first. 
		 * @return  list of all points
		 */
	private ArrayList<Integer> orderPoints(GraphModel graphModel) {
			ArrayList<Integer> selectedPoints = new ArrayList<Integer>();
			ArrayList<Integer> linkedPoints = new ArrayList<Integer>();
			ArrayList<Integer> backgroundPoints = new ArrayList<Integer>();
			for (int i = 0; i < spModel.getNumDataPoints(); i++) {
				if (spModel.isPointSelected(i))
					selectedPoints.add(i);
				else if (graphModel.neighbourSelected(i)
						|| spModel.isPointHovered(i))
					linkedPoints.add(i);
				else
					backgroundPoints.add(i);
			}
			ArrayList<Integer> allPoints = new ArrayList<Integer>();
			allPoints.addAll(backgroundPoints);
			allPoints.addAll(linkedPoints);
			allPoints.addAll(selectedPoints);
			return allPoints;
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
	
	/** Whether to add noise to the current view */
	public void addNoise(boolean showNoise) {
		this.showNoise = showNoise;
		if (showNoise) {
			spModel.updateNoise();
		} else
			// reset noise to null
			spModel.setNoise();
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

	private void drawClustering(Graphics2D g2) {
		// recursively draw lines between the centroids of each cluster
		// nb this assumes the this is a binary HC -- ie that each
		// cluster contains two members
		g2.setColor(spModel.getColours().getAxesColor());
		HierarchicalCluster cluster = spModel.getHierarchicalCluster();
		drawClusterArc(cluster, g2);
	}

	private void drawSeries(Graphics2D g2, double markerRadius) {
		double x1;
		double y1;
		double x2;
		double y2;
		Line2D.Double line;
		int i;
		g2.setColor(spModel.getColours().getAxesColor());
		Iterator<TreeSet<Instance>> allSeries = spModel.getSeries()
				.getAllSeries().values().iterator();
		Iterator<Instance> nextSeries;
		Matrix noise = spModel.getNoise();
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

}
