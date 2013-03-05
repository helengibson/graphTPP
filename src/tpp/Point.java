package tpp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import weka.core.matrix.Matrix;

/**
 * Controls the drawing of the points/nodes in the scatterplotviewpanel
 * @author Helen
 *
 */

public class Point {
	
	private ScatterPlotModel spModel;
	private PointModel pointModel;
	private GraphModel graphModel;
	private int point;
	private Color c;
	private double x;
	private double y;
	private double area;
	private double labelSize;
	
	public Point(ScatterPlotModel spModel, PointModel pointModel, int point){
		this.spModel = spModel;
		this.pointModel = pointModel;
		this.point = point;
		
		initialise();
	}
	
	private void initialise() {
		
		Matrix noise = spModel.getNoise();
		c = pointColor();
		x = spModel.getView().get(point, 0) + noise.get(point, 0);
		y = spModel.getView().get(point, 1) + noise.get(point, 1);
		graphModel = spModel.getGraphModel();
		
		double markerMin = pointModel.getMinMarkerSize();
		double markerRange = pointModel.getMarkerRange();
		
		// Size of the marker depends on size attribute
		if (pointModel.getSizeAttribute() == null && graphModel.getDegree() == null) {
			area = pointModel.getScaledMarkerSize();
		} else if (graphModel.getDegree() != null) {
			area = (markerMin + markerRange
					* (graphModel.getDegree()[point] - pointModel.sizeAttributeLowerBound)
					/ (pointModel.sizeAttributeUpperBound - pointModel.sizeAttributeLowerBound));
		} else {
			area = (markerMin + markerRange
					* (spModel.getInstances().instance(point)
							.value(pointModel.getSizeAttribute()) - pointModel.sizeAttributeLowerBound)
					/ (pointModel.sizeAttributeUpperBound - pointModel.sizeAttributeLowerBound));

		}
		
		labelSize = pointModel.getLabelSize();
	}
	
	public void drawPoint(Graphics2D g2, AffineTransform transform) {
		
		Shape marker;
		int transparency = spModel.getTransparency();
		{
			
			// if the graph isn't loaded or no points are selected
			// or the point is selected leave colors as they are
			if (!spModel.showGraph() || (spModel.isPointSelected(point) || !spModel.arePointsSelected()))
				g2.setColor(c);
			
			// if a neighbour is selected or a point is being hovered over
			// do something to indicate this 
			else if (graphModel.neighbourSelected(point) || spModel.isPointHovered(point))
				g2.setColor(c);
			
			// if a point is neither, selected, a neighbour or hovered then make
			// it transparent
			else
				g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
						transparency));

			// shape/fill of the marker depends on respective attributes
			double size = Math.sqrt((area/transform.getScaleX())/Math.PI);

			if (pointModel.getShapeAttribute() == null)
				marker = MarkerFactory.buildMarker(0, x, y, size);
			else {
				marker = MarkerFactory.buildMarker((int) spModel.instances
						.instance(point).value(pointModel.getShapeAttribute()), x, y,
						size);
			}

			if (spModel.isPointSelected(point)) {
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

				if (pointModel.getFillAttribute() == null) {
					g2.fill(marker);
					
					if (spModel.getColours().getBackgroundColor() == Color.BLACK) {
						if (!spModel.showGraph()|| !spModel.arePointsSelected()
								|| (spModel.showGraph() && (spModel.isPointSelected(point))))
							g2.setColor(Color.WHITE);
						
						else if ((spModel.showGraph() && graphModel.neighbourSelected(point))
								|| (spModel.showGraph() && spModel.isPointHovered(point)))
							g2.setColor(c.brighter());
						
						else
							g2.setColor(new Color(255, 255, 255, transparency));

					} else if (spModel.getColours().getBackgroundColor() == Color.WHITE) {
						if (!spModel.showGraph() || !spModel.arePointsSelected()
								|| (spModel.showGraph() && (spModel.isPointSelected(point))))
							g2.setColor(Color.BLACK);
						
						else if ((spModel.showGraph() && graphModel.neighbourSelected(point))
								|| (spModel.showGraph() && spModel.isPointHovered(point)))
							g2.setColor(c.brighter());
						
						else
							g2.setColor(new Color(0, 0, 0, transparency));
					}
					g2.draw(marker);
					
				} else {
					switch ((int) spModel.instances.instance(point).value(pointModel.getFillAttribute())) {
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
		
	private Color pointColor() {
		Color c;
		if (!spModel.showGraph() || !spModel.arePointsSelected()) {
			c = spModel.setColor(point);
//			g2.setColor(c);
		} else {
			if (spModel.arePointsSelected())
				c = spModel.setColor(point);
			else
				c = spModel.getColours().getGraphColor();
		}
		return c;
	}
	
	
	public void drawLabel(Graphics2D g2, AffineTransform transform) {

		// pick the colour of the label
		Color labelColor = spModel.getColours().getForegroundColor();
		Color b = spModel.getColours().getBackgroundColor();
		if (pointModel.nodeLabelColor()) {
			labelColor = pointColor();
		}

		// and what it will say
		String nodeLabel = "";
		if (!spModel.getDescriptionOfInstanceOnly(point).equals(""))
			nodeLabel += spModel.getDescriptionOfInstanceOnly(point);
		else
			nodeLabel += point + 1;

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
						+ (y / labelSize + 2 * pointModel.getMarkerRadius() / labelSize + r
								.getHeight()), r.getWidth(),
				r.getHeight());

		if (spModel.showGraph()) {
			if ((pointModel.highlightedLabels() && (spModel
					.isPointSelected(point) || graphModel.neighbourSelected(point)))
					|| (pointModel.hoverLabels() && spModel
							.isPointHovered(point))
					|| (pointModel.selectedLabels() && spModel
							.isPointSelected(point))) {
				g2.setColor(b);
				g2.fill(r);
				g2.setColor(labelColor);
			} else
				g2.setColor(new Color(labelColor.getRed(), labelColor.getGreen(), labelColor
						.getBlue(), 0));

			if (!pointModel.highlightedLabels() && !pointModel.hoverLabels()
					&& !pointModel.selectedLabels()) {
				g2.setColor(b);
				g2.fill(r);
				g2.setColor(labelColor);
			}
		} else {
			g2.setColor(b);
			g2.fill(r);
			g2.setColor(labelColor);
		}

		textTl.draw(
				g2,
				(float) (x / labelSize - (r.getWidth() / 2)),
				(float) (y / labelSize + 2 * pointModel.getMarkerRadius() / labelSize + r
						.getHeight()));
		g2.setTransform(saveXform);
	}

}
