package bdmm.parameterization;

import beast.core.parameter.RealParameter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ParameterizationTest {

    public double TOLERANCE = 1e-20;

    @Test
    public void basicTest() {

		RealParameter originParam = new RealParameter("2.0");

		Parameterization parameterization = new CanonicalParameterization();
		parameterization.initByName(
		        "typeSet", new TypeSet(2),
                "origin", originParam,
                "birthRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("4.0"), 2),
                "deathRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("3.0"), 2),
                "birthRateAmongDemes", new SkylineMatrixParameter(
                        null,
                        new RealParameter("0.0"), 2),
                "migrationRate", new SkylineMatrixParameter(
                        new RealParameter("1.0"),
                        new RealParameter("0.1 0.2"), 2),
                "samplingRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.5"), 2),
                "removalProb", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0"), 2),
                "rhoSampling", new TimedParameter(
                        originParam,
                        new RealParameter("0.0 0.0")));

		Assert.assertEquals(2, parameterization.getTotalIntervalCount());

        Assert.assertEquals(2, parameterization.getBirthRates().length);
        Assert.assertEquals(2, parameterization.getDeathRates().length);
        Assert.assertEquals(2, parameterization.getSamplingRates().length);
        Assert.assertEquals(2, parameterization.getRemovalProbs().length);
        Assert.assertEquals(2, parameterization.getRhoValues().length);

        for (int interval=0; interval<2; interval++) {
            double migRate = interval < 1 ? 0.1 : 0.2;

            for (int state1 = 0; state1 < 2; state1++) {
                Assert.assertEquals(4.0, parameterization.getBirthRates()[interval][state1], TOLERANCE);
                Assert.assertEquals(3.0, parameterization.getDeathRates()[interval][state1], TOLERANCE);
                Assert.assertEquals(1.5, parameterization.getSamplingRates()[interval][state1], TOLERANCE);
                Assert.assertEquals(1.0, parameterization.getRemovalProbs()[interval][state1], TOLERANCE);
                Assert.assertEquals(0.0, parameterization.getRhoValues()[interval][state1], TOLERANCE);

                for (int state2 = 0; state2 < 2; state2++) {
                    if (state2 == state1)
                        continue;

                    Assert.assertEquals(migRate, parameterization.getMigRates()[interval][state1][state2], TOLERANCE);
                    Assert.assertEquals(0.0, parameterization.getCrossBirthRates()[interval][state1][state2], TOLERANCE);
                }
            }
        }
    }

    @Test
    public void testClaSSEInput() {

        RealParameter originParam = new RealParameter("2.0");

        Parameterization parameterization = new CanonicalParameterization();

        /*
         * Input of speciation rates without using triplets
         */
        parameterization.initByName(
                "typeSet", new TypeSet(2),
                "origin", originParam,
                "birthRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("3.0"), 2),
                "deathRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("2.0"), 2),
                "samplingRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.5"), 2),
                "removalProb", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0"), 2),
                "cladogeneticBirthRate", new Skyline3DMatrixParameter(
                        null,
                        new RealParameter("2.0 3.0 4.0 5.0"), 2, null, null)
                );
        
        int intervalIdx=0;

        //Test whether values in cladogentic speciation rates matrix are indeed what they were specified to be in the inputs.
        Assert.assertEquals(2.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][1][0], TOLERANCE);
        Assert.assertEquals(3.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][1][1], TOLERANCE);
        Assert.assertEquals(4.0, parameterization.getCladogeneticBirthRates()[intervalIdx][1][0][0], TOLERANCE);
        Assert.assertEquals(5.0, parameterization.getCladogeneticBirthRates()[intervalIdx][1][1][0], TOLERANCE);

        /*
         * Input of speciation rates using triplets
         */
        Triplet a = new Triplet();
        a.initByName("parentState", "0",
                    "leftChildState", "0",
                    "rightChildState","1",
                    "tripletType", "value1");
        Triplet b = new Triplet();
        b.initByName("parentState", "0",
                "leftChildState", "1",
                "rightChildState","1",
                "tripletType", "value2");
        Triplet c = new Triplet();
        c.initByName("parentState", "1",
                "leftChildState", "0",
                "rightChildState","0",
                "tripletType", "value2");
        Triplet d = new Triplet();
        d.initByName("parentState", "1",
                "leftChildState", "1",
                "rightChildState","0",
                "tripletType", "value1");
        List<Triplet> tripletList = Arrays.asList(a,b,c,d);
        String tripletOrder = "value1 value2";

        parameterization.initByName(
                "typeSet", new TypeSet(2),
                "origin", originParam,
                "birthRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("3.0"), 2),
                "deathRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("2.0"), 2),
                "samplingRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.5"), 2),
                "removalProb", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0"), 2),
                "cladogeneticBirthRate", new Skyline3DMatrixParameter(
                        null,
                        new RealParameter("1.0 2.0"), 2, tripletList, tripletOrder)
        );

        //Test whether values in cladogentic speciation rates matrix are indeed what they were specified to be in the inputs.
        Assert.assertEquals(1.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][1][0], TOLERANCE);
        Assert.assertEquals(2.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][1][1], TOLERANCE);
        Assert.assertEquals(2.0, parameterization.getCladogeneticBirthRates()[intervalIdx][1][0][0], TOLERANCE);
        Assert.assertEquals(1.0, parameterization.getCladogeneticBirthRates()[intervalIdx][1][1][0], TOLERANCE);

        /*
         * Input of speciation rates using triplets with 6 types
         */
        a = new Triplet();
        a.initByName("parentState", "0",
                "leftChildState", "3",
                "rightChildState","4",
                "tripletType", "value1");
        b = new Triplet();
        b.initByName("parentState", "0",
                "leftChildState", "3",
                "rightChildState","3",
                "tripletType", "value2");
        c = new Triplet();
        c.initByName("parentState", "1",
                "leftChildState", "0",
                "rightChildState","5",
                "tripletType", "value3");
        d = new Triplet();
        d.initByName("parentState", "5",
                "leftChildState", "5",
                "rightChildState","2",
                "tripletType", "value2");
        Triplet e = new Triplet();
        e.initByName("parentState", "3",
                "leftChildState", "1",
                "rightChildState","5",
                "tripletType", "value4");
        tripletList = Arrays.asList(a,b,c,d, e);
        tripletOrder = "value1 value2 value3 value4";

        parameterization = new CanonicalParameterization();

        parameterization.initByName(
                "typeSet", new TypeSet(6),
                "origin", originParam,
                "birthRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("3.0"), 6),
                "deathRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("2.0"), 6),
                "samplingRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.5"), 6),
                "removalProb", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0"), 6),
                "cladogeneticBirthRate", new Skyline3DMatrixParameter(
                        null,
                        new RealParameter("1.0 2.0 3.0 4.0"), 6, tripletList, tripletOrder)
        );

        //Test whether values in cladogentic speciation rates matrix are indeed what they were specified to be in the inputs.
        Assert.assertEquals(1.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][4][3], TOLERANCE);
        Assert.assertEquals(2.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][3][3], TOLERANCE);
        Assert.assertEquals(3.0, parameterization.getCladogeneticBirthRates()[intervalIdx][1][5][0], TOLERANCE);
        Assert.assertEquals(2.0, parameterization.getCladogeneticBirthRates()[intervalIdx][5][5][2], TOLERANCE);
        Assert.assertEquals(4.0, parameterization.getCladogeneticBirthRates()[intervalIdx][3][5][1], TOLERANCE);
        Assert.assertEquals(0.0, parameterization.getCladogeneticBirthRates()[intervalIdx][3][1][5], TOLERANCE);
        Assert.assertEquals(0.0, parameterization.getCladogeneticBirthRates()[intervalIdx][3][3][1], TOLERANCE);
        Assert.assertEquals(0.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][1][0], TOLERANCE);
        Assert.assertEquals(0.0, parameterization.getCladogeneticBirthRates()[intervalIdx][0][2][0], TOLERANCE);

    }


    @Test
    public void testGetIntervalIndex() {
        RealParameter originParam = new RealParameter("2.0");

        Parameterization parameterization = new CanonicalParameterization();
		parameterization.initByName(
		        "typeSet", new TypeSet(2),
                "origin", originParam,
                "birthRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("4.0"), 2),
                "deathRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("3.0"), 2),
                "birthRateAmongDemes", new SkylineMatrixParameter(
                        null,
                        new RealParameter("0.0"), 2),
                "migrationRate", new SkylineMatrixParameter(
                        new RealParameter("1.0"),
                        new RealParameter("0.1 0.2"), 2),
                "samplingRate", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.5"), 2),
                "removalProb", new SkylineVectorParameter(
                        null,
                        new RealParameter("1.0"), 2),
                "rhoSampling", new TimedParameter(
                        originParam,
                        new RealParameter("0.0 0.0")));

        Assert.assertEquals(0, parameterization.getIntervalIndex(-3.0));
        Assert.assertEquals(0, parameterization.getIntervalIndex(0.0));
        Assert.assertEquals(0, parameterization.getIntervalIndex(0.1));
        Assert.assertEquals(0, parameterization.getIntervalIndex(1.0));
        Assert.assertEquals(1, parameterization.getIntervalIndex(1.1));
        Assert.assertEquals(1, parameterization.getIntervalIndex(1.9));
        Assert.assertEquals(1, parameterization.getIntervalIndex(2.0));
    }
}
