package edu.hm.cs.bess.streamsim.sim.model.object.obstacle;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;

import java.util.Objects;

/**
 * An obstacle in the simulation world.
 *
 * @author Benjamin Eder
 */
public class Obstacle implements SimObject {

    /**
     * Fixed location of the obstacle.
     */
    private final Location location;

    public Obstacle(Location location) {
        this.location = location;
    }

    @Override
    public SimObjectType getType() {
        return SimObjectType.OBSTACLE;
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
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Obstacle obstacle = (Obstacle) o;

        return Objects.equals(location, obstacle.location);
    }

    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Obstacle{" +
                "location=" + location +
                '}';
    }

}
