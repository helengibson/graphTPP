package tpp;

import java.awt.Color;
import java.io.Serializable;

/**
 * A ColourScheme is made up of a palette of colors for drawing a TPP graph.
 * Objects can be colored in one of two ways: unordered or ordered. The first
 * are suitable for nominal classifications, the second for numeric variables
 * with a lower and upper bound.
 * 
 * TODO implement the other color schemes from here:http://colorbrewer2.org/
 */
public class ColourScheme implements Serializable {

	/** The margin of error when calculating a value in a range */
	private static final double MARGIN = 0.00001;

	/**
	 * The number of colors in the spectrum. In fact this is the number of
	 * intervals in a half-range. So if INTERVAL=3 there will be 7 colors in a
	 * bipolar spectrum (+ve and -ve).
	 */
	public static final int INTERVALS = 3;
	private static final float INTERVALSf = INTERVALS * 1f;
	
	public static ColourScheme CUSTOM_DARK;
	
	public static ColourScheme CUSTOM_LIGHT; 
		
	Color backgroundColor;
	Color foregroundColor;
	Color axesColor;
	Color minColor;
	Color maxColor;
	Color midColor;
	Color[] classificationColors = null;
	Color[] spectrumColors = null;
	String description;
	Color graphColor;

	public ColourScheme(Color backgroundColor, Color foregroundColor, Color axesColor, Color graphColor, Color minColor,
			Color midColor, Color maxColor, Color[] classColors, String description) {
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.axesColor = axesColor;
		this.graphColor = graphColor;
		this.classificationColors = classColors;

		// min mid and max are the colors denoting the minimum, middle, and
		// maximum values on the ordered range
		this.minColor = minColor;
		this.midColor = midColor;
		this.maxColor = maxColor;
		this.description = description;
		initSpectrum();
	}
	
	public ColourScheme(Color backgroundColor, Color foregroundColor, Color axesColor, Color graphColor, Color[] specColors, Color[] classColors, String description) {
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.axesColor = axesColor;
		this.graphColor = graphColor;
		this.classificationColors = classColors;

		// min mid and max are the colors denoting the minimum, middle, and
		// maximum values on the ordered range
		this.spectrumColors = specColors;
		this.description = description;
		initSpectrum();
	}

	private void initSpectrum() {

		// The spectrum is symmetric around the mid color.
		//spectrumColors = new Color[2 * INTERVALS + 1];

		// get the components of the min, mid and max
		
		int rMin = spectrumColors[2].getRed();
		int rMid = spectrumColors[1].getRed();
		int rMax = spectrumColors[0].getRed();
		int gMin = spectrumColors[2].getGreen();
		int gMid = spectrumColors[1].getGreen();
		int gMax = spectrumColors[0].getGreen();
		int bMin = spectrumColors[2].getBlue();
		int bMid = spectrumColors[1].getBlue();
		int bMax = spectrumColors[0].getBlue();
		
		spectrumColors = new Color[2 * INTERVALS + 1];

		for (int c = 1; c <= INTERVALS; c++) {

			// the positive colors
			spectrumColors[INTERVALS + c] = new Color(Math.round(rMid + (c / INTERVALSf) * (rMax - rMid)),
					Math.round(gMid + (c / INTERVALSf) * (gMax - gMid)), Math.round(bMid + (c / INTERVALSf)
							* (bMax - bMid)));

			// the negative colors
			spectrumColors[INTERVALS - c] = new Color(Math.round(rMid - (c / INTERVALSf) * (rMid - rMin)),
					Math.round(gMid - (c / INTERVALSf) * (gMid - gMin)), Math.round(bMid - (c / INTERVALSf)
							* (bMid - bMin)));
		}
		spectrumColors[INTERVALS] = new Color(rMid, gMid, bMid);
	}

	public Color getAxesColor() {
		return axesColor;
	}
	
	public Color getGraphColor() {
		return graphColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color[] getClassificationColors() {
		return classificationColors;
	}

	public Color getClassificationColor(int c) {
		return classificationColors[c % classificationColors.length];
	}

	/**
	 * Get a color for representing a value of c within a range of
	 * [lower,upper].
	 */
	public Color getColorFromSpectrum(double c, double lowerBound, double upperBound) {
		if (c > upperBound - MARGIN)
			return spectrumColors[2 * INTERVALS];
		
		if (c < lowerBound + MARGIN)
			return spectrumColors[0];
		
		int i;
		double step = (upperBound - lowerBound) / (2f * INTERVALSf + 1);
		i = (int) Math.floor((c - lowerBound) / step);

		return spectrumColors[i];
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public Color getMinColor() {
		return spectrumColors[2];
	}
	
	public Color getMidColor() {
		return spectrumColors[1];
	}
	
	public Color getMaxColor() {
		return spectrumColors[0];
	}
	public String toString() {
		return description + " background=" + backgroundColor;
	}

	public Color[] getSpectrum() {
		return spectrumColors;
	}
	
	public void setClassificationColors(Color[] classColors) {
		classificationColors = classColors;
	}
	
	public void setSpectrumColors(Color[] specColors) {
		classificationColors = specColors;
	}	
	
	public static ColourScheme createDarkColorScheme(Color[] specColors, Color[] classColors) {
		CUSTOM_DARK = new ColourScheme(Color.black, Color.white, new Color(0x8CB3D9),
				new Color(192,192,192), specColors, classColors , "Custom Dark Background");
		return CUSTOM_DARK;
	}
	
	public static ColourScheme createLightColorScheme(Color[] specColors, Color[] classColors) {
		CUSTOM_LIGHT = new ColourScheme(Color.white, Color.black, Color.gray, 
				new Color(192,192,192), specColors, classColors, "Custom Light Background");
		return CUSTOM_LIGHT;
	}

	// Qualitative Schemes from Cynthia Brewer at ColorBrewer2.org
	
	private static Color[] set1 = {new Color(228,26,28), new Color(55,126,184), new Color(77,175,74), new Color(152,78,163), new Color(255,127,0), new Color(255,255,51), new Color(166,86,40), new Color(247,129,191), new Color(153,153,153)};
	private static Color[] set2 = {new Color(102,194,165), new Color(252,141,98), new Color(141,160,203), new Color(231,138,195), new Color(166,216,84), new Color(255,217,47), new Color(229,196,148), new Color(179,179,179)};
	private static Color[] set3 = {new Color(141,211,199), new Color(255,255,179), new Color(190,186,218), new Color(251,128,114), new Color(128,177,211), new Color(253,180,98), new Color(179,222,105), new Color(252,205,229), new Color(217,217,217), new Color(188,128,189), new Color(204,235,197), new Color(255,237,111)};
	private static Color[] pastel1 = {new Color(251,180,174), new Color(179,205,227), new Color(204,235,197), new Color(222,203,228), new Color(254,217,166), new Color(255,255,204), new Color(229,216,189), new Color(253,218,236), new Color(242,242,242)};
	private static Color[] pastel2 = {new Color(179,226,205), new Color(253,205,172), new Color(203,213,232), new Color(244,202,228), new Color(230,245,201), new Color(255,242,174), new Color(241,226,204), new Color(204,204,204)};
	private static Color[] dark2 = {new Color(27,158,119), new Color(217,95,2), new Color(117,112,179), new Color(231,41,138), new Color(102,166,30), new Color(230,171,2), new Color(166,118,29), new Color(102,102,102)};
	private static Color[] accent = {new Color(127,201,127), new Color(190,174,212), new Color(253,192,134), new Color(255,255,153), new Color(56,108,176), new Color(240,2,127), new Color(191,91,23), new Color(102,102,102)};
	private static Color[] paired = {new Color(166,206,227), new Color(31,120,180), new Color(178,223,138), new Color(51,160,44), new Color(251,154,153), new Color(227,26,28), new Color(253,191,111), new Color(255,127,0), new Color(202,178,214), new Color(106,61,154), new Color(255,255,153), new Color(177,89,40)};
	
	private static Color[] custom = null;
	public static void setCustomColor(Color[] classColor) {
		custom = classColor;
	}
	
	public static Color[] getSet1() {
		return set1;
	}
	
	public static Color[] getSet2() {
		return set2;
	}
	
	public static Color[] getSet3() {
		return set3;
	}
	
	public static Color[] getDark2() {
		return dark2;
	}
	
	public static Color[] getPastel1() {
		return pastel1;
	}
	
	public static Color[] getPastel2() {
		return pastel2;
	}
	
	public static Color[] getAccent() {
		return accent;
	}
	
	public static Color[] getPaired() {
		return paired;
	}
	
	public static Color[] getCustom() {
		return custom;
	}
	
	// Diverging Colour schemes from Cynthia Brewer at ColorBrewer2.org

	private static Color[] spectral = {
		new Color(215,25,28), 
		new Color(255,255,191), 
		new Color(43,131,186)};
	private static Color[] RdYlGn = {
		new Color(215,25,28), 
		new Color(255,255,191), 
		new Color(26,150,65)};
	private static Color[] RdBu = {
		new Color(202,0,32), 
		new Color(190,190,190), 
		new Color(5,113,176)};
	private static Color[] PiYG = {
		new Color(208,28,139), 
		new Color(190,190,190), 
		new Color(77,172,38)};
	private static Color[] PRGn = {
		new Color(123,50,148), 
		new Color(190,190,190), 
		new Color(0,136,55)};
	private static Color[] RdYlBu = {
		new Color(215,25,28), 
		new Color(255,255,191), 
		new Color(44,123,182)};
	private static Color[] BrBG = {
		new Color(166,97,26), 
		new Color(190,190,190), 
		new Color(1,133,113)};
	private static Color[] RdGy = {
		new Color(202,0,32), 
		new Color(255,255,255), 
		new Color(64,64,64)};
	private static Color[] PuOr = {
		new Color(230,97,1), 
		new Color(190,190,190), 
		new Color(94,60,153)};
	
	
	public static Color[] getSpectral() {
		return spectral;
	}
	
	public static Color[] getRdYlGn() {
		return RdYlGn;
	}
	
	public static Color[] getRdBu() {
		return RdBu;
	}
	
	public static Color[] getPiYG() {
		return PiYG;
	}
	
	public static Color[] getPRGn() {
		return PRGn;
	}
	
	public static Color[] getRdY1Bu() {
		return RdYlBu;
	}
	
	public static Color[] getBrBG() {
		return BrBG;
	}
	
	public static Color[] getRdGy() {
		return RdGy;
	}
	
	public static Color[] getPuOr() {
		return PuOr;
	}
	
	public static final ColourScheme DARK = new ColourScheme(Color.black, Color.white, new Color(0x8CB3D9),
			new Color(192,192,192), RdBu, dark2, "Dark background");
	
	public static final ColourScheme LIGHT = new ColourScheme(Color.white, Color.black, Color.gray, new Color(192,192,192), 
			RdBu, dark2, "Light background");
	
	// Qualitative Color scheme test
	private static Color[] GnBu = {
		new Color(247,252,240), 
		new Color(123,204,196), 
		new Color(8,64,129)};
	
	public static Color[] getGnBu(int classes) {
				
		int rMin = GnBu[2].getRed();
		int rMid = GnBu[1].getRed();
		int rMax = GnBu[0].getRed();
		int gMin = GnBu[2].getGreen();
		int gMid = GnBu[1].getGreen();
		int gMax = GnBu[0].getGreen();
		int bMin = GnBu[2].getBlue();
		int bMid = GnBu[1].getBlue();
		int bMax = GnBu[0].getBlue();
				
		Color[] newGnBu = new Color[classes];
		
		if (classes % 2 == 0){
			
			float classesf = classes * 1f;
			
			for (int c = 0; c < classes; c++) {
				
				int red = Math.round(rMax - (c / classesf) * (rMax - rMin));
				int green = Math.round(gMax - (c / classesf) * (gMax - gMin));
				int blue = Math.round(bMax - (c / classesf) * (bMax - bMin));
				
				newGnBu[c] = new Color(Math.max(red, 0), Math.max(green, 0), Math.max(blue, 0));
				
			}
			
		} else {
			
			double halfway = Math.floor((double)classes/2) + 1;
			int lowClasses = (int) Math.floor(classes/2);
			float lowClassesf = lowClasses * 1f;
			
			for (int c = 1; c < halfway; c++) {
										
				// the positive colors
				newGnBu[lowClasses + c] = new Color(Math.round(rMid + (c / lowClassesf) * (rMax - rMid)),
						Math.round(gMid + (c / lowClassesf) * (gMax - gMid)), Math.round(bMid + (c / lowClassesf)
								* (bMax - bMid)));

				// the negative colors
				newGnBu[lowClasses - c] = new Color(Math.round(rMid - (c / lowClassesf) * (rMid - rMin)),
						Math.round(gMid - (c / lowClassesf) * (gMid - gMin)), Math.round(bMid - (c / lowClassesf)
								* (bMid - bMin)));
			}
			newGnBu[lowClasses] = new Color(rMid, gMid, bMid);
		}
			
		
		return newGnBu;
	}

	
}

	
	
	