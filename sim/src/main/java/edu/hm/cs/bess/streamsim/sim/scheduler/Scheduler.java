package edu.hm.cs.bess.streamsim.sim.scheduler;

import edu.hm.cs.bess.streamsim.sim.scheduler.event.EventHandler;
import edu.hm.cs.bess.streamsim.sim.scheduler.exception.EventExecutionException;

import java.util.Optional;

/**
 * Scheduler for the simulation.
 *
 * @author Benjamin Eder
 */
public interface Scheduler {

    /**
     * Schedule the passed event in the passed relative time.
     *
     * @param handler      to schedule processing of
     * @param relativeTime in which to process the event
     */
    void scheduleIn(EventHandler handler, double relativeTime);

    /**
     * The current time of the scheduler.
     *
     * @return current time
     */
    double currentTime();

    /**
     * Clear all pending events to be processed.
     */
    void clear();

    /**
     * Execute the next event to be processed.
     *
     * @return whether there was something left to process
     */
    boolean processNext() throws EventExecutionException;

    /**
     * Peek the currently next events timestamp.
     *
     * @return next events timestamp
     */
    Optional<Double> peekNextTimestamp();

}
