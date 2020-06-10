package edu.hm.cs.bess.streamsim.sim.scheduler.event;

/**
 * Event managed by a scheduler.
 *
 * @author Benjamin Eder
 */
public class Event implements Comparable<Event> {

    /**
     * Timestamp of when the event occurs.
     */
    private double timestamp;

    /**
     * Event handler being executed when the event occurs.
     */
    private final EventHandler handler;

    public Event(EventHandler handler, double timestamp) {
        this.handler = handler;
        this.timestamp = timestamp;
    }

    /**
     * Get the timestamp of when the event occurs
     *
     * @return timestamp
     */
    public double getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp of when the event occurs.
     *
     * @param timestamp of when the event occurs
     */
    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the event handler to execute once the event occurs.
     *
     * @return event handler
     */
    public EventHandler getHandler() {
        return handler;
    }

    @Override
    public int compareTo(Event o) {
        return Double.compare(getTimestamp(), o.getTimestamp());
    }

}
