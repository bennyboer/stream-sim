package edu.hm.cs.bess.streamsim.sim.logic.spawn.speed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

/**
 * Speed generator just emitting the same speed for every person.
 *
 * @author Benjamin Eder
 */
public final class FixedSpeedGenerator implements SpeedGenerator {

    /**
     * Name of the generator.
     */
    public static final String NAME = "Fixed";

    /**
     * The default mean expectation (in cells/timeunit).
     * <p>
     * This value has been calculated from the speed of 1.34 m/s.
     * When defining a cell to be 0.4m (40cm) wide, we have a speed of 3.34 cells/time unit.
     */
    public static final double DEFAULT_SPEED = 3.34;

    /**
     * Speed to set for each person (in time units per cell).
     */
    private final double speed;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FixedSpeedGenerator(@JsonProperty("speed") double speed) {
        this.speed = speed;
    }

    @Override
    public double generateSpeed() {
        return speed;
    }

    @Override
    public void init(Random rng) {
        // Nothing to do.
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Get the fixed speed (in cells/timeunit).
     *
     * @return fixed speed
     */
    public double getSpeed() {
        return speed;
    }

}
