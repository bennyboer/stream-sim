package edu.hm.cs.bess.streamsim.sim.scheduler;

import edu.hm.cs.bess.streamsim.sim.scheduler.exception.EventExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Benjamin Eder
 */
public class EventDrivenSchedulerTest {

    @Test
    public void testOverflowBehavior() throws EventExecutionException {
        EventDrivenScheduler scheduler = new EventDrivenScheduler(Double.MAX_VALUE - 5);

        AtomicBoolean secondAlreadyExecuted = new AtomicBoolean(false);

        scheduler.scheduleIn(() -> {
            Assertions.assertFalse(secondAlreadyExecuted.get());
        }, 3); // Should be executed first
        scheduler.scheduleIn(() -> secondAlreadyExecuted.set(true), 10); // Should be executed second

        Assertions.assertEquals(0, scheduler.currentTime());

        scheduler.processNext();
        scheduler.processNext();
    }

    @Test
    public void testDeterministicOrderForCollisions() throws EventExecutionException {
        EventDrivenScheduler scheduler = new EventDrivenScheduler();

        for (int i = 0; i < 100; i++) {
            AtomicBoolean secondAlreadyExecuted = new AtomicBoolean(false);

            scheduler.scheduleIn(() -> {
                Assertions.assertFalse(secondAlreadyExecuted.get());
            }, 3); // Should be executed first
            scheduler.scheduleIn(() -> secondAlreadyExecuted.set(true), 3); // Should be executed second

            scheduler.processNext();
            scheduler.processNext();
        }
    }

    @Test
    public void testClear() throws EventExecutionException {
        EventDrivenScheduler scheduler = new EventDrivenScheduler();

        scheduler.scheduleIn(Assertions::fail, 10);
        scheduler.scheduleIn(Assertions::fail, 20);
        scheduler.scheduleIn(Assertions::fail, 30);

        scheduler.clear();

        scheduler.processNext();
    }

    @Test
    public void testSchedule() throws EventExecutionException {
        EventDrivenScheduler scheduler = new EventDrivenScheduler();

        AtomicInteger testCounter = new AtomicInteger(3);

        scheduler.scheduleIn(testCounter::decrementAndGet, 10);
        scheduler.scheduleIn(() -> Assertions.assertEquals(testCounter.get(), 2), 20);
        scheduler.scheduleIn(testCounter::decrementAndGet, 30);
        scheduler.scheduleIn(() -> Assertions.assertEquals(testCounter.get(), 1), 40);
        scheduler.scheduleIn(testCounter::decrementAndGet, 50);
        scheduler.scheduleIn(() -> Assertions.assertEquals(testCounter.get(), 0), 60);

        boolean processed = scheduler.processNext();
        Assertions.assertEquals(scheduler.currentTime(), 10);
        while (processed) {
            processed = scheduler.processNext();
        }
    }

    @Test
    public void testScheduledTimeNeedsToBeInTheFuture() throws EventExecutionException {
        EventDrivenScheduler scheduler = new EventDrivenScheduler();

        scheduler.scheduleIn(() -> {
        }, 0);
        scheduler.processNext();

        Assertions.assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleIn(Assertions::fail, -1));
    }

}
