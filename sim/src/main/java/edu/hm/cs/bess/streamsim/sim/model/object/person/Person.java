package edu.hm.cs.bess.streamsim.sim.model.object.person;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Person in the simulation.
 *
 * @author Benjamin Eder
 * @author Konstantin Schlosser
 */
public class Person implements SimObject {
    private static int cur_id = 0;

    private final int id;
    /**
     * Source location of the person.
     */
    private final Location source;

    /**
     * Target of the person.
     */
    private Location target;

    /**
     * Speed of the person (in cells/timeunit).
     */
    private final double speed;

    /**
     * Current location of the person.
     */
    private Location location;

    /**
     * Timestamp of the last move.
     */
    private double lastMoveTimestamp;

    /**
     * Times the person was not able to move.
     */
    private int couldNotMoveCounter = 0;

    /**
     * Integer describing how patient the person is.
     * The higher the better.
     * Concretely it defines how many times a person is willing to wait for a better movement decision before
     * taking a bad decision movement.
     */
    private final int patience;

    /**
     * History of speeds over time (in cells/timeunit).
     * Used to calculate the mean speed of a person.
     */
    private final List<Double> speedHistory = new ArrayList<>();

    public Person(Location source, Location target, Location location, double speed, double creationTimestamp, int patience) {
        this.source = source;
        this.target = target;
        this.speed = speed;
        this.location = location;
        this.id = cur_id++;
        this.lastMoveTimestamp = creationTimestamp;
        this.patience = patience;
    }

    @Override
    public SimObjectType getType() {
        return SimObjectType.PERSON;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    public int getPatience() {
        return patience;
    }

    /**
     * Get the source location of the person.
     *
     * @return source location
     */
    public Location getSource() {
        return source;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Get the target location of the person.
     *
     * @return target location
     */
    public Location getTarget() {
        return target;
    }

    /**
     * Set the target location of the person.
     *
     * @param target of the person
     */
    public void setTarget(Location target) {
        this.target = target;
    }

    /**
     * Get the speed of the person (in cells/timeunit).
     *
     * @return speed
     */
    public double getSpeed() {
        return speed;
    }

    public int getId() {
        return id;
    }

    /**
     * Add a movement record by the passed distance at the given timestamp.
     *
     * @param timestamp the movement happened
     * @param distance  of the movement
     */
    public void addMovementRecord(double timestamp, double distance) {
        double diff = timestamp - lastMoveTimestamp;
        double speed = distance / diff;

        lastMoveTimestamp = timestamp;

        speedHistory.add(speed);
    }

    /**
     * Get the mean speed calculated over the default window size (in cells/timeunit).
     *
     * @return mean speed
     */
    public double getMeanSpeed() {
        return getMeanSpeed(speedHistory.size());
    }

    /**
     * Get the mean speed calculated over the given window size (amount of last speeds to calculate mean speed from).
     *
     * @param windowSize amount of last speeds to take into account when calculating
     * @return mean speed
     */
    public double getMeanSpeed(int windowSize) {
        windowSize = Math.min(windowSize, speedHistory.size());
        if (windowSize <= 0) {
            return getSpeed();
        }

        double speed = 0.0;
        for (int i = speedHistory.size() - windowSize; i < speedHistory.size(); i++) {
            speed += speedHistory.get(i);
        }

        return speed / windowSize;
    }

    /**
     * Called when the person could not move.
     */
    public void couldNotMove() {
        couldNotMoveCounter++;
    }

    /**
     * Called when the person could move.
     */
    public void couldMove() {
        couldNotMoveCounter = 0;
    }

    /**
     * Get the amount of times the person could not move.
     *
     * @return could not move counter
     */
    public int getCouldNotMoveCounter() {
        return couldNotMoveCounter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (id != person.id) return false;
        if (Double.compare(person.speed, speed) != 0) return false;
        if (!source.equals(person.source)) return false;
        if (!target.equals(person.target)) return false;
        return location.equals(person.location);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        temp = Double.doubleToLongBits(speed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + location.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", source=" + source +
                ", target=" + target +
                ", speed=" + speed +
                ", location=" + location +
                '}';
    }

}
