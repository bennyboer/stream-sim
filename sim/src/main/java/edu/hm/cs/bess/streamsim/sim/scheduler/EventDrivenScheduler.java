package edu.hm.cs.bess.streamsim.sim.scheduler;

import edu.hm.cs.bess.streamsim.sim.scheduler.event.Event;
import edu.hm.cs.bess.streamsim.sim.scheduler.event.EventHandler;
import edu.hm.cs.bess.streamsim.sim.scheduler.exception.EventExecutionException;

import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Event-driven simulation scheduler.
 *
 * @author Benjamin Eder
 */
public class EventDrivenScheduler implements Scheduler {

    /**
     * The schedulers event queue.
     */
    private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();

    /**
     * The current time of the scheduler.
     */
    private double currentTime;

    public EventDrivenScheduler() {
        this(0);
    }

    public EventDrivenScheduler(double startTime) {
        currentTime = startTime;
    }

    @Override
    public void scheduleIn(EventHandler handler, double relativeTime) {
        if (relativeTime < 0) {
            throw new IllegalArgumentException("Can only schedule events in the future. relativeTime needs to be greater or equal to 0.");
        }

        double absoluteTimestamp = currentTime + relativeTime;
        if (absoluteTimestamp == Double.MAX_VALUE) {
            // Overflow occurred -> Reset the schedulers time
            resetTime();
            absoluteTimestamp = currentTime + relativeTime;
        }

        eventQueue.add(new Event(handler, absoluteTimestamp));
    }

    /**
     * Reset the schedulers internal time and of all scheduled events.
     */
    private void resetTime() {
        for (Event event : eventQueue) {
            event.setTimestamp(event.getTimestamp() - currentTime);
        }

        currentTime = 0;
    }

    @Override
    public double currentTime() {
        return currentTime;
    }

    @Override
    public void clear() {
        eventQueue.clear();
        resetTime();
    }

    @Override
    public boolean processNext() throws EventExecutionException {
        Event event = eventQueue.poll();

        if (event != null) {
            currentTime = event.getTimestamp();

            event.getHandler().process();
            return true;
        }

        return false;
    }

    @Override
    public Optional<Double> peekNextTimestamp() {
        return Optional.ofNullable(eventQueue.peek()).map(Event::getTimestamp);
    }

}
