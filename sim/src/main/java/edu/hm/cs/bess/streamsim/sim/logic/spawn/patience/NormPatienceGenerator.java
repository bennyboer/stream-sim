package edu.hm.cs.bess.streamsim.sim.logic.spawn.patience;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.logic.util.CommonsMathRandomAdapter;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Patience generator emitting normal distributed patience.
 *
 * @author Benjamin Eder
 */
public final class NormPatienceGenerator implements PatienceGenerator {

    /**
     * The default mean expectation.
     */
    public static final int DEFAULT_MEAN = 20;

    /**
     * The default deviation.
     */
    public static final int DEFAULT_DEVIATION = 10;

    /**
     * Expected mean value for the normal distribution.
     */
    private final int mean;

    /**
     * Maximum deviation from the mean.
     */
    private final int maxDeviation;

    /**
     * Normal distribution as base of the generator.
     */
    @Nullable
    private NormalDistribution dist;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public NormPatienceGenerator(
            @JsonProperty("mean") int meanExpectation,
            @JsonProperty("maxDeviation") int maxDeviation
    ) {
        this.mean = meanExpectation;
        this.maxDeviation = maxDeviation;
    }

    @Override
    public int generate() {
        double min = Math.max(0, mean - maxDeviation);
        double max = mean + maxDeviation;

        if (maxDeviation == 0 || dist == null) {
            return mean;
        } else {
            return (int) Math.round(Math.max(Math.min(dist.sample(), max), min));
        }
    }

    @Override
    public void init(Random rng) {
        if (maxDeviation > 0) {
            // Maximum deviation needs to be positive because of commons maths strictly positive requirement.
            double sd = (double) maxDeviation / 3;
            dist = new NormalDistribution(new CommonsMathRandomAdapter(rng), mean, sd);
        }
    }

    /**
     * Get the mean patience.
     *
     * @return mean patience
     */
    public int getMean() {
        return mean;
    }

    /**
     * Get the maximum deviation from the mean.
     *
     * @return maximum deviation from mean
     */
    public int getMaxDeviation() {
        return maxDeviation;
    }

}
