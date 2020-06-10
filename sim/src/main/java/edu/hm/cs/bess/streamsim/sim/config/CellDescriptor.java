package edu.hm.cs.bess.streamsim.sim.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.logic.consume.ConsumeStrategies;
import edu.hm.cs.bess.streamsim.sim.logic.move.MoveStrategies;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.SpawnStrategies;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.patience.PatienceGenerators;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.speed.SpeedGenerators;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.lightbarrier.LightBarrier;
import edu.hm.cs.bess.streamsim.sim.model.object.obstacle.Obstacle;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import org.jetbrains.annotations.Nullable;

/**
 * Description of a cell.
 *
 * @author Benjamin Eder
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CellDescriptor {

    /**
     * ID of the cell type (arbitrary).
     */
    private final int typeID;

    /**
     * Location of the cell.
     */
    private final Location location;

    /**
     * Configuration of the cell.
     * Might be null if no special stuff has been configured (Then take some default values).
     */
    @Nullable
    private final CellConfiguration configuration;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CellDescriptor(@JsonProperty("typeID") int typeID, @JsonProperty("location") Location location, @JsonProperty("configuration") @Nullable CellConfiguration configuration) {
        this.typeID = typeID;
        this.location = location;
        this.configuration = configuration;
    }

    public int getTypeID() {
        return typeID;
    }

    public Location getLocation() {
        return location;
    }

    @Nullable
    public CellConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Create simulation object from cell descriptor.
     *
     * @param cellDescriptor to create simulation object from
     * @return simulation object
     */
    public static SimObject createSimObject(CellDescriptor cellDescriptor) {
        return switch (SimObjectType.getForTypeID(cellDescriptor.getTypeID())) {
            case PERSON -> new Person(new Location(0, 0), new Location(0, 0), cellDescriptor.getLocation(), 1, 0, 20); // Can only instantiate dummy people here, the real ones come from source objects
            case OBSTACLE -> new Obstacle(cellDescriptor.getLocation());
            case SOURCE -> new Source(
                    cellDescriptor.getLocation(),
                    cellDescriptor.getConfiguration() != null
                            ? (SourceConfiguration) cellDescriptor.getConfiguration()
                            : new SourceConfiguration(
                            SpawnStrategies.DEFAULT.get(),
                            MoveStrategies.DEFAULT.get(),
                            -1, // Unlimited spawns
                            SpeedGenerators.DEFAULT.get(),
                            PatienceGenerators.DEFAULT.get()
                    ) // Default configuration
            );
            case TARGET -> new Target(
                    cellDescriptor.getLocation(),
                    cellDescriptor.getConfiguration() != null
                            ? (TargetConfiguration) cellDescriptor.getConfiguration()
                            : new TargetConfiguration(ConsumeStrategies.DEFAULT.get()) // Default configuration
            );
            case LIGHT_BARRIER -> new LightBarrier(cellDescriptor.getLocation());
        };
    }

}
