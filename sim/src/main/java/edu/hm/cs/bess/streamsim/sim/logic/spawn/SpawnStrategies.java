package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Information about available spawn strategies.
 *
 * @author Benjamin Eder
 */
public class SpawnStrategies {

    /**
     * Supplier for the default spawn strategy.
     */
    public static Supplier<SpawnStrategy> DEFAULT = () -> new FixedRateSpawnStrategy(FixedRateSpawnStrategy.DEFAULT_FIXED_RATE);

    /**
     * Lookup of available spawn strategies.
     */
    public static final Map<String, Supplier<SpawnStrategy>> LOOKUP = new HashMap<>();

    static {
        LOOKUP.put(FixedRateSpawnStrategy.NAME, () -> new FixedRateSpawnStrategy(FixedRateSpawnStrategy.DEFAULT_FIXED_RATE));
        LOOKUP.put(PoissonSpawnStrategy.NAME, () -> new PoissonSpawnStrategy(PoissonSpawnStrategy.DEFAULT_LAMBDA));
    }

}
