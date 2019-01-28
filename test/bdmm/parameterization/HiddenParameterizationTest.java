package bdmm.parameterization;

import org.junit.Assert;
import org.junit.Test;

import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;

public class HiddenParameterizationTest {
	
	public double TOLERANCE = 1e-20;

    @Test
    public void basicTest() {

		RealParameter originParam = new RealParameter("2.0");

		Parameterization parameterization = new HiddenParameterization();
		parameterization.initByName(
		        "nTypes", 4,
		        "hiddenTraitFlag", new IntegerParameter("2 2"),
		        "cidFlag", new IntegerParameter("1"),

                "origin", originParam,
                "birthRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0 2.0 3.0 4.0"), 4),
                "deathRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0 2.0 3.0 4.0"), 4),
                "birthRateAmongDemes", new SkylineMatrixParameter(
                        null,
                        new RealParameter("0.0"), 4),
                "migrationRate", new SkylineMatrixParameter(
                        null,
                        new RealParameter("0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0 1.1 1.2"), 4),
                "samplingRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.5"), 4),
                "removalProb", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0"), 4)
                );
    
    System.out.println("Number of intervals: " + parameterization.getTotalIntervalCount());

//    Assert.assertEquals(4, parameterization.getBirthRates().length);
//    Assert.assertEquals(4, parameterization.getDeathRates().length);
//    Assert.assertEquals(2, parameterization.getSamplingRates().length);
//    Assert.assertEquals(2, parameterization.getRemovalProbs().length);
//    Assert.assertEquals(2, parameterization.getRhoValues().length);
    
    }
}
