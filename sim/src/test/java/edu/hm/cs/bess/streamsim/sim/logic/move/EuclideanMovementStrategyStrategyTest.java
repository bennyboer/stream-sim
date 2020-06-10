package edu.hm.cs.bess.streamsim.sim.logic.move;

import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import org.junit.jupiter.api.Test;

public class EuclideanMovementStrategyStrategyTest extends MovementStrategyTest {

    @Test
    @Override
    public void calculatePotentialTest() {
        double[][] expected = {
                {2.8284271247461903, 2.23606797749979, 2.0},
                {2.23606797749979, 1.4142135623730951, 1.0},
                {2.0, 1.0, 0.0}
        };

        testPotential(FILE_PREFIX + EUCLIDEAN_PREFIX + BASE_CONFIG, expected);
    }

    @Test
    @Override
    public void calculatePotentialWithWallTest() {
        double[][] expected = {
                {4.0, 3.0, 2.0, 1.0, 0.0},
                {4.123105625617661, 3.1622776601683795, 2.23606797749979, 1.4142135623730951, 1.0},
                {4.47213595499958, 3.605551275463989, 2.8284271247461903, 2.23606797749979, 2.0},
                {5.0, 4.242640687119285, 3.605551275463989, 3.1622776601683795, 3.0},
                {5.656854249492381, 5.0, 4.47213595499958, 4.123105625617661, 4.0}
        };
        testPotential(FILE_PREFIX + EUCLIDEAN_PREFIX + WALL_CONFIG, expected);
    }

    @Override
    public MoveStrategy CreateSut() {
        return new EuclideanMoveStrategy(new MollifierConfiguration(MollifierConfiguration.DEFAULT_RANGE, MollifierConfiguration.DEFAULT_STRENGTH), DijkstraMoveStrategy.DEFAULT_RADIUS);
    }
}
