package edu.hm.cs.bess.streamsim.sim.logic.spawn.patience;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Random;

/**
 * Generator generating patience values for a person.
 *
 * @author Benjamin Eder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NormPatienceGenerator.class, name = "Norm"),
})
public interface PatienceGenerator {

    /**
     * Generate the patience for a person.
     *
     * @return generated patience
     */
    int generate();

    /**
     * Called when the simulation is initialized.
     *
     * @param rng random number generator to use
     */
    void init(Random rng);

}
