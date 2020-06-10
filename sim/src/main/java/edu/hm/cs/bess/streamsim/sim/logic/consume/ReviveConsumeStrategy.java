package edu.hm.cs.bess.streamsim.sim.logic.consume;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Strategy reviving every person that reaches its target at another source.
 *
 * @author Konstantin Schlosser
 */
public class ReviveConsumeStrategy implements ConsumeStrategy {

    /**
     * Logger of the class.
     */
    private static final Logger LOGGER = Logger.getLogger(ReviveConsumeStrategy.class.getSimpleName());

    /**
     * Name of the strategy.
     */
    public static final String NAME = "Revive";
    private Random rng;

    /**
     * Set of source locations.
     */
    private Set<Location> sourceLocations;

    /**
     * Set of target locations.
     */
    private Set<Location> targetLocations;

    @Override
    public void reachedTarget(Target target, Person person, State state, Scheduler scheduler) {
        Location sourceLocation = sourceLocations.toArray(new Location[0])[rng.nextInt(sourceLocations.size())];
        Source source = (Source) state.getCellOccupant(sourceLocation).orElseThrow();

        List<Location> possibleSpawns = getPossibleSpawnLocations(state, source);
        if (possibleSpawns.size() == 0) {
            LOGGER.log(Level.FINE, String.format("Could not revive person currently at %s as there is no possible spawn location around the source", person.getLocation()));

            double nextTimeStamp = scheduler.peekNextTimestamp().orElse(0.0);

            // Get random delay based on person speed
            nextTimeStamp += rng.nextDouble() * (1 / person.getSpeed());

            scheduler.scheduleIn(() -> source.getConfiguration().getMoveStrategy().move(person, state, scheduler), nextTimeStamp - scheduler.currentTime());
        } else {
            Location spawnLocation = possibleSpawns.get(rng.nextInt(possibleSpawns.size()));

            state.moveOccupant(person.getLocation(), spawnLocation);

            // Choose new target for person
            Location randomTargetLocation = targetLocations.size() > 0
                    ? targetLocations.toArray(new Location[0])[rng.nextInt(targetLocations.size())]
                    : new Location(0, 0); // Has no target
            person.setTarget(randomTargetLocation);

            double movementDistance = distance(person.getLocation(), source.getLocation());

            scheduler.scheduleIn(() -> source.getConfiguration().getMoveStrategy().move(person, state, scheduler), 1 / person.getSpeed() * movementDistance);
        }
    }

    private List<Location> getPossibleSpawnLocations(State state, Source source) {
        List<Location> possibleMoves = new ArrayList<>();
        for (int row = Math.max(source.getLocation().getRow() - 1, 0);
             row <= source.getLocation().getRow() + 1 && row < state.getRows();
             row++) {
            for (int column = Math.max(source.getLocation().getColumn() - 1, 0);
                 column <= source.getLocation().getColumn() + 1 && column < state.getColumns();
                 column++) {
                final Location newLocation = new Location(row, column);

                if (!newLocation.equals(source.getLocation())) {
                    if (state.canBeOccupied(newLocation)) {
                        possibleMoves.add(newLocation);
                    }
                }
            }
        }

        return possibleMoves;
    }

    /**
     * Find a set of locations of simulation objects in the passed state.
     *
     * @param state to search for objects in
     * @param type  to search for
     * @return set of locations of the passed type in the passed state
     */
    private Set<Location> findSimObjectInState(State state, SimObjectType type) {
        Set<Location> result = new HashSet<>();

        for (int row = 0; row < state.getRows(); row++) {
            for (int column = 0; column < state.getColumns(); column++) {
                final Location location = new Location(row, column);
                state.getCellOccupant(location).ifPresent((occupant) -> {
                    if (occupant.getType() == type) {
                        result.add(location);
                    }
                });
            }
        }

        return result;
    }

    @Override
    public void init(State state, Random rng) {
        this.rng = rng;
        sourceLocations = findSimObjectInState(state, SimObjectType.SOURCE);
        targetLocations = findSimObjectInState(state, SimObjectType.TARGET);
    }

    @Override
    public String getName() {
        return ReviveConsumeStrategy.NAME;
    }

}
