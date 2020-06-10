module edu.hm.cs.bess.streamsim.ui.cli {
    requires com.fasterxml.jackson.databind;
    requires edu.hm.cs.bess.streamsim.sim;
    requires info.picocli;
    requires java.logging;

    exports edu.hm.cs.bess.streamsim.ui.cli;

    opens edu.hm.cs.bess.streamsim.ui.cli;
}
