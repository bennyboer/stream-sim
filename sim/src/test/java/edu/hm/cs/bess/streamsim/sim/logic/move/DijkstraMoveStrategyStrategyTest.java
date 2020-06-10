package edu.hm.cs.bess.streamsim.sim.logic.move;

import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import org.junit.jupiter.api.Test;

/**
 * @author Benedikt Beil
 */
public class DijkstraMoveStrategyStrategyTest extends MovementStrategyTest {

    @Test
    @Override
    public void calculatePotentialTest() {
        double[][] expected = {
                {Double.MAX_VALUE, 2.414213562373095, 2.0},
                {2.414213562373095, 1.4142135623730951, 1.0},
                {2.0, 1.0, 0.0}
        };
        testPotential(FILE_PREFIX + DIJKSTRA_PREFIX + BASE_CONFIG, expected);
    }

    @Test
    @Override
    public void calculatePotentialWithWallTest() {
        double[][] expected = {
                {Double.MAX_VALUE, 9.242640687119284, Double.MAX_VALUE, 1.0, 0.0},
                {8.65685424949238, 8.242640687119284, Double.MAX_VALUE, 1.4142135623730951, 1.0},
                {7.65685424949238, 7.242640687119285, Double.MAX_VALUE, 2.414213562373095, 2.0},
                {7.242640687119285, 6.242640687119285, Double.MAX_VALUE, 3.414213562373095, 3.0},
                {6.82842712474619, 5.82842712474619, 4.82842712474619, 4.414213562373095, 4.0}
        };
        testPotential(FILE_PREFIX + DIJKSTRA_PREFIX + WALL_CONFIG, expected);
    }

    @Override
    public MoveStrategy CreateSut() {
        return new DijkstraMoveStrategy(new MollifierConfiguration(MollifierConfiguration.DEFAULT_RANGE, MollifierConfiguration.DEFAULT_STRENGTH), DijkstraMoveStrategy.DEFAULT_RADIUS);
    }
}
