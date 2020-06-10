package edu.hm.cs.bess.streamsim.sim.logic.spawn.speed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.logic.util.CommonsMathRandomAdapter;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Random;

/**
 * Speed generator just emitting the same speed for every person.
 *
 * @author Konstantin Schlosser
 */
public final class NormSpeedGenerator implements SpeedGenerator {

    /**
     * Name of the generator.
     */
    public static final String NAME = "Norm";

    /**
     * The default mean expectation (in cells/timeunit).
     * <p>
     * This value has been calculated from the speed of 1.34 m/s.
     * When defining a cell to be 0.4m (40cm) wide, we have a speed of 3.34 cells/time unit.
     */
    public static final double DEFAULT_MEAN_EXPECTATION = 3.34;

    /**
     * The default standard deviation (in cells/timeunit).
     * This value has been calculated form the standard deviation of 0.26 m/s.
     */
    public static final double DEFAULT_STANDARD_DEVIATION = 0.65;

    /**
     * Expected mean value for the normal distribution (in cells/timeunit).
     */
    private final double meanExpectation;

    /**
     * Standard deviation for the normal distribution (in cells/timeunit).
     */
    private final double standardDeviation;

    /**
     * Normal distribution as base of the generator.
     */
    private NormalDistribution dist;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public NormSpeedGenerator(@JsonProperty("meanExpectation") double meanExpectation, @JsonProperty("standardDeviation") double standardDeviation) {
        this.meanExpectation = meanExpectation;
        this.standardDeviation = standardDeviation;
    }

    @Override
    public double generateSpeed() {
        double min = meanExpectation - standardDeviation;
        double max = meanExpectation + standardDeviation;

        return Math.max(Math.min(dist.sample(), max), min);
    }

    @Override
    public void init(Random rng) {
        dist = new NormalDistribution(new CommonsMathRandomAdapter(rng), meanExpectation, standardDeviation);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Get the mean expected speed (in cells/time unit).
     *
     * @return mean expected speed
     */
    public double getMeanExpectation() {
        return meanExpectation;
    }

    /**
     * Get the speed standard deviation (in cells/time unit).
     *
     * @return speed standard deviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

}
