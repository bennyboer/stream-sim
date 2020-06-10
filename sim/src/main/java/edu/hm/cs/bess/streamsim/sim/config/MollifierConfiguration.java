package edu.hm.cs.bess.streamsim.sim.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration of the mollifier function we use.
 *
 * @author Benjamin Eder
 */
public final class MollifierConfiguration {

    /**
     * The default width (range) of the mollifier.
     */
    public static final int DEFAULT_RANGE = 2;

    /**
     * The default height (strength) of the mollifier.
     */
    public static final double DEFAULT_STRENGTH = 1.5;

    /**
     * Width (Range) of the mollifier.
     */
    private final int range;

    /**
     * Height (Strength) of the mollifier.
     */
    private final double strength;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MollifierConfiguration(@JsonProperty("range") int range, @JsonProperty("strength") double strength) {
        this.range = range;
        this.strength = strength;
    }

    public int getRange() {
        return range;
    }

    public double getStrength() {
        return strength;
    }

}
