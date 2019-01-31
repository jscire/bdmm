/**
 * Created by Fabio K. Mendes (fkmendes)
 */

package bdmm.parameterization;

import java.util.Arrays;

import beast.core.Input;
import beast.core.parameter.IntegerParameter;

public class HiddenParameterization extends CanonicalParameterization {
	
	final public Input<IntegerParameter> hiddenTraitFlagInput = new Input<>("hiddenTraitFlag", "Integers flag that determine if hidden trait exists, how many types it has, and the association of its types and the observed types: 0=No hidden type for the observed state, 1/2=Observed state has hidden type associated to it.", Input.Validate.REQUIRED);
	
	final public Input<IntegerParameter> cidFlagInput = new Input<>("cidFlag", "Integer flag for type-independence (i.e., CID-type models): 0=Type-dependent, 1=Type-independent.", Input.Validate.REQUIRED);
	
	private int nObsTypes;
	private Integer[] hiddenTraitFlag;
	private Integer cidFlag;
	private boolean modelChecked = false;
	
	double[] preOmissionBirthRates, preOmissionDeathRates;
	double[] omittedRates;
	
	double[][] preOmissionMatrixRates;
	double[][] omittedMatrixRates;
	
	boolean[] ratesToOmit;
	boolean isCID = false;
	
	boolean[][] matrixRatesToOmit;
	boolean[] isMigRateSymmetric;
	
	@Override
    public void initAndValidate() {
		nTypes = nTypesInput.get();
		nObsTypes = nTypes/2; // initialize with all hidden types

		isMigRateSymmetric = new boolean[nObsTypes];
		omittedRates = new double[nTypes];
		omittedMatrixRates = new double[nTypes][nTypes];
		
		ratesToOmit = new boolean[nTypes];
		matrixRatesToOmit = new boolean[nTypes][nTypes]; // could change this
		
		ZERO_VALUE_ARRAY = new double[nTypes];
        ZERO_VALUE_MATRIX = new double[nTypes][nTypes];
        ZERO_VALUE_3DMATRIX = new double[nTypes][nTypes][nTypes];
        
        dirty = true;
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

	public int getNHiddenTypes(){
		return getNTypes()-getNObsTypes();
	}

	/**
	 * Get the index of the hidden type corresponding to a given observed type.
	 * -1 if given type has no corresponding hidden type
	 */
	public int getCorrespondingHiddenTypeIdx(int typeNr) {
		if(hiddenTraitFlagInput.get().getValue(typeNr) == 0)
			return -1;
		else {
			int hiddenTypeIdx = nObsTypes;
			for (int i = 0; i < typeNr; i++) {
				if(hiddenTraitFlagInput.get().getValue(i) != 0)
					hiddenTypeIdx ++;
			}
			return hiddenTypeIdx;
		}
	}
	
	private void checkModel() {
		if (hiddenTraitFlagInput.get().isDirtyCalculation() ||
				cidFlagInput.get().isDirtyCalculation() ||
				!modelChecked) {
			
			hiddenTraitFlag = hiddenTraitFlagInput.get().getValues();
			cidFlag = cidFlagInput.get().getValue();
			isCID = (cidFlag == 1) ? true:false;
			changeModel();
		}
		
		modelChecked = true;
		return;
	}
	
	private void changeModel() {
		nTypes = 2 * nObsTypes; // start variable with all hidden types
		Arrays.fill(ratesToOmit, false); // initialize by including all type birth and death rates
		for (int i=0; i<matrixRatesToOmit.length; i++) {
			Arrays.fill(matrixRatesToOmit[i], false); // initialize by including all type mig rates
		}
		
		/*
		 * Birth and death rates
		 */
		for (int i=0; i<nObsTypes; i++) {
			if (hiddenTraitFlag[i] == 0) {
				ratesToOmit[nObsTypes+i] = true;
				nTypes--;
			}
			else if (hiddenTraitFlag[i] == 1) {
				isMigRateSymmetric[i] = true; // for omitMatrixRates
			}
		}
			
		/*
		 * Migration rate mask
		 */
		for (int i=0; i<nTypes; i++) {
			for (int j=0; j<nTypes; j++) {

				if (i != j) {			
					// upper right corner
					if (i<nObsTypes && j>=nObsTypes) {
						if (hiddenTraitFlag[j-nObsTypes] == 0) {
							matrixRatesToOmit[i][j] = true;
						}
					}
					
					// bottom left corner
					if (i>=nObsTypes) {
						if (hiddenTraitFlag[i-nObsTypes] == 0) {
							matrixRatesToOmit[i][j] = true;
						}
					}
					
					// bottom right corner
					if (i>=nObsTypes && j>=nObsTypes) {
						if ( (hiddenTraitFlag[j-nObsTypes] == 0) ||
							 (hiddenTraitFlag[i-nObsTypes] == 0) ) {
							matrixRatesToOmit[i][j] = true;
						}
					}
				}
			}
		}
	}
	
	private double[] omitRates(double[] allUntouchedRates, boolean[] ratesToOmit, boolean isCID) {
		
//		if (isCID) {
//			// take just first RealParameter inside allUntouchedRates
//			Arrays.fill(omittedRates, 0, nObsTypes, allUntouchedRates[0]);
//			Arrays.fill(omittedRates, nObsTypes, omittedRates.length, allUntouchedRates[nObsTypes]);
//		}
		
		 
		// now grabbing only the hidden types the current model has
		int j=0;
		for (int i=0; i<ratesToOmit.length; i++) {
			if (!ratesToOmit[i]) {				
				if (i < nObsTypes && isCID) {
					omittedRates[j] = allUntouchedRates[0];
				}
				else if (i >= nObsTypes && isCID) {
					omittedRates[j] = allUntouchedRates[nObsTypes];
				}
				else {
					omittedRates[j] = allUntouchedRates[i];
				}
				
				j++;
			}
		}
		
        return omittedRates;
	}

	private double[][] omitMatrixRates(double[][] allUntouchedMatrixRates, boolean[][] matrixRatesToOmit, boolean[] isMigRateSymmetric) {
		
		/*
		 *  Note that nTypes should have been updated to the right (current)
		 *  value by the birth death part of changeModel()
		 */
		int i = 0;
		int j = 0;
		/*
		 * Going over 'values[][] = allUntouchedMatrixRates'
		 * and matrixRatesToOmit (we use both to populate 'return' matrix)
		 */
		for (int i2=0; i2<matrixRatesToOmit.length; i2++) {
			for (int j2=0; j2<matrixRatesToOmit.length; j2++) {
				/*
				 * First symmetrify (or not) allUntouchedMatrixRates (our source matrix)
	
				 * Note that we are putting the top-right value on the bottom-left value,
				 * so we are ignoring the "later" RealParameter entry that would go into
				 * the bottom-left value of values[][] (i.e., allUntouchedMatrixRates)
				 */
				if (i2 >= nObsTypes && j2 < nObsTypes && isMigRateSymmetric[j2]) {
					allUntouchedMatrixRates[i2+nObsTypes][j2] =
							allUntouchedMatrixRates[j2+nObsTypes][i2];
				}
				
				// now fill in!
				if (!matrixRatesToOmit[i2][j2]) {
					if (j >= nTypes) { j = 0; i++; }
					omittedMatrixRates[i][j] = allUntouchedMatrixRates[i2][j2];
					j++;
				}
			}
		}
		
		return omittedMatrixRates;
	}
	
	public boolean typeHasHiddenType(int typeNr){
		if(typeNr < 0 || typeNr > nObsTypes)
			throw new IllegalArgumentException("Invalid type number. Type number must match an observed type.");

		boolean hasHiddenType = hiddenTraitFlagInput.get().getValue(typeNr) > 0;
		return hasHiddenType;
	}
	
	@Override
	protected double[] getBirthRateValues(double time) {
		if (!modelChecked) {
			checkModel(); // updates ommited rates
		}
//		omittedRates = omitRates(birthRateInput.get().getValuesAtTime(time), ratesToOmit);
		
		// omitRates populates omittedRates and returns it
		return omitRates(birthRateInput.get().getValuesAtTime(time), ratesToOmit, isCID);
	}

	@Override
	protected double[][] getMigRateValues(double time) {
		if (!modelChecked) {
			checkModel(); // updates ommited rates
		}
		
		return omitMatrixRates(migRateInput.get().getValuesAtTime(time), matrixRatesToOmit, isMigRateSymmetric);
	}

	

	@Override
	protected double[] getDeathRateValues(double time) {
		if (!modelChecked) {
			checkModel(); // updates ommited rates
		}
		
		// omitRates populates omittedRates and returns it
		return omitRates(deathRateInput.get().getValuesAtTime(time), ratesToOmit, isCID);
	}

	@Override
    protected void validateParameterTypeCounts() {
        if ( (birthRateInput.get().getNTypes() != hiddenTraitFlagInput.get().getDimension()*2) || 
        		(birthRateInput.get().getNTypes() != nTypesInput.get()) )
            throw new IllegalArgumentException("Birth rate skyline type count does not match type count of model or hidden type flag.");

        if ( (deathRateInput.get().getNTypes() != hiddenTraitFlagInput.get().getDimension()*2) || 
        		(deathRateInput.get().getNTypes() != nTypesInput.get()) )
            throw new IllegalArgumentException("Death rate skyline type count does not match type count of model or hidden type flag.");

        if ( (samplingRateInput.get().getNTypes() != hiddenTraitFlagInput.get().getDimension()*2) || 
        		(samplingRateInput.get().getNTypes() != nTypesInput.get()) )
            throw new IllegalArgumentException("Sampling rate skyline type count does not match type count of model or hidden type flag.");

        if ( (migRateInput.get() != null && migRateInput.get().getNTypes() != hiddenTraitFlagInput.get().getDimension()*2) ||
        		(migRateInput.get().getNTypes() != nTypesInput.get()) )
            throw new IllegalArgumentException("Migration rate skyline type count does not match type count of model.");

        if ( (crossBirthRateInput.get().getNTypes() != hiddenTraitFlagInput.get().getDimension()*2) || 
        		(crossBirthRateInput.get().getNTypes() != nTypesInput.get()) )
            throw new IllegalArgumentException("Birth rate among demes skyline type count does not match type count of model or hidden type flag.");

        if ( (removalProbInput.get().getNTypes() != hiddenTraitFlagInput.get().getDimension()*2) || 
        		(removalProbInput.get().getNTypes() != nTypesInput.get()) )
            throw new IllegalArgumentException("Removal prob skyline type count does not match type count of model or hidden type flag.");

        if ( (rhoSamplingInput.get() != null && rhoSamplingInput.get().getNTypes() != hiddenTraitFlagInput.get().getDimension()*2) ||
        		(migRateInput.get().getNTypes() != nTypesInput.get()) )
            throw new IllegalArgumentException("Rho sampling type count does not match type count of model.");
    }
}
