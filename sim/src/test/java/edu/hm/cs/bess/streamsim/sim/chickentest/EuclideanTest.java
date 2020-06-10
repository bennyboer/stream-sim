package edu.hm.cs.bess.streamsim.sim.chickentest;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Benedikt Beil
 */
public class EuclideanTest extends AbstractSimulationTest {

    private static final double MAX_SIM_DURATION = 2000;

    @Test
    public void euclideanNotPassesChickenTest() {
        String fileName = "EuclideanChickenTestConfig.json";

        SimConfig config = createSimConfigFromFile(fileName);
        if (config == null) {
            Assertions.fail("Config is null");
            return;
        }

        StreamSimulator simulator = new StreamSimulator(buildState(config), config.getSeed());
        simulator.play();

        while (simulator.isRunning()) {
            if (simulator.getScheduler().currentTime() > MAX_SIM_DURATION) {
                break;
            }
        }

        if (simulator.getPeopleCount() != 10) {
            Assertions.fail("Someone has entered the target. The Chicken Test has been passed!");
        }
    }
}
