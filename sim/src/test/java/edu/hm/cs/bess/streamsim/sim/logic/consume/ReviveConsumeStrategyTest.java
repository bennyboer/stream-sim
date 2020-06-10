package edu.hm.cs.bess.streamsim.sim.logic.consume;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * @author Beil Benedikt
 */
public class ReviveConsumeStrategyTest extends AbstractSimulationTest {
    @Test
    public void reachedTargetTest() {
        final String fileName = "ReviveConsumeStrategyTestConfig.json";

        SimConfig config = createSimConfigFromFile(fileName);
        assert config != null;
        State state = buildState(config);

        StreamSimulator simulator = new StreamSimulator(state, config.getSeed());

        final int initNumOfPeople = simulator.getPeopleCount();
        Assertions.assertEquals(0, initNumOfPeople);

        final Location initLocationOfThePerson = new Location(3, 3);
        final Source source = (Source) simulator.getCurrentState().getObjectsForType(SimObjectType.SOURCE).iterator().next();
        final Target target = (Target) simulator.getCurrentState().getObjectsForType(SimObjectType.TARGET).iterator().next();

        Person person = new Person(source.getLocation(), target.getLocation(), initLocationOfThePerson, 5.0, 123.0, 1);

        simulator.getCurrentState().setCellOccupant(person, person.getLocation());

        // init the simulation
        simulator.play();
        simulator.pause();

        final int numOfPeopleAfterAdd = simulator.getPeopleCount();
        Assertions.assertEquals(1, numOfPeopleAfterAdd);

        target.getConfiguration().getConsumeStrategy().reachedTarget(target, person, simulator.getCurrentState(), simulator.getScheduler());

        final int numOfPeopleAfterRevive = simulator.getPeopleCount();
        Assertions.assertEquals(1, numOfPeopleAfterRevive);

        Optional<SimObject> result = state.getCellOccupant(initLocationOfThePerson);
        Assertions.assertTrue(result.isEmpty());

        final double distanceBetweenPersonAndSource = target.getConfiguration().getConsumeStrategy().distance(person.getLocation(), source.getLocation());
        Assertions.assertTrue(distanceBetweenPersonAndSource < 1.5);
    }
}
