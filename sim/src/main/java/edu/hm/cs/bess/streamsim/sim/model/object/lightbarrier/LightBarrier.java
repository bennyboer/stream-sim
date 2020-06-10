package edu.hm.cs.bess.streamsim.sim.model.object.lightbarrier;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.WalkableSimObject;

import java.util.Objects;

/**
 * A light barrier in the simulation world.
 *
 * @author Benjamin Eder
 */
public class LightBarrier extends WalkableSimObject {

    private static int TRIGGER_COUNT = 0;

    /**
     * Fixed location of the barrier.
     */
    private final Location location;

    public LightBarrier(Location location) {
        this.location = location;
    }

    public static void trigger() {
        TRIGGER_COUNT++;
    }

    public static int getTriggerCount() {
        return TRIGGER_COUNT;
    }

    public static void resetTriggerCount() {
        TRIGGER_COUNT = 0;
    }

    @Override
    public SimObjectType getActualType() {
        return SimObjectType.LIGHT_BARRIER;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightBarrier obstacle = (LightBarrier) o;

        return Objects.equals(location, obstacle.location);
    }

    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LightBarrier{" +
                "location=" + location +
                '}';
    }

}
