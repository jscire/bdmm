package bdmm.parameterization;

import bdmm.util.Utils;
import beast.core.CalculationNode;
import beast.core.Input;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.Tree;

import java.util.Arrays;

/**
 * Parameter in which the value of each element corresponds to a particular time.
 */
public class TimedParameter extends CalculationNode {

    public Input<RealParameter> timesInput = new Input<>(
            "times",
            "Times associated with probabilities.",
            Input.Validate.REQUIRED);

    public Input<Boolean> timesAreAgesInput = new Input<>(
            "timesAreAges",
            "True if times are ages (before most recent sample) instead of times after origin.",
            false);

    public Input<Boolean> timesAreRelativeInput = new Input<>(
            "timesAreRelative",
            "True if times are relative to the origin. (Default false.)",
            false);

    public Input<RealParameter> originInput = new Input<>("origin",
            "Parameter specifying origin of process.");

    public Input<Tree> treeInput = new Input<>("tree",
            "Tree when root time is used to identify the start of the process.");

    public Input<Integer> nTypesInput = new Input<>("nTypes",
            "Number of distinct types in model.  If unspecified, inferred" +
                    "from length of other parameter inputs.  Use this when a " +
                    "single parameter value is shared among all types.");

    public Input<RealParameter> valuesInput = new Input<>(
            "values",
            "Probability values associated with each time.",
            Input.Validate.REQUIRED);

    boolean timesAreAges, timesAreRelative, inputIsScalar;

    double[] times, storedTimes;
    double[][] values, storedValues;
    double[] valuesAtTime, zeroValuesAtTime;
    int nTimes, nTypes;

    boolean isDirty;

    public TimedParameter() { }

    public TimedParameter(RealParameter timesParam, RealParameter valuesParam) {
        timesInput.setValue(timesParam, this);
        valuesInput.setValue(valuesParam, this);
        initAndValidate();
    }

    public TimedParameter(RealParameter timesParam, RealParameter valuesParam, Integer n) {
        timesInput.setValue(timesParam, this);
        valuesInput.setValue(valuesParam, this);
        nTypesInput.setValue(n, this);
        initAndValidate();
    }

    public TimedParameter(RealParameter timesParam, RealParameter valuesParam, RealParameter originParam) {
        timesInput.setValue(timesParam, this);
        valuesInput.setValue(valuesParam, this);
        originInput.setValue(originParam, this);
        timesAreAgesInput.setValue(true, this);
        initAndValidate();
    }

    public TimedParameter(RealParameter timesParam, RealParameter valuesParam, Tree tree) {
        timesInput.setValue(timesParam, this);
        valuesInput.setValue(valuesParam, this);
        treeInput.setValue(tree, this);
        timesAreAgesInput.setValue(true, this);
        initAndValidate();
    }

    @Override
    public void initAndValidate() {
        timesAreAges = timesAreAgesInput.get();
        timesAreRelative = timesAreRelativeInput.get();

        if ((timesAreAges || timesAreRelative) && (originInput.get() == null && treeInput.get() == null))
            throw new IllegalArgumentException("Origin parameter or tree must be supplied " +
                    "when times are given as ages and/or when times are relative.");

        if (originInput.get() != null && treeInput.get() != null)
            throw new IllegalArgumentException("Only one of origin or tree " +
                    "should be specified.");

        nTimes = timesInput.get().getDimension();
        times = new double[nTimes];
        storedTimes = new double[nTimes];

        int valsPerInterval = valuesInput.get().getDimension()/nTimes;
        inputIsScalar = valsPerInterval==1;

        if (nTypesInput.get() != null) {
            nTypes = nTypesInput.get();

            if (!inputIsScalar && nTypes != valsPerInterval)
                throw new IllegalArgumentException("TimedParameter has an incorrect " +
                        "number of elements.");
        } else {
            nTypes = valsPerInterval;
        }

        values = new double[nTimes][nTypes];
        storedValues = new double[nTimes][nTypes];

        valuesAtTime = new double[nTypes];

        zeroValuesAtTime = new double[nTypes];
        Arrays.fill(zeroValuesAtTime, 0.0);

        isDirty = true;
    }

    public int getNTypes() {
        update();

        return nTypes;
    }

    public double[] getTimes() {
        update();

        return times;
    }

    public double[] getValuesAtTime(double time) {
        update();

        int intervalIdx = Arrays.binarySearch(times, time);

        if (intervalIdx<0)
            return zeroValuesAtTime;

        System.arraycopy(values[intervalIdx], 0, valuesAtTime, 0, nTypes);

        return valuesAtTime;
    }

    private void update() {
        if (!isDirty)
            return;

        updateTimes();
        updateValues();

        isDirty = false;
    }

    private void updateTimes() {
        for (int i=0; i<nTimes; i++)
            times[i] = timesInput.get().getValue(i);

        if (timesAreRelative) {
            double startAge = originInput.get() != null
                    ? originInput.get().getValue()
                    : treeInput.get().getRoot().getHeight();

            for (int i=0; i<nTimes; i++)
                times[i] *= startAge;
        }

        if (timesAreAges) {
            Utils.reverseDoubleArray(times);

            double startAge = originInput.get() != null
                    ? originInput.get().getValue()
                    : treeInput.get().getRoot().getHeight();

            for (int i=0; i<times.length; i++) {
                times[i] = startAge-times[i];
            }
        }
    }

    private void updateValues() {
        for (int timeIdx=0; timeIdx<nTimes; timeIdx++) {
            for (int typeIdx=0; typeIdx<nTypes; typeIdx++) {
                if (inputIsScalar)
                    values[timeIdx][typeIdx] = valuesInput.get().getValue(timeIdx);
                else
                    values[timeIdx][typeIdx] = valuesInput.get().getValue(timeIdx*nTypes + typeIdx);
            }
        }

        if (timesAreAges)
            Utils.reverseArray(values);
    }

    @Override
    protected void store() {
        super.store();

        System.arraycopy(times, 0, storedTimes, 0, nTimes);

        for (int timeIdx=0; timeIdx<nTimes; timeIdx++)
            System.arraycopy(values[timeIdx], 0, storedValues[timeIdx], 0, nTypes);
    }

    @Override
    protected void restore() {
        super.restore();
    }

    @Override
    protected boolean requiresRecalculation() {
        isDirty = true;

        return true;
    }
}
