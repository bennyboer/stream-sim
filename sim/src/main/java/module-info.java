module edu.hm.cs.bess.streamsim.sim {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires org.jetbrains.annotations;
    requires java.logging;
    requires commons.math3;

    exports edu.hm.cs.bess.streamsim.sim;
    exports edu.hm.cs.bess.streamsim.sim.config;
    exports edu.hm.cs.bess.streamsim.sim.model.misc;
    exports edu.hm.cs.bess.streamsim.sim.model.object;
    exports edu.hm.cs.bess.streamsim.sim.model.object.source;
    exports edu.hm.cs.bess.streamsim.sim.model.object.target;
    exports edu.hm.cs.bess.streamsim.sim.model.object.obstacle;
    exports edu.hm.cs.bess.streamsim.sim.model.state;
    exports edu.hm.cs.bess.streamsim.sim.model.state.cell;
    exports edu.hm.cs.bess.streamsim.sim.logic.consume;
    exports edu.hm.cs.bess.streamsim.sim.logic.move;
    exports edu.hm.cs.bess.streamsim.sim.logic.spawn;
    exports edu.hm.cs.bess.streamsim.sim.logic.spawn.speed;
    exports edu.hm.cs.bess.streamsim.sim.logic.spawn.patience;

    opens edu.hm.cs.bess.streamsim.sim.config;
    opens edu.hm.cs.bess.streamsim.sim.model.misc;
    opens edu.hm.cs.bess.streamsim.sim.logic.consume;
    opens edu.hm.cs.bess.streamsim.sim.logic.move;
    opens edu.hm.cs.bess.streamsim.sim.logic.spawn;
    opens edu.hm.cs.bess.streamsim.sim.logic.spawn.speed;
    opens edu.hm.cs.bess.streamsim.sim.logic.spawn.patience;
    exports edu.hm.cs.bess.streamsim.sim.model.object.person;
}
