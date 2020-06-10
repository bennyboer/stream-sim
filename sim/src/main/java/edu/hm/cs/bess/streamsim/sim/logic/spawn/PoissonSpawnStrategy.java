package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.logic.util.CommonsMathRandomAdapter;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.Random;

/**
 * Strategy spawning people after a configured poisson distribution.
 *
 * @author Konstantin Schlosser
 * @author Benedikt Beil
 */
public class PoissonSpawnStrategy extends DefaultSpawnStrategy {

    /**
     * Name of the strategy.
     */
    public static final String NAME = "Poisson";

    /**
     * The default poisson parameter lambda.
     * It specifies the mean event rate.
     */
    public static double DEFAULT_LAMBDA = 10.0;

    /**
     * The mean event rate lambda.
     */
    private final double lambda;

    private PoissonDistribution dist;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PoissonSpawnStrategy(@JsonProperty("lambda") double lambda) {
        this.lambda = lambda;
    }

    @Override
    public void init(State state, Random rng) {
        this.rng = rng;
        cachedTargets = findTargetsInState(state);
        dist = new PoissonDistribution(new CommonsMathRandomAdapter(rng), this.lambda, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
    }

    @Override
    public String getName() {
        return PoissonSpawnStrategy.NAME;
    }

    @Override
    public double getNextSpawnTime() {
        return dist.sample();
    }

    /**
     * Get the mean event rate (lambda).
     *
     * @return mean event rate
     */
    public double getLambda() {
        return lambda;
    }
}
