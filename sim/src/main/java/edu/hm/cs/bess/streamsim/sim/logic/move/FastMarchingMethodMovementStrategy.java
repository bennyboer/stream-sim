package edu.hm.cs.bess.streamsim.sim.logic.move;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import edu.hm.cs.bess.streamsim.sim.logic.move.util.fmm.FastMarchingMethod;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.state.State;

import java.util.Optional;

/**
 * Strategy moving a person to its target based on the fast marching method algorithm.
 *
 * @author Benjamin Eder
 */
public class FastMarchingMethodMovementStrategy extends DefaultMovementStrategy {

    /**
     * The default radius to respect other people in the neighbourhood in.l
     */
    public static final int DEFAULT_RADIUS = 3;

    /**
     * Name of the strategy.
     */
    public static final String NAME = "Fast marching method";


    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FastMarchingMethodMovementStrategy(
            @JsonProperty("mollifierConfiguration") MollifierConfiguration mollifierConfiguration,
            @JsonProperty("radius") int radius
    ) {
        super(mollifierConfiguration, radius);
    }

    @Override
    public String getName() {
        return FastMarchingMethodMovementStrategy.NAME;
    }

    /**
     * Calculate the utility function using the fast marching method.
     *
     * @param state  to calculate on
     * @param target to calculate from
     * @return the utility function in a discrete matrix form
     */
    @Override
    public double[][] calculateBasePotential(State state, Location target) {
        FastMarchingMethod fmm = new FastMarchingMethod(
                state.getRows(),
                state.getColumns(),
                (location) -> {
                    Optional<SimObject> occupant = state.getCellOccupant(location);
                    if (occupant.isPresent() && occupant.get().getType() == SimObjectType.OBSTACLE) {
                        return Double.MAX_VALUE; // Avoid obstacles!
                    }

                    return null;
                },
                (location) -> 1.0
        );

        fmm.calculate(target);

        return fmm.getResult();
    }

}
