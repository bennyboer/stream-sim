package edu.hm.cs.bess.streamsim.sim;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.hm.cs.bess.streamsim.sim.config.CellDescriptor;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObject;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class AbstractSimulationTest {

    protected static final String EUCLIDEAN_PREFIX = "euclidean/";
    protected static final String DIJKSTRA_PREFIX = "dijkstra/";
    protected static final String FAST_MARCHING_PREFIX = "fastmarching/";

    protected SimConfig createSimConfigFromFile(@NotNull String fileName) {
        try {
            return new ObjectMapper().readValue(Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)), SimConfig.class);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return null;
    }

    protected State buildState(SimConfig config) {
        int rows = config.getRows();
        int columns = config.getColumns();

        State state = new State(rows, columns);

        for (CellDescriptor cellDescriptor : config.getCellDescriptors().values()) {
            SimObject simObject = CellDescriptor.createSimObject(cellDescriptor);
            state.setCellOccupant(simObject, cellDescriptor.getLocation());
        }
        return state;
    }
}
