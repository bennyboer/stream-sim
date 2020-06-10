package edu.hm.cs.bess.streamsim.ui.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.CellDescriptor;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line interface for the simulator.
 *
 * @author Benjamin Eder
 */
@CommandLine.Command(
        name = "stream_sim_cli",
        mixinStandardHelpOptions = true,
        version = "v0.1.0",
        description = "Command line interface for the stream simulator"
)
public class App implements Callable<Integer> {

    /**
     * Logger for the CLI.
     */
    private static final Logger LOGGER = Logger.getLogger("CLI");

    /**
     * File holding the configuration of the simulation.
     */
    @CommandLine.Parameters(index = "0", description = "File holding the configuration of the simulation")
    private File configurationFile;

    /**
     * The seed to use instead of the one in the configuration.
     */
    @CommandLine.Option(names = {"-s", "--seed"}, description = "The seed to use instead of the one in the configuration")
    private long seed = Long.MIN_VALUE;

    /**
     * Automatically use a random seed instead of the one in the configuration.
     */
    @CommandLine.Option(names = {"-as", "--auto-seed"}, description = "Automatically choose a random seed other than the seed in the configuration")
    private boolean autoSeed;

    /**
     * Number of times to run the simulation.
     */
    @CommandLine.Option(names = {"-r", "--runs"}, description = "Specify the number of times to run the simulation")
    private int runs = 1;

    /**
     * Time unit of the simulation in milliseconds.
     */
    @CommandLine.Option(names = {"-d", "--delay"}, description = "Delay the simulation: Specify how long a time unit should take (in milliseconds)")
    private int delay = 0;

    /**
     * Whether writing the simulation logs to the file system is enabled.
     */
    @CommandLine.Option(names = {"-l", "--log"}, description = "Whether writing the simulation logs to the file system is enabled")
    private boolean enableLogging = false;

    /**
     * Folder to log simulation run logs to.
     */
    @CommandLine.Option(names = {"-lf", "--log-folder"}, description = "Folder to write simulation logs to")
    private File logFolder = new File(System.getProperty("user.dir"));

    /**
     * Name of the log files produced by the simulator.
     */
    @CommandLine.Option(names = {"-ln", "--log-file-name"}, description = "File name prefix for simulation logs written to the file system")
    private String logFilePrefix = "sim_log";

    /**
     * Time units to wait before logging statistics again.
     */
    @CommandLine.Option(names = {"--statistics-logging-debounce-delay"}, description = "Time units to wait before logging statistics again")
    private double statisticsLoggingDebounceDelay = 10.0;

    /**
     * How many cells fit in a meter. Used to calculate density.
     * Value of 2.5 means a person needs 40cm.
     */
    @CommandLine.Option(names = {"--cells-per-meter"}, description = "How many cells fit in a meter. Used to calculate density. Value of 2.5 means a person needs 40cm.")
    private double cellsPerMeter = 2.5;

    /**
     * Size of the window to use to calculate the mean speed of people.
     * For example 5 means that we only use the 5 latest speed records of a person to calculate the mean speed.
     */
    @CommandLine.Option(names = {"--mean-speed-window-size"}, description = "Size of the window to use to calculate the mean speed of people")
    private int meanSpeedWindowSize = 5;

    /**
     * The maximum simulation time to early exit the simulation.
     */
    @CommandLine.Option(names = {"--max-simulation-time"}, description = "The maximum simulation time to early exit the simulation")
    private double maxSimulationTime = -1;

    /**
     * The maximum density to early exit the simulation.
     */
    @CommandLine.Option(names = {"--max-density"}, description = "The maximum density to early exit the simulation")
    private double maxDensity = -1;

    /**
     * The maximum amount of people to early exit the simulation.
     */
    @CommandLine.Option(names = {"--max-people"}, description = "The maximum amount of people to early exit the simulation")
    private int maxPeople = -1;

    /**
     * How much time is waited until logging the current simulation time again.
     */
    @CommandLine.Option(names = {"--log-simulation-time-change-delay"}, description = "The delay specifies how much time is waited until logging the current simulation time again")
    private double logSimulationTimeChangeDelay = 1;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        SimConfig config = loadConfig();

        Random rng = new Random();

        for (int run = 1; run <= runs; run++) {
            LOGGER.log(Level.INFO, String.format("Starting simulation run %d of %d", run, runs));

            long seed = autoSeed
                    ? rng.nextInt(999999999)
                    : this.seed != Long.MIN_VALUE ? this.seed : config.getSeed();

            LOGGER.log(Level.INFO, String.format("Using seed '%d' for simulation", seed));

            run(config, seed, run);
        }

        return 0;
    }

    /**
     * Load the simulation configuration.
     *
     * @return loaded configuration
     */
    private SimConfig loadConfig() throws IOException {
        if (!configurationFile.exists()) {
            throw new IOException("Provided configuration at '" + configurationFile + "' does not exist");
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        try {
            return reader.readValue(configurationFile, SimConfig.class);
        } catch (IOException e) {
            throw new IOException("Could not parse provided simulation configuration file", e);
        }
    }

    /**
     * Run the simulation with the passed configuration.
     *
     * @param config to run simulation with
     * @param seed   to use
     * @param run    the simulation run
     */
    private void run(SimConfig config, long seed, int run) {
        StreamSimulator simulator = new StreamSimulator(buildState(config), seed, enableLogging, logFolder, String.format("%s%d", logFilePrefix, run));
        simulator.setTimeUnitInMillis(delay);
        simulator.setStatisticsUpdateDebounceDelay(statisticsLoggingDebounceDelay);
        simulator.setStatisticsCellsPerMeter(cellsPerMeter);
        simulator.setStatisticsMeanSpeedWindowSize(meanSpeedWindowSize);

        CyclicBarrier endBarrier = new CyclicBarrier(2);

        simulator.addLifeCycleEventListener(new StreamSimulator.SimulationLifeCycleEventListener() {

            /**
             * Timestamp of when the last time change has been logged.
             */
            private double lastSimulationTimeChangeLogged = -1;

            @Override
            public void onStart() {
                LOGGER.log(Level.INFO, "[Simulation Lifecycle Change]: STARTED");
            }

            @Override
            public void onEnd() {
                LOGGER.log(Level.INFO, "[Simulation Lifecycle Change]: FINISHED");
                try {
                    endBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    // Not too bad ;)
                }
            }

            @Override
            public void onPause() {
                LOGGER.log(Level.INFO, "[Simulation Lifecycle Change]: PAUSED");
            }

            @Override
            public void onContinue() {
                LOGGER.log(Level.INFO, "[Simulation Lifecycle Change]: CONTINUED");
            }

            @Override
            public void onReset() {
                LOGGER.log(Level.INFO, "[Simulation Lifecycle Change]: RESET");
                try {
                    endBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    // Not too bad ;)
                }
            }

            @Override
            public void onTimeChange(double time) {
                if (lastSimulationTimeChangeLogged == -1 || time - lastSimulationTimeChangeLogged >= logSimulationTimeChangeDelay) {
                    lastSimulationTimeChangeLogged = time;
                    LOGGER.log(Level.INFO, String.format("[Simulation Lifecycle Change]: SIMULATION TIME = %f", time));
                }

                if (maxSimulationTime > 0 && time >= maxSimulationTime) {
                    LOGGER.log(Level.INFO, String.format("Simulation reached the specified maximum simulation time of %f -> Exiting...", maxSimulationTime));
                    simulator.terminate();
                }
            }

        });

        simulator.addStatisticsChangeListener((peopleCount, density, meanSpeed, flow) -> {
            LOGGER.log(Level.INFO, String.format("[CURRENT STATS] People count = %d, density = %f, meanSpeed = %f, flow = %f", peopleCount, density, meanSpeed, flow));

            if (maxPeople > 0 && peopleCount >= maxPeople) {
                LOGGER.log(Level.INFO, String.format("Simulation reached the specified maximum people count of %d -> Exiting...", maxPeople));
                simulator.terminate();
            }

            if (maxDensity > 0 && density >= maxDensity) {
                LOGGER.log(Level.INFO, String.format("Simulation reached the specified maximum density of %f -> Exiting...", maxDensity));
                simulator.terminate();
            }
        });

        simulator.play();

        Thread shutdownHookThread = new Thread(simulator::terminate);
        Runtime.getRuntime().addShutdownHook(shutdownHookThread);

        try {
            endBarrier.await(); // Wait for simulation end
        } catch (InterruptedException | BrokenBarrierException e) {
            // Not too bad ;)
        }

        Runtime.getRuntime().removeShutdownHook(shutdownHookThread);

        LOGGER.log(Level.INFO, "Saving logs...");
        simulator.saveLogs();

        LOGGER.log(Level.INFO, "Terminated simulation run");
    }

    /**
     * Build the state from the passed configuration.
     *
     * @param config to build from
     * @return state
     */
    private State buildState(SimConfig config) {
        int rows = config.getRows();
        int columns = config.getColumns();

        State state = new State(rows, columns);

        for (CellDescriptor cellDescriptor : config.getCellDescriptors().values()) {
            state.setCellOccupant(CellDescriptor.createSimObject(cellDescriptor), cellDescriptor.getLocation());
        }

        return state;
    }

}
