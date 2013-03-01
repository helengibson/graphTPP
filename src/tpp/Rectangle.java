package tpp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// TODO make this a subclass (or decorator?) of Shape, to give more flexibility in the choice of selector
// TODO this should be a member of the ScatterPlotModel rather than the Panel

/**
 * A 'rubber band' rectangle used to select points in a ScatterPlotViewPanel.
 * All coordinates are in data (rather than device) space
 */
public class Rectangle {

	// The coordinates of the corners
	// x1,y1 is the first corner created and x2,y2 is the second
	private double x1 = 0, x2 = 0, y1 = 0, y2 = 0;

	private RectangleMovementListener listener;

	public double getX1() {
		return x1;
	}

	public void setX1(double x1) {
		this.x1 = x1;
	}

	public double getX2() {
		return x2;
	}

	public void setX2(double x2) {
		this.x2 = x2;
	}

	public double getY1() {
		return y1;
	}

	public void setY1(double y1) {
		this.y1 = y1;
	}

	public double getY2() {
		return y2;
	}

	public void setY2(double y2) {
		this.y2 = y2;
	}

	/** Expand rectangle as required to include a point. */
	public void expandToInclude(double x, double y) {
		if (x < min(x1, x2)) {
			if (x1 < x2)
				x1 = x;
			else
				x2 = x;
		}
		if (x > max(x1, x2)) {
			if (x1 > x2)
				x1 = x;
			else
				x2 = x;
		}
		if (y < min(y1, y2)) {
			if (y1 < y2)
				y1 = y;
			else
				y2 = y;
		}
		if (y > max(y1, y2)) {
			if (y1 > y2)
				y1 = y;
			else
				y2 = y;
		}
	}

	private double min(double a, double b) {
		return (a < b ? a : b);
	}

	private double max(double a, double b) {
		return (a > b ? a : b);
	}

	/** Translate the position of the rectangle by the given amount */
	public void translate(double dx, double dy) {
		x1 += dx;
		x2 += dx;
		y1 += dy;
		y2 += dy;
		if (listener != null)
			listener.rectangleTranslated(dx, dy);
	}

	/**
	 * Scale the rectangle, by moving a corner from its original location
	 * (ox,oy) by a displacement of (dx,dy).
	 */
	public void scale(double ox, double oy, double dx, double dy) {

		// if ox is bigger than cx (and dx>0) then we have a zoom in, otherwise
		// zoom out
		double zx = (ox > centerX() ? dx : -dx);
		double zy = (oy > centerY() ? dy : -dy);
		scale((double) zx, (double) zy);
	}

	/**
	 * Increase the size of the rectangle by zx in the x direction, zy in the y.
	 * (expand if zx,y>0, shrink if zx,zy<0)
	 */
	public void scale(double zx, double zy) {
		if (x1 > x2) {
			x1 += zx;
			x2 -= zx;
		} else {
			x1 -= zx;
			x2 += zx;
		}
		if (y1 > y2) {
			y1 += zy;
			y2 -= zy;
		} else {
			y1 -= zy;
			y2 += zy;
		}
		if (listener != null)
			listener.rectangleScaled(zx, zy);
	}

	/** The x coordinate of the center of the rectangle */
	public double centerX() {
		return (x1 + x2) / 2;
	}

	/** The y coordinate of the center of the rectangle */
	public double centerY() {
		return (y1 + y2) / 2;
	}

	/** Is (x,y) inside the rectangle? */
	public boolean contains(double x, double y) {
		return (x >= min(x1, x2) && x <= max(x1, x2) && y <= max(y1, y2) && y >= min(y1, y2));
	}

	public Rectangle(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}

	public Rectangle(Point2D p1, Point2D p2) {
		this.x1 = p1.getX();
		this.x2 = p2.getX();
		this.y1 = p1.getY();
		this.y2 = p2.getY();
	}

	public double width() {
		return abs(x1 - x2);
	}

	public double height() {
		return abs(y1 - y2);
	}

	private double abs(double i) {
		return (i > 0 ? i : -i);
	}

	public void draw(Graphics2D g) {
		double x, y, w, h;
		if (x1 < x2) {
			x = x1;
			w = x2 - x1;
		} else {
			x = x2;
			w = x1 - x2;
		}
		if (y1 < y2) {
			y = y1;
			h = y2 - y1;
		} else {
			y = y2;
			h = y1 - y2;
		}

		g.draw(new Rectangle2D.Double(x, y, w, h));
	}

	/** Is the given point on a corner, given the error margin. */
	public boolean onCorner(double x, double y, double margin) {
		return ((abs(x1 - x) < margin || abs(x2 - x) < margin) && (abs(y1 - y) < margin || abs(y2 - y) < margin));
	}

	public String toString() {
		return "(" + x1 + "," + y1 + ")-(" + x2 + "," + y2 + ")";
	}

	/**
	 * Create a listener that is capable of updating a model in response to
	 * changes to this rectangle. Until this method is called then changes to
	 * the rectangle will not have any effect on the projection
	 */
	public RectangleMovementListener initialiseListener(ScatterPlotModel model) {
		listener = new RectangleMovementListener(model);
		return listener;
	}
}
