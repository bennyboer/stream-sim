package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Beil Benedikt
 */
public class FixedRateSpawnStrategyTest {

    @Test
    public void getNextSpawnTimeTest() {
        final int wantedSpawnTime = new Random().nextInt();
        SpawnStrategy sut = CreateSut(wantedSpawnTime);

        final double gotSpawnTime = sut.getNextSpawnTime();

        Assertions.assertEquals(wantedSpawnTime, gotSpawnTime);
    }

    @Test
    public void getNextSpawnTimeIsConstantTest() {
        final int fixedSpawnTime = 10;
        SpawnStrategy sut = CreateSut(fixedSpawnTime);

        final int NUM_OF_SPAWN = 10000000;
        double sum_of_spawn_time = 0;
        List<Double> nextSpawnTimes = new ArrayList<>();
        for(int i = 0; i < NUM_OF_SPAWN; i++) {
            final double nextSpawnTime = sut.getNextSpawnTime();
            nextSpawnTimes.add(nextSpawnTime);
            sum_of_spawn_time += nextSpawnTime;
        }

        final double meanSpawnTime = sum_of_spawn_time / nextSpawnTimes.size();
        Assertions.assertEquals(fixedSpawnTime, meanSpawnTime);

        Assertions.assertTrue(nextSpawnTimes.contains((double)fixedSpawnTime));
        Assertions.assertFalse(nextSpawnTimes.contains(9.0));
        Assertions.assertFalse(nextSpawnTimes.contains(11.0));
        Assertions.assertFalse(nextSpawnTimes.contains((double)(fixedSpawnTime - new Random().nextInt())));
    }

    @Test
    public void getNameTest() {
        final String wanted = "Fixed rate";
        final String got = CreateSut(new Random().nextInt()).getName();

        Assertions.assertEquals(wanted, got);
    }

    private SpawnStrategy CreateSut(int fixedSpawnRate) {
        return new FixedRateSpawnStrategy(fixedSpawnRate);
    }
}
