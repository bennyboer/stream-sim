package edu.hm.cs.bess.streamsim.sim.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.logic.consume.ConsumeStrategy;

/**
 * Configuration for a target cell.
 *
 * @author Benjamin Eder
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TargetConfiguration extends CellConfiguration {

    /**
     * The consume strategy to apply to people that reach their target.
     */
    private final ConsumeStrategy consumeStrategy;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public TargetConfiguration(@JsonProperty("consumeStrategy") ConsumeStrategy consumeStrategy) {
        this.consumeStrategy = consumeStrategy;
    }

    public ConsumeStrategy getConsumeStrategy() {
        return consumeStrategy;
    }

}
