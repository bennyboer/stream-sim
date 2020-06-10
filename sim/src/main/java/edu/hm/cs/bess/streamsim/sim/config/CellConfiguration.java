package edu.hm.cs.bess.streamsim.sim.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Configuration for a cell.
 *
 * @author Benjamin Eder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SourceConfiguration.class, name = "Source"),
        @JsonSubTypes.Type(value = TargetConfiguration.class, name = "Target")}
)
public abstract class CellConfiguration {

}
