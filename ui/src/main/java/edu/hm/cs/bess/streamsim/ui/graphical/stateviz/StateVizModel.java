package edu.hm.cs.bess.streamsim.ui.graphical.stateviz;

import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.CellDescriptor;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Model for the state visualization.
 *
 * @author Benjamin Eder
 */
public class StateVizModel {

    /**
     * Map of the currently set cells in the state visulization.
     */
    private final Map<Location, CellDescriptor> cellLookup = new HashMap<>();

    /**
     * Lookup of paints to use for cell type IDs.
     */
    private final Map<Integer, PaintDescriptor> paintLookup;

    /**
     * Available paints list.
     */
    private final List<PaintDescriptor> paints;

    /**
     * Color displayed when a cell is not set.
     */
    private final ObjectProperty<Color> emptyColorProperty = new SimpleObjectProperty<>();

    /**
     * Rows to display.
     */
    private final IntegerProperty rows = new SimpleIntegerProperty();

    /**
     * Columns to display.
     */
    private final IntegerProperty columns = new SimpleIntegerProperty();

    /**
     * All change listeners listening for cell descriptor changes.
     */
    private List<CellDescriptorChangeListener> cellDescriptorChangeListeners;

    /**
     * The current type ID to paint cells with.
     */
    private final IntegerProperty paint = new SimpleIntegerProperty(1);

    /**
     * Property holding whether editing is currently enabled.
     */
    private final BooleanProperty editingEnabled = new SimpleBooleanProperty(true);

    /**
     * Whether paint mode is enabled.
     */
    private final BooleanProperty paintModeEnabled = new SimpleBooleanProperty(true);

    /**
     * Currently selected cell descriptor for configuration.
     */
    private final ObjectProperty<CellDescriptor> selectedForConfigurationProperty = new SimpleObjectProperty<>(null);

    /**
     * Property holding a potential matrix to display.
     */
    private final ObjectProperty<double[][]> potentialMatrixProperty = new SimpleObjectProperty<>(null);

    /**
     * Whether the potential of the potential matrix should be shown.
     */
    private final BooleanProperty showPotential = new SimpleBooleanProperty(false);

    /**
     * The current maximum potential used to scale the colors correctly when showing the potential.
     */
    private double maxPotential;

    /**
     * Size of the window to calculate the chart from.
     */
    private final IntegerProperty chartWindowSize = new SimpleIntegerProperty(5);

    /**
     * Supplier of the current simulator (if any).
     */
    private final Supplier<StreamSimulator> simulatorSupplier;

    /**
     * Animation time.
     */
    private final DoubleProperty animationTime = new SimpleDoubleProperty(0);

    /**
     * Time units to wait before repainting the chart.
     */
    private final IntegerProperty chartRepaintDebounceTimeUnits = new SimpleIntegerProperty(10);

    /**
     * How many cells fit in a meter.
     */
    private final DoubleProperty cellsPerMeter = new SimpleDoubleProperty(2.5);

    public StateVizModel(List<PaintDescriptor> paints, Color emptyColor, Supplier<StreamSimulator> simulatorSupplier) {
        this.simulatorSupplier = simulatorSupplier;

        paintLookup = new HashMap<>();
        for (PaintDescriptor paintDescriptor : paints) {
            paintLookup.put(paintDescriptor.getTypeID(), paintDescriptor);
        }
        this.paints = paints;

        this.emptyColorProperty.set(emptyColor);

        rowsProperty().addListener((observable, oldValue, newValue) -> clearCellDescriptions());
        columnsProperty().addListener((observable, oldValue, newValue) -> clearCellDescriptions());

        potentialMatrixProperty.addListener((observable, oldValue, newValue) -> {
            maxPotential = Double.MIN_VALUE;
            for (double[] row : newValue) {
                for (double value : row) {
                    if (value < Double.MAX_VALUE && value > maxPotential) {
                        maxPotential = value;
                    }
                }
            }
        });

        editingEnabledProperty().addListener((observable, oldValue, newValue) -> {
            setSelectedForConfigurationProperty(null);
        });
    }

    public CellDescriptor getSelectedForConfigurationProperty() {
        return selectedForConfigurationProperty.get();
    }

    public ObjectProperty<CellDescriptor> selectedForConfigurationPropertyProperty() {
        return selectedForConfigurationProperty;
    }

    public void setSelectedForConfigurationProperty(CellDescriptor selectedForConfigurationProperty) {
        this.selectedForConfigurationProperty.set(selectedForConfigurationProperty);
    }

    public Color getEmptyColor() {
        return emptyColorProperty.get();
    }

    public ObjectProperty<Color> emptyColorPropertyProperty() {
        return emptyColorProperty;
    }

    public void setEmptyColor(Color emptyColorProperty) {
        this.emptyColorProperty.set(emptyColorProperty);
    }

    public boolean isPaintModeEnabled() {
        return paintModeEnabled.get();
    }

    public BooleanProperty paintModeEnabledProperty() {
        return paintModeEnabled;
    }

    public void setPaintModeEnabled(boolean paintModeEnabled) {
        this.paintModeEnabled.set(paintModeEnabled);
    }

    public boolean getEditingEnabled() {
        return editingEnabled.get();
    }

    public BooleanProperty editingEnabledProperty() {
        return editingEnabled;
    }

    public void setEditingEnabled(boolean editingEnabled) {
        this.editingEnabled.set(editingEnabled);
    }

    public List<PaintDescriptor> getPaints() {
        return paints;
    }

    public int getPaint() {
        return paint.get();
    }

    public IntegerProperty paintProperty() {
        return paint;
    }

    public void setPaint(int paint) {
        this.paint.set(paint);
    }

    public int getRows() {
        return rows.get();
    }

    public IntegerProperty rowsProperty() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows.set(rows);
    }

    public int getColumns() {
        return columns.get();
    }

    public IntegerProperty columnsProperty() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns.set(columns);
    }

    public int getChartWindowSize() {
        return chartWindowSize.get();
    }

    public IntegerProperty chartWindowSizeProperty() {
        return chartWindowSize;
    }

    public void setChartWindowSize(int chartWindowSize) {
        this.chartWindowSize.set(chartWindowSize);
    }

    public Supplier<StreamSimulator> getSimulatorSupplier() {
        return simulatorSupplier;
    }

    public double getAnimationTime() {
        return animationTime.get();
    }

    public DoubleProperty animationTimeProperty() {
        return animationTime;
    }

    public void setAnimationTime(double animationTime) {
        this.animationTime.set(animationTime);
    }

    public int getChartRepaintDebounceTimeUnits() {
        return chartRepaintDebounceTimeUnits.get();
    }

    public IntegerProperty chartRepaintDebounceTimeUnitsProperty() {
        return chartRepaintDebounceTimeUnits;
    }

    public void setChartRepaintDebounceTimeUnits(int chartRepaintDebounceTimeUnits) {
        this.chartRepaintDebounceTimeUnits.set(chartRepaintDebounceTimeUnits);
    }

    public double getCellsPerMeter() {
        return cellsPerMeter.get();
    }

    public DoubleProperty cellsPerMeterProperty() {
        return cellsPerMeter;
    }

    public void setCellsPerMeter(double cellsPerMeter) {
        this.cellsPerMeter.set(cellsPerMeter);
    }

    /**
     * Get the color for the passed location.
     *
     * @param location to get color for
     * @return color
     */
    public Color getColorForLocation(Location location) {
        return getCellDescription(location)
                .map((cellDescriptor) -> paintLookup.get(cellDescriptor.getTypeID()).getColor())
                .orElseGet(() -> {
                    if (isShowPotential() && getPotentialMatrixProperty() != null) {
                        double scaleFactor = Math.min(getPotentialMatrixProperty()[location.getRow()][location.getColumn()], maxPotential) / maxPotential;
                        return Color.YELLOW.interpolate(Color.MEDIUMVIOLETRED, scaleFactor);
                    }

                    return getEmptyColor();
                });
    }

    /**
     * Get a cell description for the passed location (if any).
     *
     * @param location to get cell description for
     * @return cell description
     */
    public Optional<CellDescriptor> getCellDescription(Location location) {
        return Optional.ofNullable(cellLookup.get(location));
    }

    /**
     * Set a cell description for the passed location.
     *
     * @param descriptor of the cell
     */
    public void setCellDescription(CellDescriptor descriptor) {
        setCellDescription(descriptor, true);
    }

    public double[][] getPotentialMatrixProperty() {
        return potentialMatrixProperty.get();
    }

    public ObjectProperty<double[][]> potentialMatrixPropertyProperty() {
        return potentialMatrixProperty;
    }

    public void setPotentialMatrixProperty(double[][] potentialMatrixProperty) {
        this.potentialMatrixProperty.set(potentialMatrixProperty);
    }

    public boolean isShowPotential() {
        return showPotential.get();
    }

    public BooleanProperty showPotentialProperty() {
        return showPotential;
    }

    public void setShowPotential(boolean showPotential) {
        this.showPotential.set(showPotential);
    }

    /**
     * Set a cell description for the passed location.
     *
     * @param descriptor of the cell
     * @param emitEvent  whether to emit a change event
     */
    public void setCellDescription(CellDescriptor descriptor, boolean emitEvent) {
        Optional<CellDescriptor> old = getCellDescription(descriptor.getLocation());

        cellLookup.put(descriptor.getLocation(), descriptor);

        if (emitEvent) {
            notifyCellDescriptorListeners(old.orElse(null), descriptor);
        }
    }

    /**
     * Remove a cell description at the given location.
     *
     * @param location of the cell description
     */
    public void removeCellDescription(Location location) {
        removeCellDescription(location, true);
    }

    /**
     * Remove a cell description at the given location.
     *
     * @param location  of the cell description
     * @param emitEvent whether to emit an event
     */
    public void removeCellDescription(Location location, boolean emitEvent) {
        Optional<CellDescriptor> old = getCellDescription(location);

        cellLookup.remove(location);

        if (emitEvent) {
            notifyCellDescriptorListeners(old.orElse(null), null);
        }
    }

    /**
     * Clear all cell descriptions.
     */
    public void clearCellDescriptions() {
        List<CellDescriptor> tmp = new ArrayList<>(cellLookup.values());
        for (CellDescriptor cellDescriptor : tmp) {
            removeCellDescription(cellDescriptor.getLocation());
        }
    }

    /**
     * Get a iterable of all cell descriptors.
     *
     * @return cell descriptors iterable
     */
    public Iterable<CellDescriptor> cellDescriptors() {
        return cellLookup.values();
    }

    /**
     * Add a listener for change descriptor changes.
     *
     * @param listener for changes
     */
    public void addCellDescriptorListener(CellDescriptorChangeListener listener) {
        if (cellDescriptorChangeListeners == null) {
            cellDescriptorChangeListeners = new ArrayList<>();
        }

        cellDescriptorChangeListeners.add(listener);
    }

    /**
     * Remove an active cell descriptor change listener.
     *
     * @param listener to remove
     */
    public void removeCellDescriptorListener(CellDescriptorChangeListener listener) {
        if (cellDescriptorChangeListeners != null) {
            cellDescriptorChangeListeners.remove(listener);
        }
    }

    /**
     * Notify all cell descriptor listeners.
     *
     * @param oldValue old cell descriptor
     * @param newValue new cell descriptor
     */
    protected void notifyCellDescriptorListeners(CellDescriptor oldValue, CellDescriptor newValue) {
        if (cellDescriptorChangeListeners == null) {
            return;
        }

        for (var listener : cellDescriptorChangeListeners) {
            listener.changed(oldValue, newValue);
        }
    }

    /**
     * Change listener for cell descriptor changes of this model.
     */
    @FunctionalInterface
    public interface CellDescriptorChangeListener {

        /**
         * Called when a cell descriptor has been changed.
         *
         * @param oldValue the old cell descriptor
         * @param newValue the new cell descriptor
         */
        void changed(@Nullable CellDescriptor oldValue, @Nullable CellDescriptor newValue);

    }

}
