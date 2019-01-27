/**
 * Created by Fabio K. Mendes (fkmendes)
 */

package bdmm.parameterization;

import java.util.Arrays;

import beast.core.Input;
import beast.core.parameter.IntegerParameter;

public class HiddenParameterization extends CanonicalParameterization {

	public Input<SkylineVectorParameter> birthRateInput = new Input<>("birthRate",
            "Birth rate skyline.", Input.Validate.REQUIRED);
	
	public Input<SkylineMatrixParameter> migRateInput = new Input<>("migrationRate",
            "Migration rate skyline.");
	
	final public Input<IntegerParameter> hiddenTraitFlagInput = new Input<>("hiddenTraitFlag", "Integers flag that determine if hidden trait exists, how many types it has, and the association of its types and the observed types: 0=No hidden type for the observed state, 1/2=Observed state has hidden type associated to it.");
	
	final public Input<IntegerParameter> cidFlagInput = new Input<>("cidFlag", "Integer flag for type-independence (i.e., CID-type models): 0=Type-dependent, 1=Type-independent.");
	
	private int nObsTypes;
	private Integer[] hiddenTraitFlag;
	private Integer cidFlag;
	private boolean modelChecked = false;
	
	double[] preOmissionBirthRates, preOmissionDeathRates;
	double[] omittedRates;
	
	double[][] preOmissionMatrixRates;
	double[][] omittedMatrixRates;
	
	boolean[] ratesToOmit;
	boolean[][] matrixRatesToOmit;
	boolean isCID = false;
	
	@Override
    public void initAndValidate() {
		nTypes = nTypesInput.get();
		nObsTypes = nTypes/2; // initialize with all hidden types
		
		omittedRates = new double[nTypes];
		omittedMatrixRates = new double[nTypes][nTypes];
		
		ratesToOmit = new boolean[nTypes];
		matrixRatesToOmit = new boolean[nTypes][nTypes]; // could change this
		
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
			isCID = (cidFlag == 1) ? true:false;
			changeModel();
		};
		
		modelChecked = true;
		return;
	}
	
	private void changeModel() {
		nTypes = nObsTypes;
		
		/*
		 * Birth and death rate masks
		 */
		for (int i=0; i<nObsTypes; i++) {
			ratesToOmit[i] = false; // all observed rates are always included
			
			// but the hidden might or not be included
			if (hiddenTraitFlag[i] == 1 || hiddenTraitFlag[i] == 2) {
				ratesToOmit[nObsTypes+i] = false;
				nTypes++;
			} else { ratesToOmit[nObsTypes+i] = true; }
		}
		
		/*
		 * Migration rate mask
		 */
		
	}
	
	private double[] omitRates(double[] allUntouchedRates, boolean[] ratesToOmit, boolean isCID) {
		
		if (isCID) {
			// take just first RealParameter inside allUntouchedRates
			Arrays.fill(omittedRates, 0, nObsTypes-1, allUntouchedRates[0]); }
		else { 
			// always return the rates for observed types if not CID
			System.arraycopy(allUntouchedRates, 0, omittedRates, 0, nObsTypes);
		}
        
        // now grabbing only the hidden types the current model has
        int j=0;
        for (int i=0; i<ratesToOmit.length; i++) {
        	if (!ratesToOmit[i]) {
        		if (isCID) {
        			// take just first RealParameter of hidden type inside allUntouchedRates
            		omittedRates[nObsTypes+j] = allUntouchedRates[nObsTypes+i];
            	}
        		else {
        			omittedRates[nObsTypes+j] = allUntouchedRates[nObsTypes+i];
        		}
        		
        		j++;
        	}
        }
        
        return omittedRates;
	}
	
	private double[][] omitMatrixRates(double[][] allUntouchedMatrixRates, boolean[][] matrixRatesToOmit) {
		// TODO: do things to omitted2Drates
		return omittedMatrixRates;
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
		if (!modelChecked) {
			checkModel(); // updates ommited rates
		}
		omittedRates = omitRates(birthRateInput.get().getValuesAtTime(time), ratesToOmit);
		
		return omittedRates;
	}

	@Override
	protected double[][] getMigRateValues(double time) {
		if (!modelChecked) {
			checkModel(); // updates ommited rates
		}
		omittedMatrixRates = omitMatrixRates(migRateInput.get().getValuesAtTime(time), matrixRatesToOmit);
		
		return omittedMatrixRates;
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
