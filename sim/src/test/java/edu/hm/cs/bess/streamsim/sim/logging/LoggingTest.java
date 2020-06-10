package edu.hm.cs.bess.streamsim.sim.logging;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingTest extends AbstractSimulationTest {

    private File folder = new File(System.getProperty("user.dir"), "logs");

    @AfterEach
    public void deleteFiles() throws IOException {
        Arrays.stream(folder.listFiles()).forEach(File::delete);
        if (!folder.delete()) {
            throw new IOException();
        }
    }

    @Test
    public void shouldLogTest() throws InterruptedException {

        StreamSimulator simulator = initSimulationWithConfig("LoggingConfig.json");
        simulator.play();

        while (simulator.isRunning()) {
            Thread.sleep(1);
        }
        simulator.saveLogs();
        assertThat(folder.list().length).isEqualTo(2);
        assertThat(Arrays.stream(folder.listFiles()).filter(file -> file.getTotalSpace() > 0).count()).isEqualTo(2);

    }

    private StreamSimulator initSimulationWithConfig(final String fileName) {
        SimConfig config = createSimConfigFromFile(fileName);
        return new StreamSimulator(buildState(config), config.getSeed(), true, folder, "sim_log");
    }
}
