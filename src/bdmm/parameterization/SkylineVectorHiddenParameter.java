package bdmm.parameterization;

public class SkylineVectorHiddenParameter extends SkylineVectorParameter {

	int nObsTypes;
	
	@Override
	public void initAndValidate() {
		super.initAndValidate();
		
		nObsTypes = nTypes/2;
	}
	
	/**
     * Retrieve value of vector at a chosen time (not age), after omitting values.
     *
     * @param time when to evaluate the skyline parameter.
     * @param ratesOmitted has true=type index to ignore
     * @return value of the vector at the chosen time.
     */
	protected double[] getValuesAtTime(double time, boolean[] ratesOmitted) {
        update();

        int intervalIdx = getIntervalIdx(time);
        
        // always return the rates for observed types
        System.arraycopy(values[intervalIdx], 0, valuesAtTime, 0, nObsTypes);
        
        // now grabbing only the hidden types the current model has
        int j=0;
        for (int i=0; i<ratesOmitted.length; i++) {
        	if (!ratesOmitted[i]) {
        		valuesAtTime[j] = values[intervalIdx][nObsTypes+i];
        		j++;
        	}
        }

        return valuesAtTime;
        }
	
	protected int getNObsTypes() {
		return nObsTypes;
	}
}
