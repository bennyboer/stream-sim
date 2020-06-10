package edu.hm.cs.bess.streamsim.sim.logic.spawn.speed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Random;

/**
 * Generator for the speed of a person.
 *
 * @author Benjamin Eder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedSpeedGenerator.class, name = "Fixed"),
        @JsonSubTypes.Type(value = NormSpeedGenerator.class, name = "Norm")
})
public interface SpeedGenerator {

    /**
     * Generate the speed for a person in (time units per cell).
     *
     * @return generated speed
     */
    double generateSpeed();

    /**
     * Called when the simulation is initialized.
     *
     * @param rng random number generator to use
     */
    void init(Random rng);

    /**
     * Get the name of the generator.
     *
     * @return name of the generator
     */
    String getName();

}
