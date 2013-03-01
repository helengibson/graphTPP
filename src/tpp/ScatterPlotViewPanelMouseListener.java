package tpp;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import javax.swing.SwingUtilities;

import tpp.ScatterPlotViewPanel.ScatterPlotViewPanelCanvas;

/**
 * Mouse Listener for handling all mouse events in a ScatterPlotViewPanel TODO
 * This mouse listener should know about the frame (or the controller that
 * replaces it, not the view panel, and all events should be passed through the
 * controller.
 */
class ScatterPlotViewPanelMouseListener implements MouseListener,
		MouseMotionListener {

	/** The current states of the rectangle, and the mouse */
	private int rectangleState, mouseState;

	private static final int RECTANGLE_NO_CURRENT_ACTION = 0;
	private static final int RECTANGLE_DRAWING_NEW = 1;
	// private static final int RECTANGLE_DRAWN = 2;
	private static final int RECTANGLE_SCALING = 3;
	private static final int RECTANGLE_TRANSLATING = 4;
	private static final String[] RECTANGLE_STATE_DESCRIPTIONS = new String[] {
			"no rectangle", "drawing new rectangle", "rectangle drawn",
			"scaling rectangle", "translating rectangle" };

	private static final int MOUSE_DEFAULT = 0;
	private static final int MOUSE_IN_RECTANGLE = 1;
	private static final int MOUSE_ON_RECTANGLE_CORNER = 2;
	private static final int MOUSE_ON_ORIGIN = 3;
	private static final int MOUSE_ON_POINT = 4;
	private static final int MOUSE_DRAWING_RECTANGLE = 5;
	private static final int MOUSE_ON_AXIS = 6;
	private static final String[] MOUSE_STATE_DESCRIPTIONS = new String[] {
			"default mouse", "mouse in rectangle", "mouse on rectangle corner",
			"mouse on origin", "mouse on point", "mouse drawing rectangle",
			"mouse on axis" };

	// The panel this listener is listening to
	private ScatterPlotViewPanelCanvas panel;

	// the location of the mouse cursor position in data space
	private Point2D.Double mousePosition;

	/**
	 * When dragging, record the start of the move in data space.
	 */
	private Point2D.Double startOfDrag;

	// The error margin in device space
	private static final int ERROR_MARGIN = 2;

	private int[] nearestPoints;

	private ScatterPlotModel model;

	private int[] nearestAxes;

	public ScatterPlotViewPanelMouseListener(ScatterPlotViewPanelCanvas panel,
			ScatterPlotModel tpp) {
		startOfDrag = null;
		this.panel = panel;
		this.model = tpp;
		setRectangleState(RECTANGLE_NO_CURRENT_ACTION);
		setMouseState(MOUSE_DEFAULT);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {

		try {
			mousePosition = getMousePosition(e);
		} catch (NoninvertibleTransformException e1) {
			//printErrorMsg();
			return;
		}

		// the error margin in data space
		double error = ERROR_MARGIN / model.getTransform().getScaleX();

		// is the mouse in a rectangle?
		if (model.rectangle != null
				&& model.rectangle.contains(mousePosition.x, mousePosition.y)) {
			setMouseState(MOUSE_IN_RECTANGLE);
			return;
		}

		// assume no points or axes
		nearestPoints = null;
		nearestAxes = null;

		// is the mouse on a point?
		nearestPoints = panel.findNearestPoints(mousePosition);
		if (nearestPoints.length > 0) {
			setMouseState(MOUSE_ON_POINT);
			return;
		}

		// is the mouse on axes
		nearestAxes = panel.findNearestAxes(mousePosition);
		if (nearestAxes.length > 0) {
			setMouseState(MOUSE_ON_AXIS);
			return;
		}

		// is the mouse on a rectangle corner?
		if (model.rectangle != null
				&& model.rectangle.onCorner(mousePosition.x, mousePosition.y,
						ERROR_MARGIN / model.getTransform().getScaleX())) {
			setMouseState(MOUSE_ON_RECTANGLE_CORNER);
			return;

		}

		// is the mouse on the origin?
		Point2D.Double origin = new Point2D.Double(0, 0);
		if (mousePosition.distance(origin) < error) {
			setMouseState(MOUSE_ON_ORIGIN);
			return;
		}

		// default
		setMouseState(MOUSE_DEFAULT);

	}

	private void setRectangleState(int newRectangleState) {
		rectangleState = newRectangleState;
	}

	/** Change mouse state. Also changes mouse cursor appearance. */
	private void setMouseState(int newMouseState) {
		switch (newMouseState) {
		case MOUSE_ON_RECTANGLE_CORNER:
			panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			break;
		case MOUSE_ON_ORIGIN:
			panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			break;
		case MOUSE_IN_RECTANGLE:
			panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			break;
		case MOUSE_DRAWING_RECTANGLE:
			panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			break;
		case MOUSE_ON_POINT:
			panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			panel.setToolTipText(model.getDescriptionOfInstance(nearestPoints));
			model.setHoverPoints(nearestPoints);
			break;
		case MOUSE_ON_AXIS:
			panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			panel.setToolTipText(model.getDescriptionOfAttributes(nearestAxes));
			break;
		case MOUSE_DEFAULT:
			panel.setCursor(Cursor.getDefaultCursor());
			panel.setToolTipText(null);
			if (model.arePointsHovered())
				model.removeHoverPoints();
			break;
		}
		mouseState = newMouseState;
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

		// find the location of the mouse cursor position in data space and
		// clone it in case this press turns into a drag
		try {
			mousePosition = getMousePosition(e);
			startOfDrag = (Double) mousePosition.clone();
		} catch (NoninvertibleTransformException e1) {
			printErrorMsg();
			return;
		}

		// if the middle button has been pressed then display information about
		// the local points
		if (e.getButton() == MouseEvent.BUTTON2) {
			// TODO this method should be able to display information for many
			// points -- or find a better way of selecting individual points
			// TODO show information using some kind of tool tip text area on
			// hover, rather than requiring the user to press a button,
			int[] points = panel.findNearestPoints(mousePosition);
			// presenter.displayPointInformation(points[0]);
		}

		// if the right button is pressed then rescale the view
		if ((e.getButton() == MouseEvent.BUTTON3) && e.isShiftDown() ) {
			//model.setTransform(panel.getTransform());
			panel.scale = 1;
			panel.translateX = 0;
			panel.translateY = 0;
			model.resizePlot();
		}

		// the left button has been pressed
		if (e.getButton() == MouseEvent.BUTTON1) {

			// if we are on some points then select those points
			if (mouseState == MOUSE_ON_POINT) {
				// if CTRL not pressed then start a new selection
				if (!e.isControlDown()) 
					model.unselectPoints();
				// if only CTRL is pressed then select points with no rectangle
				if (e.isControlDown())
					model.selectPoints(nearestPoints);
				// Additionally hold shift to select points with a rectangle
				if (e.isControlDown() && e.isShiftDown())
					model.drawRectangleAroundSelectedPoints();
				return;
			}

			// if we are on axes then select those axes
			if (mouseState == MOUSE_ON_AXIS) {
				// if CTRL not pressed then start a new selection
				if (!e.isControlDown())
					model.unselectAxes();
				model.selectAxes(nearestAxes);
				model.drawRectangleAroundSelectedAxes();
				return;
			}
		}

		// by pressing the mouse button at this point the user has decided
		// to translate the rectangle
		if (mouseState == MOUSE_IN_RECTANGLE) {
			setRectangleState(RECTANGLE_TRANSLATING);
			model.rectangle.initialiseListener(model);
			return;
		}

		// by pressing the mouse button at this point the user has decided
		// to scale the rectangle
		if (mouseState == MOUSE_ON_RECTANGLE_CORNER) {
			setRectangleState(RECTANGLE_SCALING);
			model.rectangle.initialiseListener(model);
			return;
		}

		// if mouse is not selecting anything
		if (mouseState == MOUSE_DEFAULT) {

			// unselect any axes
			model.unselectAxes();

			// if there is a rectangle then get rid of it
			if (model.rectangle != null) {
				model.rectangle = null;
				model.unselectPoints();
				setRectangleState(RECTANGLE_NO_CURRENT_ACTION);
				setMouseState(MOUSE_DEFAULT);
				return;
			}

			// If there isn't a rectangle then start a new one
			if (model.rectangle == null) {
				startOfDrag = (Double) mousePosition.clone();
				model.initRectangle(startOfDrag, startOfDrag);
				setRectangleState(RECTANGLE_DRAWING_NEW);
				setMouseState(MOUSE_DRAWING_RECTANGLE);
				return;
			}

		}
	}

	public void mouseDragged(MouseEvent e) {
		
		// Just want this to happen if its the left mouse button doing the dragging
		if(SwingUtilities.isLeftMouseButton(e)) {
			

			// System.out.println(this);
	
			// find the location of the mouse cursor position in data space
			try {
				mousePosition = getMousePosition(e);
			} catch (NoninvertibleTransformException e1) {
				printErrorMsg();
				return;
			}
	
			// moving axes by rectangle
			if (rectangleState == RECTANGLE_TRANSLATING && model.areAxesSelected()) {
				model.moveSelectedAxes(mousePosition.x - startOfDrag.x,
						mousePosition.y - startOfDrag.y);
			}
	
			// moving individual axes
			if (mouseState == MOUSE_ON_AXIS && model.areAxesSelected()) {
				model.moveSelectedAxes(mousePosition.x - startOfDrag.x,
						mousePosition.y - startOfDrag.y);
			}
	
			// moving points by rectangle
			if (rectangleState == RECTANGLE_SCALING && model.arePointsSelected())
				model.rectangle.scale(startOfDrag.x, startOfDrag.y, mousePosition.x
						- startOfDrag.x, mousePosition.y - startOfDrag.y);
	
			if (rectangleState == RECTANGLE_TRANSLATING
					&& model.arePointsSelected())
				model.rectangle.translate(mousePosition.x - startOfDrag.x,
						mousePosition.y - startOfDrag.y);
	
			// moving individual points
			// TODO this doesn't seem to work
			if (mouseState == MOUSE_ON_POINT && model.arePointsSelected())
				model.rectangle.translate(mousePosition.x - startOfDrag.x,
						mousePosition.y - startOfDrag.y);
	
			// new start of the drag
			startOfDrag = (Double) mousePosition.clone();
	
			// if we are drawing a new rectangle
			if (rectangleState == RECTANGLE_DRAWING_NEW) {
	
				// adjust rectangle
				model.rectangle.setX2(mousePosition.x);
				model.rectangle.setY2(mousePosition.y);
	
				// whether to add points or rectangles to the selection?
				if (model.arePointsSelected()) {
					model.selectPointsByRectangle();
					return;
				}
				if (model.areAxesSelected()) {
					model.selectAxesByRectangle();
					return;
				}
				// if we don't yet know, then see if anything has yet been
				// included in the rectangle
				model.selectPointsByRectangle();
				// any points yet?
				if (!model.arePointsSelected()) {
					// otherwise try seeing if we have grabbed any axes yet
					if (model.showAxes()) {
						model.selectAxesByRectangle();
					}
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		// System.out.println("Mouse Released in Scatter plot view panel");

		if (e.getButton() == MouseEvent.BUTTON1)
			setMouseState(MOUSE_DEFAULT);

	}

	/**
	 * Get position of the mouse event in data space, converting from device
	 * space
	 */
	private Double getMousePosition(MouseEvent e)
			throws NoninvertibleTransformException {
		try {
//			return (Double) model.getTransform().inverseTransform(
//					new Point2D.Double(e.getX(), e.getY()), null);
			return (Double) panel.getTransform().inverseTransform(
					new Point2D.Double(e.getX(), e.getY()), null);
			
			
		} catch (NullPointerException npe) {
			throw new NoninvertibleTransformException(npe.toString());
		}
	}

	private void printErrorMsg() {
		System.out.println("Cannot invert transform. Scale="
				+ model.getTransform().getScaleX() + ","
				+ model.getTransform().getScaleY() + " translation="
				+ model.getTransform().getTranslateX() + ","
				+ model.getTransform().getTranslateX());
	}

	public String toString() {
		String s = MOUSE_STATE_DESCRIPTIONS[mouseState] + " - "
				+ RECTANGLE_STATE_DESCRIPTIONS[rectangleState] + " - ";
		if (model.rectangle != null)
			s += model.rectangle.toString();
		return s;
	}

}
