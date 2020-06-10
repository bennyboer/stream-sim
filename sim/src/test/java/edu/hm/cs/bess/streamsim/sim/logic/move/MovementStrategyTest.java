package edu.hm.cs.bess.streamsim.sim.logic.move;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import edu.hm.cs.bess.streamsim.sim.model.state.State;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class MovementStrategyTest extends AbstractSimulationTest {

    String FILE_PREFIX = "movementstrategy/";

    String BASE_CONFIG = "BaseTestConfig.json";
    String WALL_CONFIG = "WallTestConfig.json";

    abstract void calculatePotentialTest();

    abstract void calculatePotentialWithWallTest();

    abstract MoveStrategy CreateSut();

    void testPotential(String filePath, double[][] expectedPotential) {
        SimConfig config = createSimConfigFromFile(filePath);
        if (config == null) {
            fail("The config is null!");
            return;
        }

        State state = buildState(config);

        MoveStrategy sut = CreateSut();
        sut.init(state, new Random());
        double[][] actualPotential = sut.calculatePotential(state);

        assertThat(actualPotential).isEqualTo(expectedPotential);
    }
}
