package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import edu.hm.cs.bess.streamsim.sim.model.state.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Benedikt Beil
 */
public class PoissonSpawnStrategyTest {

    @Test
    public void getNextSpawnTimeInMeanLambdaTest() {

        final double THRESHOLD = .001;
        final int NUM_OF_SPAWN = 10000000;
        final double lambda = 10.0;
        double sum_of_spawn_times = 0;
        SpawnStrategy sut = CreateSut(lambda);
        sut.init(new State(0, 0), new Random());

        List<Double> nextSpawnTimes = new ArrayList<>();
        for (int i = 0; i < NUM_OF_SPAWN; i++) {
            final double nextSpawnTime = sut.getNextSpawnTime();
            nextSpawnTimes.add(nextSpawnTime);
            sum_of_spawn_times += nextSpawnTime;
        }
        final double meanSpawnTime = sum_of_spawn_times / nextSpawnTimes.size();

        Assertions.assertTrue(Math.abs(lambda - meanSpawnTime) < THRESHOLD);
        Assertions.assertTrue(nextSpawnTimes.contains(3.0));
        Assertions.assertTrue(nextSpawnTimes.contains(4.0));
        Assertions.assertTrue(nextSpawnTimes.contains(5.0));
        Assertions.assertTrue(nextSpawnTimes.contains(6.0));
        Assertions.assertTrue(nextSpawnTimes.contains(7.0));
        Assertions.assertTrue(nextSpawnTimes.contains(8.0));
        Assertions.assertTrue(nextSpawnTimes.contains(9.0));
        Assertions.assertTrue(nextSpawnTimes.contains(10.0));
        Assertions.assertTrue(nextSpawnTimes.contains(11.0));
        Assertions.assertTrue(nextSpawnTimes.contains(12.0));
        Assertions.assertTrue(nextSpawnTimes.contains(13.0));
        Assertions.assertTrue(nextSpawnTimes.contains(14.0));
        Assertions.assertTrue(nextSpawnTimes.contains(15.0));
        Assertions.assertTrue(nextSpawnTimes.contains(16.0));
        Assertions.assertTrue(nextSpawnTimes.contains(17.0));
        Assertions.assertTrue(nextSpawnTimes.contains(18.0));
        Assertions.assertTrue(nextSpawnTimes.contains(19.0));
    }

    @Test
    public void getNameTest() {
        final String wanted = "Poisson";
        final String got = CreateSut(new Random().nextInt()).getName();

        Assertions.assertEquals(wanted, got);
    }

    private SpawnStrategy CreateSut(double lambda) {
        return new PoissonSpawnStrategy(lambda);
    }
}
