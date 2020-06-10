package edu.hm.cs.bess.streamsim.sim.logic.move.util.dijkstra;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Benedikt Beil
 */
public class Node {

    final static double ADJACENT_MAX_DISTANCE = 1.50;
    final static double ADJACENT_MIN_DISTANCE = 0.95;

    private final Map<Node, Double> adjacentNodes = new HashMap<>(); // adjacent nodes with the distance between them
    private final Location representativeLocation;  // Location represented by this node in the grid
    private double totalDistanceToTarget = Double.MAX_VALUE;

    public Node(Location representativeLocation) {
        if (representativeLocation == null) throw new IllegalArgumentException(Location.class.getName());
        this.representativeLocation = representativeLocation;
    }

    public int getColumn() {
        return representativeLocation.getColumn();
    }

    public int getRow() {
        return representativeLocation.getRow();
    }

    public void setDistanceToTarget(double distanceToTarget) {
        //TODO: This method causes an overflow for a large field, because this method is called too often in itself.
        if (distanceToTarget > totalDistanceToTarget) return;

        totalDistanceToTarget = distanceToTarget;
        for (Map.Entry<Node, Double> entry : adjacentNodes.entrySet()) {
            final double newTotalDistanceToTarget = distanceToTarget + entry.getValue();
            if (entry.getKey().getTotalDistanceToTarget() > newTotalDistanceToTarget) {
                entry.getKey().setDistanceToTarget(newTotalDistanceToTarget);
            }
            /* => to move to the target, but not the fastest way. looks like drunken people
            if (entry.getKey().getTotalDistanceToTarget() == Double.MAX_VALUE) {
                entry.getKey().setDistanceToTarget(newTotalDistanceToTarget);
            }
            */
        }
    }

    public double getTotalDistanceToTarget() {
        return totalDistanceToTarget;
    }

    public Location getRepresentativeLocation() {
        return representativeLocation;
    }

    /**
     * Check the node, see if it's a neighbor.
     *
     * @param testNode
     * @return
     */
    public boolean isAdjacent(Node testNode) {
        if (testNode == null) return false;
        final double distanceToTestNode = distance(testNode.getRepresentativeLocation(), representativeLocation);
        return ADJACENT_MAX_DISTANCE > distanceToTestNode && distanceToTestNode > ADJACENT_MIN_DISTANCE;
    }

    /**
     * Adds the node if it is a neighbor.
     *
     * @param node to be added
     */
    public void addAdjacent(Node node) {
        if (node == null) return;
        if (isAdjacent(node)) {
            if (!adjacentNodes.containsKey(node)) {
                final double distanceToNode = distance(node.getRepresentativeLocation(), representativeLocation);
                adjacentNodes.put(node, distanceToNode);
                node.addAdjacent(this);
            }
        }
    }

    /**
     * Calculate the euclidean distance from the passed location to the passed location.
     *
     * @param from location
     * @param to   location
     * @return euclidean distance
     */
    private double distance(Location from, Location to) {
        return Math.hypot(from.getRow() - to.getRow(), from.getColumn() - to.getColumn());
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Node)) {
            return false;
        }

        Node node = (Node) o;

        return representativeLocation.equals(node.representativeLocation);
    }

    @Override
    public int hashCode() {
        return representativeLocation.hashCode();
    }
}
