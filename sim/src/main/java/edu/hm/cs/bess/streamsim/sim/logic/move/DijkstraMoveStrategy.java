package edu.hm.cs.bess.streamsim.sim.logic.move;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import edu.hm.cs.bess.streamsim.sim.logic.move.util.dijkstra.Graph;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.person.Person;
import edu.hm.cs.bess.streamsim.sim.model.state.State;

/**
 * Strategy moving a person to its target based on the dijkstra floor-flooding algorithm.
 *
 * @author Benedikt Beil
 */
public class DijkstraMoveStrategy extends DefaultMovementStrategy {

    /**
     * The default radius to respect other people in the neighbourhood in.l
     */
    public static final int DEFAULT_RADIUS = 3;

    /**
     * Name of the strategy.
     */
    public static final String NAME = "Dijkstra";


    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DijkstraMoveStrategy(
            @JsonProperty("mollifierConfiguration") MollifierConfiguration mollifierConfiguration,
            @JsonProperty("radius") int radius
    ) {
        super(mollifierConfiguration, radius);
    }


    @Override
    public String getName() {
        return DijkstraMoveStrategy.NAME;
    }

    @Override
    protected double normalizeDiagonalPotentialDescent(double potentialDescent, Person person) {
        if (potentialDescent > 1.4 && potentialDescent < 1.5) {
            final int rowDiff = Math.abs(person.getSource().getRow() - person.getTarget().getRow());
            final int columnDiff = Math.abs(person.getSource().getColumn() - person.getTarget().getColumn());

            final int numOfDiagonalSteps = Math.min(rowDiff, columnDiff);
            final int numOfStraightSteps = Math.max(rowDiff, columnDiff) - numOfDiagonalSteps;
            final int totalNumOfDiffs = numOfDiagonalSteps + numOfStraightSteps;

            final double percentOfDiagonal = (double) numOfDiagonalSteps / totalNumOfDiffs;
            final double percentOfStraight = (double) numOfStraightSteps / totalNumOfDiffs;
            final double randomPercent = rng.nextDouble();

            if (numOfDiagonalSteps > numOfStraightSteps) {
                if (randomPercent < percentOfDiagonal) {
                    return 1.01;
                } else {
                    return 0.99;
                }
            } else {
                if (randomPercent < percentOfStraight) {
                    return 0.99;
                } else {
                    return 1.01;
                }
            }
        }
        return -1.0;
    }

    @Override
    public double[][] calculateBasePotential(State state, Location target) {
        double[][] potentialMatrix = new double[state.getRows()][state.getColumns()];
        for (int row = 0; row < state.getRows(); row++) {
            for (int column = 0; column < state.getColumns(); column++) {
                potentialMatrix[row][column] = Double.MAX_VALUE;
            }
        }

        Graph graph = new Graph(state, target);
        graph.setDistanceToTargetOnMatrix(potentialMatrix);

        return potentialMatrix;
    }

}


