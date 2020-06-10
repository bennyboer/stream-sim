package edu.hm.cs.bess.streamsim.sim.logic.consume;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Information about available spawn strategies.
 *
 * @author Benjamin Eder
 */
public class ConsumeStrategies {

    /**
     * Supplier for the default consume strategy.
     */
    public static Supplier<ConsumeStrategy> DEFAULT = RemoveConsumeStrategy::new;

    /**
     * Lookup of available consume strategies.
     */
    public static final Map<String, Supplier<ConsumeStrategy>> LOOKUP = new HashMap<>();

    static {
        LOOKUP.put(RemoveConsumeStrategy.NAME, RemoveConsumeStrategy::new);
        LOOKUP.put(ReviveConsumeStrategy.NAME, ReviveConsumeStrategy::new);
    }

}
