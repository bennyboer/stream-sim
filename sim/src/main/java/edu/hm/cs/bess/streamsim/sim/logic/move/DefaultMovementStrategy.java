package edu.hm.cs.bess.streamsim.sim.logic.move;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default move strategy.
 *
 * @author Benjamin Eder
 * @author Konstantin Schlosser
 * @author Benedikt Beil
 */
public abstract class DefaultMovementStrategy implements MoveStrategy {

    /**
     * Logger of the class.
     */
    private final static Logger LOGGER = Logger.getLogger(DefaultMovementStrategy.class.getName());

    /**
     * Logger logging to csv.
     */
    private final static Logger CSV_LOGGER = Logger.getLogger(MoveStrategy.class.getName());

    /**
     * Random number generator to use.
     */
    protected Random rng;

    /**
     * Set of targets currently in the simulation world.
     */
    private Set<Location> cachedTargets;

    /**
     * Cache of the utility functions for each path to each target.
     */
    private Map<Location, double[][]> pathUtilityCache;

    /**
     * Radius specifying the neighbourhood in which to respect other people when calculating the next move.
     */
    private final int radius;

    /**
     * The mollifier configuration to use.
     */
    private final MollifierConfiguration mollifierConfiguration;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DefaultMovementStrategy(
            @JsonProperty("mollifierConfiguration") MollifierConfiguration mollifierConfiguration,
            @JsonProperty("radius") int radius
    ) {
        this.mollifierConfiguration = mollifierConfiguration;
        this.radius = radius;
    }

    @Override
    public void init(State state, Random rng) {
//        LOGGER.setLevel(Level.FINE);
//        Handler consoleHandler = new ConsoleHandler();
//        consoleHandler.setLevel(Level.FINE);
//        LOGGER.addHandler(consoleHandler);

        this.rng = rng;
        this.cachedTargets = findTargetsInState(state);

        pathUtilityCache = new HashMap<>();
        for (Location targetLocation : getCachedTargets()) {
            pathUtilityCache.put(targetLocation, calculateBasePotential(state, targetLocation));
        }
    }

    /**
     * Calculate the potential matrix for the strategy.
     *
     * @param state  of the simulation world
     * @param target to calculate potential matrix for
     * @return potential matrix
     */
    abstract double[][] calculateBasePotential(State state, Location target);

    /**
     * Get the strategies name.
     *
     * @return name
     */
    abstract public String getName();

    @Override
    public void move(Person person, State state, Scheduler scheduler) {
        List<Location> possibleMoveLocations = chooseNextLocations(person, state);

        if (possibleMoveLocations.isEmpty()) {
            onCouldNotMovePerson(person, scheduler, state);
        } else {
            onMovePerson(person, scheduler, state, possibleMoveLocations);
        }

        // log in csv format using the default locale to force . instead of , for floating point numbers
        CSV_LOGGER.finest(String.format(
                Locale.ROOT,
                "%f;%d;%d;%d;%f;%f;%f",
                scheduler.currentTime(),
                person.getId(),
                person.getLocation().getRow(),
                person.getLocation().getColumn(),
                person.getSpeed(),
                person.getMeanSpeed(),
                person.getMeanSpeed(5)
        ));
    }

    /**
     * Called when a person could not be moved.
     *
     * @param person    that could not be moved
     * @param scheduler of the simulation
     * @param state     of the simulation
     */
    private void onCouldNotMovePerson(Person person, Scheduler scheduler, State state) {
        LOGGER.log(Level.FINE, String.format("Could not move person at %s", person.getLocation()));

        person.couldNotMove();

        double nextTimeStamp = scheduler.peekNextTimestamp().orElse(0.0);

        // Get random delay based on person speed
        nextTimeStamp += rng.nextDouble() * (1 / person.getSpeed());

        scheduler.scheduleIn(() -> move(person, state, scheduler), nextTimeStamp - scheduler.currentTime());
    }

    /**
     * Called when a person should be moved to one of the passed possible move locations.
     *
     * @param person                to move
     * @param scheduler             of the simulation
     * @param state                 of the simulation
     * @param possibleMoveLocations the possible movements
     */
    private void onMovePerson(Person person, Scheduler scheduler, State state, List<Location> possibleMoveLocations) {
        final Location newLocation = possibleMoveLocations.size() == 1
                ? possibleMoveLocations.get(0)
                : possibleMoveLocations.get(rng.nextInt(possibleMoveLocations.size()));

        person.couldMove();

        if (cachedTargets.contains(newLocation)) {
            // Let target consume strategy handle that
            final Target target = (Target) state.getCellOccupant(newLocation).orElseThrow();

            LOGGER.log(Level.FINE, String.format("Person at reached its target at %s", target.getLocation()));

            target.getConfiguration().getConsumeStrategy().reachedTarget(target, person, state, scheduler);
        } else {
            // Check movement distance (diagonal moves are longer: sqrt(2) > 1) -> schedule later
            double distance = distance(person.getLocation(), newLocation);

            LOGGER.log(Level.FINE, String.format("Moving person from %s to %s", person.getLocation(), newLocation));
            state.moveOccupant(person.getLocation(), newLocation);

            double nextMovementDelta = 1 / person.getSpeed() * distance;
            double nextTimestamp = scheduler.currentTime() + nextMovementDelta;

            // Record the movement time to calculate mean speed of the person later on.
            person.addMovementRecord(nextTimestamp, distance);

            // Schedule new movement event based on the speed of the person
            LOGGER.log(Level.FINE, String.format("Scheduling next movement of person at %s in %f time units", person.getLocation(), nextMovementDelta));
            scheduler.scheduleIn(() -> move(person, state, scheduler), nextMovementDelta);
        }
    }

    /**
     * Choose the next possible locations the given person will be able to move.
     *
     * @param person to move
     * @param state  to find possible move locations in
     * @return possible move locations
     */
    public List<Location> chooseNextLocations(Person person, State state) {
        double[][] potentialMatrix = pathUtilityCache.get(person.getTarget());
        assert potentialMatrix != null;

        final List<Location> otherPeopleInRadius = getOtherPeopleInRadius(person, state);
        final double currentPotential = potentialMatrix[person.getLocation().getRow()][person.getLocation().getColumn()];

        List<Location> possibleMoveLocations = new ArrayList<>();
        AtomicReference<Double> greatestPotentialDescent = new AtomicReference<>(-Double.MAX_VALUE);
        forEachNeighbour(person.getLocation(), state, location -> {
            final double potential = potentialMatrix[location.getRow()][location.getColumn()];

            double potentialDescent = currentPotential - potential;

            final boolean isDiagonal = distance(person.getLocation(), location) > 1.0;
            if (isDiagonal) {
                potentialDescent = normalizeDiagonalPotentialDescent(potentialDescent, person);
            }

            // Apply mollifier for nearby people
            if (getRadius() > 0) {
                // Increase the potential for nearby people -> less likely to move there if people are nearby!
                for (Location otherPersonLocation : otherPeopleInRadius) {
                    potentialDescent += calculateMollifierValue(location, otherPersonLocation);
                }
            }

            // Check if move is possible
            if (state.canBeOccupied(location) || state.getCellOccupant(location).get().getType() == SimObjectType.TARGET) {
                if (potentialDescent > greatestPotentialDescent.get()) {
                    greatestPotentialDescent.set(potentialDescent);
                    possibleMoveLocations.clear();
                    possibleMoveLocations.add(location);
                } else if (potentialDescent == greatestPotentialDescent.get()) {
                    possibleMoveLocations.add(location);
                }
            }
        });

        if (greatestPotentialDescent.get() < 0.0) { // Is a bad decision movement
            if (person.getCouldNotMoveCounter() <= person.getPatience()) {
                // Person is patient enough and is not willing to take bad decisions
                return Collections.emptyList(); // Reject a bad decision movement
            }
        }

        return possibleMoveLocations;
    }

    protected double normalizeDiagonalPotentialDescent(double potentialDescent, Person person) {
        final int rowDiff = Math.abs(person.getSource().getRow() - person.getTarget().getRow());
        final int columnDiff = Math.abs(person.getSource().getColumn() - person.getTarget().getColumn());

        if (rowDiff > columnDiff) {
            potentialDescent /= 1.0 + (double) columnDiff / rowDiff;
        } else {
            potentialDescent /= 1.0 + (double) rowDiff / columnDiff;
        }
        return potentialDescent;
    }

    private List<Location> getOtherPeopleInRadius(Person person, State state) {
        // Find people in specified radius around the person to move.
        List<Location> otherPeopleInRadius = new ArrayList<>();
        if (getRadius() > 0) {
            forEachNeighbour(person.getLocation(), state, getRadius(), location -> state.getUpperCellOccupant(location).ifPresent(occupant -> {
                if (occupant.getType() == SimObjectType.PERSON) {
                    if (distance(location, person.getLocation()) <= radius) {
                        otherPeopleInRadius.add(location);
                    }
                }
            }));
        }
        return otherPeopleInRadius;
    }

    /**
     * Calculate the euclidean distance from the passed location to the passed location.
     *
     * @param from location
     * @param to   location
     * @return euclidean distance
     */
    protected double distance(Location from, Location to) {
        return Math.hypot(from.getRow() - to.getRow(), from.getColumn() - to.getColumn());
    }

    /**
     * Find a set of targets in the passed simulation world state.
     *
     * @param state to find targets in
     * @return targets set
     */
    private Set<Location> findTargetsInState(State state) {
        Set<Location> targetLocations = new HashSet<>();
        for (int row = 0; row < state.getRows(); row++) {
            for (int column = 0; column < state.getColumns(); column++) {
                final Location location = new Location(row, column);
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
     * Calculate the potential for the whole simulation world.
     * This is used in the visualization to display the current potential function.
     *
     * @param state to calculate potential for
     * @return potential
     */
    @Override
    public double[][] calculatePotential(State state) throws UnsupportedOperationException {
        if (getCachedTargets().size() != 1) {
            throw new UnsupportedOperationException("Cannot calculate the potential for multiple targets");
        }

        double[][] potentialMatrix = pathUtilityCache.get(getCachedTargets().iterator().next());

        // Check if we have to apply the mollifier for all people in the simulation world
        if (getRadius() > 0) {
            // First and foremost deep copy the potential matrix
            double[][] tmp = new double[state.getRows()][state.getColumns()];
            for (int row = 0; row < state.getRows(); row++) {
                if (state.getColumns() >= 0) System.arraycopy(potentialMatrix[row], 0, tmp[row], 0, state.getColumns());
            }

            // Find all people and apply the mollifier on the temporary potential matrix
            state.getObjectsForType(SimObjectType.PERSON).stream()
                    .map(o -> (Person) o)
                    .forEach(p -> forEachNeighbour(
                            p.getLocation(),
                            state,
                            getMollifierConfiguration().getRange(),
                            location -> tmp[location.getRow()][location.getColumn()] -= calculateMollifierValue(location, p.getLocation())));

            return tmp;
        }

        return potentialMatrix;
    }

    /**
     * Calculate the mollifier value for the passed location from the given center.
     *
     * @param location to calculate mollifier value for
     * @param center   the center of the mollifier
     * @return mollifier value
     */
    private double calculateMollifierValue(Location location, Location center) {
        double distance = distance(location, center);

        if (distance < getMollifierConfiguration().getRange()) {
            return -getMollifierConfiguration().getStrength() * Math.exp(1 / (Math.pow(distance / getMollifierConfiguration().getRange(), 2) - 1));
        }

        return 0.0;
    }

    /**
     * Get the mollifier configuration to use.
     *
     * @return mollifier configuration
     */
    public MollifierConfiguration getMollifierConfiguration() {
        return mollifierConfiguration;
    }

    /**
     * Get the radius in which to respect other people when calculating the next move.
     *
     * @return radius
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Execute the passed consumer for each neighbour of the passed location in the simulation state.
     *
     * @param location          to find neighbours of
     * @param state             to find neighbours in
     * @param neighbourConsumer to execute for each neighbour of the passed location
     */
    void forEachNeighbour(Location location, State state, Consumer<Location> neighbourConsumer) {
        forEachNeighbour(location, state, 1, neighbourConsumer);
    }

    /**
     * Execute the passed consumer for each neighbour of the passed location in the simulation state.
     *
     * @param location          to find neighbours of
     * @param state             to find neighbours in
     * @param radius            defining the neighbourhood size
     * @param neighbourConsumer to execute for each neighbour of the passed location
     */
    void forEachNeighbour(Location location, State state, int radius, Consumer<Location> neighbourConsumer) {
        forEachNeighbour(location, state, radius, radius, neighbourConsumer);
    }

    /**
     * Execute the passed consumer for each neighbour of the passed location in the simulation state.
     *
     * @param location          to find neighbours of
     * @param state             to find neighbours in
     * @param rowOffset         defining the neighbourhood size
     * @param columnOffset      defining the neighbourhood size
     * @param neighbourConsumer to execute for each neighbour of the passed location
     */
    void forEachNeighbour(Location location, State state, int rowOffset,
                          int columnOffset, Consumer<Location> neighbourConsumer) {
        for (int row = Math.max(0, location.getRow() - rowOffset);
             row <= Math.min(location.getRow() + rowOffset, state.getRows() - 1); row++) {

            for (int column = Math.max(0, location.getColumn() - columnOffset);
                 column <= Math.min(location.getColumn() + columnOffset, state.getColumns() - 1); column++) {

                if (row != location.getRow() || column != location.getColumn()) {
                    neighbourConsumer.accept(new Location(row, column));
                }
            }
        }
    }

    /**
     * Get the set of cached targets in the simulation world.
     *
     * @return set of targets
     */
    private Set<Location> getCachedTargets() {
        return cachedTargets;
    }

    /**
     * Get the logger used to log movements to CSV.
     *
     * @return csv movement logger
     */
    public static Logger getCSVMovementLogger() {
        return CSV_LOGGER;
    }
}
