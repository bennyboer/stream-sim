package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Spawn strategy spawning new people at a fixed rate.
 *
 * @author Benjamin Eder
 * @author Benedikt Beil
 */
public class FixedRateSpawnStrategy extends DefaultSpawnStrategy {

    /**
     * Name of the strategy.
     */
    public static final String NAME = "Fixed rate";

    /**
     * The default fixed rate.
     */
    public static int DEFAULT_FIXED_RATE = 10;

    /**
     * Fixed rate to spawn people with (Every n time units).
     */
    private final double fixedRate;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FixedRateSpawnStrategy(@JsonProperty("fixedRate") double fixedRate) {
        this.fixedRate = fixedRate;
    }

    @Override
    public String getName() {
        return FixedRateSpawnStrategy.NAME;
    }

    @Override
    public double getNextSpawnTime() {
        return fixedRate;
    }

    public double getFixedRate() {
        return fixedRate;
    }
}
