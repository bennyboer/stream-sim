package edu.hm.cs.bess.streamsim.sim.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hm.cs.bess.streamsim.sim.logic.move.MoveStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.SpawnStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.patience.PatienceGenerator;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.speed.SpeedGenerator;

/**
 * Configuration for a source cell.
 *
 * @author Benjamin Eder
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SourceConfiguration extends CellConfiguration {

    /**
     * The strategy to use for spawning new people.
     */
    private final SpawnStrategy spawnStrategy;

    /**
     * Move strategy to initialize people with.
     */
    private final MoveStrategy moveStrategy;

    /**
     * Maximum amount of spawns this source is able to do.
     * If unrestricted this number is negative or equal to 0.
     */
    private final int maxSpawns;

    /**
     * Generator used to generate speeds for spawned people.
     */
    private final SpeedGenerator speedGenerator;

    /**
     * Generator used to generate people patience.
     */
    private final PatienceGenerator patienceGenerator;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SourceConfiguration(
            @JsonProperty("spawnStrategy") SpawnStrategy spawnStrategy,
            @JsonProperty("moveStrategy") MoveStrategy moveStrategy,
            @JsonProperty("maxSpawns") int maxSpawns,
            @JsonProperty("speedGenerator") SpeedGenerator speedGenerator,
            @JsonProperty("patienceGenerator") PatienceGenerator patienceGenerator
    ) {
        this.spawnStrategy = spawnStrategy;
        this.moveStrategy = moveStrategy;
        this.maxSpawns = maxSpawns;
        this.speedGenerator = speedGenerator;
        this.patienceGenerator = patienceGenerator;
    }

    public SpawnStrategy getSpawnStrategy() {
        return spawnStrategy;
    }

    public MoveStrategy getMoveStrategy() {
        return moveStrategy;
    }

    /**
     * Get the maximum amount of spawns that are allowed to happen from that source.
     *
     * @return maximum amount of spawns
     */
    public int getMaxSpawns() {
        return maxSpawns;
    }

    /**
     * Whether spawns are limited.
     *
     * @return whether spawns are limited.
     */
    public boolean areSpawnsLimited() {
        return maxSpawns > 0;
    }

    /**
     * Whether spawns are unlimited.
     *
     * @return whether spawns are unlimited.
     */
    public boolean areSpawnsUnlimited() {
        return maxSpawns <= 0;
    }

    /**
     * Get the generator to generate speeds for new people with.
     *
     * @return speed generator
     */
    public SpeedGenerator getSpeedGenerator() {
        return speedGenerator;
    }

    /**
     * Get the patience generator.
     *
     * @return patience generator
     */
    public PatienceGenerator getPatienceGenerator() {
        return patienceGenerator;
    }

}


