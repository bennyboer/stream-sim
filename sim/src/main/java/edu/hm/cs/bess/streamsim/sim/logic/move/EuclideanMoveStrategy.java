package edu.hm.cs.bess.streamsim.sim.logic.move;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.state.State;

/**
 * Strategy moving a person to its target using the euclidean distance.
 *
 * @author Benedikt Beil
 */
public class EuclideanMoveStrategy extends DefaultMovementStrategy {

    /**
     * The default radius to respect other people in the neighbourhood in.l
     */
    public static final int DEFAULT_RADIUS = 3;

    /**
     * Name of the strategy.
     */
    public static final String NAME = "Euclidean";


    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EuclideanMoveStrategy(
            @JsonProperty("mollifierConfiguration") MollifierConfiguration mollifierConfiguration,
            @JsonProperty("radius") int radius
    ) {
        super(mollifierConfiguration, radius);
    }

    @Override
    public double[][] calculateBasePotential(State state, Location target) {

        double[][] potentialMatrix = new double[state.getRows()][state.getColumns()];

        if (target == null) return potentialMatrix;

        for (int row = 0; row < state.getRows(); row++) {
            for (int columns = 0; columns < state.getColumns(); columns++) {
                potentialMatrix[row][columns] = distance(new Location(row, columns), target);
            }
        }

        return potentialMatrix;
    }

    @Override
    public String getName() {
        return EuclideanMoveStrategy.NAME;
    }
}
