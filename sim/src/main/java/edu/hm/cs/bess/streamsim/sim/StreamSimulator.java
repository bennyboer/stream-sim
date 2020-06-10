package edu.hm.cs.bess.streamsim.sim;

import edu.hm.cs.bess.streamsim.sim.logic.move.DefaultMovementStrategy;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.lightbarrier.LightBarrier;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.object.target.Target;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.sim.scheduler.EventDrivenScheduler;
import edu.hm.cs.bess.streamsim.sim.scheduler.Scheduler;
import edu.hm.cs.bess.streamsim.sim.scheduler.exception.EventExecutionException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Formatter;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * The actual simulation implementation root.
 *
 * @author Benjamin Eder
 */
public class StreamSimulator {

    /**
     * Logger to log statistics with.
     */
    private static final Logger STATISTICS_LOGGER = Logger.getLogger("StatisticsLogger");

    /**
     * Starting state of the simulation.
     */
    private State startState;

    /**
     * The current state of the simulation.
     */
    private State currentState;

    /**
     * Scheduler responsible for scheduling events of the event-driven simulation.
     */
    private final Scheduler scheduler = new EventDrivenScheduler();

    /**
     * Whether the simulation has already been started (regardless of whether it is paused or not).
     */
    private boolean started = false;

    /**
     * Whether the simulation is currently running.
     */
    private boolean running = false;

    /**
     * Lock guarding the running property.
     */
    private final ReadWriteLock runningLock = new ReentrantReadWriteLock();

    /**
     * List of life cycle event listeners.
     */
    private List<SimulationLifeCycleEventListener> lifeCycleEventListeners;

    /**
     * List of statistics change listeners.
     */
    private List<StatisticsChangeListener> statisticsChangeListeners;

    /**
     * Executor service scheduling the event processing.
     */
    private ScheduledExecutorService executorService;

    /**
     * Milliseconds per time unit in the event-driven scheduler.
     * Set to 0 if you want an "As-soon-as-possible" execution.
     */
    private final AtomicInteger timeUnitInMillis = new AtomicInteger(0);

    /**
     * Seed for the random number generator.
     */
    private final long seed;

    /**
     * Current source objects in the simulator.
     */
    @Nullable
    private List<Source> sources;

    /**
     * Buffer for the movement logging.
     */
    private MemoryHandler movementLogBuffer;

    /**
     * Buffer for the statistics logging.
     */
    private MemoryHandler statsLogBuffer;

    /**
     * File handler dealing with saving the movement logs.
     */
    private FileHandler movementLogFileHandler;

    /**
     * File handler dealing with saving the statistics logs.
     */
    private FileHandler statsLogFileHandler;

    /**
     * Whether logging should be enabled.
     */
    private final boolean shouldLog;

    /**
     * Life cycle event listener of the simulator.
     */
    private StreamSimulator.SimulationLifeCycleEventListener lifeCycleEventListener;

    /**
     * Timestamp of when the last statistics have been logged.
     */
    private double lastStatisticsUpdateTimestamp = -1;

    /**
     * Used to debounce the statistics logging.
     * For example a value of 10 would mean that we should only
     * log statistics every 10 time units.
     */
    private double statisticsUpdateDebounceDelay = 10.0;

    /**
     * How many cells fit in a meter.
     * The default value of 2.5 means that a cells size is 40 cm.
     * This is used to calculate some metrics for the statistics logs.
     */
    private double cellsPerMeter = 2.5;

    /**
     * Window size of the window to calculate the people mean speed with in the statistics log.
     */
    private int meanSpeedWindowSize = 5;

    /**
     * Count of walkable cells.
     * Used in the density calculation.
     */
    private int walkableCellCount;

    /**
     * Create simulator using the passed start state and seed.
     *
     * @param state of the simulation world
     * @param seed  to use
     */
    public StreamSimulator(State state, long seed) {
        this(state, seed, false, new File(System.getProperty("user.dir")), "sim_log");
    }

    /**
     * Create simulator using the passed options.
     *
     * @param state         to use as start
     * @param seed          to use
     * @param shouldLog     whether logging should be enabled
     * @param logFolder     folder to save logs in
     * @param logFilePrefix prefix of the log file
     */
    public StreamSimulator(State state, long seed, boolean shouldLog, File logFolder, String logFilePrefix) {
        this.seed = seed;
        this.shouldLog = shouldLog;

        try {
            startState = (State) state.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(); // Will not happen
        }

        if (shouldLog) {
            initializeLoggingHandlers(logFolder, logFilePrefix);
        }

        reset();
    }

    /**
     * Set the time unit in milliseconds the simulation should run with.
     * Set to 0 if you want an "As-soon-as-possible" execution.
     *
     * @param timeUnitInMillis how many milliseconds a time unit should take
     */
    public void setTimeUnitInMillis(int timeUnitInMillis) {
        this.timeUnitInMillis.set(timeUnitInMillis);
    }

    /**
     * Get the set time unit in milliseconds.
     *
     * @return time unit in milliseconds
     */
    public int getTimeUnitInMillis() {
        return timeUnitInMillis.get();
    }

    /**
     * Set the time to wait until logging statistics again.
     * For example a value of 10 would mean the simulator should only log statistics every 10 time units.
     *
     * @param statisticsUpdateDebounceDelay to wait
     */
    public void setStatisticsUpdateDebounceDelay(double statisticsUpdateDebounceDelay) {
        this.statisticsUpdateDebounceDelay = statisticsUpdateDebounceDelay;
    }

    /**
     * Set how many cells fit into a meter for the statistics calculation.
     *
     * @param cellsPerMeter how many cells fit into a meter
     */
    public void setStatisticsCellsPerMeter(double cellsPerMeter) {
        this.cellsPerMeter = cellsPerMeter;
    }

    /**
     * Set the statistics logs window size to calculate the people mean speed with.
     *
     * @param meanSpeedWindowSize size to use
     */
    public void setStatisticsMeanSpeedWindowSize(int meanSpeedWindowSize) {
        this.meanSpeedWindowSize = meanSpeedWindowSize;
    }

    /**
     * Get the scheduler used by the simulator.
     *
     * @return scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Initialize the logging handlers.
     *
     * @param logFolder folder to save logs to
     * @param prefix    of the log files
     */
    public void initializeLoggingHandlers(File logFolder, String prefix) {
        try {
            logFolder.mkdirs();

            if (!logFolder.isDirectory()) {
                throw new IllegalArgumentException("Location must be a directory");
            }

            movementLogFileHandler = new FileHandler(logFolder.getAbsolutePath() + File.separator + prefix + "_movement.csv");
            statsLogFileHandler = new FileHandler(logFolder.getAbsolutePath() + File.separator + prefix + "_stats.csv");

            movementLogBuffer = new MemoryHandler(movementLogFileHandler, 10000, Level.OFF);
            movementLogBuffer.setFormatter(new SuperSimpleFormatter());
            statsLogBuffer = new MemoryHandler(statsLogFileHandler, 10000, Level.OFF);
            statsLogBuffer.setFormatter(new SuperSimpleFormatter());

            movementLogFileHandler.setFormatter(new SuperSimpleFormatter());
            movementLogFileHandler.setLevel(Level.ALL);
            statsLogFileHandler.setFormatter(new SuperSimpleFormatter());
            statsLogFileHandler.setLevel(Level.ALL);

            DefaultMovementStrategy.getCSVMovementLogger().addHandler(movementLogBuffer);
            DefaultMovementStrategy.getCSVMovementLogger().setLevel(Level.ALL);
            DefaultMovementStrategy.getCSVMovementLogger().setUseParentHandlers(false);
            DefaultMovementStrategy.getCSVMovementLogger().finest("Time;PersonId;Row;Column;Speed;MeanSpeed;MeanSpeed5");
            STATISTICS_LOGGER.addHandler(statsLogFileHandler);
            STATISTICS_LOGGER.setLevel(Level.ALL);
            STATISTICS_LOGGER.setUseParentHandlers(false);
            STATISTICS_LOGGER.finest("Time;PeopleCount;WindowSize;CellsInMeter;MeanSpeed;Density;Flow");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start or continue playing the simulation.
     */
    public void play() {
        executorService = Executors.newSingleThreadScheduledExecutor();

        setRunning(true);

        if (!isStarted()) {
            started = true; // Set simulation as started
            notifyLifeCycleEventListeners(LifeCycleEvent.START);

            initializeSimulationStart();
        } else {
            notifyLifeCycleEventListeners(LifeCycleEvent.CONTINUE);
        }

        // Start/continue event processing
        scheduleNextEventProcessing();
    }

    /**
     * Initialize at simulation start.
     */
    private void initializeSimulationStart() {
        Random rng = new Random(seed);

        sources = new ArrayList<>();

        AtomicInteger walkableCellsCounter = new AtomicInteger();
        for (int row = 0; row < currentState.getRows(); row++) {
            for (int column = 0; column < currentState.getColumns(); column++) {
                Optional<SimObject> optionalSimObject = currentState.getCellOccupant(new Location(row, column));

                if (optionalSimObject.isPresent()) {
                    SimObject simObject = optionalSimObject.get();

                    switch (simObject.getType()) {
                        case SOURCE -> {
                            Source source = (Source) simObject;

                            // Initialize spawn strategy
                            source.getConfiguration().getSpawnStrategy().init(currentState, rng);

                            // Initialize move strategy
                            source.getConfiguration().getMoveStrategy().init(currentState, rng);

                            // Initialize speed generator
                            source.getConfiguration().getSpeedGenerator().init(rng);

                            // Initialize patience generator
                            source.getConfiguration().getPatienceGenerator().init(rng);

                            // Start spawning people
                            scheduler.scheduleIn(
                                    () -> source.getConfiguration().getSpawnStrategy().spawn(source, currentState, scheduler),
                                    source.getConfiguration().getSpawnStrategy().getNextSpawnTime()
                            );

                            sources.add(source);
                        }
                        case TARGET -> {
                            Target target = (Target) simObject;

                            // Initialize consume strategy
                            target.getConfiguration().getConsumeStrategy().init(currentState, rng);
                        }
                        default -> {
                            // Nothing to do.
                        }
                    }

                    if (simObject.isWalkable()) {
                        walkableCellsCounter.getAndIncrement();
                    }
                } else {
                    walkableCellsCounter.getAndIncrement();
                }
            }
        }

        walkableCellCount = walkableCellsCounter.get();
    }

    /**
     * Get the current sources in the simulation.
     *
     * @return current source objects
     */
    @Nullable
    public List<Source> getSources() {
        return sources;
    }

    /**
     * Schedule the processing of the next event.
     */
    private void scheduleNextEventProcessing() {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        Optional<Double> nextTimestampOptional = scheduler.peekNextTimestamp();
        if (nextTimestampOptional.isPresent()) {
            double nextTime = nextTimestampOptional.get();
            double currentTime = scheduler.currentTime();

            double diff = nextTime - currentTime;

            long delay = Math.round(timeUnitInMillis.get() * diff * 1000 * 1000);

            try {
                executorService.schedule(() -> {
                    try {
                        scheduler.processNext();

                        notifyLifeCycleEventListeners(LifeCycleEvent.TIME_CHANGE);

                        scheduleNextEventProcessing();
                    } catch (RuntimeException | EventExecutionException e) {
                        e.printStackTrace();
                    }
                }, delay, TimeUnit.NANOSECONDS);
            } catch (RejectedExecutionException e) {
                // That's ok, simulation has been cancelled/paused/reset.
            }
        } else {
            // Nothing to do anymore -> Exit simulation
            executorService.shutdown();
            setRunning(false);
            notifyLifeCycleEventListeners(LifeCycleEvent.END);
        }
    }

    /**
     * Pause the simulation.
     */
    public void pause() {
        if (!isRunning()) {
            return;
        }

        executorService.shutdownNow();
        setRunning(false);

        notifyLifeCycleEventListeners(LifeCycleEvent.PAUSE);
    }

    /**
     * Reset the simulation to the start state.
     */
    public void reset() {
        if (isRunning()) {
            pause();
        }

        scheduler.clear();

        // Reinitialize current state from start state
        try {
            currentState = (State) startState.clone();
        } catch (CloneNotSupportedException e) {
            // Will not happen since cloning IS supported
        }

        started = false;

        notifyLifeCycleEventListeners(LifeCycleEvent.RESET);

        if (lifeCycleEventListener == null) {
            lifeCycleEventListener = new SimulationLifeCycleEventListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onEnd() {
                }

                @Override
                public void onPause() {
                }

                @Override
                public void onContinue() {
                }

                @Override
                public void onReset() {
                }

                @Override
                public void onTimeChange(double time) {
                    if (lastStatisticsUpdateTimestamp == -1 || time - lastStatisticsUpdateTimestamp >= statisticsUpdateDebounceDelay) {
                        double timeElapsed = time - lastStatisticsUpdateTimestamp;
                        lastStatisticsUpdateTimestamp = time;
                        updateStatistics(timeElapsed);
                    }
                }
            };

            addLifeCycleEventListener(lifeCycleEventListener);
        }
    }

    /**
     * Log some statistics to file.
     *
     * @param timeElapsed the elapsed time between now and the last logStatistics call
     */
    private void updateStatistics(double timeElapsed) {
        final AtomicInteger peopleCount = new AtomicInteger();
        final AtomicInteger lightBarrierCount = new AtomicInteger();

        final AtomicReference<Double> meanSpeed = new AtomicReference<>();
        final AtomicReference<Double> density = new AtomicReference<>();
        final AtomicReference<Double> flow = new AtomicReference<>();

        currentState.readObjectTypeMapping(mapping -> {
            Set<Location> lightBarrierLocations = mapping.get(SimObjectType.LIGHT_BARRIER);
            lightBarrierCount.set(lightBarrierLocations != null ? lightBarrierLocations.size() : 0);

            Set<Location> peopleLocations = mapping.get(SimObjectType.PERSON);
            peopleCount.set(peopleLocations != null ? peopleLocations.size() : 0);

            Set<Person> people = peopleLocations != null ? peopleLocations.stream()
                    .map(l -> (Person) currentState.getUpperCellOccupant(l).orElseThrow())
                    .collect(Collectors.toUnmodifiableSet()) : null;

            meanSpeed.set(calculateMeanSpeed(meanSpeedWindowSize, cellsPerMeter, people));
            density.set(calculateDensity(cellsPerMeter, peopleCount.get()));

            double lightBarrierWidth = lightBarrierCount.get() > 0 ? lightBarrierCount.get() / cellsPerMeter : 1; // Avoid division by zero error
            flow.set(LightBarrier.getTriggerCount() / lightBarrierWidth / timeElapsed); // Flow in people/m/s
            LightBarrier.resetTriggerCount();
        });

        notifyStatisticsChangeListeners(peopleCount.get(), density.get(), meanSpeed.get(), flow.get());

        if (shouldLog) {
            STATISTICS_LOGGER.finest(String.format(
                    Locale.ROOT,
                    "%f;%d;%d;%f;%f;%f;%f",
                    scheduler.currentTime(),
                    peopleCount.get(),
                    meanSpeedWindowSize,
                    cellsPerMeter,
                    meanSpeed.get(),
                    density.get(),
                    flow.get()
            ));
        }
    }

    public void saveLogs() {
        if (!shouldLog) {
            return;
        }

        movementLogBuffer.push();
        movementLogFileHandler.flush();
        movementLogFileHandler.close();
        statsLogBuffer.push();
        statsLogFileHandler.flush();
        statsLogFileHandler.close();
    }

    /**
     * Terminate the simulator (NO MORE USABLE AFTERWARDS).
     */
    public void terminate() {
        // Log last statistics before terminating.
        updateStatistics(scheduler.currentTime() - lastStatisticsUpdateTimestamp);

        reset();
    }

    /**
     * Whether the simulation has already been started.
     *
     * @return whether already started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Check if the simulation is currently running.
     *
     * @return whether the simulation is currently running
     */
    public boolean isRunning() {
        runningLock.readLock().lock();
        try {
            return running;
        } finally {
            runningLock.readLock().unlock();
        }
    }

    /**
     * Set whether the simulation is currently running.
     *
     * @param running whether running
     */
    private void setRunning(boolean running) {
        runningLock.writeLock().lock();
        try {
            this.running = running;
        } finally {
            runningLock.writeLock().unlock();
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    /**
     * Get the current count of people in the simulation world.
     *
     * @return current count of people in the simulation world
     */
    public int getPeopleCount() {
        return currentState.getObjectTypeCount(SimObjectType.PERSON);
    }

    /**
     * Calculate the density of people (1/m²) in the simulation world.
     *
     * @param cellsInMeter how many cells fit in a meter
     * @param peopleCount  count of people in the simulation
     * @return density
     */
    private double calculateDensity(double cellsInMeter, int peopleCount) {
        double totalSpaceInMeter = walkableCellCount / Math.pow(cellsInMeter, 2);

        return peopleCount / totalSpaceInMeter;
    }

    /**
     * Calculate the mean speed for all people in the simulation world (in m/time unit).
     *
     * @param windowSize    size of the window to calculate mean speed with (negative for the whole history).
     * @param cellsPerMeter how many cells fit in a meter
     * @param people        set of people currently in the simulation
     * @return mean speed of all people in the simulation
     */
    private double calculateMeanSpeed(int windowSize, double cellsPerMeter, Set<Person> people) {
        double meanSpeed = 0.0;

        if (people == null || people.size() == 0) {
            return 0.0;
        }

        for (Person p : people) {
            meanSpeed += windowSize < 0 ? p.getMeanSpeed() : p.getMeanSpeed(windowSize);
        }

        meanSpeed /= people.size();
        meanSpeed /= cellsPerMeter;

        return meanSpeed;
    }

    /**
     * Add an life cycle event listener.
     * NOTE THAT THE LISTENER WILL BE CALLED IN ANOTHER THREAD!
     *
     * @param listener to add
     */
    public void addLifeCycleEventListener(SimulationLifeCycleEventListener listener) {
        if (lifeCycleEventListeners == null) {
            lifeCycleEventListeners = new CopyOnWriteArrayList<>();
        }

        lifeCycleEventListeners.add(listener);
    }

    /**
     * Remove the passed life cycle event listener.
     *
     * @param listener to remove
     */
    public void removeLifeCycleEventListener(SimulationLifeCycleEventListener listener) {
        if (lifeCycleEventListeners != null) {
            lifeCycleEventListeners.remove(listener);
        }
    }

    /**
     * Notify all listeners of a life cycle event.
     *
     * @param event that happened
     */
    protected void notifyLifeCycleEventListeners(LifeCycleEvent event) {
        if (lifeCycleEventListeners != null) {
            for (var l : lifeCycleEventListeners) {
                switch (event) {
                    case START -> l.onStart();
                    case CONTINUE -> l.onContinue();
                    case RESET -> l.onReset();
                    case PAUSE -> l.onPause();
                    case END -> l.onEnd();
                    case TIME_CHANGE -> l.onTimeChange(scheduler.currentTime());
                }
            }
        }
    }

    /**
     * Add a new statistics listener listening on statistics updates.
     *
     * @param listener to register
     */
    public void addStatisticsChangeListener(StatisticsChangeListener listener) {
        if (statisticsChangeListeners == null) {
            statisticsChangeListeners = new CopyOnWriteArrayList<>();
        }

        statisticsChangeListeners.add(listener);
    }

    /**
     * Remove the passed statistics change listener.
     *
     * @param listener to remove
     */
    public void removeStatisticsChangeListener(StatisticsChangeListener listener) {
        if (statisticsChangeListeners != null) {
            statisticsChangeListeners.remove(listener);
        }
    }

    /**
     * Notify all listeners of a life cycle event.
     *
     * @param peopleCount count of people currently
     * @param density     of the current simulation
     * @param meanSpeed   of the current simulation
     * @param flow        of the current simulation
     */
    protected void notifyStatisticsChangeListeners(int peopleCount, double density, double meanSpeed, double flow) {
        if (statisticsChangeListeners != null) {
            for (var l : statisticsChangeListeners) {
                l.onUpdate(peopleCount, density, meanSpeed, flow);
            }
        }
    }

    /**
     * Enumeration of life cycle events.
     */
    private enum LifeCycleEvent {
        START,
        PAUSE,
        CONTINUE,
        RESET,
        END,
        TIME_CHANGE
    }

    /**
     * Listener to simulation life cycle events.
     */
    public interface SimulationLifeCycleEventListener {

        /**
         * Called when the simulation starts.
         */
        void onStart();

        /**
         * Called when the simulation end (no more events to process).
         */
        void onEnd();

        /**
         * Called on simulation pause.
         */
        void onPause();

        /**
         * Called when the simulation continues.
         */
        void onContinue();

        /**
         * Called when the simulation has been reset.
         */
        void onReset();

        /**
         * Called when the simulation time changed.
         *
         * @param time current time
         */
        void onTimeChange(double time);

    }

    /**
     * Change listener listening for statistics updates.
     */
    public interface StatisticsChangeListener {

        /**
         * Called when the statistics are updated.
         *
         * @param peopleCount count of people currently
         * @param density     of the current simulation
         * @param meanSpeed   of the current simulation
         * @param flow        of the current simulation
         */
        void onUpdate(int peopleCount, double density, double meanSpeed, double flow);

    }

    /**
     * Formatter for our log messages.
     */
    public static class SuperSimpleFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }
    }

}
