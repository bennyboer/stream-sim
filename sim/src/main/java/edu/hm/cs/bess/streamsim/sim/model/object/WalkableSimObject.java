package edu.hm.cs.bess.streamsim.sim.model.object;

/**
 * @author Benjamin Eder
 */
public abstract class WalkableSimObject implements SimObject {

    /**
     * The current occupant that is "walking" on this object.
     */
    protected SimObject occupant;

    public SimObject getOccupant() {
        return occupant;
    }

    public void setOccupant(SimObject occupant) {
        this.occupant = occupant;
    }

    /**
     * Check whether the walkable object is currently not being walked on.
     *
     * @return free
     */
    public boolean isFree() {
        return occupant == null;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public SimObjectType getType() {
        return isFree() ? getActualType() : occupant.getType();
    }

    public abstract SimObjectType getActualType();

}
