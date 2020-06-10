package edu.hm.cs.bess.streamsim.sim.scheduler.exception;

/**
 * Exception which should be thrown in case an event execution has gone wrong.
 *
 * @author Benjamin Eder
 */
public class EventExecutionException extends Exception {

    public EventExecutionException() {
        super();
    }

    public EventExecutionException(String message) {
        super(message);
    }

    public EventExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventExecutionException(Throwable cause) {
        super(cause);
    }

}
