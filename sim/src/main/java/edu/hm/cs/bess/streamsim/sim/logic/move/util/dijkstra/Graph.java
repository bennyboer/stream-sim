package edu.hm.cs.bess.streamsim.sim.logic.move.util.dijkstra;

import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.state.State;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Benedikt Beil
 */
public class Graph {
    private final Node[][] nodes;
    private final State state;
    private final Node targetNode;

    public Graph(State state, Location target) {
        this.state = state;
        nodes = new Node[state.getRows()][state.getColumns()];
        targetNode = new Node(target);
        nodes[target.getRow()][target.getColumn()] = targetNode;

        initGraph();
    }

    private void initGraph() {
        addAllValidLocationsAsNode();
        connectAdjacentNodes();
        setDistanceToTargetForAllNodes();
    }

    private void connectAdjacentNodes() {
        for (int row = 0; row < state.getRows(); row++) {
            for (int column = 0; column < state.getColumns(); column++) {
                final Node currentNode = nodes[row][column];
                if (currentNode != null) {
                    Set<Node> adjacentNodes = getAllAdjacentNodes(currentNode);
                    for (Node adjacentNode : adjacentNodes) {
                        currentNode.addAdjacent(adjacentNode);
                    }
                }
            }
        }
    }

    private void addAllValidLocationsAsNode() {
        for (int row = 0; row < state.getRows(); row++) {
            for (int column = 0; column < state.getColumns(); column++) {
                final Location currentLocation = new Location(row, column);

                if (state.canBeOccupied(currentLocation)) {
                    nodes[row][column] = new Node(currentLocation);
                } else if (state.getCellOccupant(currentLocation).get().getType() != SimObjectType.TARGET) {
                    nodes[row][column] = null;
                }
            }
        }
    }

    private Set<Node> getAllAdjacentNodes(Node node) {
        final Location location = node.getRepresentativeLocation();
        Set<Node> adjacentNodes = new HashSet<>();
        for (int row = Math.max(0, location.getRow() - 1);
             row <= Math.min(location.getRow() + 1, state.getRows() - 1); row++) {
            for (int column = Math.max(0, location.getColumn() - 1);
                 column <= Math.min(location.getColumn() + 1, state.getColumns() - 1); column++) {
                if (row != location.getRow() || column != location.getColumn()) {
                    final Location currentLocation = new Location(row, column);
                    if (state.canBeOccupied(currentLocation)
                            || state.getCellOccupant(currentLocation).get().getType() == SimObjectType.TARGET) {
                        adjacentNodes.add(nodes[currentLocation.getRow()][currentLocation.getColumn()]);
                    }
                }
            }
        }
        return adjacentNodes;
    }

    private void setDistanceToTargetForAllNodes() {
        targetNode.setDistanceToTarget(0); // The target node sets its neighbors and so on
    }

    public void setDistanceToTargetOnMatrix(double[][] potentialMatrix) {
        for (int row = 0; row < state.getRows(); row++) {
            for (int column = 0; column < state.getColumns(); column++) {
                final Node currentNode = nodes[row][column];
                if (currentNode != null) {
                    potentialMatrix[currentNode.getRow()][currentNode.getColumn()] = currentNode.getTotalDistanceToTarget();
                }
            }
        }
    }
}
