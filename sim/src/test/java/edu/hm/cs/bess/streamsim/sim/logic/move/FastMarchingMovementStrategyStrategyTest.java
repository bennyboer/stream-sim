package edu.hm.cs.bess.streamsim.sim.logic.move;

import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import org.junit.jupiter.api.Test;

public class FastMarchingMovementStrategyStrategyTest extends MovementStrategyTest {

    @Test
    @Override
    public void calculatePotentialTest() {
        double[][] expected = {
                {3.2524357066126695, 2.545328925426122, 2.0},
                {2.545328925426122, 1.7071067811865475, 1.0},
                {2.0, 1.0, 0.0}
        };

        testPotential(FILE_PREFIX + FAST_MARCHING_PREFIX + BASE_CONFIG, expected);
    }

    @Test
    @Override
    public void calculatePotentialWithWallTest() {
        double[][] expected = {
                {10.741804598955463, 10.370902299477738, Double.MAX_VALUE, 1.0, 0.0},
                {9.813132706281781, 9.370902299477738, Double.MAX_VALUE, 1.7071067811865475, 1.0},
                {8.916231224903841, 8.370902299477738, Double.MAX_VALUE, 2.545328925426122, 2.0},
                {8.078009080664286, 7.370902299477738, Double.MAX_VALUE, 3.4422304068040503, 3.0},
                {7.370902299477738, 6.370902299477738, 5.370902299477738, 4.370902299477738, 4.0}
        };
        testPotential(FILE_PREFIX + FAST_MARCHING_PREFIX + WALL_CONFIG, expected);
    }

    @Override
    public MoveStrategy CreateSut() {
        return new FastMarchingMethodMovementStrategy(new MollifierConfiguration(MollifierConfiguration.DEFAULT_RANGE, MollifierConfiguration.DEFAULT_STRENGTH), DijkstraMoveStrategy.DEFAULT_RADIUS);
    }
}
