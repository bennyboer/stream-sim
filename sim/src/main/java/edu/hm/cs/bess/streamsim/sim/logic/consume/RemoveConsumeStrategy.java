package edu.hm.cs.bess.streamsim.sim.logic.consume;

import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;

import java.util.Random;

/**
 * Consume strategy that simply removes people of the simulation world when they reach their target.
 *
 * @author Benjamin Eder
 */
public class RemoveConsumeStrategy implements ConsumeStrategy {

    /**
     * Name of the strategy.
     */
    public static final String NAME = "Remove";

    @Override
    public void reachedTarget(Target target, Person person, State state, Scheduler scheduler) {
        state.removeOccupant(person.getLocation());
    }

    @Override
    public void init(State state, Random rng) {
        // Nothing to do
    }

    @Override
    public String getName() {
        return RemoveConsumeStrategy.NAME;
    }

}
