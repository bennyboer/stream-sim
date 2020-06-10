package edu.hm.cs.bess.streamsim.sim.rimeatest;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EvacuationTest extends AbstractSimulationTest {

    final static double MAX_SIM_DURATION = 400;

    @Test
    public void ComparisonOfEvacuationWithTwoAndFourDoors() {
        String twoDoorsFileName = "TwoDoorsTestConfig.json";
        String fourDoorsFileName = "FourDoorsTestConfig.json";

        StreamSimulator twoDoorsSim = createSimulatorFromFile(twoDoorsFileName);
        StreamSimulator fourDoorsSim = createSimulatorFromFile(fourDoorsFileName);

        if (twoDoorsSim == null || fourDoorsSim == null) {
            Assertions.fail("Simulator is null");
        }

        twoDoorsSim.play();
        fourDoorsSim.play();

        while (twoDoorsSim.isRunning() || fourDoorsSim.isRunning()) {
            // Wait until the simulation ends. The simulation will only end when all persons reach their target.

            if (twoDoorsSim.getScheduler().currentTime() > MAX_SIM_DURATION) {
                Assertions.fail("The simulation takes too long. ");
            }

            if (fourDoorsSim.getScheduler().currentTime() > MAX_SIM_DURATION) {
                Assertions.fail("The simulation takes too long. ");
            }
        }

        final double totalTwoDoorsSimDuration = twoDoorsSim.getScheduler().currentTime();
        final double totalFourDoorsSimDuration = fourDoorsSim.getScheduler().currentTime();

        Assertions.assertTrue(totalTwoDoorsSimDuration > totalFourDoorsSimDuration);

        final double fasterFactor = totalTwoDoorsSimDuration / totalFourDoorsSimDuration;
        System.out.println("Faster factor is " + fasterFactor);

        Assertions.assertTrue(fasterFactor > 1.7);
    }

    private StreamSimulator createSimulatorFromFile(@NotNull String fileName) {
        SimConfig config = createSimConfigFromFile(fileName);
        if (config == null) {
            return null;
        }

        return new StreamSimulator(buildState(config), config.getSeed());
    }

}
