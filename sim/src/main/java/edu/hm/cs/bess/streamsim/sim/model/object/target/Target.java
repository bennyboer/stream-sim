package edu.hm.cs.bess.streamsim.sim.model.object.target;

import edu.hm.cs.bess.streamsim.sim.config.TargetConfiguration;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;

/**
 * Simulation object consuming people.
 *
 * @author Benjamin Eder
 */
public class Target implements SimObject {

    /**
     * Location of the target.
     */
    private final Location location;

    /**
     * Configuration of the target.
     */
    private final TargetConfiguration configuration;

    public Target(Location location, TargetConfiguration configuration) {
        this.location = location;
        this.configuration = configuration;
    }

    @Override
    public SimObjectType getType() {
        return SimObjectType.TARGET;
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

    public TargetConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        return location.equals(target.location);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

}
