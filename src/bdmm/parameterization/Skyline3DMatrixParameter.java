package bdmm.parameterization;

import beast.core.parameter.RealParameter;

/**
 * This class is a first attempt at having inputs that represent claSSE-type/cladogenetic birth rates
 * It may later be replaced by sth smarter, that is not so tedious to set up for the user.
 */
public class Skyline3DMatrixParameter extends SkylineParameter {

    int nTypes;


    double[][][][] values, storedValues;
    double[][][] valuesAtTime;

    boolean inputIsScalar;

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

        if (nTypesInput.get() != null) {
            nTypes = nTypesInput.get();

            if (!inputIsScalar && elementsPerMatrix != nTypes*(nTypes-1)) {
                throw new IllegalArgumentException("SkylineMatrix parameter has " +
                        "an incorrect number of elements.");
            }
        } else {
            //TODO WRONG: adapt to 3 dimension array!!!!!!!!!
            nTypes = (int) Math.round((1 + Math.sqrt(1 + 4 * elementsPerMatrix)) / 2);
        }

        values = new double[nIntervals][nTypes][nTypes][nTypes];
        storedValues = new double[nIntervals][nTypes][nTypes][nTypes];

        valuesAtTime = new double[nTypes][nTypes][nTypes];
    }

    @Override
    //TODO check that the value assignment done here, from the RealParameter to the 3 dimensional array, is correct
    protected void updateValues() {
        int idx=0;
        for (int interval=0; interval<nIntervals; interval++) {
            for (int i=0; i<nTypes; i++) {
                for (int j=0; j<nTypes; j++) {
                    for (int k = 0; k < nTypes; k++) {
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
}
