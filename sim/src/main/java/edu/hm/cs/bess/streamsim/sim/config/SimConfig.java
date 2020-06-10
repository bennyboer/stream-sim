package edu.hm.cs.bess.streamsim.sim.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;

import java.util.Map;

/**
 * Configuration of a simulation.
 *
 * @author Benjamin Eder
 */
public final class SimConfig {

    /**
     * Seed of the simulation.
     */
    private final long seed;

    /**
     * Rows of the simulation world.
     */
    private final int rows;

    /**
     * Columns of the simulation world.
     */
    private final int columns;

    /**
     * Cell descriptions.
     */
    @JsonDeserialize(keyUsing = Location.LocationKeyDeserializer.class)
    private final Map<Location, CellDescriptor> cellDescriptors;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SimConfig(
            @JsonProperty("rows") int rows,
            @JsonProperty("columns") int columns,
            @JsonProperty("seed") long seed,
            @JsonProperty("cellDescriptors") Map<Location, CellDescriptor> cellDescriptors
    ) {
        this.rows = rows;
        this.columns = columns;
        this.seed = seed;
        this.cellDescriptors = cellDescriptors;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public long getSeed() {
        return seed;
    }

    public Map<Location, CellDescriptor> getCellDescriptors() {
        return cellDescriptors;
    }

}
