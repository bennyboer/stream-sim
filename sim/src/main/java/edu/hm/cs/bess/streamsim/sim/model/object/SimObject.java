package edu.hm.cs.bess.streamsim.sim.model.object;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;

/**
 * Object in the simulation.
 *
 * @author Benjamin Eder
 */
public interface SimObject {

    /**
     * Get the type of the object.
     *
     * @return type
     */
    SimObjectType getType();

    /**
     * Get the location of the object.
     *
     * @return location
     */
    Location getLocation();

    /**
     * Set the location of the object.
     *
     * @param location of the object
     */
    void setLocation(Location location);

    /**
     * Whether the object is walkable.
     *
     * @return whether walkable
     */
    boolean isWalkable();

}
