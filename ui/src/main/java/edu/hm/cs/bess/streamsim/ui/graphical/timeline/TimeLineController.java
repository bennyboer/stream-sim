package edu.hm.cs.bess.streamsim.ui.graphical.timeline;

/**
 * Controller for the time line.
 *
 * @author Benjamin Eder
 */
public class TimeLineController {

    /**
     * Model of the timeline.
     */
    private final TimeLineModel model;

    public TimeLineController(TimeLineModel model) {
        this.model = model;
    }

    /**
     * Toggle between playing and pause.
     */
    public void togglePlayPause() {
        model.setPlaying(!model.isPlaying());
    }

    /**
     * Call reset.
     */
    public void reset() {
        model.reset();
    }

}
