package edu.hm.cs.bess.streamsim.ui.graphical.stateviz;

import edu.hm.cs.bess.streamsim.sim.config.CellDescriptor;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;

/**
 * Controller for the state visualization.
 *
 * @author Benjamin Eder
 */
public class StateVizController {

    /**
     * Model for the state visualization.
     */
    private final StateVizModel model;

    public StateVizController(StateVizModel model) {
        this.model = model;
    }

    /**
     * Paint a cell at the given location with the passed type.
     *
     * @param location to paint cell at
     * @param typeID   to paint cell with
     */
    public void paintCell(Location location, int typeID) {
        model.setCellDescription(new CellDescriptor(typeID, location, null));
    }

    /**
     * Remove the paint from the given location.
     *
     * @param location to remove paint from
     */
    public void removePaint(Location location) {
        model.removeCellDescription(location);
    }

    /**
     * Clear the paint from the whole grid.
     */
    public void clearPaint() {
        model.clearCellDescriptions();
    }

    /**
     * Select the paint mode.
     */
    public void selectPaintMode() {
        model.setPaintModeEnabled(true);
    }

    /**
     * Select the configure mode.
     */
    public void selectConfigureMode() {
        model.setPaintModeEnabled(false);
    }

    /**
     * Select the passed cell descriptor for configuration.
     *
     * @param cellDescriptor to select
     */
    public void selectForConfiguration(CellDescriptor cellDescriptor) {
        model.setSelectedForConfigurationProperty(cellDescriptor);
    }

}
