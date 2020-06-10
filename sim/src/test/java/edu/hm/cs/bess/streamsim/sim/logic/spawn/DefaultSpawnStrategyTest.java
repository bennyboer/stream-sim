package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.EventDrivenScheduler;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Beil Benedikt
 */
public class DefaultSpawnStrategyTest extends AbstractSimulationTest {

    @Test
    public void spawnTest() {
        final Scheduler scheduler = new EventDrivenScheduler();
        final String fileName = "DefaultSpawnStrategyTestConfig.json";

        SimConfig config = createSimConfigFromFile(fileName);
        assert config != null;
        State state = buildState(config);
        StreamSimulator simulator = new StreamSimulator(buildState(config), config.getSeed());
        final int initNumOfPeople = state.getObjectTypeCount(SimObjectType.PERSON);

        // init the simulation
        simulator.play();
        simulator.pause();

        assert simulator.getSources() != null;
        final Source source = simulator.getSources().get(0);


        // spawn two people
        source.getConfiguration().getSpawnStrategy().spawn(source, state, scheduler);
        source.getConfiguration().getSpawnStrategy().spawn(source, state, scheduler);


        final int numOfPeopleAfterSpawn = state.getObjectTypeCount(SimObjectType.PERSON);

        Assertions.assertTrue(numOfPeopleAfterSpawn > initNumOfPeople);
        Assertions.assertTrue(numOfPeopleAfterSpawn == 2);
    }

}
