package edu.hm.cs.bess.streamsim.sim.logic.spawn;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Strategy spawning people after a configured poisson distribution.
 *
 * @author Beil Benedikt
 * @author Benjamin Eder
 */
public abstract class DefaultSpawnStrategy implements SpawnStrategy {

    /**
     * List of cached targets.
     */
    protected List<Location> cachedTargets;

    /**
     * Logger of the class.
     */
    private static final Logger LOGGER = Logger.getLogger(DefaultSpawnStrategy.class.getName());

    /**
     * Random number generator to use.
     */
    protected Random rng;

    /**
     * Called when a new person should be spawned.
     *
     * @param source    the source that should spawn a new person
     * @param state     the current simulation state
     * @param scheduler of the simulation
     */
    public void spawn(Source source, State state, Scheduler scheduler) {
        List<Location> possibleSpawnLocations = getPossibleSpawnLocations(state, source.getLocation());

        if (possibleSpawnLocations.isEmpty()) {
            LOGGER.log(Level.FINE, String.format("Could not spawn new person from source at %s", source.getLocation()));

            // Schedule try-again spawn event
            double nextTimeStamp = scheduler.peekNextTimestamp().orElse(0.0);

            // Get random delay based on next spawn time
            nextTimeStamp += rng.nextDouble() * getNextSpawnTime();

            scheduler.scheduleIn(() -> source.getConfiguration().getSpawnStrategy().spawn(source, state, scheduler), nextTimeStamp - scheduler.currentTime());
        } else {
            Location newLocation = possibleSpawnLocations.get(rng.nextInt(possibleSpawnLocations.size()));

            if (cachedTargets == null) {
                cachedTargets = findTargetsInState(state);
            }
            Location randomTargetLocation = cachedTargets.size() > 0
                    ? cachedTargets.get(rng.nextInt(cachedTargets.size()))
                    : new Location(0, 0); // Has no target

            Person person = new Person(
                    source.getLocation(),
                    randomTargetLocation,
                    newLocation,
                    source.getConfiguration().getSpeedGenerator().generateSpeed(),
                    scheduler.currentTime(),
                    source.getConfiguration().getPatienceGenerator().generate()
            );
            state.setCellOccupant(person, person.getLocation());

            LOGGER.log(Level.FINE, String.format("Spawned new person at %s from source at %s with target location %s and speed %f cells/time unit", newLocation, source.getLocation(), randomTargetLocation, person.getSpeed()));

            source.increaseSpawnCounter();

            double movementDistance = distance(person.getLocation(), source.getLocation());

            person.addMovementRecord(scheduler.currentTime() + 1 / person.getSpeed() * movementDistance, movementDistance);

            // Schedule new movement event based on the speed of the person
            scheduler.scheduleIn(() -> source.getConfiguration().getMoveStrategy().move(person, state, scheduler), 1 / person.getSpeed() * movementDistance);

            // Schedule next spawn event
            if (source.getConfiguration().areSpawnsUnlimited()
                    || source.getSpawnCounter() < source.getConfiguration().getMaxSpawns()) {
                scheduler.scheduleIn(() -> source.getConfiguration().getSpawnStrategy().spawn(source, state, scheduler), getNextSpawnTime());
            }
        }
    }

    /**
     * Get a list of possible spawn locations around the given base location.
     *
     * @param state of the simulation
     * @param base  location
     * @return possible spawn locations
     */
    private List<Location> getPossibleSpawnLocations(State state, Location base) {
        List<Location> possibleSpawnLocations = new ArrayList<>();
        for (int row = Math.max(base.getRow() - 1, 0);
             row >= 0 && row <= base.getRow() + 1 && row < state.getRows();
             row++) {
            for (int column = Math.max(base.getColumn() - 1, 0);
                 column >= 0 && column <= base.getColumn() + 1 && column < state.getColumns();
                 column++) {
                Location newLocation = new Location(row, column);

                if (!newLocation.equals(base) && state.canBeOccupied(newLocation)) {
                    possibleSpawnLocations.add(newLocation);
                }
            }
        }

        return possibleSpawnLocations;
    }

    /**
     * Find a list of targets in the passed state.
     *
     * @param state to find targets in
     * @return list of targets
     */
    protected List<Location> findTargetsInState(State state) {
        List<Location> targetLocations = new ArrayList<>();
        for (int row = 0; row < state.getRows(); row++) {
            for (int column = 0; column < state.getColumns(); column++) {
                Location location = new Location(row, column);
                state.getCellOccupant(location).ifPresent((occupant) -> {
                    if (occupant.getType() == SimObjectType.TARGET) {
                        targetLocations.add(location);
                    }
                });
            }
        }

        return targetLocations;
    }

    /**
     * Calculate the euclidean distance from the passed location to the passed location.
     *
     * @param from location
     * @param to   location
     * @return euclidean distance
     */
    private double distance(Location from, Location to) {
        return Math.hypot(from.getRow() - to.getRow(), from.getColumn() - to.getColumn());
    }

    public void init(State state, Random rng) {
        this.rng = rng;
        cachedTargets = findTargetsInState(state);
    }

    abstract public String getName();

    abstract public double getNextSpawnTime();
}
