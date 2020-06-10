package edu.hm.cs.bess.streamsim.sim.logic.spawn.patience;

import java.util.function.Supplier;

/**
 * Information about available patience generators.
 *
 * @author Benjamin Eder
 */
public class PatienceGenerators {

    /**
     * Supplier for the default patience generator.
     */
    public static Supplier<PatienceGenerator> DEFAULT = () -> new NormPatienceGenerator(NormPatienceGenerator.DEFAULT_MEAN, NormPatienceGenerator.DEFAULT_DEVIATION);

}
