package edu.hm.cs.bess.streamsim.sim.rimeatest;

import edu.hm.cs.bess.streamsim.sim.AbstractSimulationTest;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.exception.EventExecutionException;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FreeFlowTest extends AbstractSimulationTest {

    private static final java.util.logging.Logger LOGGER = Logger.getLogger("FreeFlowTest");

    private static final String FILE_PREFIX = "freeflow/";

    private static final String CORNER_CONFIG = "CornerConfig.json";
    private static final String HORIZONTAL_CONFIG = "HorizontalLineConfig.json";
    private static final String VERTICAL_CONFIG = "VerticalLineConfig.json";
    private static final String SMALL_ANGLE_CONFIG = "SmallAngleConfig.json";

    private static final double DEFAULT_ACCURACY = 2.1;

    @BeforeAll
    public static void initLogger() {
        LOGGER.setLevel(Level.INFO);
    }

    @Test
    public void dijkstraCornerTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + DIJKSTRA_PREFIX + CORNER_CONFIG,
                new Location(49, 49),
                new Location(0, 0),
                DEFAULT_ACCURACY);
    }

    @Test
    public void dijkstraHorizontalTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + DIJKSTRA_PREFIX + HORIZONTAL_CONFIG,
                new Location(10, 49),
                new Location(10, 0),
                DEFAULT_ACCURACY);
    }

    @Test
    public void dijkstraVerticalTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + DIJKSTRA_PREFIX + VERTICAL_CONFIG,
                new Location(49, 10),
                new Location(0, 10),
                DEFAULT_ACCURACY);
    }

    @Test
    public void dijkstraSmallAngleTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + DIJKSTRA_PREFIX + SMALL_ANGLE_CONFIG,
                new Location(49, 49),
                new Location(20, 14),
                DEFAULT_ACCURACY);
    }

    @Test
    public void euclideanCornerTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + EUCLIDEAN_PREFIX + CORNER_CONFIG,
                new Location(49, 49),
                new Location(0, 0),
                DEFAULT_ACCURACY);
    }

    @Test
    public void euclideanHorizontalTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + EUCLIDEAN_PREFIX + HORIZONTAL_CONFIG,
                new Location(10, 49),
                new Location(10, 0),
                DEFAULT_ACCURACY);
    }

    @Test
    public void euclideanVerticalTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + EUCLIDEAN_PREFIX + VERTICAL_CONFIG,
                new Location(49, 10),
                new Location(0, 10),
                DEFAULT_ACCURACY);
    }

    @Test
    public void euclideanSmallAngleTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + EUCLIDEAN_PREFIX + SMALL_ANGLE_CONFIG,
                new Location(49, 49),
                new Location(20, 14),
                DEFAULT_ACCURACY);
    }

    @Test
    public void fastMarchingCornerTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + FAST_MARCHING_PREFIX + CORNER_CONFIG,
                new Location(49, 49),
                new Location(0, 0),
                DEFAULT_ACCURACY);
    }

    @Test
    public void fastMarchingHorizontalTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + FAST_MARCHING_PREFIX + HORIZONTAL_CONFIG,
                new Location(10, 49),
                new Location(10, 0),
                DEFAULT_ACCURACY);
    }

    @Test
    public void fastMarchingVerticalTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + FAST_MARCHING_PREFIX + VERTICAL_CONFIG,
                new Location(49, 10),
                new Location(0, 10),
                DEFAULT_ACCURACY);
    }

    @Test
    public void fastMarchingSmallAngleTest() throws EventExecutionException {
        executeSimulation(FILE_PREFIX + FAST_MARCHING_PREFIX + SMALL_ANGLE_CONFIG,
                new Location(49, 49),
                new Location(20, 14),
                DEFAULT_ACCURACY);
    }

    private void executeSimulation(String fileName, Location sourceLocation, Location targetLocation, double accuracyPercent) throws EventExecutionException {
        StreamSimulator simulator = initSimulationWithConfig(fileName);

        // Wait until a person is spawned
        AtomicReference<Location> personLocationRef = new AtomicReference<>();
        CyclicBarrier barrier = new CyclicBarrier(2);
        State.StateUpdateListener stateUpdateListener = (events) -> {
            for (State.UpdateEvent event : events) {
                if (event.getType() == State.EventType.ADDED && event.getNewOccupant().getType() == SimObjectType.PERSON) {
                    personLocationRef.set(event.getNewOccupant().getLocation());

                    simulator.pause();
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        LOGGER.fine("ignoring: " + e.getClass());
                    }
                }
            }
        };
        simulator.getCurrentState().addUpdateListener(stateUpdateListener);
        simulator.play();
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            LOGGER.fine("ignoring: " + e.getMessage());
        }
        simulator.getCurrentState().removeUpdateListener(stateUpdateListener);

        Location personLocation = personLocationRef.get();
        Assertions.assertThat(simulator.getCurrentState().getCellOccupant(sourceLocation)).isNotEmpty();
        Assertions.assertThat(simulator.getCurrentState().getCellOccupant(sourceLocation).get().getType()).
                isEqualTo(SimObjectType.SOURCE);
        Assertions.assertThat(simulator.getCurrentState().getCellOccupant(targetLocation)).isNotEmpty();
        Assertions.assertThat(simulator.getCurrentState().getCellOccupant(targetLocation).get().getType()).
                isEqualTo(SimObjectType.TARGET);
        Assertions.assertThat(simulator.getCurrentState().getCellOccupant(personLocation)).isNotEmpty();
        Assertions.assertThat(simulator.getCurrentState().getCellOccupant(personLocation).get().getType()).
                isEqualTo(SimObjectType.PERSON);

        double startTime = simulator.getScheduler().currentTime();
        double speed = ((Person) simulator.getCurrentState().getCellOccupant(personLocation).get()).getSpeed();

        Location newPersonLocation;
        SimObjectType targetType = SimObjectType.PERSON;
        while (!personLocation.equals(targetLocation)) {
            // process events until person has moved -> location is no more present
            do {
                simulator.getScheduler().processNext();
            } while (simulator.getCurrentState().getCellOccupant(personLocation).isPresent());

            newPersonLocation = getSpawnedLocation(simulator.getCurrentState(), personLocation);

            if (personLocation.getRow() <= targetLocation.getRow()) {
                Assertions.assertThat(newPersonLocation.getRow()).isGreaterThanOrEqualTo(personLocation.getRow());
            } else {
                Assertions.assertThat(newPersonLocation.getRow()).isLessThanOrEqualTo(personLocation.getRow());
            }

            if (personLocation.getColumn() <= targetLocation.getColumn()) {
                Assertions.assertThat(newPersonLocation.getColumn()).isGreaterThanOrEqualTo(personLocation.getColumn());
            } else {
                Assertions.assertThat(newPersonLocation.getColumn()).isLessThanOrEqualTo(personLocation.getColumn());
            }

            // when moving at an angle the last movement should be the one to the target
            if (newPersonLocation.equals(targetLocation)) {
                targetType = SimObjectType.TARGET;
            }

            Assertions.assertThat(simulator.getCurrentState().getCellOccupant(newPersonLocation)).isNotEmpty();
            Assertions.assertThat(simulator.getCurrentState().getCellOccupant(newPersonLocation).get().getType()).
                    isEqualTo(targetType);

            personLocation = newPersonLocation;
        }

        double actualTime = simulator.getScheduler().currentTime() - startTime;
        double targetTime = getEuclideanDistance(sourceLocation, targetLocation) / speed;

        LOGGER.info("Time deviation: " + (Math.abs(targetTime-actualTime)/targetTime)*100 + " %");
        Assertions.assertThat(actualTime).isCloseTo(targetTime, Percentage.withPercentage(accuracyPercent));
    }

    private double getEuclideanDistance(Location source, Location target) {
        return Math.sqrt(Math.pow(source.getRow() - target.getRow(), 2) + Math.pow(source.getColumn() - target.getColumn(), 2));
    }

    private Location getSpawnedLocation(State currentState, Location sourceLocation) {
        for (int row = sourceLocation.getRow() - 1; row <= sourceLocation.getRow() + 1; row++) {
            for (int col = sourceLocation.getColumn() - 1; col <= sourceLocation.getColumn() + 1; col++) {
                Location location = new Location(row, col);
                if (currentState.getColumns() <= col || currentState.getRows() <= row || location.equals(sourceLocation)
                        || row < 0 || col < 0) {
                    continue;
                }
                if (currentState.getCellOccupant(location).isPresent()) {
                    if (currentState.getCellOccupant(location).get().getType() == SimObjectType.PERSON ||
                            currentState.getCellOccupant(location).get().getType() == SimObjectType.TARGET) {
                        return location;
                    }
                }
            }
        }
        return null;
    }


    private StreamSimulator initSimulationWithConfig(String fileName) {
        SimConfig config = createSimConfigFromFile(fileName);
        return new StreamSimulator(buildState(config), config.getSeed());
    }
}
