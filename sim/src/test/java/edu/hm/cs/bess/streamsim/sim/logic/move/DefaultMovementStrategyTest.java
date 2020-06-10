package edu.hm.cs.bess.streamsim.sim.logic.move;

import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.exception.EventExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Benjamin Eder
 */
public class DefaultMovementStrategyTest {

    DefaultMovementStrategy strategy;

    @BeforeEach
    void beforeAll() {
        strategy = new EuclideanMoveStrategy(
                new MollifierConfiguration(
                        MollifierConfiguration.DEFAULT_RANGE,
                        MollifierConfiguration.DEFAULT_STRENGTH
                ),
                FastMarchingMethodMovementStrategy.DEFAULT_RADIUS
        );
    }

    @Test
    public void testForEachNeighbour_radius_noAdjustments() throws EventExecutionException {
        Set<Location> anticipatedLocations = new HashSet<>();
        for (int row = 0; row <= 2; row++) {
            for (int column = 0; column <= 2; column++) {
                if (row != 1 || column != 1) { // Leave center location out
                    anticipatedLocations.add(new Location(row, column));
                }
            }
        }
        Assertions.assertEquals(anticipatedLocations.size(), 8);

        strategy.forEachNeighbour(new Location(1, 1), new State(3, 3), anticipatedLocations::remove);

        assertTrue(anticipatedLocations.isEmpty());
    }

    @Test
    public void testForEachNeighbour_radius_adjustments() throws EventExecutionException {
        Set<Location> anticipatedLocations = new HashSet<>();
        for (int row = 0; row <= 1; row++) {
            for (int column = 0; column <= 1; column++) {
                if (row != 0 || column != 0) { // Leave center location out
                    anticipatedLocations.add(new Location(row, column));
                }
            }
        }
        Assertions.assertEquals(anticipatedLocations.size(), 3);

        strategy.forEachNeighbour(new Location(0, 0), new State(3, 3), anticipatedLocations::remove);

        assertTrue(anticipatedLocations.isEmpty());
    }

    @Test
    public void testForEachNeighbour_width_height_noAdjustments() throws EventExecutionException {
        Set<Location> anticipatedLocations = new HashSet<>();
        for (int row = 0; row <= 4; row++) {
            for (int column = 0; column <= 2; column++) {
                if (row != 2 || column != 1) { // Leave center location out
                    anticipatedLocations.add(new Location(row, column));
                }
            }
        }
        Assertions.assertEquals(anticipatedLocations.size(), 5 * 3 - 1);

        strategy.forEachNeighbour(new Location(2, 1), new State(5, 3), 2, 1, anticipatedLocations::remove);

        assertTrue(anticipatedLocations.isEmpty());
    }

    @Test
    public void testForEachNeighbour_width_height_adjustments() throws EventExecutionException {
        Set<Location> anticipatedLocations = new HashSet<>();
        for (int row = 0; row <= 4; row++) {
            for (int column = 0; column <= 2; column++) {
                if (row != 2 || column != 1) { // Leave center location out
                    anticipatedLocations.add(new Location(row, column));
                }
            }
        }
        Assertions.assertEquals(anticipatedLocations.size(), 5 * 3 - 1);

        strategy.forEachNeighbour(new Location(2, 1), new State(4, 3), 2, 1, anticipatedLocations::remove);

        assertEquals(anticipatedLocations.size(), 3);
    }

}
