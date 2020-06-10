package edu.hm.cs.bess.streamsim.ui.graphical;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.CellDescriptor;
import edu.hm.cs.bess.streamsim.sim.config.SimConfig;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import edu.hm.cs.bess.streamsim.sim.model.object.source.Source;
import edu.hm.cs.bess.streamsim.sim.model.state.State;
import edu.hm.cs.bess.streamsim.ui.graphical.dialog.create.CreateDialog;
import edu.hm.cs.bess.streamsim.ui.graphical.stateviz.PaintDescriptor;
import edu.hm.cs.bess.streamsim.ui.graphical.stateviz.StateVizController;
import edu.hm.cs.bess.streamsim.ui.graphical.stateviz.StateVizModel;
import edu.hm.cs.bess.streamsim.ui.graphical.stateviz.StateVizView;
import edu.hm.cs.bess.streamsim.ui.graphical.timeline.TimeLineController;
import edu.hm.cs.bess.streamsim.ui.graphical.timeline.TimeLineModel;
import edu.hm.cs.bess.streamsim.ui.graphical.timeline.TimeLineView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point of the JavaFX application.
 *
 * @author Benjamin Eder
 * @author Konstantin Schlosser
 */
public class App extends Application {

    /**
     * Logger of the class.
     */
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    /**
     * Title of the application.
     */
    private static final String TITLE = "StreamSim";

    /**
     * The default style.
     */
    private static final Style DEFAULT_STYLE = Style.DARK;

    /**
     * Paint descriptors for the state visualization.
     */
    private static final PaintDescriptor[] PAINT_DESCRIPTORS = {
            App.paintDescriptorFromType(SimObjectType.OBSTACLE, true, Color.SLATEGRAY),
            App.paintDescriptorFromType(SimObjectType.PERSON, true, Color.CORNFLOWERBLUE),
            App.paintDescriptorFromType(SimObjectType.SOURCE, true, Color.LIGHTSEAGREEN),
            App.paintDescriptorFromType(SimObjectType.TARGET, true, Color.LIGHTCORAL),
            App.paintDescriptorFromType(SimObjectType.LIGHT_BARRIER, true, Color.LIGHTYELLOW),
    };

    /**
     * The root layout of the UI.
     */
    private BorderPane rootLayout;

    /**
     * The JMetro JavaFX theme instance.
     */
    private JMetro jMetro;

    /**
     * The primary stage.
     */
    private Stage stage;

    /**
     * Model of the timeline.
     */
    private TimeLineModel timeLineModel;

    /**
     * Model of the state visualization.
     */
    private StateVizModel stateVizModel;

    /**
     * View of the state visualization.
     */
    private StateVizView stateVizView;

    /**
     * Visualization page.
     */
    private Parent visualizationPage;

    /**
     * Placeholder (starting screen) page.
     */
    private Parent placeholderPage;

    /**
     * Combobox holding the currently selected theme.
     */
    private ComboBox<Style> styleComboBox;

    /**
     * Property holding the actual simulator.
     */
    private final ObjectProperty<StreamSimulator> simulatorProperty = new SimpleObjectProperty<>(null);

    /**
     * Event listener for simulator life cycle events.
     */
    private StreamSimulator.SimulationLifeCycleEventListener simulationLifeCycleEventListener;

    /**
     * Update listener of the simulation state in case it is running.
     */
    private State.StateUpdateListener simulationStateUpdateListener;

    /**
     * Statistics change listener.
     */
    private StreamSimulator.StatisticsChangeListener statisticsChangeListener;

    /**
     * Currently saved simulation configuration.
     */
    private SimConfig restoreConfiguration;

    /**
     * Lock used for repainting from the simulation thread.
     */
    private final Lock repaintLock = new ReentrantLock();

    /**
     * Condition used to repaint synchronously from the simulation thread.
     */
    private final Condition repaintNeeded = repaintLock.newCondition();

    /**
     * Output directory for the log files. Defaults to current dir.
     */
    private File logLocation = new File(System.getProperty("user.dir"));

    /**
     * Indicates whether or not the menu item "should log" is checked.
     */
    private BooleanProperty shouldLog = new SimpleBooleanProperty();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle(TITLE);
        primaryStage.getIcons().add(new Image("icon.png"));

        rootLayout = new BorderPane();
        rootLayout.getStyleClass().add(JMetroStyleClass.BACKGROUND); // Tell theme to style this according to the theme

        Scene scene = new Scene(rootLayout, 1000, 800);
        scene.getStylesheets().add("style.css");

        // Initialize theme
        jMetro = new JMetro(DEFAULT_STYLE);
        jMetro.setScene(scene);

        // Register icon font
        Font.loadFont("SegMDL2.ttf", 10);

        initUI();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (simulatorProperty.get() != null) {
            simulatorProperty.get().terminate();
        }

        Platform.exit();
        System.exit(0);
    }

    /**
     * Initialize the UI.
     */
    private void initUI() {
        styleComboBox = new ComboBox<>();
        styleComboBox.getItems().addAll(Style.DARK, Style.LIGHT);
        styleComboBox.setValue(DEFAULT_STYLE);
        styleComboBox.valueProperty().addListener(observable -> jMetro.setStyle(styleComboBox.getValue()));

        placeholderPage = buildPlaceholderPage();
        visualizationPage = buildVisualizationPage();

        buildMenu();

        showPage(placeholderPage);
    }

    /**
     * Show the passed parent as current page.
     *
     * @param parent to show
     */
    private void showPage(Parent parent) {
        rootLayout.setCenter(parent);
    }

    /**
     * Build the visualization page.
     *
     * @return visualization page
     */
    private VBox buildVisualizationPage() {
        VBox layout = new VBox();

        Parent timeLineView = buildTimeLine();
        Parent stateVizView = buildStateViz();
        VBox.setVgrow(stateVizView, Priority.ALWAYS);

        layout.getChildren().addAll(stateVizView, timeLineView);

        return layout;
    }

    /**
     * Build the placeholder page (Starting screen).
     *
     * @return placeholder
     */
    private Parent buildPlaceholderPage() {
        VBox verticalLayout = new VBox();
        verticalLayout.setAlignment(Pos.CENTER);

        HBox horizontalLayout = new HBox();
        horizontalLayout.setAlignment(Pos.CENTER);
        verticalLayout.getChildren().add(horizontalLayout);

        VBox innerLayout = new VBox(10);
        innerLayout.setAlignment(Pos.CENTER);

        Image snail = new Image("snail.png");
        ImageView imageView = new ImageView(snail);
        imageView.setPreserveRatio(true);

        Label welcomeLabel = new Label(String.format("Welcome to %s!", TITLE));
        welcomeLabel.setFont(new Font(32));
        VBox.setMargin(welcomeLabel, new Insets(20));

        Button createButton = new Button("Create new scenario");
        createButton.setOnAction(event -> openCreateDialog());

        Button loadButton = new Button("Load scenario");
        loadButton.setOnAction(event -> openLoadDialog());
        VBox.setMargin(loadButton, new Insets(0, 0, 20, 0));

        innerLayout.getChildren().addAll(imageView, welcomeLabel, createButton, loadButton);

        horizontalLayout.getChildren().add(innerLayout);

        return verticalLayout;
    }

    /**
     * Initialize the state visualization.
     *
     * @return parent
     */
    private Parent buildStateViz() {
        stateVizModel = new StateVizModel(
                Arrays.asList(App.PAINT_DESCRIPTORS),
                styleComboBox.getValue() == Style.LIGHT ? Color.gray(0.9) : Color.gray(0.25),
                simulatorProperty::get
        );
        stateVizModel.editingEnabledProperty().bind(
                Bindings.and(
                        timeLineModel.playingProperty().not(),
                        Bindings.createBooleanBinding(() -> simulatorProperty.get() == null || !simulatorProperty.get().isStarted(), simulatorProperty)
                )
        ); // Only enable painting if not currently playing
        stateVizModel.animationTimeProperty().bind(timeLineModel.animationTimeProperty());
        stateVizModel.cellsPerMeterProperty().addListener((observable, oldValue, newValue) -> {
            if (simulatorProperty.get() != null) {
                simulatorProperty.get().setStatisticsCellsPerMeter(newValue.doubleValue());
            }
        });
        stateVizModel.chartWindowSizeProperty().addListener((observable, oldValue, newValue) -> {
            if (simulatorProperty.get() != null) {
                simulatorProperty.get().setStatisticsMeanSpeedWindowSize(newValue.intValue());
            }
        });
        styleComboBox.valueProperty().addListener((observable, oldValue, newValue)
                -> stateVizModel.setEmptyColor(newValue == Style.LIGHT ? Color.gray(0.9) : Color.gray(0.25)));

        stateVizModel.setRows(20);
        stateVizModel.setColumns(20); // TODO Set from generated field
        StateVizController controller = new StateVizController(stateVizModel);
        stateVizView = new StateVizView(controller, stateVizModel);

        return stateVizView.asParent();
    }

    /**
     * Build the state from the current state visualization model.
     *
     * @return state
     */
    private State buildState() {
        int rows = stateVizModel.getRows();
        int columns = stateVizModel.getColumns();

        State state = new State(rows, columns);

        for (CellDescriptor cellDescriptor : stateVizModel.cellDescriptors()) {
            state.setCellOccupant(CellDescriptor.createSimObject(cellDescriptor), cellDescriptor.getLocation());
        }

        return state;
    }

    /**
     * Start or continue the simulation.
     */
    private void startSimulation() {
        if (simulatorProperty.get() == null) {
            saveConfig();
            // TODO
            simulatorProperty.set(new StreamSimulator(buildState(), timeLineModel.getSeed(), shouldLog.getValue(), logLocation, "output"));
            attachSimulatorListeners();

            simulatorProperty.get().setTimeUnitInMillis(timeLineModel.getSpeed());
        }

        stateVizModel.setShowPotential(false);

        simulatorProperty.get().play();
    }

    /**
     * Pause the simulation.
     */
    private void pauseSimulation() {
        if (simulatorProperty.get() != null) {
            simulatorProperty.get().pause();

            displayPotentialInStateViz();
        }
    }

    /**
     * Try to display a potential matrix in the state visualization.
     */
    private void displayPotentialInStateViz() {
        if (simulatorProperty.get() != null) {
            List<Source> sources = simulatorProperty.get().getSources();
            if (sources != null && !sources.isEmpty()) {
                Source source = sources.get(0);

                try {
                    double[][] potentialMatrix = source.getConfiguration().getMoveStrategy().calculatePotential(simulatorProperty.get().getCurrentState());
                    stateVizModel.setPotentialMatrixProperty(potentialMatrix);
                    stateVizModel.setShowPotential(true);
                } catch (UnsupportedOperationException e) {
                    // That is expected to happen and an allowed response of the calculatePotential method.
                }
            }
        }
    }

    /**
     * Reset the simulation.
     */
    private void resetSimulation() {
        if (simulatorProperty.get() != null) {
            simulatorProperty.get().reset();
            detachSimulatorListeners();
            simulatorProperty.set(null);

            timeLineModel.setAnimationTime(0);

            stateVizModel.setShowPotential(false);

            restoreConfig();
        }
    }

    /**
     * Save the current configuration.
     */
    private void saveConfig() {
        resetSimulation();

        Map<Location, CellDescriptor> cellDescriptors = new HashMap<>();

        for (CellDescriptor cellDescriptor : stateVizModel.cellDescriptors()) {
            cellDescriptors.put(cellDescriptor.getLocation(), cellDescriptor);
        }

        restoreConfiguration = new SimConfig(stateVizModel.getRows(), stateVizModel.getColumns(), timeLineModel.getSeed(), cellDescriptors);
    }

    /**
     * Restore the current configuration.
     */
    private void restoreConfig() {
        stateVizModel.clearCellDescriptions();

        if (restoreConfiguration != null) {
            stateVizModel.setRows(restoreConfiguration.getRows());
            stateVizModel.setColumns(restoreConfiguration.getColumns());
            timeLineModel.setSeed(restoreConfiguration.getSeed());

            for (CellDescriptor cellDescriptor : restoreConfiguration.getCellDescriptors().values()) {
                stateVizModel.setCellDescription(cellDescriptor);
            }
        }
    }

    /**
     * Initialize the timeline.
     *
     * @return parent
     */
    private Parent buildTimeLine() {
        timeLineModel = new TimeLineModel();
        TimeLineController controller = new TimeLineController(timeLineModel);
        TimeLineView view = new TimeLineView(controller, timeLineModel);

        timeLineModel.playingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startSimulation();
            } else {
                pauseSimulation();
            }
        });

        timeLineModel.speedProperty().addListener((observable, oldValue, newValue) -> {
            if (simulatorProperty.get() != null) {
                simulatorProperty.get().setTimeUnitInMillis(newValue.intValue());
            }
        });

        timeLineModel.resetProperty().addListener((observable, oldValue, newValue) -> resetSimulation());

        return view.asParent();
    }

    /**
     * Initialize the top menu.
     */
    private void buildMenu() {
        ToolBar toolBar = new ToolBar();

        MenuBar menuBar = new MenuBar();
        menuBar.disableProperty().bind(timeLineModel.playingProperty());

        Menu fileMenu = new Menu("File", new MDL2IconFont("\uED41"));
        MenuItem newItem = new MenuItem("New", new MDL2IconFont("\uED0E"));
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newItem.setOnAction(event -> openCreateDialog());

        MenuItem openItem = new MenuItem("Open", new MDL2IconFont("\uE8E5"));
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openItem.setOnAction(event -> openLoadDialog());

        MenuItem saveItem = new MenuItem("Save", new MDL2IconFont("\uE74E"));
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(event -> openSaveDialog());

        MenuItem closeItem = new MenuItem("Close", new MDL2IconFont("\uE8BB"));
        closeItem.setOnAction(event -> showPage(placeholderPage));

        fileMenu.getItems().addAll(newItem, openItem, saveItem, closeItem);

        menuBar.getMenus().add(fileMenu);

        Menu logMenu = new Menu("Logging", new MDL2IconFont("\uF0E3"));
        MenuItem logLocation = new MenuItem("Choose location", new MDL2IconFont("\uE81D"));
        logLocation.setOnAction(event -> openLogLocationDialog());
        CheckMenuItem logEnabled = new CheckMenuItem("Logging enabled");
        logEnabled.setSelected(false);
        shouldLog.bind(logEnabled.selectedProperty());

        logMenu.getItems().addAll(logEnabled, logLocation);

        menuBar.getMenus().add(logMenu);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolBar.getItems().addAll(menuBar, spacer, styleComboBox);

        rootLayout.setTop(toolBar);
    }

    /**
     * Open the create dialog.
     */
    private void openCreateDialog() {
        CreateDialog dialog = new CreateDialog(stage, styleComboBox.getValue());
        dialog.showAndWait().ifPresent((result) -> {
            detachSimulatorListeners();
            simulatorProperty.set(null);

            timeLineModel.setAnimationTime(0);

            stateVizModel.clearCellDescriptions();
            stateVizModel.setPaintModeEnabled(true);

            stateVizModel.setRows(result.getHeight());
            stateVizModel.setColumns(result.getWidth());

            showPage(visualizationPage);
        });
    }

    /**
     * Attach simulator listeners.
     */
    private void attachSimulatorListeners() {
        StreamSimulator simulator = simulatorProperty.get();
        if (simulator == null) {
            return;
        }

        simulationStateUpdateListener = events -> {
            Runnable repaintFunc = () -> {
                for (State.UpdateEvent event : events) {
                    switch (event.getType()) {
                        case ADDED, CHANGED -> stateVizModel.setCellDescription(new CellDescriptor(event.getNewOccupant().getType().getID(), event.getLocation(), null));
                        case REMOVED -> stateVizModel.removeCellDescription(event.getLocation());
                    }
                }
            };

            if (Platform.isFxApplicationThread()) {
                repaintFunc.run();
            } else {
                repaintLock.lock();
                try {
                    Platform.runLater(() -> {
                        repaintFunc.run();

                        repaintLock.lock();
                        try {
                            repaintNeeded.signal();
                        } finally {
                            repaintLock.unlock();
                        }
                    });

                    repaintNeeded.await();
                } catch (InterruptedException e) {
                    // Will only happen when pausing.
                } finally {
                    repaintLock.unlock();
                }
            }
        };
        simulator.getCurrentState().addUpdateListener(simulationStateUpdateListener);

        simulationLifeCycleEventListener = new StreamSimulator.SimulationLifeCycleEventListener() {
            @Override
            public void onStart() {
                LOGGER.log(Level.INFO, "Simulation has been started");
            }

            @Override
            public void onEnd() {
                Platform.runLater(() -> timeLineModel.setPlaying(false));
                LOGGER.log(Level.INFO, "Simulation has ended");
                simulator.saveLogs();
            }

            @Override
            public void onPause() {
                LOGGER.log(Level.INFO, "Simulation has been paused");
            }

            @Override
            public void onContinue() {
                LOGGER.log(Level.INFO, "Simulation has been continued");
            }

            @Override
            public void onReset() {
                LOGGER.log(Level.INFO, "Simulation has been reset");
                simulator.saveLogs();
            }

            @Override
            public void onTimeChange(double time) {
                Platform.runLater(() -> timeLineModel.setAnimationTime(time));
                LOGGER.log(Level.FINE, String.format("Simulation time changed to %f", time));
            }

        };
        simulator.addLifeCycleEventListener(simulationLifeCycleEventListener);

        statisticsChangeListener = (peopleCount, density, meanSpeed, flow)
                -> Platform.runLater(() -> stateVizView.updateStatistics(peopleCount, density, meanSpeed, flow));
        simulator.addStatisticsChangeListener(statisticsChangeListener);
    }

    /**
     * Detach simulator listeners.
     */
    private void detachSimulatorListeners() {
        StreamSimulator simulator = simulatorProperty.get();
        if (simulator == null) {
            return;
        }

        if (simulationStateUpdateListener != null) {
            simulator.getCurrentState().removeUpdateListener(simulationStateUpdateListener);
        }

        if (simulationLifeCycleEventListener != null) {
            simulator.removeLifeCycleEventListener(simulationLifeCycleEventListener);
        }

        if (statisticsChangeListener != null) {
            simulator.removeStatisticsChangeListener(statisticsChangeListener);
        }
    }

    /**
     * Open the load/open dialog.
     */
    private void openLoadDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load scenario");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectReader reader = mapper.reader();

            try {
                restoreConfiguration = reader.readValue(file, SimConfig.class);

                showPage(visualizationPage);

                restoreConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Open the save dialog.
     */
    private void openSaveDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save scenario");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            saveConfig();

            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            try {
                writer.writeValue(file, restoreConfiguration);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Open the save dialog.
     */
    private void openLogLocationDialog() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Log File location");
        fileChooser.setInitialDirectory(logLocation);
        logLocation = fileChooser.showDialog(stage);
    }

    /**
     * Create a paint descriptor for the passed simulation object type.
     *
     * @param type     to create paint descriptor from
     * @param canPaint whether the user will be able to paint with the paint
     * @return paint descriptor
     */
    private static PaintDescriptor paintDescriptorFromType(SimObjectType type, boolean canPaint, Color color) {
        return new PaintDescriptor(type.getID(), type.getName(), color, canPaint);
    }

}
