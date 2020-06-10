package edu.hm.cs.bess.streamsim.sim.model.object.source;

import edu.hm.cs.bess.streamsim.sim.config.SourceConfiguration;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;

/**
 * Simulation object spawning new people.
 *
 * @author Benjamin Eder
 */
public class Source implements SimObject {

    /**
     * Location of the source.
     */
    private final Location location;

    /**
     * Configuration of the source.
     */
    private final SourceConfiguration configuration;

    /**
     * Counter of spawns that already happened on that source.
     */
    private int spawnCounter = 0;

    public Source(Location location, SourceConfiguration configuration) {
        this.location = location;
        this.configuration = configuration;
    }

    @Override
    public SimObjectType getType() {
        return SimObjectType.SOURCE;
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

    public SourceConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Increase the spawn counter.
     */
    public void increaseSpawnCounter() {
        spawnCounter++;
    }

    /**
     * Get the total amount of spawns that already happened from that source.
     *
     * @return spawn count
     */
    public int getSpawnCounter() {
        return spawnCounter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        return location.equals(source.location);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

}
