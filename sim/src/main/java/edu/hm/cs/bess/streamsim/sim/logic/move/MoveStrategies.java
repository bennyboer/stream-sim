package edu.hm.cs.bess.streamsim.sim.logic.move;

import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Information about move strategies.
 *
 * @author Benjamin Eder
 */
public class MoveStrategies {

    /**
     * Lookup of available move strategies.
     */
    public static final Map<String, Supplier<MoveStrategy>> LOOKUP = new HashMap<>();

    static {
        LOOKUP.put(EuclideanMoveStrategy.NAME, () -> new EuclideanMoveStrategy(new MollifierConfiguration(MollifierConfiguration.DEFAULT_RANGE, MollifierConfiguration.DEFAULT_STRENGTH), EuclideanMoveStrategy.DEFAULT_RADIUS));
        LOOKUP.put(DijkstraMoveStrategy.NAME, () -> new DijkstraMoveStrategy(new MollifierConfiguration(MollifierConfiguration.DEFAULT_RANGE, MollifierConfiguration.DEFAULT_STRENGTH), DijkstraMoveStrategy.DEFAULT_RADIUS));
        LOOKUP.put(FastMarchingMethodMovementStrategy.NAME, () -> new FastMarchingMethodMovementStrategy(new MollifierConfiguration(MollifierConfiguration.DEFAULT_RANGE, MollifierConfiguration.DEFAULT_STRENGTH), FastMarchingMethodMovementStrategy.DEFAULT_RADIUS));
    }

    /**
     * Supplier for the default move strategy.
     */
    public static final Supplier<MoveStrategy> DEFAULT = LOOKUP.get(EuclideanMoveStrategy.NAME);
}
