/**
 * Created by Fabio K. Mendes (fkmendes)
 */

package bdmm.parameterization;

import beast.core.Input;
import beast.core.parameter.IntegerParameter;

public class HiddenParameterization extends CanonicalParameterization {

	public Input<SkylineVectorHiddenParameter> birthRateInput = new Input<>("birthRate",
            "Birth rate skyline.", Input.Validate.REQUIRED);
	
	public Input<SkylineMatrixHiddenParameter> migRateInput = new Input<>("migrationRate",
            "Migration rate skyline.");
	
	final public Input<IntegerParameter> hiddenTraitFlagInput = new Input<>("hiddenTraitFlag", "Integers flag that determine if hidden trait exists, how many types it has, and the association of its types and the observed types: 0=No hidden type for the observed state, 1/2=Observed state has hidden type associated to it.");
	
	final public Input<IntegerParameter> cidFlagInput = new Input<>("cidFlag", "Integer flag for type-independence (i.e., CID-type models): 0=Type-dependent, 1=Type-independent.");
	
	private int nObsTypes;
	private Integer[] hiddenTraitFlag;
	private Integer cidFlag;
	private boolean flagDirty = true;
	
	boolean[] birthRatesOmitted;
	
	@Override
    public void initAndValidate() {
		nTypes = nTypesInput.get();
		nObsTypes = nTypes/2; // initialize with all hidden types
		
		birthRatesOmitted = new boolean[nObsTypes];
		
		ZERO_VALUE_ARRAY = new double[nTypes];
        ZERO_VALUE_MATRIX = new double[nTypes][nTypes];
        ZERO_VALUE_3DMATRIX = new double[nTypes][nTypes][nTypes];
	}
	
	/*
	 * Now nTypes can vary if we operate on flags (model averaging)
	 */
	@Override
	public int getNTypes() {
		checkModel();
        return nTypes;
    }
	
	public int getNObsTypes() {
		return nObsTypes;
	}
	
	private void checkModel() {
		if (hiddenTraitFlagInput.get().isDirtyCalculation() ||
				cidFlagInput.get().isDirtyCalculation()) {
			
			hiddenTraitFlag = hiddenTraitFlagInput.get().getValues();
			cidFlag = hiddenTraitFlagInput.get().getValue();
			
			changeModel();
		};
		
		flagDirty = false;
		return;
	}
	
	private void changeModel() {
		nTypes = nObsTypes;
		
		/*
		 * Birth and death rate masks
		 */
		for (int i=0; i<nObsTypes; i++) {
			if (hiddenTraitFlag[i] == 1 || hiddenTraitFlag[i] == 2) {
				birthRatesOmitted[i] = false;
				nTypes++;
			} else { birthRatesOmitted[i] = true; }
		}
		
		/*
		 * Migration rate mask
		 */
		
	}
	
	@Override
	public double[] getBirthRateChangeTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getMigRateChangeTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getCrossBirthRateChangeTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getCladogeneticBirthRateChangeTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getDeathRateChangeTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getSamplingRateChangeTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getRemovalProbChangeTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getRhoSamplingTimes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[] getBirthRateValues(double time) {
		checkModel(); // updates rate masks if necessary
		
		/*
		 * IMPORTANT: whoever uses this return needs to know only
		 * to use the first nType elements
		 */
		return birthRateInput.get().getValuesAtTime(time, birthRatesOmitted);
	}

	@Override
	protected double[][] getMigRateValues(double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[][] getCrossBirthRateValues(double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[][][] getCladogeneticBirthRateValues(double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[] getDeathRateValues(double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[] getSamplingRateValues(double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[] getRemovalProbValues(double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[] getRhoValues(double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void validateParameterTypeCounts() {
		// TODO Auto-generated method stub

	}

}
