package edu.hm.cs.bess.streamsim.sim.logic.spawn.patience;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Benjamin Eder
 */
public class NormPatienceGeneratorTest {

    @Test
    public void testDistribution() {
        NormPatienceGenerator patienceGenerator = new NormPatienceGenerator(15, 10);
        patienceGenerator.init(new Random(0));

        Set<Integer> possibleValues = Sets.newHashSet(Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25));

        for (int i = 0; i < 10000; i++) {
            int patience = patienceGenerator.generate();

            if (patience < 15 - 10 || patience > 15 + 10) {
                fail();
            }

            possibleValues.remove(patience);
        }

        Assertions.assertEquals(possibleValues.size(), 0);
    }

    @Test
    public void testMaxDeviationZero() {
        NormPatienceGenerator patienceGenerator = new NormPatienceGenerator(15, 0);
        patienceGenerator.init(new Random(0));

        for (int i = 0; i < 100; i++) {
            int patience = patienceGenerator.generate();

            if (patience != 15) {
                fail();
            }
        }
    }

    @Test
    public void testGetters() {
        NormPatienceGenerator patienceGenerator = new NormPatienceGenerator(15, 10);
        patienceGenerator.init(new Random(0));

        Assertions.assertEquals(patienceGenerator.getMean(), 15);
        Assertions.assertEquals(patienceGenerator.getMaxDeviation(), 10);
    }

}
