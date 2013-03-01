package tpp;

/**
 * An event indicating that a TPPModel has been changed.
 */
public class TPPModelEvent {

	// TODO the event types that are specific to scatter plots should be moved
	// to a subclass of TPPModelEvent called ScatterPlotModelEvent
	// TODO probably don't even need most of these event types -- see what ones
	// actually get used in practice by the listeners

	private TPPModel model;
	private int type;

	/** The data set has been changed */
	public static final int DATA_SET_CHANGED = 0;

	/**
	 * The structure of the data set has changed (attributes or points have been
	 * added or removed)
	 */
	public static final int DATA_STRUCTURE_CHANGED = 1;

	/** The values of the data set have been changed */
	public static final int DATA_VALUE_CHANGED = 2;

	/** The values of the data set have been changed */
	public static final int PROJECTION_CHANGED = 3;

	/** The selection of axes have changed */
	public static final int AXIS_SELECTION_CHANGED = 4;

	/** The selection of points have changed */
	public static final int POINT_SELECTION_CHANGED = 5;

	/** The color scheme has changed */
	public static final int COLOR_SCHEME_CHANGED = 6;

	/** The retinal attributes used to draw the point markers has changed */
	public static final int RETINAL_ATTRIBUTE_CHANGED = 7;

	/** The position or state of selection rectangle has changed */
	public static final int RECTANGLE_CHANGED = 8;

	/**
	 * Other information used to decorate the visualisation (such as series
	 * lines or target markers) has changed
	 */
	public static final int DECORATION_CHANGED = 9;
	
	public static final int CONTROL_PANEL_UPDATE = 10;

	private static final String[] TYPE_DESCRIPTIONS = { "data set changed", "data structure changed",
			"data value changed", "projection changed", "axis selection changed", "point selection changed",
			"color scheme changed", "retinal attribute changed", "rectangle changed", "decoration changed",
			"control panel update"};

	public TPPModelEvent(TPPModel model, int type) {
		this.model = model;
		this.type = type;
	}

	public TPPModel getSource() {
		return model;
	}

	public int getType() {
		return type;
	}

	public String toString() {
		return TYPE_DESCRIPTIONS[type];
	}

}
