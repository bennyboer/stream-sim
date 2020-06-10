package edu.hm.cs.bess.streamsim.sim.model.object;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of simulation object types.
 *
 * @author Benjamin Eder
 */
public enum SimObjectType {

    PERSON("Person", 1),
    OBSTACLE("Obstacle", 2),
    SOURCE("Source", 3),
    TARGET("Target", 4),
    LIGHT_BARRIER("Light barrier", 5);

    /**
     * Lookup of simulation objects from their type ID.
     */
    private static final Map<Integer, SimObjectType> typeLookup = new HashMap<>();

    /**
     * Name of the type.
     */
    private final String name;

    /**
     * ID of the type.
     */
    private final int ID;

    SimObjectType(String name, int ID) {
        this.name = name;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }

    /**
     * Get the simulation object type by its ID.
     *
     * @param ID of the type
     */
    public static SimObjectType getForTypeID(int ID) {
        if (typeLookup.isEmpty()) {
            // Initialize type lookup first
            for (SimObjectType type : SimObjectType.values()) {
                typeLookup.put(type.getID(), type);
            }
        }

        return typeLookup.get(ID);
    }

}
