package tpp.protein;

import java.util.HashMap;
import java.util.Vector;

import weka.core.Attribute;

/**
 * Encapsulates a property of amino acids, such as surface area. The following
 * properties are defined as static constants:
 * <ul>
 * <li><b>Absolute entropy:</b> Absolute entropy values for each amino acid
 * internal side chain rotation. (Hutchens, J.O. (1970) Heat capacities,
 * absolute entropies, and entropies of formation of amino acids and related
 * compounds. In "Handbook of Biochemistry", 2nd ed. (Sober, H.A., ed.),
 * Chemical Rubber Co., Cleveland, Ohio, pp. B60-B61.)</li>
 * <li><b>Accessible surface area:</b> Accessible surface area (ASA) is the
 * surface area of a protein that is accessible to a solvent. (Radzicka, A. &
 * Wolfenden, R. (1988) Comparing the polarities of the amino acids:</b>
 * Side-chain distribution coefficients between the vapor phase, cyclohexane,
 * 1-octanol, and neutral aqueous solution. Biochemistry, 27, 1664-1670.)</li>
 * <li><b>Contact number:</b> Number of residues surrounding each C alpha atom
 * of a protein. (Nishikawa, K. & Ooi, T. (1986) Radial locations of amino acid
 * residues in a globular protein:</b> Correlation with the sequence. J. Biochem
 * 100, 1043-1047.)</li>
 * <li><b>Hydration potential:</b> hydration potential is the free energies of
 * transfer from the vapor phase to neutral aqueous solution. (Wolfenden, R.
 * Andersson, L., Cullis, P.M., & Southgate C.C.B. (1981) Affinities of amino
 * acid side chains for solvent water. Biochemistry, 20, 849-855.)</li>
 * <li><b>Hydropathy index:</b> Hydropathy index is a measure of how strongly
 * the side chains are pushed out of water. (Kyte, J & Doolittle, R.F. (1982) A
 * simple method for displaying the hydropathic character of a protein. J. Mol.
 * Biol, 157, 105-132)</li>
 * <li><b>Hydrophobicity index:</b> The hydrophobicity index is a measure of the
 * how soluble an amino acid is in water. (Jones, D.D. (1975) Amino acid
 * properties and side-chain orientation in proteins:</b> A cross correlation
 * approach. Theor. Biol, 50, 167-183.)</li>
 * <li><b>Isoelectric point:</b> A pH point at which vast majority of the amino
 * acids are present as the zwitterions. (Zimmerman, J.M., Eliezer, N. & Simha,
 * R. (1968) The characterization of amino acid sequences in proteins by
 * statistical methods. J. Theor. Biol, 21, 170-201)</li>
 * <li><b>Molecular weight:</b> Molecular weight of each amino acid. (Fasman,
 * G.D. (1976) Handbook of Biochemistry and Molecular Biology", 3rd ed.,
 * Proteins - Volume 1, CRC Press, Cleveland. )</li>
 * <li><b>Net charge:</b> Total charge of an amino acid. (Klein, P., Kanehisa,
 * M. & DeLisi, C. (1984) Prediction of protein function from sequence
 * properties:</b> Discriminant analysis of a data base. Biochim. Biophys. Acta,
 * 787, 221-226)</li>
 * <li><b>Normalized flexibility parameters:</b> A measure to indicate protein
 * side chain flexibility (Vihinen, M., Torkkila, E. & Riikonen, P. (1994)
 * Accuracy of protein flexibility predictions. Proteins, 19, 141-149.)</li>
 * <li><b>pKa_(RCOOH):</b> Amino acid pKa values (normalized). (Fauchere, J.L.,
 * Charton, M., Kier, L.B., Verloop, A. & Pliska, V. (1988) Amino acid side
 * chain parameters for correlation studies in biology and pharmacology. Int. J.
 * Peptide Protein Res, 32, 269-278.)</li>
 * <li><b>Polarity:</b> Polarity index of side chains of amino acid (Grantham,
 * R. (1974) Amino acid difference formula to help explain protein evolution.
 * Science, 185, 862-864.)</li>
 * <li><b>Relative mutability:</b> Probability of undergoing mutation for each
 * amino acid (Jones, D.T., Taylor, W.R. & Thornton, J.M. (1992) The rapid
 * generation of mutation data matrices from protein sequences. CABIOS, 8,
 * 275-282.)</li>
 * <li><b>Residue accessible surface area in folded protein:</b> Propensity of
 * amino acids to be in accessible surface area within a folded protein.
 * (Chothia, C. (1976). The nature of the accessible and buried surfaces in
 * proteins. J. Mol. Biol, 105, 1-14.)</li>
 * <li><b>Side chain orientation preference:</b> Preferential orientation
 * pattern of side chains of each amino acid (Rackovsky, S. and Scheraga, H.A.
 * (1977) Hydrophobicity, hydrophilicity, and the radial and orientational
 * distributions of residues in native proteins. Proc. Natl. Acad. Sci. USA, 74,
 * 5248-5251.)</li>
 * <li><b>Size:</b> Size of amino acids. (Dawson, D.M. (1972) The Biochemical
 * Genetics of Man (Brock, D.J.H. and Mayo, O., eds.), Academic Press, New York,
 * pp.1-38. )</li>
 * <li><b>Volume:</b> Volume of amino acids. (Grantham, R. (1974) Amino acid
 * difference formula to help explain protein evolution. Science, 185, 862-864.)
 * </li>
 * </ul>
 */
public class AminoAcidProperty {
	protected static final String[] RESIDUES = { "A", "R", "N", "D", "C", "Q", "E", "G", "H", "I", "L", "K", "M", "F",
			"P", "S", "T", "W", "Y", "V" };

	public static final AminoAcidProperty contact_number = new AminoAcidProperty("contact_number", new double[] {
			0.405737704918033, 0.332991803278688, 0.156762295081967, 0.00614754098360652, 0.905737704918033,
			0.145491803278689, 0.055327868852459, 0.262295081967213, 0.559426229508197, 1, 0.941598360655738, 0,
			0.787909836065574, 0.968237704918033, 0.117827868852459, 0.137295081967213, 0.305327868852459,
			0.961065573770492, 0.648565573770492, 0.88422131147541 });

	public static final AminoAcidProperty absolute_entropy = new AminoAcidProperty("absolute_entropy", new double[] {
			0.140535591668574, 1, 0.388189517051957, 0.36438544289311, 0.665827420462348, 0.500801098649576,
			0.463263904783703, 0, 0.944151979858091, 0.571526665140764, 0.592355230029755, 0.880521858548867,
			0.699931334401465, 0.602426184481575, 0.331197070267796, 0.249713893339437, 0.269169146257725,
			0.807049668116274, 0.604486152437629, 0.412222476539254 });

	public static final AminoAcidProperty accessible_surface_area = new AminoAcidProperty("accessible_surface_area",
			new double[] { 0.34499263622975, 0.921944035346097, 0.538659793814433, 0.525036818851252,
					0.497790868924889, 0.654270986745213, 0.673416789396171, 0.193667157584683, 0.692562592047128,
					0.670839469808542, 0.639543446244477, 0.792341678939617, 0.727540500736377, 0.841678939617084, 0,
					0.403166421207658, 0.52319587628866, 1, 0.883284241531664, 0.578792341678939 });

	public static final AminoAcidProperty hydration_potential = new AminoAcidProperty("hydration_potential",
			new double[] { 0.97982967279247, 0, 0.458987001344689, 0.402061855670103, 0.837292693859256,
					0.472433886149709, 0.435679067682654, 1, 0.432541461228149, 0.989242492155984, 0.995069475571493,
					0.466158673240699, 0.82653518601524, 0.858807709547288, 0.727924697445092, 0.666069027341999,
					0.674137158225011, 0.629314208874944, 0.619004930524429, 0.982070820259973 });

	public static final AminoAcidProperty hydropathy_index = new AminoAcidProperty("hydropathy_index", new double[] {
			0.7, 0, 0.111111111111111, 0.111111111111111, 0.777777777777778, 0.111111111111111, 0.111111111111111,
			0.455555555555555, 0.144444444444444, 1, 0.922222222222222, 0.0666666666666667, 0.711111111111111,
			0.811111111111111, 0.322222222222222, 0.411111111111111, 0.422222222222222, 0.4, 0.355555555555556,
			0.966666666666667 });

	public static final AminoAcidProperty hydrophobicity_index = new AminoAcidProperty("hydrophobicity_index",
			new double[] { 0.230188679245283, 0.226415094339623, 0.0226415094339623, 0.173584905660377,
					0.40377358490566, 0, 0.177358490566038, 0.0264150943396226, 0.230188679245283, 0.837735849056604,
					0.577358490566038, 0.433962264150943, 0.445283018867925, 0.762264150943396, 0.735849056603774,
					0.0188679245283019, 0.0188679245283019, 1, 0.709433962264151, 0.49811320754717 });

	public static final AminoAcidProperty isoelectric_point = new AminoAcidProperty("isoelectric_point", new double[] {
			0.404255319148936, 1, 0.330413016270338, 0, 0.285356695869837, 0.360450563204005, 0.0563204005006258,
			0.400500625782228, 0.603254067584481, 0.406758448060075, 0.401752190237797, 0.872340425531915,
			0.37171464330413, 0.339173967459324, 0.44180225281602, 0.364205256570713, 0.361702127659574,
			0.390488110137672, 0.361702127659574, 0.399249061326658 });

	public static final AminoAcidProperty molecular_weight = new AminoAcidProperty("molecular_weight", new double[] {
			0.108539134473949, 0.767438259657815, 0.441666021522025, 0.449252922505226, 0.356739180924363,
			0.550282573352946, 0.557869474336146, 0, 0.620035611984207, 0.434311372609739, 0.434311372609739,
			0.550592242780831, 0.573972284586204, 0.697685221026554, 0.310133932027561, 0.232406905628242,
			0.341023457459162, 1, 0.821552992180847, 0.32577223813579 });

	public static final AminoAcidProperty net_charge = new AminoAcidProperty("net_charge", new double[] { 0.5, 1, 0.5,
			0, 0.5, 0.5, 0, 0.5, 0.5, 0.5, 0.5, 1, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 });

	public static final AminoAcidProperty normalized_flexibility_parameters = new AminoAcidProperty(
			"normalized_flexibility_parameters", new double[] { 0.404040404040404, 0.525252525252525,
					0.727272727272727, 0.828282828282828, 0.0101010101010101, 0.671717171717171, 0.95959595959596,
					0.641414141414141, 0.232323232323232, 0.116161616161616, 0.156565656565657, 1, 0.242424242424242,
					0.0555555555555556, 0.732323232323232, 0.717171717171717, 0.469696969696969, 0, 0.126262626262626,
					0.136363636363636 });

	public static final AminoAcidProperty relative_mutability = new AminoAcidProperty("relative_mutability",
			new double[] { 0.815217391304348, 0.630434782608696, 0.858695652173913, 0.66304347826087,
					0.206521739130435, 0.641304347826087, 0.565217391304348, 0.271739130434783, 0.717391304347826,
					0.847826086956522, 0.315217391304348, 0.510869565217391, 0.739130434782609, 0.282608695652174,
					0.358695652173913, 1, 0.891304347826087, 0, 0.271739130434783, 0.793478260869565 });

	public static final AminoAcidProperty residue_accessible_surface_area_in_folded_protein = new AminoAcidProperty(
			"residue_accessible_surface_area_in_folded_protein", new double[] { 0.0886075949367089, 0.911392405063291,
					0.569620253164557, 0.405063291139241, 0.0126582278481013, 0.670886075949367, 0.392405063291139,
					0.0632911392405063, 0.316455696202532, 0, 0.0632911392405063, 1, 0.164556962025316,
					0.0759493670886076, 0.405063291139241, 0.329113924050633, 0.367088607594937, 0.177215189873418,
					0.531645569620253, 0 });

	public static final AminoAcidProperty side_chain_orientational_preference = new AminoAcidProperty(
			"side_chain_orientational_preference", new double[] { 0.217142857142857, 0.377142857142857,
					0.448571428571429, 0.645714285714286, 0.0285714285714286, 0.991428571428571, 0.571428571428571,
					0.351428571428571, 0.131428571428571, 0.0485714285714286, 0.0314285714285714, 1, 0,
					0.00857142857142856, 0.468571428571429, 0.345714285714286, 0.308571428571429, 0.1,
					0.377142857142857, 0.0542857142857143 });

	public static final AminoAcidProperty pk_a_rcooh = new AminoAcidProperty("pk-a_rcooh", new double[] {
			0.836555360281195, 0.755711775043937, 0.639718804920914, 1, 0.644991212653778, 0.797891036906854,
			0.963093145869947, 0.662565905096661, 0.499121265377856, 0.845342706502636, 0.84182776801406,
			0.750439367311072, 0.746924428822496, 0.757469244288225, 0, 0.67311072056239, 0.680140597539543,
			0.834797891036907, 0.755711775043937, 0.854130052724077 });

	public static final AminoAcidProperty polarity = new AminoAcidProperty("polarity", new double[] {
			0.395061728395062, 0.691358024691358, 0.827160493827161, 1, 0.074074074074074, 0.691358024691358,
			0.91358024691358, 0.506172839506173, 0.679012345679012, 0.037037037037037, 0, 0.790123456790124,
			0.0987654320987654, 0.037037037037037, 0.382716049382716, 0.530864197530864, 0.45679012345679,
			0.0617283950617284, 0.160493827160494, 0.123456790123457 });

	public static final AminoAcidProperty size = new AminoAcidProperty("size", new double[] { 0.285714285714286, 1,
			0.642857142857143, 0.285714285714286, 0.357142857142857, 0.785714285714286, 0.642857142857143, 0,
			0.785714285714286, 0.714285714285714, 0.714285714285714, 0.928571428571428, 0.785714285714286,
			0.857142857142857, 0.714285714285714, 0.357142857142857, 0.642857142857143, 0.928571428571428,
			0.928571428571428, 0.642857142857143 });

	public static final AminoAcidProperty volume = new AminoAcidProperty("volume", new double[] { 0.167664670658683,
			0.724550898203593, 0.317365269461078, 0.305389221556886, 0.311377245508982, 0.491017964071856,
			0.479041916167665, 0, 0.55688622754491, 0.646706586826347, 0.646706586826347, 0.694610778443114,
			0.610778443113772, 0.772455089820359, 0.176646706586826, 0.173652694610778, 0.347305389221557, 1,
			0.796407185628743, 0.485029940119761 });

	public static final AminoAcidProperty[] ALL_PROPERTIES = new AminoAcidProperty[] { contact_number,
			absolute_entropy, accessible_surface_area, hydration_potential, hydropathy_index, hydrophobicity_index,
			isoelectric_point, molecular_weight, net_charge, normalized_flexibility_parameters, relative_mutability,
			residue_accessible_surface_area_in_folded_protein, side_chain_orientational_preference, pk_a_rcooh,
			polarity, size, volume };

	/** The character that denotes an insertion */
	public static final String INSERTION = "-";

	protected String name;

	protected HashMap<String, Double> values;

	private double meanValue;

	/**
	 * Flag indicating that the value of insertions in an aligned sequence
	 * should be treated as the mean of their neighbours
	 */
	public static final int GAPPING_STRATEGY_MEAN_OF_NEIGHBOURS = 0;

	/**
	 * Flag indicating that the value of insertions in an aligned sequence
	 * should be treated as the mean of all residues
	 */
	public static final int GAPPING_STRATEGY_MEAN_OF_ALL_RESIDUES = 1;

	/**
	 * Short codes indicating which gapping strategy was used - array is indexed
	 * by the gapping strategy
	 */
	public static final String[] GAPPING_STRATEGY_CODES = new String[] { "nbs", "mn" };

	/**
	 * get the value of this property for a particular residue. Throws runtime
	 * exception if not defined
	 */
	public double getValue(String residue) {
		if (values.containsKey(residue))
			return ((Double) values.get(residue)).doubleValue();
		else
			throw new RuntimeException("Unknown " + getName() + " for residue " + residue);
	}

	/**
	 * Construct a Property from arrays of residues values
	 * 
	 * @param name
	 * @param ref
	 * @param array
	 *            of values for residues A, R, N, D, C, Q, E, G, H, I, L, K, M,
	 *            F, P, S, T, W, Y, V respectively. B,Z, and X are then
	 *            calculated.
	 */
	private AminoAcidProperty(String name, double[] values) {
		this.name = name;
		this.values = new HashMap<String, Double>();
		// Copy values into hash and calculate mean value
		double totalValue = 0;
		for (int i = 0; i < values.length; i++) {
			this.values.put(RESIDUES[i], new Double(values[i]));
			totalValue += values[i];
		}
		meanValue = totalValue / RESIDUES.length;

		// calculate possible missing values
		if (!this.values.containsKey("B"))
			this.values.put("B", new Double((getValue("D") + getValue("N")) / 2));
		if (!this.values.containsKey("Z"))
			this.values.put("Z", new Double((getValue("E") + getValue("Q")) / 2));
		if (!this.values.containsKey("X"))
			this.values.put("X", new Double(meanValue));

	}

	public AminoAcidProperty() {
		// TODO Auto-generated constructor stub
	}

	/** The name of this property */
	public String getName() {
		return name;
	}

	/**
	 * Convert a sequence of amino acid residues into an array of values using
	 * the default gapping strategy.
	 * 
	 * @param sequence
	 *            the sequence to be converted
	 * @return
	 */
	public double[] getValuesForSequence(String sequence) {
		return getValuesForSequence(sequence, GAPPING_STRATEGY_MEAN_OF_NEIGHBOURS);
	}

	/**
	 * Convert a sequence of amino acid residues into an array of values.
	 * 
	 * @param sequence
	 *            the sequence to be converted
	 * @param gappingStrategy
	 *            how to deal with insertions. They can either be converted to a
	 *            fixed value, equal to the average property value of all
	 *            residues (mn); or can be set to the mean of the residues
	 *            neighbouring the insertion (nbs).
	 * @return
	 */
	public double[] getValuesForSequence(String sequence, int gappingStrategy) {
		sequence = sequence.toUpperCase();
		double[] valueSequence = new double[sequence.length()];

		// the way we deal with insertions depends on the gapping strategy.
		if (gappingStrategy == GAPPING_STRATEGY_MEAN_OF_ALL_RESIDUES) {
			for (int i = 0; i < sequence.length(); i++)
				if (sequence.substring(i, i + 1).equals(INSERTION))
					valueSequence[i] = meanValue;
				else
					valueSequence[i] = getValue(sequence.substring(i, i + 1));

		}

		if (gappingStrategy == GAPPING_STRATEGY_MEAN_OF_NEIGHBOURS) {

			// first convert all the non-insertions
			for (int i = 0; i < sequence.length(); i++)
				if (!sequence.substring(i, i + 1).equals(INSERTION))
					valueSequence[i] = getValue(sequence.substring(i, i + 1));
			// then convert the insertions to be equal to the bordering values
			boolean isInsertionAtStart, isInsertionAtEnd;
			double previousValue, nextValue;
			for (int i = 0; i < sequence.length(); i++)
				if (sequence.substring(i, i + 1).equals(INSERTION)) {

					// find the previous value
					isInsertionAtStart = true;
					previousValue = 0;
					for (int p = i; p >= 0; p--)
						if (!sequence.substring(p, p + 1).equals(INSERTION)) {
							isInsertionAtStart = false;
							previousValue = getValue(sequence.substring(p, p + 1));
						}

					// find the next value
					isInsertionAtEnd = true;
					nextValue = 0;
					for (int p = i; p < sequence.length(); p++)
						if (!sequence.substring(p, p + 1).equals(INSERTION)) {
							isInsertionAtEnd = false;
							nextValue = getValue(sequence.substring(p, p + 1));
						}

					// if the insertion is neither at the end or the beginning
					// then
					// the values is the average of the previous and next
					if (!isInsertionAtEnd && !isInsertionAtStart)
						valueSequence[i] = (previousValue + nextValue) / 2;
					if (isInsertionAtEnd)
						valueSequence[i] = previousValue;
					if (isInsertionAtStart)
						valueSequence[i] = nextValue;
				}
		}
		return valueSequence;
	}
	
	/** Get attributes suitable for holding the values for this sequence. */
	public Vector<Attribute> getAttributesForSequence(String sequence){
		Vector<Attribute> va = new Vector<Attribute>();
		for (int p = 0; p < sequence.length(); p++)
			va.addElement(new Attribute("p" + p+"."+getName()));
		return va;
	}

	public String toString() {
		return getName();
	}
}
