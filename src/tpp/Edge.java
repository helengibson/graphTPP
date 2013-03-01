/**
 * 
 */
package tpp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import processing.core.PVector;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.matrix.Matrix;

/**
 * @author Helen
 *
 */
public class Edge {
	
	private ScatterPlotModel spModel;
	private EdgeModel edgeModel;
	private Connection cnxn;
	private Line2D arrowLine;

	public Edge(Connection cnxn, EdgeModel edgeModel, ScatterPlotModel spModel) {
		this.edgeModel = edgeModel;	
		this.cnxn = cnxn;
		this.spModel = spModel;
	}

	public void drawEdge(Graphics2D g, float lineWidth) {
		
		// If we're filtering the edges by weight then only draw those edges that fall within the weight range
		if (edgeModel.filterEdgesByWeight()) {
			if ((cnxn.getEdgeWeight() >= edgeModel.getLowerEdgeWeightRange())
					&& (cnxn.getEdgeWeight() <= edgeModel.getUpperEdgeWeightRange()))
				drawEdge(cnxn, g, lineWidth);
		} else
			drawEdge(cnxn, g, lineWidth);
	}
	
	private void drawEdge(Connection cnxn, Graphics2D g2, float lineWidth) {
		
		double x1,y1,x2,y2;
		Line2D line;
		int i, j;
		Color c;
		
		System.out.println(spModel.getTransparency());
		int transparency = spModel.getTransparency();
		double curveFactor = edgeModel.getCurveFactor();
		double[] clusterEdgesDrawn = edgeModel.getClusterEdgesDrawn();
		{
			Instance source = cnxn.getSourceInstance();
			Instance target = cnxn.getTargetInstance();
			double edgeWeight = cnxn.getEdgeWeight();
			
			Matrix noise = spModel.getNoise();

			if (source != null && target != null) {
				i = cnxn.getSourceIndex();

				x1 = spModel.getView().get(i, 0) + noise.get(i, 0);
				y1 = spModel.getView().get(i, 1) + noise.get(i, 1);

				j = cnxn.getTargetIndex();
				x2 = spModel.getView().get(j, 0) + noise.get(j, 0);
				y2 = spModel.getView().get(j, 1) + noise.get(j, 1);

				if (!edgeModel.filterAllEdges()	|| (edgeModel.filterAllEdges()
						&& (spModel.isPointSelected(i) || spModel.isPointSelected(j)))) {

					// Color the edges of the graph
					if (edgeModel.sourceColorEdges())
						c = spModel.setColor(i);
					else if (edgeModel.targetColorEdges())
						c = spModel.setColor(j);
					else if (edgeModel.mixedColorEdges())
						c = addColors(spModel.setColor(i), spModel.setColor(j));
					else if (edgeModel.defaultColorEdges())
						c = spModel.getColours().getGraphColor();
					else
						c = spModel.getColours().getGraphColor();

					evaluateEdgeColorOptions(g2, i, j, transparency, c);

					if (edgeModel.viewEdgeWeights())
						g2.setStroke(new BasicStroke((float) (lineWidth * edgeWeight)));

					// Code from gephi
					// TODO Add proper attribution(preview.plugin.renderers.edgerender.java)
					if (edgeModel.bezierEdges())
						drawBezierEdge(g2, x1, y1, x2, y2);
					else if (edgeModel.bundledEdges()) 
						drawBundledEdges(i, j, x1, y1, x2, y2, g2, clusterEdgesDrawn,curveFactor);
					else if (edgeModel.fannedEdges()) 
						drawFannedEdges(i, j, x1, y1, x2, y2, g2);
					else if (edgeModel.intelligentEdges())
						drawIntelligentBundledEdges(i, j, x1, y1, x2, y2, g2,
								clusterEdgesDrawn, curveFactor);
					else {
						line = new Line2D.Double(x1, y1, x2, y2);
						g2.draw(line);
						if (edgeModel.arrowedEdges()) {
							arrowLine = line;
						}
					}
				}
			}
		}
	}
	
	public Line2D getArrowLine() {
		return arrowLine;
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
				&& (edgeModel.incomingEdges() && edgeModel.outgoingEdges()))
			g2.setColor(c);
		else if (spModel.isPointSelected(i)
				&& (edgeModel.incomingEdges() && !edgeModel.outgoingEdges()))
			g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
					transparency));
		else if (spModel.isPointSelected(i)
				&& (!edgeModel.incomingEdges() && edgeModel.outgoingEdges()))
			g2.setColor(c);
		else if (spModel.isPointSelected(j)
				&& (edgeModel.incomingEdges() && !edgeModel.outgoingEdges()))
			g2.setColor(c);
		else if (spModel.isPointSelected(j)
				&& (!edgeModel.incomingEdges() && edgeModel.outgoingEdges()))
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
		
	public void drawBezierEdge(Graphics2D g2, double x1, double y1, double x2,
			double y2) {
		CubicCurve2D beizerLine;
		PVector direction = new PVector((float) x2, (float) y2);
		direction.sub(new PVector((float) x1, (float) y1));
		float length = direction.mag();
		direction.normalize();

		float factor = 0.2f * length;
		// float factor = spModel.getBeizerCurviness() * length;

		// normal vector to the edge
		PVector n = new PVector(direction.y, -direction.x);
		n.mult(factor);

		// first control point
		PVector v1 = new PVector(direction.x, direction.y);
		v1.mult(factor);
		v1.add(new PVector((float) x1, (float) y1));
		v1.add(n);

		// second control point
		PVector v2 = new PVector(direction.x, direction.y);
		v2.mult(-factor);
		v2.add(new PVector((float) x2, (float) y2));
		v2.add(n);

		beizerLine = new CubicCurve2D.Double(x1, y1, (double) v1.x,
				(double) v1.y, (double) v2.x, (double) v2.y, x2, y2);
		g2.draw(beizerLine);
	}

	private void drawFannedEdges(int i, int j, double x1, double y1, double x2,
			double y2, Graphics2D g2) {

		Attribute sepAtt = spModel.getSeparationAttribute();
		int c = (int) spModel.getInstances().instance(i).value(sepAtt);
		int d = (int) spModel.getInstances().instance(j).value(sepAtt);
		
		ArrayList<Point2D> centroids = edgeModel.getCentroids();
		Point2D sourceCentroid = centroids.get(c);
		Point2D targetCentroid = centroids.get(d);

		double scx = sourceCentroid.getX();
		double scy = sourceCentroid.getY();

		double tcx = targetCentroid.getX();
		double tcy = targetCentroid.getY();

		Point2D centroidMidPoint = new Point2D.Double((scx + tcx) / 2,
				(scy + tcy) / 2);

		double mpx = centroidMidPoint.getX();
		double mpy = centroidMidPoint.getY();

		// //////////////////////////////////////////////////////////
		// ------------------------------------------------------
		// Edges meet at the midpoint only, composed of two curves

		// if(c == d) {
		// drawBezierEdge(g2, x1, y1, x2, y2);
		// } else {

		PVector p1 = new PVector((float) mpx, (float) mpy);
		p1.sub(new PVector((float) x1, (float) y1));

		PVector absP1 = new PVector((float) Math.abs(p1.x),
				(float) Math.abs(p1.y));
		absP1.normalize();

		float lengthp1 = p1.mag();
		p1.normalize();

		float factorp1 = 0.2f * lengthp1;

		// normal vector to the edge
		PVector n = new PVector(absP1.y, -absP1.x);
		n.mult(factorp1);

		// first control point
		PVector c1p1 = new PVector(p1.x, p1.y);
		c1p1.mult(factorp1);
		c1p1.add(new PVector((float) x1, (float) y1));
		c1p1.add(n);

		// second control point
		PVector c2p1 = new PVector(p1.x, p1.y);
		c2p1.mult(-factorp1);
		c2p1.add(new PVector((float) mpx, (float) mpy));
		c2p1.add(n);

		PVector p4 = new PVector((float) mpx, (float) mpy); // second centroid
		// to target node
		p4.sub(new PVector((float) x2, (float) y2));

		PVector absP4 = new PVector((float) Math.abs(p4.x),
				(float) Math.abs(p4.y));
		absP4.normalize();

		float lengthp4 = p4.mag();
		p4.normalize();

		float factorp4 = 0.2f * lengthp4;

		// normal vector to the edge
		PVector n4 = new PVector(-absP4.y, absP4.x);
		n4.mult(factorp4);

		// first control point
		PVector c1p4 = new PVector(p4.x, p4.y);
		c1p4.mult(-factorp4);
		c1p4.add(new PVector((float) mpx, (float) mpy));
		c1p4.add(n4);

		// // second control point
		PVector c2p4 = new PVector(p4.x, p4.y);
		c2p4.mult(factorp4);
		c2p4.add(new PVector((float) x2, (float) y2));
		c2p4.add(n4);

		Path2D.Double path = new Path2D.Double();
		path.moveTo(x1, y1);
		path.curveTo(c1p1.x, c1p1.y, c2p1.x, c2p1.y, mpx, mpy);
		path.curveTo(c1p4.x, c1p4.y, c2p4.x, c2p4.y, x2, y2);
		g2.draw(path);

		// }
	}

	private void drawBundledEdges(int i, int j, double x1, double y1,
			double x2, double y2, Graphics2D g2, double[] k, double curveFactor) {

		Attribute sepAtt = spModel.getSeparationAttribute();
		 int numClasses = sepAtt.numValues();
//		int numClasses = spModel.getCurrentBundledAttribute().numValues();
		int c = (int) spModel.getInstances().instance(i).value(sepAtt);
		int d = (int) spModel.getInstances().instance(j).value(sepAtt);

		int arrayListPosition = d + (numClasses) * c;
		k[arrayListPosition] = k[arrayListPosition] + 1;

		ArrayList<Point2D> centroids = edgeModel.getCentroids();
		ArrayList<PVector> vectors = edgeModel.getVectors();
		
		PVector direction = vectors.get(arrayListPosition);
		direction.normalize();
		PVector normal = new PVector(direction.y, -direction.x);

		Point2D sourceCentroid = centroids.get(c);
		Point2D targetCentroid = centroids.get(d);

		double scx = sourceCentroid.getX() + k[arrayListPosition]
				* (normal.x / curveFactor);
		double scy = sourceCentroid.getY() - k[arrayListPosition]
				* (normal.y / curveFactor);

		double tcx = targetCentroid.getX() + k[arrayListPosition]
				* (normal.x / curveFactor);
		double tcy = targetCentroid.getY() - k[arrayListPosition]
				* (normal.y / curveFactor);

		// //////////////////////////////////////////
		// ----------------------------------------
		// Source node to source centroid to target centroid to target node
		// (i.e. misses out the midpoint.)
		//
		// //////////////////////////////////////////

		if (c == d) {
			drawBezierEdge(g2, x1, y1, x2, y2);
		} else {
			PVector p1 = new PVector((float) scx, (float) scy); // Source node
																// to source
																// centroid
			p1.sub(new PVector((float) x1, (float) y1));
			float lengthp1 = p1.mag();
			p1.normalize();

			float factorp1 = 0.05f * lengthp1;

			// normal vector to the edge
			PVector n = new PVector(p1.y, -p1.x);
			n.mult(factorp1);

			// first control point
			PVector c1p1 = new PVector(p1.x, p1.y);
			c1p1.mult(factorp1);
			c1p1.add(new PVector((float) x1, (float) y1));
			c1p1.add(n);

			// second control point
			PVector c2p1 = new PVector(p1.x, p1.y);
			c2p1.mult(-factorp1);
			c2p1.add(new PVector((float) scx, (float) scy));
			c2p1.add(n);

			// /////////////////////////////////////////

			PVector p2 = new PVector((float) tcx, (float) tcy); // first
																// centroid to
																// mid point of
																// centroids
			p2.sub(new PVector((float) scx, (float) scy));

			float lengthp2 = p2.mag();
			p2.normalize();

			// replace 1000 with calculated value: e.g nodes/edges * 200
			// float factorp2 = (0.01f + (float) (k[arrayListPosition] /
			// curveFactor))
			// * lengthp2;
			float factorp2 = 0.1f * lengthp2;

			// normal vector to the edge
			PVector n2 = new PVector(p2.y, -p2.x);
			n2.mult(factorp2);

			// first control point
			PVector c1p2 = new PVector(p2.x, p2.y);
			c1p2.mult(factorp2);
			c1p2.add(new PVector((float) scx, (float) scy));
			c1p2.add(n2);

			// second control point
			PVector c2p2 = new PVector(p2.x, p2.y);
			c2p2.mult(-factorp2);
			c2p2.add(new PVector((float) tcx, (float) tcy));
			c2p2.add(n2);

			// ///////////////////////////////////////////

			PVector p4 = new PVector((float) x2, (float) y2); // second centroid
																// to target
																// node
			p4.sub(new PVector((float) tcx, (float) tcy));

			float lengthp4 = p4.mag();
			p4.normalize();

			float factorp4 = 0.05f * lengthp4;

			// normal vector to the edge
			PVector n4 = new PVector(p4.y, -p4.x);
			n4.mult(factorp4);

			// first control point
			PVector c1p4 = new PVector(p4.x, p4.y);
			c1p4.mult(factorp4);
			c1p4.add(new PVector((float) tcx, (float) tcy));
			c1p4.add(n4);

			// second control point
			PVector c2p4 = new PVector(p4.x, p4.y);
			c2p4.mult(-factorp4);
			c2p4.add(new PVector((float) x2, (float) y2));
			c2p4.add(n4);

			Path2D.Double path = new Path2D.Double();
			path.moveTo(x1, y1);
			path.curveTo(c1p1.x, c1p1.y, c2p1.x, c2p1.y, scx, scy);
			path.curveTo(c1p2.x, c1p2.y, c2p2.x, c2p2.y, tcx, tcy);
			path.curveTo(c1p4.x, c1p4.y, c2p4.x, c2p4.y, x2, y2);
			g2.draw(path);
		}

		// System.out.println("bundled edge draw");
	}

	private void drawIntelligentBundledEdges(int i, int j, double x1,
			double y1, double x2, double y2, Graphics2D g2, double[] k,
			double curveFactor) {

		// Four routes:
		// 1. Both nodes are within the desired centroid radius - bundle is
		// drawn normally
		// 2. Source nodes in the radius, target node is not
		// 3. Source node is not in the radius, target node is
		// 4. Neither node is in the target radius

		// Solutions:
		// 1. Draw the bundled edge as usual
		// 2. At the midpoint bundled edge becomes a fan (or draw a direct
		// bezier edge)
		// 3. Up until the midpoint the edge is a fan then it becomes part of
		// the bundle (or draw a direct bezier edge)
		// 4. The edge never becomes part of the bundle but passes through it.
		// The whole edge
		// is a fan shape.

		// Options: how big should the radius be?
		// 100% = all points of that cluster are inside the radius
		// 0% = the radius is the exact point of the centroid.

		// When bundling don't change the curviness of the curve but increase
		// its slightly

		// double radius = 0.5;

		Attribute sepAtt = spModel.getSeparationAttribute();
		int numClasses = edgeModel.getCurrentBundledAttribute().numValues();
		int c = (int) spModel.getInstances().instance(i).value(sepAtt);
		int d = (int) spModel.getInstances().instance(j).value(sepAtt);

		int arrayListPosition = d + (numClasses) * c;
		k[arrayListPosition] = k[arrayListPosition] + 1;
		
		ArrayList<Point2D> centroids = edgeModel.getCentroids();
		ArrayList<PVector> vectors = edgeModel.getVectors();

		PVector direction = vectors.get(arrayListPosition);
		direction.normalize();
		PVector normal = new PVector(direction.y, -direction.x);

		Point2D sourceCentroid = centroids.get(c);
		Point2D targetCentroid = centroids.get(d);

		double scx = sourceCentroid.getX() + k[arrayListPosition]
				* (normal.x / curveFactor);
		double scy = sourceCentroid.getY() - k[arrayListPosition]
				* (normal.y / curveFactor);

		double tcx = targetCentroid.getX() + k[arrayListPosition]
				* (normal.x / curveFactor);
		double tcy = targetCentroid.getY() - k[arrayListPosition]
				* (normal.y / curveFactor);

		// For options 1 and 2 p1 will always be the vector from the source node
		// to the source centroid.
		// For option 3 and 4 it will be the same as p1 in the fanned edge
		// calculation

		// Compute vectors between source node and source centroid and the
		// magnitude
		PVector p1 = new PVector((float) scx, (float) scy); // Source node to
															// source centroid
		p1.sub(new PVector((float) x1, (float) y1));
		float lengthp1 = p1.mag();

		// Compute vectors between source node and source centroid and the
		// magnitude
		PVector p4 = new PVector((float) x2, (float) y2); // second centroid to
															// target node
		p4.sub(new PVector((float) tcx, (float) tcy));

		float lengthp4 = p4.mag();

		// find the midpoint of the bezier curve between the two centroids
		PVector p2 = new PVector((float) tcx, (float) tcy); // first centroid to
															// mid point of
															// centroids
		p2.sub(new PVector((float) scx, (float) scy));

		float lengthp2 = p2.mag();
		p2.normalize();

		float factorp2 = 0.1f * lengthp2;

		// normal vector to the edge
		PVector n2 = new PVector(p2.y, -p2.x);
		n2.mult(factorp2);

		// first control point
		PVector c1p2 = new PVector(p2.x, p2.y);
		c1p2.mult(factorp2);
		c1p2.add(new PVector((float) scx, (float) scy));
		c1p2.add(n2);

		// second control point
		PVector c2p2 = new PVector(p2.x, p2.y);
		c2p2.mult(-factorp2);
		c2p2.add(new PVector((float) tcx, (float) tcy));
		c2p2.add(n2);

		ArrayList<PVector> bezierMidPoints = edgeModel.getBezierMidPoints();
		float mpx = bezierMidPoints .get(arrayListPosition).x;
		float mpy = bezierMidPoints.get(arrayListPosition).y;

		double bmpx = (0.125 * (scx + tcx)) + (0.375 * (c1p2.x + c2p2.x));
		double bmpy = (0.125 * (scy + tcy)) + (0.375 * (c1p2.y + c2p2.y));

		if (c == d) {
			drawBezierEdge(g2, x1, y1, x2, y2);
		} else {
			if ((double) lengthp1 <= 2.5 && (double) lengthp4 <= 2.5) {
				drawBundledEdges(i, j, x1, y1, x2, y2, g2, k, curveFactor);
				// option 1
			} else if ((double) lengthp1 <= 2.5 && (double) lengthp4 > 2.5) {
				// option 2 source in target out

				PVector dist = new PVector((float) x2, (float) y2); // vector
																	// from
																	// source
																	// centroid
																	// to target
				dist.sub(new PVector((float) scx, (float) scy));
				float length_dist = dist.mag();

				if (length_dist < lengthp2) {
					drawBezierEdge(g2, x1, y1, x2, y2);
				} else {

					p1.normalize();

					float factorp1 = 0.05f * lengthp1;

					// normal vector to the edge
					PVector n = new PVector(p1.y, -p1.x);
					n.mult(factorp1);

					// first control point
					PVector c1p1 = new PVector(p1.x, p1.y);
					c1p1.mult(factorp1);
					c1p1.add(new PVector((float) x1, (float) y1));
					c1p1.add(n);

					// second control point
					PVector c2p1 = new PVector(p1.x, p1.y);
					c2p1.mult(-factorp1);
					c2p1.add(new PVector((float) scx, (float) scy));
					c2p1.add(n);

					// /////////////////////////////////////////

					// Using "de Casteljau's" algorithm to draw half the bezier
					// curve

					PVector c1 = new PVector((float) (scx + c1p2.x) / 2,
							(float) (scy + c1p2.y) / 2);
					PVector c2 = new PVector((float) (c1p2.x + c2p2.x) / 2,
							(float) (c1p2.y + c2p2.y) / 2);
					PVector c3 = new PVector((float) (c2p2.x + tcx) / 2,
							(float) (c2p2.y + tcx) / 2);

					PVector d1 = new PVector((c1.x + c2.x) / 2,
							(c1.y + c2.y) / 2);
					PVector d2 = new PVector((c2.x + c3.x) / 2,
							(c2.y + c3.y) / 2);

					PVector e1 = new PVector((d1.x + d2.x) / 2,
							(d1.y + d2.y) / 2);

					// /////////////////////////////////////

					p4 = new PVector((float) bmpx, (float) bmpy);
					// second centroid to target node
					p4.sub(new PVector((float) x2, (float) y2));

					// PVector absP4 = new PVector((float) Math.abs(p4.x),
					// (float) Math.abs(p4.y));
					// absP4.normalize();

					lengthp4 = p4.mag();
					p4.normalize();

					float factorp4 = 0.1f * lengthp4;

					// normal vector to the edge
					PVector n4 = new PVector(p4.y, p4.x);
					n4.mult(factorp4);

					// first control point
					PVector c1p4 = new PVector(p4.x, p4.y);
					c1p4.mult(-factorp4);
					c1p4.add(new PVector((float) bmpx, (float) bmpy));
					c1p4.add(n4);

					// second control point
					PVector c2p4 = new PVector(p4.x, p4.y);
					c2p4.mult(factorp4);
					c2p4.add(new PVector((float) x2, (float) y2));
					c2p4.add(n4);

					Path2D.Double path = new Path2D.Double();
					path.moveTo(x1, y1);
					path.curveTo(c1p1.x, c1p1.y, c2p1.x, c2p1.y, scx, scy);
					path.curveTo(c1.x, c1.y, d1.x, d1.y, bmpx, bmpy);
					path.curveTo(c1p4.x, c1p4.y, c2p4.x, c2p4.y, x2, y2);
					g2.draw(path);
				}

				// end of option 2

			} else if ((double) lengthp1 > 2.5 && (double) lengthp4 <= 2.5) {
				// option 3 - source out target in

				PVector dist = new PVector((float) x1, (float) y1); // vector
																	// from
																	// source
																	// centroid
																	// to target
				dist.sub(new PVector((float) tcx, (float) tcy));
				float length_dist = dist.mag();

				if (length_dist < lengthp2) {
					drawBezierEdge(g2, x1, y1, x2, y2);
				} else {

					p1 = new PVector((float) bmpx, (float) bmpy);

					p1.sub(new PVector((float) x1, (float) y1));

					// PVector absP1 = new PVector((float) Math.abs(p1.x),
					// (float) Math.abs(p1.y));
					// absP1.normalize();

					lengthp1 = p1.mag();
					p1.normalize();

					float factorp1 = 0.1f * lengthp1;

					// normal vector to the edge
					PVector n = new PVector(p1.y, -p1.x);
					n.mult(factorp1);

					// first control point
					PVector c1p1 = new PVector(p1.x, p1.y);
					c1p1.mult(factorp1);
					c1p1.add(new PVector((float) x1, (float) y1));
					c1p1.add(n);

					// second control point
					PVector c2p1 = new PVector(p1.x, p1.y);
					c2p1.mult(-factorp1);
					c2p1.add(new PVector((float) bmpx, (float) bmpy));
					c2p1.add(n);

					// /////////////////

					// Using "de Casteljau's" algorithm to draw half the bezier
					// curve

					PVector c1 = new PVector((float) (scx + c1p2.x) / 2,
							(float) (scy + c1p2.y) / 2);
					PVector c2 = new PVector((float) (c1p2.x + c2p2.x) / 2,
							(float) (c1p2.y + c2p2.y) / 2);
					PVector c3 = new PVector((float) (c2p2.x + tcx) / 2,
							(float) (c2p2.y + tcy) / 2);

					PVector d1 = new PVector((c1.x + c2.x) / 2,
							(c1.y + c2.y) / 2);
					PVector d2 = new PVector((c2.x + c3.x) / 2,
							(c2.y + c3.y) / 2);

					PVector e1 = new PVector((d1.x + d2.x) / 2,
							(d1.y + d2.y) / 2);

					// //////////

					p4.normalize();

					float factorp4 = 0.05f * lengthp4;

					// normal vector to the edge
					PVector n4 = new PVector(p4.y, -p4.x);
					n4.mult(factorp4);

					// first control point
					PVector c1p4 = new PVector(p4.x, p4.y);
					c1p4.mult(factorp4);
					c1p4.add(new PVector((float) tcx, (float) tcy));
					c1p4.add(n4);

					// second control point
					PVector c2p4 = new PVector(p4.x, p4.y);
					c2p4.mult(-factorp4);
					c2p4.add(new PVector((float) x2, (float) y2));
					c2p4.add(n4);

					// ////////////////

					Path2D.Double path = new Path2D.Double();
					path.moveTo(x1, y1);
					path.curveTo(c1p1.x, c1p1.y, c2p1.x, c2p1.y, bmpx, bmpy);
					path.curveTo(d2.x, d2.y, c3.x, c3.y, tcx, tcy);
					path.curveTo(c1p4.x, c1p4.y, c2p4.x, c2p4.y, x2, y2);
					g2.draw(path);
				}

			} else {

				p1 = new PVector((float) mpx, (float) mpy);
				p1.sub(new PVector((float) x1, (float) y1));

				PVector absP1 = new PVector((float) Math.abs(p1.x),
						(float) Math.abs(p1.y));
				absP1.normalize();

				lengthp1 = p1.mag();
				p1.normalize();

				float factorp1 = 0.1f * lengthp1;

				// normal vector to the edge
				PVector n = new PVector(absP1.y, -absP1.x);
				n.mult(factorp1);

				// first control point
				PVector c1p1 = new PVector(p1.x, p1.y);
				c1p1.mult(factorp1);
				c1p1.add(new PVector((float) x1, (float) y1));
				c1p1.add(n);

				// second control point
				PVector c2p1 = new PVector(p1.x, p1.y);
				c2p1.mult(-factorp1);
				c2p1.add(new PVector((float) mpx, (float) mpy));
				c2p1.add(n);

				p4 = new PVector((float) mpx, (float) mpy); // second centroid
				// to target node
				p4.sub(new PVector((float) x2, (float) y2));

				PVector absP4 = new PVector((float) Math.abs(p4.x),
						(float) Math.abs(p4.y));
				absP4.normalize();

				lengthp4 = p4.mag();
				p4.normalize();

				float factorp4 = 0.1f * lengthp4;

				// normal vector to the edge
				PVector n4 = new PVector(-absP4.y, absP4.x);
				n4.mult(factorp4);

				// first control point
				PVector c1p4 = new PVector(p4.x, p4.y);
				c1p4.mult(-factorp4);
				c1p4.add(new PVector((float) mpx, (float) mpy));
				c1p4.add(n4);

				// // second control point
				PVector c2p4 = new PVector(p4.x, p4.y);
				c2p4.mult(factorp4);
				c2p4.add(new PVector((float) x2, (float) y2));
				c2p4.add(n4);

				Path2D.Double path = new Path2D.Double();
				path.moveTo(x1, y1);
				path.curveTo(c1p1.x, c1p1.y, c2p1.x, c2p1.y, mpx, mpy);
				path.curveTo(c1p4.x, c1p4.y, c2p4.x, c2p4.y, x2, y2);
				g2.draw(path);
			}
		}
	}
}