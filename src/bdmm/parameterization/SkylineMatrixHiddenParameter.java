package bdmm.parameterization;

public class SkylineMatrixHiddenParameter extends SkylineMatrixParameter {

	int nObsTypes;
	
	/**
     * Retrieve value of matrix parameter at particular time (not age), after omitting values
     *
     * @param time when to evaluate the parameter.
     * @return the matrix value at the chosen time.
     */
    protected double[][] getValuesAtTime(double time, PUT_IDX_TO_IGNORE_HERE) {
        update();

        int intervalIdx = getIntervalIdx(time);

        for (int i=0; i<nTypes; i++) {
            System.arraycopy(values[intervalIdx][i], 0,
                    valuesAtTime[i], 0, nTypes);
        }

        return valuesAtTime;
    }
}
