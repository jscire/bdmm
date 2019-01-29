package bdmm.parameterization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import beast.core.Input;
import beast.core.parameter.RealParameter;

/**
 * This class is a first attempt at having inputs that represent ClaSSE-type/cladogenetic birth rates
 * It may later be replaced by sth smarter, that is not so tedious to set up for the user.
 */
public class Skyline3DMatrixParameter extends SkylineParameter {

    public Input<List<Triplet>> tripletsInput = new Input<>("tripletList", "List of Triplet objects that contain states of a parent and its children, and the triplet type", new ArrayList<>());
	public Input<String[]> tripletOrderInput = new Input<>("tripletTypeList", "List of triplet type strings, one per real parameter (rate) in a single interval (order will be repeated in all intervals).");
    
	int nTypes;

    double[][][][] values, storedValues;
    double[][][] valuesAtTime;

    boolean inputIsScalar;
    
    private List<Triplet> triplets;
    private String[] tripletOrder;
    private HashMap<String, Integer> tripletTypeRealParameterMap = new HashMap<>(); // ctor populates this

    public Skyline3DMatrixParameter() { }

    public Skyline3DMatrixParameter(RealParameter changeTimesParam,
                                    RealParameter rateValuesParam) {
        super(changeTimesParam, rateValuesParam);
    }

    public Skyline3DMatrixParameter(RealParameter changeTimesParam,
                                    RealParameter rateValuesParam,
                                    int nTypes) {
        super(changeTimesParam, rateValuesParam, nTypes);
    }

    public Skyline3DMatrixParameter(RealParameter changeTimesParam,
                                    RealParameter rateValuesParam,
                                    int nTypes,
                                    List<Triplet> speciationTriplets,
                                    String[] tripletsType) {
        changeTimesInput.setValue(changeTimesParam, this);
        rateValuesInput.setValue(rateValuesParam, this);
        typeSetInput.setValue(new TypeSet(nTypes), this);
        tripletsInput.setValue(speciationTriplets, this);
        tripletOrderInput.setValue(tripletsType, this);
        initAndValidate();
    }

    @Override
    public void initAndValidate() {
        super.initAndValidate();
        
        int totalElementCount = rateValuesInput.get() != null
                ? rateValuesInput.get().getDimension()
                : 0;

        if (totalElementCount % nIntervals != 0)
            throw new IllegalArgumentException("Value parameter dimension must " +
                    "be a multiple of the number of intervals.");

        int elementsPerMatrix = totalElementCount/nIntervals;
        inputIsScalar = elementsPerMatrix == 1;

        if (typeSetInput.get() != null) {
            nTypes = typeSetInput.get().getNTypes();


            if(tripletsInput.get() != null) {
                /*
                 * Validating triplets
                 */
                triplets = tripletsInput.get(); //TODO make sure all triplets have a valid triplet name (stored in tripletOrder below)

                tripletOrder = tripletOrderInput.get();
                if (tripletOrder.length < elementsPerMatrix)
                    throw new IllegalArgumentException("Misspecification: the unique number of tags in tripletOrder is smaller than the number of elements in the rate input.");
            } else {

                if (!inputIsScalar && elementsPerMatrix != nTypes * (nTypes - 1) * (nTypes / 2 + 1)) {
                    throw new IllegalArgumentException("Skyline3DMatrix parameter has " +
                            "an incorrect number of elements.");
                }
            }
        } else {
            throw new IllegalArgumentException("Number of types must be input when using Skyline3DMatrixParameter"); //TODO implement a way to infer the number of types
        }

        values = new double[nIntervals][nTypes][nTypes][nTypes];
        storedValues = new double[nIntervals][nTypes][nTypes][nTypes];

        valuesAtTime = new double[nTypes][nTypes][nTypes];
        		

    }

    @Override
    protected void updateValues() {
        if(tripletsInput.get() != null) {
            populateValues();
        } else {
            //TODO check that the value assignment done here, from the RealParameter to the 3 dimensional array, is correct
            int idx=0;
            for (int interval=0; interval<nIntervals; interval++) {
                for (int i=0; i<nTypes; i++) {
                    for (int j=0; j<nTypes; j++) {
                        for (int k = 0; k <= j ; k++) {
                            if (i==j && j==k) {
                                values[interval][i][j][k] = 0.0;
                                continue;
                            }

                            if (inputIsScalar)
                                values[interval][i][j][k] = rateValuesInput.get().getValue(interval);
                            else
                                values[interval][i][j][k] = rateValuesInput.get().getValue(idx);

                            idx += 1;
                        }
                    }
                }
            }
        }

    }

    /**
     * Retrieve value of matrix parameter at particular time (not age).
     *
     * @param time when to evaluate the parameter.
     * @return the matrix value at the chosen time.
     */
    protected double[][][] getValuesAtTime(double time) {
        update();

        int intervalIdx = getIntervalIdx(time);

        for (int i=0; i<nTypes; i++) {
            for (int j = 0; j < nTypes; j++) {
                System.arraycopy(values[intervalIdx][i][j], 0,
                        valuesAtTime[i][j], 0, nTypes);
            }
        }

        return valuesAtTime;
    }

    public int getNTypes() {
        return nTypes;
    }

    /**
     * Populate the values array with values in the rateValuesInput, following the mapping given in the triplets inputs.
     */
    public void populateValues () {
    	// build hashmap with "triplet type":index
    	for (int i=0; i<tripletOrder.length; i++) {
        	tripletTypeRealParameterMap.put(tripletOrder[i], i);
        }
     
    	// now populate values[interval][parent][left][right]
    	int i,j,k, tripletTypeIdx;
    	String tripletType;
    	for (Triplet triplet: triplets) {
    		int[] types = triplet.getTriplet();
    		i = types[0];
    		j = Math.max(types[1], types[2]); // j is always greater or equal to k, b_ijk is set to 0 if k>j.
    		k = Math.min(types[1], types[2]);
    		tripletType = triplet.getTripletType();
    		tripletTypeIdx = tripletTypeRealParameterMap.get(tripletType);
    		
    		for (int interval=0; interval<nIntervals; interval++) {
        		values[interval][i][j][k] = rateValuesInput.get().getValues()[tripletTypeIdx];
        	}
    	}
    }
    
    @Override
    protected void store() {
        super.store();

        for (int interval=0; interval<nIntervals; interval++) {
            for (int i=0; i<nTypes; i++) {
                System.arraycopy(values[interval][i], 0,
                        storedValues[interval][i], 0, nTypes);
            }
        }
    }

    @Override
    protected void restore() {
        super.restore();

        double[][][][] tmp;
        tmp = values;
        values = storedValues;
        storedValues = tmp;
    }

    public static void main(String[] args) {

    }
}
