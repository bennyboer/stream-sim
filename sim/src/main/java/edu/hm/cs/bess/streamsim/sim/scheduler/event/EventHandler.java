package edu.hm.cs.bess.streamsim.sim.scheduler.event;

import edu.hm.cs.bess.streamsim.sim.scheduler.exception.EventExecutionException;

/**
 * Event being processed by an simulation scheduler.
 *
 * @author Benjamin Eder
 */
@FunctionalInterface
public interface EventHandler {

    /**
     * Process the event.
     *
     * @throws EventExecutionException in case the execution has gone wrong
     */
    void process() throws EventExecutionException;

}
