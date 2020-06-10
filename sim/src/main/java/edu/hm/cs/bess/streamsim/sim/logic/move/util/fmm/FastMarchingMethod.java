package edu.hm.cs.bess.streamsim.sim.logic.move.util.fmm;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for calculating the fast marching method.
 *
 * @author Benjamin Eder
 */
public class FastMarchingMethod {

    /**
     * Rows of the area to calculate.
     */
    private final int rows;

    /**
     * Columns of the area to calculate.
     */
    private final int columns;

    /**
     * Function used to initialize a location in the known matrix.
     */
    private final Function<Location, Double> initializeLocationFunction;

    /**
     * Function used to fetch the surface speed for the passed location needed by the algorithm.
     */
    private final Function<Location, Double> speedFunction;

    /**
     * The current known matrix (result of the calculation once algorithm finished).
     */
    private double[][] known;

    /**
     * Lookup for currently considered nodes.
     */
    Map<Location, ConsideredNode> consideredNodeLookup;

    /**
     * Queue of currently considered nodes.
     * The one with the lowest value will always be picked first.
     */
    PriorityQueue<ConsideredNode> consideredNodes;

    /**
     * Create fast marching method calculator.
     *
     * @param rows                       of the area to calculate
     * @param columns                    of the area to calculate
     * @param initializeLocationFunction function to intitialize the result matrix with
     * @param speedFunction              to fetch the surface speed for passed locations
     */
    public FastMarchingMethod(int rows, int columns, Function<Location, Double> initializeLocationFunction, Function<Location, Double> speedFunction) {
        this.rows = rows;
        this.columns = columns;
        this.initializeLocationFunction = initializeLocationFunction;
        this.speedFunction = speedFunction;
    }

    /**
     * Calculate the known matrix using the fast marching method.
     *
     * @param location to calculate algorithm from
     */
    public void calculate(Location location) {
        initialize();

        known[location.getRow()][location.getColumn()] = 0.0; // Set starting point

        Location next = location;
        while (next != null) {
            forEachNeighbour(next, (neighbourLocation) -> {
                // Check if not already fixed in known matrix
                double curValue = known[neighbourLocation.getRow()][neighbourLocation.getColumn()];
                if (curValue < Double.POSITIVE_INFINITY) {
                    return;
                }

                double newValue = calculateValue(neighbourLocation);
                if (newValue >= known[neighbourLocation.getRow()][neighbourLocation.getColumn()]) {
                    return;
                }

                // Set new update value for the considered node
                ConsideredNode consideredNode = consideredNodeLookup.get(neighbourLocation);
                if (consideredNode != null) {
                    consideredNode.setValue(newValue);
                } else {
                    consideredNode = new ConsideredNode(newValue, neighbourLocation);
                    consideredNodeLookup.put(neighbourLocation, consideredNode);
                    consideredNodes.add(consideredNode);
                }
            });

            ConsideredNode nextNode = consideredNodes.poll();
            if (nextNode != null) {
                known[nextNode.getLocation().getRow()][nextNode.getLocation().getColumn()] = nextNode.value;
                next = nextNode.getLocation();
            } else {
                next = null;
            }
        }
    }

    /**
     * Calculate the update value for the passed location.
     *
     * @param location to calculate update value for
     * @return update value
     */
    private double calculateValue(Location location) {
        double v0 = location.getRow() - 1 >= 0 ? known[location.getRow() - 1][location.getColumn()] : Double.POSITIVE_INFINITY;
        double v1 = location.getRow() + 1 < rows ? known[location.getRow() + 1][location.getColumn()] : Double.POSITIVE_INFINITY;
        double v = Math.min(v0, v1);

        double h0 = location.getColumn() - 1 >= 0 ? known[location.getRow()][location.getColumn() - 1] : Double.POSITIVE_INFINITY;
        double h1 = location.getColumn() + 1 < columns ? known[location.getRow()][location.getColumn() + 1] : Double.POSITIVE_INFINITY;
        double h = Math.min(h0, h1);

        double speed = speedFunction.apply(location);
        double inverseSpeed = 1 / speed;

        if (Math.abs(v - h) <= inverseSpeed) {
            return (h + v) / 2 + 0.5 * Math.sqrt(Math.pow(h + v, 2) - 2 * (Math.pow(h, 2) + Math.pow(v, 2) - Math.pow(inverseSpeed, 2)));
        } else {
            return Math.min(h, v) + inverseSpeed;
        }
    }

    /**
     * Get the resulting known matrix.
     *
     * @return result
     */
    public double[][] getResult() {
        return known;
    }

    /**
     * Initialize the known matrix.
     */
    private void initialize() {
        consideredNodeLookup = new HashMap<>();
        consideredNodes = new PriorityQueue<>();

        known = new double[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                known[row][column] = Objects.requireNonNullElse(
                        initializeLocationFunction.apply(new Location(row, column)),
                        Double.POSITIVE_INFINITY
                );
            }
        }
    }

    /**
     * Execute the passed consumer for each neighbour of the passed location.
     *
     * @param location          to find neighbours of
     * @param neighbourConsumer to execute for each neighbour of the passed location
     */
    private void forEachNeighbour(Location location, Consumer<Location> neighbourConsumer) {
        for (int row = Math.max(0, location.getRow() - 1); row <= Math.min(location.getRow() + 1, rows - 1); row++) {
            for (int column = Math.max(0, location.getColumn() - 1); column <= Math.min(location.getColumn() + 1, columns - 1); column++) {
                if (row != location.getRow() || column != location.getColumn()) {
                    neighbourConsumer.accept(new Location(row, column));
                }
            }
        }
    }

    /**
     * Transform the passed result (known matrix) to string.
     *
     * @param result to transform
     * @return result matrix in string form
     */
    public static String resultToString(double[][] result) {
        int columns = result[0].length;

        StringBuilder output = new StringBuilder();
        for (double[] rowVals : result) {
            for (int column = 0; column < columns; column++) {
                double v = rowVals[column];
                if (v == Double.MAX_VALUE) {
                    output.append("MAX_VALUE");
                } else {
                    output.append(String.format("%09.3f", rowVals[column]));
                }
                output.append(' ');
            }

            output.append('\n');
        }

        return output.toString();
    }

    /**
     * A considered node of the fast marching algorithm.
     */
    private static class ConsideredNode implements Comparable<ConsideredNode> {

        /**
         * The current value.
         */
        private double value;

        /**
         * Location of the considered node.
         */
        private final Location location;

        public ConsideredNode(double value, Location location) {
            this.value = value;
            this.location = location;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public Location getLocation() {
            return location;
        }

        @Override
        public int compareTo(@NotNull ConsideredNode o) {
            return Double.compare(getValue(), o.getValue());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConsideredNode that = (ConsideredNode) o;

            return location.equals(that.location);
        }

        @Override
        public int hashCode() {
            return location.hashCode();
        }

    }

}
