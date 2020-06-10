package edu.hm.cs.bess.streamsim.sim.model.state;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.WalkableSimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.lightbarrier.LightBarrier;
import edu.hm.cs.bess.streamsim.sim.model.state.cell.StateCell;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The simulations state representation.
 *
 * @author Benjamin Eder
 */
public class State implements Cloneable {

    /**
     * Cells forming the two-dimensional simulation world.
     */
    private final StateCell[][] cells;

    /**
     * Update listeners getting notified of state changes during the simulation.
     */
    private List<StateUpdateListener> updateListeners;

    /**
     * Object type to location mapping.
     */
    private final Map<SimObjectType, Set<Location>> objectTypeMapping = new HashMap<>();

    /**
     * Lock for the object type mapping.
     */
    private final ReadWriteLock updateLock = new ReentrantReadWriteLock();

    public State(int rows, int columns) {
        cells = new StateCell[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                cells[row][column] = new StateCell();
            }
        }
    }

    /**
     * Get the columns of the simulation world.
     *
     * @return columns
     */
    public int getColumns() {
        return cells.length > 0 ? cells[0].length : 0;
    }

    /**
     * Get the rows of the simulation world.
     *
     * @return rows
     */
    public int getRows() {
        return cells.length;
    }

    /**
     * Set a cell occupant.
     *
     * @param occupant of the cell
     * @param location of the cell
     */
    public void setCellOccupant(SimObject occupant, Location location) {
        UpdateEvent event;

        updateLock.writeLock().lock();
        try {
            SimObject oldOccupant = cells[location.getRow()][location.getColumn()].getOccupant().orElse(null);
            if (occupant == null) {
                throw new IllegalArgumentException("Occupant to set must be non-null, to remove please use method State.removeOccupant(...)");
            }

            if (oldOccupant != null && oldOccupant.isWalkable()) {
                WalkableSimObject walkableSimObject = (WalkableSimObject) oldOccupant;
                if (!walkableSimObject.isFree()) {
                    throw new IllegalArgumentException("Cannot occupy already occupied walkable simulation object");
                }

                walkableSimObject.setOccupant(occupant);

                objectTypeMapping.computeIfAbsent(occupant.getType(), k -> new HashSet<>()).add(location);

                event = new UpdateEvent(EventType.CHANGED, location, occupant, walkableSimObject);
            } else {
                cells[location.getRow()][location.getColumn()].setOccupant(occupant);

                objectTypeMapping.computeIfAbsent(occupant.getType(), k -> new HashSet<>()).add(location);

                event = new UpdateEvent(EventType.ADDED, location, occupant, oldOccupant);
            }
        } finally {
            updateLock.writeLock().unlock();
        }

        notifyUpdateListeners(Collections.singletonList(event));
    }

    /**
     * Move a occupant from one location to another.
     *
     * @param from location to move from
     * @param to   location to move to
     * @return whether the move could be made
     */
    public boolean moveOccupant(Location from, Location to) {
        if (from.equals(to)) {
            return false;
        }

        List<UpdateEvent> events = new ArrayList<>();

        updateLock.writeLock().lock();
        try {
            if (!canBeOccupied(to)) {
                return false;
            }

            SimObject occupant = getCellOccupant(from).orElseThrow();
            if (occupant.isWalkable()) {
                WalkableSimObject walkableSimObject = (WalkableSimObject) occupant;
                occupant = walkableSimObject.getOccupant();
                walkableSimObject.setOccupant(null);

                events.add(new UpdateEvent(EventType.CHANGED, from, walkableSimObject, occupant));
            } else {
                SimObject oldOccupant = cells[from.getRow()][from.getColumn()].getOccupant().orElse(null);
                cells[from.getRow()][from.getColumn()].setOccupant(null);

                events.add(new UpdateEvent(EventType.REMOVED, from, null, oldOccupant));
            }

            occupant.setLocation(to);

            Optional<SimObject> optionalSimObject = getCellOccupant(to);
            if (optionalSimObject.isPresent() && optionalSimObject.get().isWalkable()) {
                ((WalkableSimObject) optionalSimObject.get()).setOccupant(occupant);

                if (optionalSimObject.get() instanceof LightBarrier) {
                    LightBarrier.trigger();
                }

                events.add(new UpdateEvent(EventType.CHANGED, to, occupant, optionalSimObject.get()));
            } else {
                cells[to.getRow()][to.getColumn()].setOccupant(occupant);

                events.add(new UpdateEvent(EventType.ADDED, to, occupant, null));
            }

            // Update object type mapping
            objectTypeMapping.get(occupant.getType()).remove(from);
            objectTypeMapping.get(occupant.getType()).add(to);
        } finally {
            updateLock.writeLock().unlock();
        }

        notifyUpdateListeners(events);

        return true;
    }

    /**
     * Remove an occupant from the passed location.
     *
     * @param location the location of th
     * @return whether occupant could be removed
     */
    public boolean removeOccupant(Location location) {
        UpdateEvent event;

        updateLock.writeLock().lock();
        try {
            SimObject occupant = getCellOccupant(location).orElse(null);
            if (occupant == null) {
                return false;
            }

            if (occupant.isWalkable()) {
                WalkableSimObject walkableSimObject = (WalkableSimObject) occupant;

                if (walkableSimObject.isFree()) {
                    // Remove the walkable sim object
                    cells[location.getRow()][location.getColumn()].setOccupant(null);

                    objectTypeMapping.get(walkableSimObject.getType()).remove(location);

                    event = new UpdateEvent(EventType.REMOVED, location, null, walkableSimObject);
                } else {
                    SimObject oldOccupant = walkableSimObject.getOccupant();

                    // Remove only the object currently walking on the walkable sim object
                    walkableSimObject.setOccupant(null);

                    objectTypeMapping.get(oldOccupant.getType()).remove(location);

                    event = new UpdateEvent(EventType.CHANGED, location, walkableSimObject, oldOccupant);
                }
            } else {
                cells[location.getRow()][location.getColumn()].setOccupant(null);

                objectTypeMapping.get(occupant.getType()).remove(location);

                event = new UpdateEvent(EventType.REMOVED, location, null, occupant);
            }
        } finally {
            updateLock.writeLock().unlock();
        }

        notifyUpdateListeners(Collections.singletonList(event));

        return true;
    }

    /**
     * Get the cell occupant at the passed location (or null).
     *
     * @param location to get occupant for
     * @return occupant (or null)
     */
    public Optional<SimObject> getCellOccupant(Location location) {
        return cells[location.getRow()][location.getColumn()].getOccupant();
    }

    /**
     * Get the cell occupant that may be currently walking on another walkable cell occupant.
     *
     * @param location to get upper cell occupant for
     * @return the upper cell occupant
     */
    public Optional<SimObject> getUpperCellOccupant(Location location) {
        SimObject simObject = getCellOccupant(location).orElse(null);

        if (simObject != null && simObject.isWalkable()) {
            WalkableSimObject walkableSimObject = (WalkableSimObject) simObject;

            if (walkableSimObject.getOccupant() != null) {
                return Optional.of(walkableSimObject.getOccupant());
            } else {
                return Optional.of(walkableSimObject);
            }
        } else {
            return Optional.ofNullable(simObject);
        }
    }

    /**
     * Get the object type count for the passed type.
     *
     * @param type to get count for
     * @return count
     */
    public int getObjectTypeCount(SimObjectType type) {
        updateLock.readLock().lock();
        try {
            Set<Location> locations = objectTypeMapping.get(type);
            if (locations == null) {
                return 0;
            } else {
                return locations.size();
            }
        } finally {
            updateLock.readLock().unlock();
        }
    }

    /**
     * Get the objects with the passed type.
     *
     * @param type to get count for
     * @return count
     */
    public Set<SimObject> getObjectsForType(SimObjectType type) {
        updateLock.readLock().lock();
        try {
            Set<Location> locations = objectTypeMapping.get(type);
            if (locations == null) {
                return Collections.emptySet();
            } else {
                return locations.stream().map(l -> getCellOccupant(l).orElseThrow()).collect(Collectors.toUnmodifiableSet());
            }
        } finally {
            updateLock.readLock().unlock();
        }
    }

    /**
     * Read the object type mapping in a exclusive access environment.
     *
     * @param consumer to execute when locked
     */
    public void readObjectTypeMapping(Consumer<Map<SimObjectType, Set<Location>>> consumer) {
        updateLock.readLock().lock();
        try {
            consumer.accept(Collections.unmodifiableMap(objectTypeMapping));
        } finally {
            updateLock.readLock().unlock();
        }
    }

    /**
     * Check whether the passed cell location is free.
     *
     * @param location of the cell
     * @return whether the cell is free
     */
    public boolean isCellFree(Location location) {
        return cells[location.getRow()][location.getColumn()].isFree();
    }

    /**
     * Whether something could be moved to the passed location.
     *
     * @param location to move something
     * @return can be occupied
     */
    public boolean canBeOccupied(Location location) {
        return cells[location.getRow()][location.getColumn()].canBeOccupied();
    }

    /**
     * Add an update listener getting notified of state changes during the simulation.
     * NOTE THAT THE LISTENER WILL BE CALLED IN ANOTHER THREAD!
     *
     * @param listener to add
     */
    public void addUpdateListener(StateUpdateListener listener) {
        if (updateListeners == null) {
            updateListeners = new CopyOnWriteArrayList<>();
        }

        updateListeners.add(listener);
    }

    /**
     * Remove the passed update listener.
     *
     * @param listener to remove
     */
    public void removeUpdateListener(StateUpdateListener listener) {
        if (updateListeners != null) {
            updateListeners.remove(listener);
        }
    }

    /**
     * Notify all update listeners of a state change
     *
     * @param events that occurred
     */
    protected void notifyUpdateListeners(List<UpdateEvent> events) {
        if (updateListeners != null) {
            for (var l : updateListeners) {
                l.updated(events);
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Listener to state updates.
     */
    @FunctionalInterface
    public interface StateUpdateListener {

        /**
         * Called the state is updated.
         *
         * @param events that occurred
         */
        void updated(List<UpdateEvent> events);

    }

    public static class UpdateEvent {
        private final EventType type;
        private final Location location;

        @Nullable
        private final SimObject newOccupant;

        @Nullable
        private final SimObject oldOccupant;

        public UpdateEvent(EventType type, Location location, @Nullable SimObject newOccupant, @Nullable SimObject oldOccupant) {
            this.type = type;
            this.location = location;
            this.newOccupant = newOccupant;
            this.oldOccupant = oldOccupant;
        }

        public EventType getType() {
            return type;
        }

        public Location getLocation() {
            return location;
        }

        @Nullable
        public SimObject getNewOccupant() {
            return newOccupant;
        }

        @Nullable
        public SimObject getOldOccupant() {
            return oldOccupant;
        }
    }

    public enum EventType {
        ADDED,
        REMOVED,
        CHANGED
    }

}
