package edu.hm.cs.bess.streamsim.sim.logic.consume;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;

import java.util.Random;

/**
 * Strategy applied to people that reached their target.
 *
 * @author Benjamin Eder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RemoveConsumeStrategy.class, name = RemoveConsumeStrategy.NAME),
        @JsonSubTypes.Type(value = ReviveConsumeStrategy.class, name = ReviveConsumeStrategy.NAME)
})
public interface ConsumeStrategy {

    /**
     * Called when the passed person reaches their target.
     *
     * @param target    the target that has been reached
     * @param person    that reached their target.
     * @param state     the current simulation state
     * @param scheduler of the simulation
     */
    void reachedTarget(Target target, Person person, State state, Scheduler scheduler);

    /**
     * Called when the simulation is initialized.
     *
     * @param state starting state of the simulation
     * @param rng   random number generator to use
     */
    void init(State state, Random rng);

    /**
     * Get the strategy name.
     *
     * @return name
     */
    @JsonIgnore
    String getName();

    /**
     * Calculate the euclidean distance from the passed location to the passed location.
     *
     * @param from location
     * @param to   location
     * @return euclidean distance
     */
    default double distance(Location from, Location to) {
        return Math.hypot(from.getRow() - to.getRow(), from.getColumn() - to.getColumn());
    }

}
