package edu.hm.cs.bess.streamsim.sim.logic.spawn.speed;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Information about available speed generators.
 *
 * @author Benjamin Eder
 */
public class SpeedGenerators {

    /**
     * Supplier for the default speed generator.
     */
    public static Supplier<SpeedGenerator> DEFAULT = () -> new FixedSpeedGenerator(FixedSpeedGenerator.DEFAULT_SPEED);

    /**
     * Lookup of available speed strategies.
     */
    public static final Map<String, Supplier<SpeedGenerator>> LOOKUP = new HashMap<>();

    static {
        LOOKUP.put(FixedSpeedGenerator.NAME, () -> new FixedSpeedGenerator(FixedSpeedGenerator.DEFAULT_SPEED));
        LOOKUP.put(NormSpeedGenerator.NAME, () -> new NormSpeedGenerator(NormSpeedGenerator.DEFAULT_MEAN_EXPECTATION, NormSpeedGenerator.DEFAULT_STANDARD_DEVIATION));
    }

}
