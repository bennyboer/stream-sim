package edu.hm.cs.bess.streamsim.sim.chickentest;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Benedikt Beil
 */
public class FastMarchingMethodTest extends AbstractSimulationTest {

    private static final double MAX_SIM_DURATION = 200;

    @Test
    public void FastMarchingMethodPassesChickenTest() {
        String fileName = "FastMarchingMethodChickenTestConfig.json";

        SimConfig config = createSimConfigFromFile(fileName);
        if (config == null) {
            Assertions.fail("Config is null");
            return;
        }

        StreamSimulator simulator = new StreamSimulator(buildState(config), config.getSeed());
        simulator.play();

        while (simulator.isRunning()) {
            // Wait until the simulation ends. The simulation will only end when all persons reach their target.

            if (simulator.getScheduler().currentTime() > MAX_SIM_DURATION) {
                Assertions.fail("Chicken test failed! The persons did not reach the target in the given time!");
            }
        }

        assertEquals(0, simulator.getPeopleCount());
    }
}
