package edu.hm.cs.bess.streamsim.sim.logic.util;

import org.apache.commons.math3.random.RandomGenerator;

import java.util.Random;

public class CommonsMathRandomAdapter implements RandomGenerator {

    private final Random rng;

    public CommonsMathRandomAdapter(Random rng) {
        this.rng = rng;
    }

    @Override
    public void setSeed(int seed) {
        rng.setSeed(seed);
    }

    @Override
    public void setSeed(int[] seed) {
        rng.setSeed(seed[0]);
    }

    @Override
    public void setSeed(long seed) {
        rng.setSeed(seed);
    }

    @Override
    public void nextBytes(byte[] bytes) {
        rng.nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return rng.nextInt();
    }

    @Override
    public int nextInt(int n) {
        return rng.nextInt(n);
    }

    @Override
    public long nextLong() {
        return rng.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return rng.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return rng.nextFloat();
    }

    @Override
    public double nextDouble() {
        return rng.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return rng.nextGaussian();
    }

}
