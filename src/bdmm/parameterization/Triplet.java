/**
 * Created by Fabio K. Mendes (fkmendes)
 */

package bdmm.parameterization;

import beast.core.BEASTObject;
import beast.core.Input;
import beast.core.Input.Validate;

public class Triplet extends BEASTObject {

	final public Input<Integer> parentStateInput = new Input<>("parentState", "Type (trait state) of parent.", Validate.REQUIRED);
	final public Input<Integer> leftChildInput = new Input<>("leftChildState", "Type (trait state) of left child.", Validate.REQUIRED);
	final public Input<Integer> rightChildInput = new Input<>("rightChildState", "Type (trait state) of right child.", Validate.REQUIRED);	
	final public Input<String> tripletTypeInput = new Input<>("tripletType", "Triplet type (e.g., sympatric, subsympatric, vicariant).", Validate.REQUIRED);
	
	private int[] cladogeneticEvent;
	private String tripletType;
	
	@Override
	public void initAndValidate() {
		cladogeneticEvent = new int[3];
		cladogeneticEvent[0] = parentStateInput.get();
		cladogeneticEvent[1] = leftChildInput.get();
		cladogeneticEvent[2] = rightChildInput.get();
		tripletType = tripletTypeInput.get();
	}

	public int[] getTriplet() {
		return cladogeneticEvent;
	}
	
	public String getTripletType() {
		return tripletType;
	}
}
