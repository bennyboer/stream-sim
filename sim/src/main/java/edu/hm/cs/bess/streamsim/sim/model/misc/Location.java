package edu.hm.cs.bess.streamsim.sim.model.misc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

/**
 * Location of an object in the simulation world.
 *
 * @author Benjamin Eder
 */
public final class Location {

    /**
     * Row in the simulation world.
     */
    private final int row;

    /**
     * Column in the simulation world.
     */
    private final int column;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Location(@JsonProperty("row") int row, @JsonProperty("column") int column) {
        this.row = row;
        this.column = column;
    }

    /**
     * Get the row in the simulation world.
     *
     * @return row
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the column in the simulation world.
     *
     * @return column
     */
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (row != location.row) return false;
        return column == location.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }

    @Override
    public String toString() {
        return String.format("R%sC%s", getRow(), getColumn());
    }

    /**
     * Deserializer of the location key for jackson.
     */
    public static class LocationKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext ctx) throws IOException, JsonProcessingException {
            var parts = key.split("C");
            var rowPart = parts[0].substring(1);
            var columnPath = parts[1];

            int row = Integer.parseInt(rowPart);
            int column = Integer.parseInt(columnPath);

            return new Location(row, column);
        }

    }

}
