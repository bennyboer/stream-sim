package edu.hm.cs.bess.streamsim.sim.logic.move;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;

import java.util.Random;

/**
 * Strategy for people to move.
 *
 * @author Benjamin Eder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EuclideanMoveStrategy.class, name = EuclideanMoveStrategy.NAME),
        @JsonSubTypes.Type(value = DijkstraMoveStrategy.class, name = DijkstraMoveStrategy.NAME),
        @JsonSubTypes.Type(value = FastMarchingMethodMovementStrategy.class, name = FastMarchingMethodMovementStrategy.NAME)
})
public interface MoveStrategy {
    /**
     * Called when the simulation is initialized.
     *
     * @param state starting state of the simulation
     * @param rng   random number generator to use
     */
    void init(State state, Random rng);

    /**
     * Called when the passed person needs to move
     *
     * @param person    to move
     * @param state     the current simulation state
     * @param scheduler of the simulation
     */
    void move(Person person, State state, Scheduler scheduler);

    /**
     * Get the strategy name.
     *
     * @return name
     */
    @JsonIgnore
    String getName();

    /**
     * Calculate the potential for the whole simulation world.
     * This is used in the visualization to display the current potential function.
     *
     * @param state to calculate potential for
     * @return potential
     */
    double[][] calculatePotential(State state) throws UnsupportedOperationException;

}
