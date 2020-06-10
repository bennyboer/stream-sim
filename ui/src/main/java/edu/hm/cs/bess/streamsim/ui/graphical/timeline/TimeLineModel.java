package edu.hm.cs.bess.streamsim.ui.graphical.timeline;

import javafx.beans.property.*;

/**
 * Model for the timeline.
 *
 * @author Benjamin Eder
 */
public class TimeLineModel {

    /**
     * Whether the simulation is currently playing.
     */
    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    /**
     * Property defining the speed of the simulation (ms per timeunit).
     */
    private final IntegerProperty speed = new SimpleIntegerProperty(20);

    /**
     * Current animation time.
     */
    private final DoubleProperty animationTime = new SimpleDoubleProperty(0.0);

    /**
     * The current seed.
     */
    private final LongProperty seed = new SimpleLongProperty(0);

    /**
     * Whether reset has been invoked.
     */
    private final BooleanProperty reset = new SimpleBooleanProperty(false);

    public boolean isPlaying() {
        return playing.get();
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public int getSpeed() {
        return speed.get();
    }

    public IntegerProperty speedProperty() {
        return speed;
    }

    public BooleanProperty resetProperty() {
        return reset;
    }

    public double getAnimationTime() {
        return animationTime.get();
    }

    public DoubleProperty animationTimeProperty() {
        return animationTime;
    }

    public void setAnimationTime(double animationTime) {
        this.animationTime.set(animationTime);
    }

    public void reset() {
        reset.set(!reset.get());
    }

    public long getSeed() {
        return seed.get();
    }

    public LongProperty seedProperty() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed.set(seed);
    }
}
