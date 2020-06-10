package edu.hm.cs.bess.streamsim.sim.model.state.cell;

import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.WalkableSimObject;

import java.util.Optional;

/**
 * Cell of the simulation state.
 *
 * @author Benjamin Eder
 */
public class StateCell {

    /**
     * The current occupant of the cell.
     */
    private SimObject occupant;

    /**
     * Check what object is occupying the cell.
     *
     * @return occupant of the cell
     */
    public Optional<SimObject> getOccupant() {
        return Optional.ofNullable(occupant);
    }

    /**
     * Set the occupant of the cell.
     *
     * @param occupant of the cell
     */
    public void setOccupant(SimObject occupant) {
        this.occupant = occupant;
    }

    /**
     * Check whether the cell is free.
     *
     * @return whether the cell is free
     */
    public boolean isFree() {
        return occupant == null;
    }

    /**
     * Whether something could be moved to this cell.
     *
     * @return can be occupied
     */
    public boolean canBeOccupied() {
        return isFree() || (occupant instanceof WalkableSimObject && ((WalkableSimObject) occupant).isFree());
    }

}
