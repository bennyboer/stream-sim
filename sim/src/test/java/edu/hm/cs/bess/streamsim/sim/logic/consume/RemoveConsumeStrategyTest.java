package edu.hm.cs.bess.streamsim.sim.logic.consume;

import edu.hm.cs.bess.streamsim.sim.config.TargetConfiguration;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class RemoveConsumeStrategyTest {

    @Test
    public void reachedTargetTest() {
        final ConsumeStrategy sut = CreateSut();
        State state = new State(3, 3);

        Target target = new Target(new Location(2, 2), new TargetConfiguration(sut));
        Person person = new Person(new Location(0, 0), target.getLocation(), new Location(1, 1), 5.0, 123.0, 1 );

        state.setCellOccupant(target, target.getLocation());
        state.setCellOccupant(person, person.getLocation());


        sut.reachedTarget(target, person, state, null);

        Optional<SimObject> result = state.getCellOccupant(person.getLocation());
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, state.getObjectTypeCount(SimObjectType.PERSON));
    }

    private ConsumeStrategy CreateSut() {
        return ConsumeStrategies.LOOKUP.get(RemoveConsumeStrategy.NAME).get();
    }

}
