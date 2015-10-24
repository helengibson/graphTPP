/*
Updated by Helen Gibson from original TPP version
*/

package tpp;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;

public class MarkerFactory {

	static final double ARROW_ANGLE = Math.PI / 12;

	/**
	 * Build a marker Shape object
	 * 
	 * @param type
	 *            0=circle, 1=square, 2=down-triangle, 3=up-triangle, 4=diamond 5=pentagon, 
	 *            6=5-pointed star, 7=6-pointed-star
	 * @param x
	 *            the center of the shape
	 * @param y
	 * @param size
	 *            teh size of the shape
	 * @return
	 */
	public static Shape buildMarker(int type, double x, double y, double size) {
		Shape marker;
		int shape = type % 8;
		switch (shape) {
		case 0: {
			// circle
			marker = new Ellipse2D.Double(x - size, y - size, size * 2, size * 2);
			return marker;
		}
		case 1: {
			// square
			marker = new Rectangle2D.Double(x - size, y - size, size * 2, size * 2);
			return marker;
		}
		case 2: {
			// down-triangle
			marker = new Path2D.Double();
			((Path2D.Double) marker).moveTo(x - size, y - size);
			((Path2D.Double) marker).lineTo(x + size, y - size);
			((Path2D.Double) marker).lineTo(x, y + size);
			((Path2D.Double) marker).closePath();
			return marker;
		}
		case 3: {
			// up-triangle
			marker = new Path2D.Double();
			((Path2D.Double) marker).moveTo(x - size, y + size);
			((Path2D.Double) marker).lineTo(x + size, y + size);
			((Path2D.Double) marker).lineTo(x, y - size);
			((Path2D.Double) marker).closePath();
			return marker;
		}
		case 4: {
			// diamond
			marker = new Path2D.Double();
			((Path2D.Double) marker).moveTo(x - size, y);
			((Path2D.Double) marker).lineTo(x, y + size);
			((Path2D.Double) marker).lineTo(x + size, y);
			((Path2D.Double) marker).lineTo(x, y - size);
			((Path2D.Double) marker).closePath();
			return marker;
		}
		case 5: {
			// pentagon
			marker = new Path2D.Double();
			double angle = Math.PI/2.5;
			((Path2D.Double) marker).moveTo(x, y - size);
			((Path2D.Double) marker).lineTo(x + size * Math.sin(angle), y - size * Math.cos(angle));
			((Path2D.Double) marker).lineTo(x + size * Math.sin(angle/2), y + size * Math.cos(angle/2));
			((Path2D.Double) marker).lineTo(x - size * Math.sin(angle/2), y + size * Math.cos(angle/2));
			((Path2D.Double) marker).lineTo(x - size * Math.sin(angle), y - size * Math.cos(angle));
			((Path2D.Double) marker).closePath();
			return marker;
		}
		case 6: {
			// 5 pointed star
			marker = new Path2D.Double();
			double angle = Math.PI/2.5;
			double inner_radius = 2*size/3;
			((Path2D.Double) marker).moveTo(x, y - size);
			((Path2D.Double) marker).lineTo(x + inner_radius * Math.sin(angle/2), y - inner_radius * Math.cos(angle/2));
			((Path2D.Double) marker).lineTo(x + size * Math.sin(angle), y - size * Math.cos(angle));
			((Path2D.Double) marker).lineTo(x + inner_radius * Math.sin(angle), y + inner_radius * Math.cos(angle));
			((Path2D.Double) marker).lineTo(x + size * Math.sin(angle/2), y + size * Math.cos(angle/2));
			((Path2D.Double) marker).lineTo(x, y + inner_radius);
			((Path2D.Double) marker).lineTo(x - size * Math.sin(angle/2), y + size * Math.cos(angle/2));
			((Path2D.Double) marker).lineTo(x - inner_radius * Math.sin(angle), y + inner_radius * Math.cos(angle));
			((Path2D.Double) marker).lineTo(x - size * Math.sin(angle), y - size * Math.cos(angle));
			((Path2D.Double) marker).lineTo(x - inner_radius * Math.sin(angle/2), y - inner_radius * Math.cos(angle/2));
			((Path2D.Double) marker).closePath();
			return marker;
		}
		
		case 7: {
			// 6 pointed star
			marker = new Path2D.Double();
			double angle = Math.PI/3;
			double inner_radius = 2*size/3;
			
			((Path2D.Double) marker).moveTo(x, y - size);
			((Path2D.Double) marker).lineTo(x + inner_radius * Math.sin(angle/2), y - inner_radius * Math.cos(angle/2));
			((Path2D.Double) marker).lineTo(x + size * Math.sin(angle), y - size * Math.cos(angle));
			
			((Path2D.Double) marker).lineTo(x + inner_radius, y);
			((Path2D.Double) marker).lineTo(x + size * Math.sin(angle), y + size * Math.cos(angle));
			((Path2D.Double) marker).lineTo(x + inner_radius * Math.sin(angle/2), y + inner_radius * Math.cos(angle/2));
			
			((Path2D.Double) marker).lineTo(x, y + size);
			((Path2D.Double) marker).lineTo(x - inner_radius * Math.sin(angle/2), y + inner_radius * Math.cos(angle/2));
			((Path2D.Double) marker).lineTo(x - size * Math.sin(angle), y + size * Math.cos(angle));
			
			((Path2D.Double) marker).lineTo(x - inner_radius, y);
			((Path2D.Double) marker).lineTo(x - size * Math.sin(angle), y - size * Math.cos(angle));
			((Path2D.Double) marker).lineTo(x - inner_radius * Math.sin(angle/2), y - inner_radius * Math.cos(angle/2));
			
			((Path2D.Double) marker).closePath();
			return marker;
		}
		
		default:
			marker = new Ellipse2D.Double(x - size, y - size, size * 2, size * 2);
			return marker;
		}
	}

	/**
	 * Build an arrow head for the given line
	 * 
	 * @param arrowLength
	 *            the length of the arrow head.
	 * @param indent if true then don't put the arrow head right at the end of the line but indent it by the length of a single arrow (to allow for markers) 
	 * @return
	 */
	public static Shape buildArrowHead(Line2D line, double arrowLength, boolean indent) {
		Shape head = new Path2D.Double();
		
		// the end of the line
		double x = line.getX2();
		double y = line.getY2();

		// The direction of the line (reversed, from end to start)
		double lineAngleR = Math.atan2(line.getY1() - y, line.getX1() - x);

		if (indent){
			// indent the arrow by one arrow length
			x=x + arrowLength * Math.cos(lineAngleR);
			y=y + arrowLength * Math.sin(lineAngleR);
		}
		
		((Path2D.Double) head).moveTo(x, y);
		((Path2D.Double) head).lineTo(x + arrowLength * Math.cos(lineAngleR + ARROW_ANGLE),
				y + arrowLength * Math.sin(lineAngleR + ARROW_ANGLE));
		((Path2D.Double) head).lineTo(x + arrowLength * Math.cos(lineAngleR - ARROW_ANGLE),
				y + arrowLength * Math.sin(lineAngleR - ARROW_ANGLE));
		((Path2D.Double) head).closePath();

		return head;
	}

}
