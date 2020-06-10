package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;

import java.util.Random;


/**
 * Strategy for spawning new people at sources.
 *
 * @author Benjamin Eder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedRateSpawnStrategy.class, name = FixedRateSpawnStrategy.NAME),
        @JsonSubTypes.Type(value = PoissonSpawnStrategy.class, name = PoissonSpawnStrategy.NAME)
})
public interface SpawnStrategy {

    /**
     * Called when a new person should be spawned.
     *
     * @param source    the source that should spawn a new person
     * @param state     the current simulation state
     * @param scheduler of the simulation
     */
    void spawn(Source source, State state, Scheduler scheduler);


    /**
     * Called when the simulation is initialized.
     *
     * @param state starting state of the simulation
     * @param rng   random number generator to use
     */
    void init(State state, Random rng);

    /**
     * Get the next spawn time.
     *
     * @return next spawn time
     */
    @JsonIgnore
    double getNextSpawnTime();

    /**
     * Get the strategy name.
     *
     * @return name
     */
    @JsonIgnore
    String getName();
}
