package edu.hm.cs.bess.streamsim.ui.graphical.stateviz;

import edu.hm.cs.bess.streamsim.sim.StreamSimulator;
import edu.hm.cs.bess.streamsim.sim.config.CellDescriptor;
import edu.hm.cs.bess.streamsim.sim.config.MollifierConfiguration;
import edu.hm.cs.bess.streamsim.sim.config.SourceConfiguration;
import edu.hm.cs.bess.streamsim.sim.config.TargetConfiguration;
import edu.hm.cs.bess.streamsim.sim.logic.consume.ConsumeStrategies;
import edu.hm.cs.bess.streamsim.sim.logic.consume.ConsumeStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.consume.RemoveConsumeStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.consume.ReviveConsumeStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.move.*;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.FixedRateSpawnStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.PoissonSpawnStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.SpawnStrategies;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.SpawnStrategy;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.patience.NormPatienceGenerator;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.patience.PatienceGenerator;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.patience.PatienceGenerators;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.speed.FixedSpeedGenerator;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.speed.NormSpeedGenerator;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.speed.SpeedGenerator;
import edu.hm.cs.bess.streamsim.sim.logic.spawn.speed.SpeedGenerators;
import edu.hm.cs.bess.streamsim.sim.model.misc.Location;
import edu.hm.cs.bess.streamsim.sim.model.object.SimObjectType;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import jfxtras.styles.jmetro.MDL2IconFont;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * View for the state visualization.
 *
 * @author Benjamin Eder
 * @author Konstantin Schlosser
 */
public class StateVizView {

    /**
     * Border of the cells.
     */
    private static final double BORDER = 1.0;

    /**
     * Controller for the state visualization.
     */
    private final StateVizController controller;

    /**
     * Model for the state visualization.
     */
    private final StateVizModel model;

    /**
     * Border pane being the main layout of this view.
     */
    private BorderPane borderPane;

    /**
     * Canvas displaying the state visualization.
     */
    private Canvas canvas;

    /**
     * Layout containing the canvas.
     */
    private StackPane canvasLayout;

    /**
     * Layout for the paint settings.
     */
    private BorderPane paintSettingsLayout;

    /**
     * Layout for the configuration settings.
     */
    private BorderPane configurationSettingsLayout;

    /**
     * Layout for the visualization settings.
     */
    private BorderPane vizSettingsLayout;

    /**
     * The currently shown settings layout.
     */
    private BorderPane currentlyShownSettingsLayout;

    /**
     * The previously shown editing settings layout.
     */
    private BorderPane oldEditingSettingsLayout;

    /**
     * The currently hovered cell location.
     */
    @Nullable
    private Location currentlyHoveredLocation;

    /**
     * Old simulator reference used by the chart.
     */
    private StreamSimulator oldSimulatorRef;

    /**
     * Chart holding the data to be displayed.
     */
    private ScatterChart<Number, Number> densityChart;

    /**
     * Chart holding the flow data to be displayed.
     */
    private ScatterChart<Number, Number> flowChart;

    /**
     * Density axis of the density chart.
     */
    private NumberAxis densityChartDensityAxis;

    /**
     * Mean speed axis of the density chart.
     */
    private NumberAxis densityChartMeanSpeedAxis;

    /**
     * Density axis of the flow chart.
     */
    private NumberAxis flowChartDensityAxis;

    /**
     * Flow axis of the flow chart.
     */
    private NumberAxis flowChartFlowAxis;

    /**
     * Series of the density chart.
     */
    private XYChart.Series<Number, Number> densityChartSeries;

    /**
     * Series of the flow chart.
     */
    private XYChart.Series<Number, Number> flowChartSeries;

    private Label peopleCountLabel;

    public StateVizView(StateVizController controller, StateVizModel model) {
        this.controller = controller;
        this.model = model;

        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        borderPane = new BorderPane();

        initEditModeSettings();

        initCanvas();
        model.rowsProperty().addListener((observable, oldValue, newValue) -> resizeCanvas());
        model.columnsProperty().addListener((observable, oldValue, newValue) -> resizeCanvas());

        initPaintSettings();
        initConfigureSettings();
        initVizSettings();

        showSettingsPane(paintSettingsLayout);

        model.editingEnabledProperty().addListener((observable, oldValue, newValue) -> {
            updateCursor();

            if (!newValue) {
                oldEditingSettingsLayout = currentlyShownSettingsLayout;
                showSettingsPane(vizSettingsLayout);
            } else {
                showSettingsPane(oldEditingSettingsLayout);
            }
        });
        model.paintModeEnabledProperty().addListener((observable, oldValue, newValue) -> {
            updateCursor();
            showSettingsPane(newValue ? paintSettingsLayout : configurationSettingsLayout);
        });
        updateCursor();

        model.selectedForConfigurationPropertyProperty().addListener((observable, oldValue, newValue) -> updateConfigurationSettings(newValue));
    }

    /**
     * Show the passed settings pane.
     *
     * @param toShow pane to show
     */
    private void showSettingsPane(BorderPane toShow) {
        BorderPane toHide = currentlyShownSettingsLayout;
        currentlyShownSettingsLayout = toShow;
        BorderPane.setMargin(currentlyShownSettingsLayout, new Insets(10));
        currentlyShownSettingsLayout.setPadding(new Insets(10));

        if (toHide != null) {
            fadeRegion(toHide, true, event -> {
                fadeRegion(toShow, false, null);
                borderPane.setRight(toShow);
            });
        } else {
            fadeRegion(toShow, false, null);
            borderPane.setRight(toShow);
        }
    }

    /**
     * Fade the passed pane out.
     *
     * @param pane  to fade out
     * @param out   whether to fade in or out
     * @param onEnd runnable to execute on fade end
     */
    private void fadeRegion(Region pane, boolean out, EventHandler<ActionEvent> onEnd) {
        FadeTransition fade = new FadeTransition(new Duration(200), pane);
        fade.setInterpolator(Interpolator.EASE_BOTH);

        if (onEnd != null) {
            fade.setOnFinished(onEnd);
        }

        fade.currentTimeProperty().addListener((timeObservable, oldTime, newTime) -> {
            double progress = newTime.toMillis() / fade.getDuration().toMillis();
            if (out) {
                progress = 1.0 - progress;
            }

            pane.setMaxWidth(progress * pane.getPrefWidth());
        });

        if (!out) {
            pane.setMaxWidth(0);

            fade.setFromValue(0.0);
            fade.setToValue(1.0);
        } else {
            pane.setPrefWidth(pane.getWidth());

            fade.setFromValue(1.0);
            fade.setToValue(0.0);
        }

        fade.play();
    }

    /**
     * Force a canvas resize.
     */
    private void resizeCanvas() {
        double aspectRatio = (double) model.getColumns() / model.getRows();

        double width = canvasLayout.getWidth();
        double height = width / aspectRatio;

        if (height > canvasLayout.getHeight()) {
            height = canvasLayout.getHeight();
            width = height * aspectRatio;
        }

        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    /**
     * Update the cursor based on the model.
     */
    private void updateCursor() {
        if (model.getEditingEnabled()) {
            if (model.isPaintModeEnabled()) {
                canvas.setCursor(Cursor.CROSSHAIR);
            } else {
                canvas.setCursor(Cursor.HAND);
            }
        } else {
            canvas.setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Initialize the canvas.
     */
    private void initCanvas() {
        canvasLayout = new StackPane();

        canvas = new ResizableCanvas(this::repaintCanvas);

        canvasLayout.widthProperty().addListener((observable, oldValue, newValue) -> resizeCanvas());
        canvasLayout.heightProperty().addListener((observable, oldValue, newValue) -> resizeCanvas());

        canvasLayout.getChildren().add(canvas);

        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (model.isPaintModeEnabled()) {
                markCell(event, getLocationForMouseEvent(event));
            } else {
                // Select cell for configuration
                configureCell(event, getLocationForMouseEvent(event));
            }
        });
        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (model.isPaintModeEnabled()) {
                markCell(event, getLocationForMouseEvent(event));
            }
        });
        canvas.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (model.getEditingEnabled()) {
                hoverCell(event, getLocationForMouseEvent(event));
            }
        });
        canvas.addEventFilter(MouseEvent.MOUSE_EXITED, event -> hoverCell(event, null));

        model.addCellDescriptorListener(((oldValue, newValue) -> {
            if (newValue != null) {
                paintCellAt(newValue.getLocation().getRow(), newValue.getLocation().getColumn(), model.getColorForLocation(newValue.getLocation()));
            } else if (oldValue != null) {
                paintCellAt(oldValue.getLocation().getRow(), oldValue.getLocation().getColumn(), model.getColorForLocation(oldValue.getLocation()));
            }
        }));

        model.emptyColorPropertyProperty().addListener((observable, oldValue, newValue) -> {
            repaint();

            paintSettingsLayout.setBackground(new Background(new BackgroundFill(newValue, new CornerRadii(5), null)));
            configurationSettingsLayout.setBackground(new Background(new BackgroundFill(newValue, new CornerRadii(5), null)));
            vizSettingsLayout.setBackground(new Background(new BackgroundFill(newValue, new CornerRadii(5), null)));
        });

        model.showPotentialProperty().addListener((observable, oldValue, newValue) -> repaint());

        borderPane.setCenter(canvasLayout);
    }

    /**
     * Request a repaint of the canvas.
     */
    public void repaint() {
        repaintCanvas(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Initialize the edit mode settings.
     */
    private void initEditModeSettings() {
        HBox modeSelection = new HBox();
        modeSelection.setPadding(new Insets(10));
        modeSelection.setAlignment(Pos.CENTER);
        modeSelection.setSpacing(10);
        modeSelection.setMinHeight(0);

        Button paintModeBtn = new Button("Paint", new MDL2IconFont("\uE790"));
        paintModeBtn.defaultButtonProperty().bind(model.paintModeEnabledProperty());
        paintModeBtn.setOnAction(event -> controller.selectPaintMode());

        Button configureModeBtn = new Button("Configure", new MDL2IconFont("\uE70F"));
        configureModeBtn.defaultButtonProperty().bind(model.paintModeEnabledProperty().not());
        configureModeBtn.setOnAction(event -> controller.selectConfigureMode());

        modeSelection.getChildren().addAll(new Label("Edit mode:"), paintModeBtn, configureModeBtn);

        borderPane.setTop(modeSelection);

        model.editingEnabledProperty().addListener((observable, oldValue, newValue) -> {
            FadeTransition fade = new FadeTransition(new Duration(200), modeSelection);
            fade.setInterpolator(Interpolator.EASE_BOTH);

            fade.currentTimeProperty().addListener((timeObservable, oldTime, newTime) -> {
                double progress = newTime.toMillis() / fade.getDuration().toMillis();
                if (!newValue) {
                    progress = 1.0 - progress;
                }

                modeSelection.setMaxHeight(progress * modeSelection.getPrefHeight());
            });

            if (newValue) {
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
            } else {
                modeSelection.setPrefHeight(modeSelection.getHeight());

                fade.setFromValue(1.0);
                fade.setToValue(0.0);
            }

            fade.play();
        });
    }

    /**
     * Initialize the paint settings.
     */
    private void initPaintSettings() {
        paintSettingsLayout = new BorderPane();
        paintSettingsLayout.setMinWidth(0);
        paintSettingsLayout.setPrefWidth(250);
        paintSettingsLayout.setBackground(new Background(new BackgroundFill(model.getEmptyColor(), new CornerRadii(5), null)));

        Label headerLabel = new Label("Paints");
        BorderPane.setAlignment(headerLabel, Pos.CENTER);
        BorderPane.setMargin(headerLabel, new Insets(0, 0, 10, 0));
        headerLabel.setFont(Font.font(20));
        headerLabel.setGraphic(new MDL2IconFont("\uE790"));

        paintSettingsLayout.setTop(headerLabel);

        ListView<PaintDescriptor> paintList = new ListView<>();
        paintList.setItems(FXCollections.observableList(model.getPaints()
                .stream()
                .filter(PaintDescriptor::canPaint)
                .collect(Collectors.toList())));
        paintList.setCellFactory((ListView<PaintDescriptor> l) -> new PaintDescriptorListCell());

        paintSettingsLayout.setCenter(paintList);

        paintList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        paintList.getSelectionModel().select(0);
        model.paintProperty().bind(Bindings.createIntegerBinding(
                () -> paintList.getSelectionModel().selectedItemProperty().get().getTypeID(),
                paintList.getSelectionModel().selectedItemProperty()));

        VBox otherControlsLayout = new VBox(10);
        BorderPane.setMargin(otherControlsLayout, new Insets(10, 0, 0, 0));

        Button clearBtn = new Button("Clear", new MDL2IconFont("\uE74D"));
        HBox.setHgrow(clearBtn, Priority.ALWAYS);
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(event -> controller.clearPaint());

        otherControlsLayout.getChildren().addAll(clearBtn);

        paintSettingsLayout.setBottom(otherControlsLayout);
    }

    /**
     * Initialize the configure settings.
     */
    private void initConfigureSettings() {
        configurationSettingsLayout = new BorderPane();
        configurationSettingsLayout.setMinWidth(0);
        configurationSettingsLayout.setPrefWidth(250);
        configurationSettingsLayout.setBackground(new Background(new BackgroundFill(model.getEmptyColor(), new CornerRadii(5), null)));

        Label headerLabel = new Label("Configuration", new MDL2IconFont("\uE70F"));
        BorderPane.setAlignment(headerLabel, Pos.CENTER);
        BorderPane.setMargin(headerLabel, new Insets(0, 0, 10, 0));
        headerLabel.setFont(Font.font(20));

        configurationSettingsLayout.setTop(headerLabel);
    }

    /**
     * Initialize the visualization settings.
     */
    private void initVizSettings() {
        vizSettingsLayout = new BorderPane();
        vizSettingsLayout.setMinWidth(0);
        vizSettingsLayout.setPrefWidth(250);
        vizSettingsLayout.setBackground(new Background(new BackgroundFill(model.getEmptyColor(), new CornerRadii(5), null)));

        Label headerLabel = new Label("Statistics", new MDL2IconFont("\uE9D9"));
        BorderPane.setAlignment(headerLabel, Pos.CENTER);
        BorderPane.setMargin(headerLabel, new Insets(0, 0, 20, 0));
        headerLabel.setFont(Font.font(20));

        vizSettingsLayout.setTop(headerLabel);

        VBox layout = new VBox();

        {
            HBox peopleCountLayout = new HBox(10);
            peopleCountLayout.setAlignment(Pos.CENTER);

            Label peopleCountDescLabel = new Label("People count:", new MDL2IconFont("\uE716"));
            peopleCountDescLabel.setFont(Font.font(16));

            peopleCountLabel = new Label("0");
            peopleCountLabel.setFont(Font.font(16));

            peopleCountLayout.getChildren().addAll(peopleCountDescLabel, peopleCountLabel);

            layout.getChildren().add(peopleCountLayout);
        }

        Separator separator = new Separator();
        VBox.setMargin(separator, new Insets(10, 0, 10, 0));

        layout.getChildren().add(separator);

        {
            // DENSITY CHART
            final double minDensity = 0.0;
            final double maxDensity = 1.0;
            densityChartDensityAxis = new NumberAxis(minDensity, maxDensity, 1);
            densityChartDensityAxis.setLabel("Density (in 1/m^2)");

            final double minSpeed = 0.0;
            final double maxSpeed = 1.0;
            densityChartMeanSpeedAxis = new NumberAxis(minSpeed, maxSpeed, 1);
            densityChartMeanSpeedAxis.setLabel("Mean pedestrian speed (in m/s)");

            densityChart = new ScatterChart<>(densityChartDensityAxis, densityChartMeanSpeedAxis);
            densityChart.setTitle("Mean people speeds as a function of the density");
            densityChart.setLegendVisible(false);
            densityChart.setPrefHeight(200);

            densityChartSeries = new XYChart.Series<>();
            densityChart.getData().add(densityChartSeries);

            HBox densityChartBox = new HBox(10);
            densityChartBox.setAlignment(Pos.CENTER);
            densityChartBox.getChildren().addAll(densityChart);

            // FLOW CHART
            flowChartDensityAxis = new NumberAxis(minDensity, maxDensity, 1);
            flowChartDensityAxis.setLabel("Density (in 1/m^2)");

            final double minFlow = 0.0;
            final double maxFlow = 1.0;
            flowChartFlowAxis = new NumberAxis(minFlow, maxFlow, 1);
            flowChartFlowAxis.setLabel("Flow (in People/m/s)");

            flowChart = new ScatterChart<>(flowChartDensityAxis, flowChartFlowAxis);
            flowChart.setTitle("Flow of the simulation (using Lightbarriers)");
            flowChart.setLegendVisible(false);
            flowChart.setPrefHeight(200);

            flowChartSeries = new XYChart.Series<>();
            flowChart.getData().add(flowChartSeries);

            HBox flowChartBox = new HBox(10);
            flowChartBox.setAlignment(Pos.CENTER);
            flowChartBox.getChildren().addAll(flowChart);

            // CHART SETTINGS
            HBox windowSpinnerLayout = new HBox(10);
            windowSpinnerLayout.setAlignment(Pos.CENTER);

            Spinner<Integer> windowSpinner = new Spinner<>(1, Integer.MAX_VALUE, 5, 1);
            windowSpinner.getEditor().setTextFormatter(StateVizView.getIntegerNumberFormatter(5));
            windowSpinner.setEditable(true);
            HBox.setHgrow(windowSpinner, Priority.ALWAYS);
            windowSpinner.setMaxWidth(Double.MAX_VALUE);
            windowSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                model.setChartWindowSize(newValue);

                clearStatistics();
            });

            windowSpinnerLayout.getChildren().addAll(new Label("Window:"), windowSpinner);

            HBox densityAreaLayout = new HBox(10);
            densityAreaLayout.setAlignment(Pos.CENTER);

            Spinner<Double> cellsPerMeterSpinner = new Spinner<>(0.5, Double.MAX_VALUE, model.getCellsPerMeter(), 0.5);
            cellsPerMeterSpinner.setEditable(true);
            HBox.setHgrow(cellsPerMeterSpinner, Priority.ALWAYS);
            cellsPerMeterSpinner.setMaxWidth(Double.MAX_VALUE);
            cellsPerMeterSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                model.setCellsPerMeter(newValue);

                clearStatistics();
            });

            densityAreaLayout.getChildren().addAll(new Label("Cells per m:"), cellsPerMeterSpinner);

            HBox saveBox = new HBox();
            saveBox.setAlignment(Pos.CENTER);
            Button save = new Button("Export charts...");
            save.setOnAction(event -> {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
                File file = chooser.showSaveDialog(null);

                String parentPath = file.getParent();
                String fileName = FilenameUtils.getBaseName(file.getName());

                saveChart(densityChart, new File(String.format("%s/%s_density.png", parentPath, fileName)));
                saveChart(flowChart, new File(String.format("%s/%s_flow.png", parentPath, fileName)));
            });
            saveBox.getChildren().add(save);

            layout.getChildren().addAll(densityChartBox, flowChartBox, saveBox, windowSpinnerLayout, densityAreaLayout);
        }

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        layout.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * 6;
            double width = scrollPane.getContent().getBoundsInLocal().getWidth();
            double vvalue = scrollPane.getVvalue();
            scrollPane.setVvalue(vvalue + -deltaY / width);
        });

        vizSettingsLayout.setCenter(scrollPane);
    }

    /**
     * Clear the statistics.
     */
    public void clearStatistics() {
        peopleCountLabel.setText(String.format("%d", 0));
        densityChartSeries.getData().clear();
        flowChartSeries.getData().clear();
    }

    /**
     * Update the statistics.
     *
     * @param peopleCount currently in the simulation
     * @param density     currently in the simulation
     * @param meanSpeed   currently in the simulation
     * @param flow        currently in the simulation
     */
    public void updateStatistics(int peopleCount, double density, double meanSpeed, double flow) {
        peopleCountLabel.setText(String.format("%d", peopleCount));
        updateCharts(meanSpeed, density, flow);
    }

    /**
     * Update the charts.
     *
     * @param density   currently in the simulation
     * @param meanSpeed currently in the simulation
     * @param flow      currently in the simulation
     */
    private void updateCharts(double meanSpeed, double density, double flow) {
        if (density > densityChartDensityAxis.getUpperBound()) {
            densityChartDensityAxis.setUpperBound(density + (densityChartDensityAxis.getUpperBound() - densityChartDensityAxis.getLowerBound()) * 0.05);
        }
        if (meanSpeed > densityChartMeanSpeedAxis.getUpperBound()) {
            densityChartMeanSpeedAxis.setUpperBound(meanSpeed + (densityChartMeanSpeedAxis.getUpperBound() - densityChartMeanSpeedAxis.getLowerBound()) * 0.05);
        }

        densityChartSeries.getData().add(new XYChart.Data<>(density, meanSpeed));

        if (density > flowChartDensityAxis.getUpperBound()) {
            flowChartDensityAxis.setUpperBound(density + (flowChartDensityAxis.getUpperBound() - flowChartDensityAxis.getLowerBound()) * 0.05);
        }
        if (flow > flowChartFlowAxis.getUpperBound()) {
            flowChartFlowAxis.setUpperBound(meanSpeed + (flowChartFlowAxis.getUpperBound() - flowChartFlowAxis.getLowerBound()) * 0.05);
        }

        flowChartSeries.getData().add(new XYChart.Data<>(density, flow));
    }

    /**
     * Update the configuration settings for the passed cell descriptor.
     *
     * @param cellDescriptor to show settings for
     */
    private void updateConfigurationSettings(CellDescriptor cellDescriptor) {
        if (cellDescriptor == null) {
            configurationSettingsLayout.setCenter(new Label("Select a cell to configure it"));
        } else {

            VBox controlsLayout = new VBox();

            Label locationInfoLabel = new Label(String.format("Selected row: %s, column: %s", cellDescriptor.getLocation().getRow(), cellDescriptor.getLocation().getColumn()));
            Label typeInfoLabel = new Label(String.format("Cell type: '%s'", SimObjectType.getForTypeID(cellDescriptor.getTypeID()).getName()));
            VBox.setMargin(typeInfoLabel, new Insets(5, 0, 15, 0));

            controlsLayout.getChildren().addAll(locationInfoLabel, typeInfoLabel);

            switch (SimObjectType.getForTypeID(cellDescriptor.getTypeID())) {
                case SOURCE -> {
                    SourceConfiguration oldConfig = cellDescriptor.getConfiguration() != null
                            ? (SourceConfiguration) cellDescriptor.getConfiguration()
                            : new SourceConfiguration(
                            SpawnStrategies.DEFAULT.get(),
                            MoveStrategies.DEFAULT.get(),
                            -1,
                            SpeedGenerators.DEFAULT.get(),
                            PatienceGenerators.DEFAULT.get()
                    );

                    ObjectProperty<SpawnStrategy> currentSpawnStrategy = new SimpleObjectProperty<>(oldConfig.getSpawnStrategy());
                    ObjectProperty<MoveStrategy> currentMoveStrategy = new SimpleObjectProperty<>(oldConfig.getMoveStrategy());
                    IntegerProperty currentMaxSpawns = new SimpleIntegerProperty(oldConfig.getMaxSpawns());
                    ObjectProperty<SpeedGenerator> currentSpeedGenerator = new SimpleObjectProperty<>(oldConfig.getSpeedGenerator());
                    ObjectProperty<PatienceGenerator> currentPatienceGenerator = new SimpleObjectProperty<>(oldConfig.getPatienceGenerator());

                    Runnable updateSourceConfig = () -> {
                        SourceConfiguration newConfig = new SourceConfiguration(currentSpawnStrategy.get(), currentMoveStrategy.get(), currentMaxSpawns.get(), currentSpeedGenerator.get(), currentPatienceGenerator.get());
                        model.setCellDescription(new CellDescriptor(cellDescriptor.getTypeID(), cellDescriptor.getLocation(), newConfig), false);
                    };

                    currentSpawnStrategy.addListener((observable, oldValue, newValue) -> updateSourceConfig.run());
                    currentMoveStrategy.addListener((observable, oldValue, newValue) -> updateSourceConfig.run());
                    currentMaxSpawns.addListener((observable, oldValue, newValue) -> updateSourceConfig.run());
                    currentSpeedGenerator.addListener((observable, oldValue, newValue) -> updateSourceConfig.run());
                    currentPatienceGenerator.addListener((observable, oldValue, newValue) -> updateSourceConfig.run());

                    // SPAWN STRATEGY CONTROLS

                    BorderPane spawnStrategySettings = new BorderPane();
                    VBox.setMargin(spawnStrategySettings, new Insets(5));

                    ComboBox<String> spawnStrategyComboBox = new ComboBox<>();
                    VBox.setMargin(spawnStrategyComboBox, new Insets(5, 0, 5, 0));
                    HBox.setHgrow(spawnStrategyComboBox, Priority.ALWAYS);
                    spawnStrategyComboBox.setMaxWidth(Double.MAX_VALUE);
                    spawnStrategyComboBox.getItems().addAll(SpawnStrategies.LOOKUP.keySet().stream()
                            .sorted(Collator.getInstance(Locale.getDefault()))
                            .collect(Collectors.toList()));
                    spawnStrategyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.equals(FixedRateSpawnStrategy.NAME)) {
                            FixedRateSpawnStrategy oldStrategy = oldConfig.getSpawnStrategy() instanceof FixedRateSpawnStrategy
                                    ? (FixedRateSpawnStrategy) oldConfig.getSpawnStrategy()
                                    : new FixedRateSpawnStrategy(FixedRateSpawnStrategy.DEFAULT_FIXED_RATE);

                            currentSpawnStrategy.set(new FixedRateSpawnStrategy(oldStrategy.getFixedRate()));

                            VBox vBox = new VBox();

                            Spinner<Double> fixedRateSpinner = new Spinner<>(0.01, 100000, oldStrategy.getFixedRate(), 1);
                            fixedRateSpinner.setEditable(true);
                            HBox.setHgrow(fixedRateSpinner, Priority.ALWAYS);
                            fixedRateSpinner.setMaxWidth(Double.MAX_VALUE);
                            fixedRateSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                    -> currentSpawnStrategy.set(new FixedRateSpawnStrategy(newValue1)));

                            vBox.getChildren().add(fixedRateSpinner);

                            spawnStrategySettings.setCenter(vBox);
                        } else if (newValue.equals(PoissonSpawnStrategy.NAME)) {
                            PoissonSpawnStrategy oldStrategy = oldConfig.getSpawnStrategy() instanceof PoissonSpawnStrategy
                                    ? (PoissonSpawnStrategy) oldConfig.getSpawnStrategy()
                                    : new PoissonSpawnStrategy(PoissonSpawnStrategy.DEFAULT_LAMBDA);

                            currentSpawnStrategy.set(new PoissonSpawnStrategy(oldStrategy.getLambda()));

                            Spinner<Integer> meanEventRateSpinner = new Spinner<>(1, 1000, Math.max((int) oldStrategy.getLambda(), 1), 1);
                            meanEventRateSpinner.getEditor().setTextFormatter(StateVizView.getIntegerNumberFormatter(Math.max((int) oldStrategy.getLambda(), 1)));
                            meanEventRateSpinner.setEditable(true);
                            HBox.setHgrow(meanEventRateSpinner, Priority.ALWAYS);
                            meanEventRateSpinner.setMaxWidth(Double.MAX_VALUE);
                            meanEventRateSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                    -> currentSpawnStrategy.set(new PoissonSpawnStrategy(newValue1.doubleValue())));

                            Label meanEventRateLabel = new Label("Mean event rate (time units):");

                            VBox vBox = new VBox();
                            vBox.getChildren().addAll(meanEventRateLabel, meanEventRateSpinner);

                            spawnStrategySettings.setCenter(vBox);
                        }
                    });
                    spawnStrategyComboBox.getSelectionModel().select(oldConfig.getSpawnStrategy().getName());

                    // MOVE STRATEGY CONTROLS

                    BorderPane moveStrategiesSettings = new BorderPane();
                    VBox.setMargin(moveStrategiesSettings, new Insets(5));

                    ComboBox<String> moveStrategiesComboBox = new ComboBox<>();
                    VBox.setMargin(moveStrategiesComboBox, new Insets(5, 0, 5, 0));
                    HBox.setHgrow(moveStrategiesComboBox, Priority.ALWAYS);
                    moveStrategiesComboBox.setMaxWidth(Double.MAX_VALUE);
                    moveStrategiesComboBox.getItems().addAll(MoveStrategies.LOOKUP.keySet().stream()
                            .sorted(Collator.getInstance(Locale.getDefault()))
                            .collect(Collectors.toList()));

                    moveStrategiesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        switch (newValue) {
                            case EuclideanMoveStrategy.NAME -> {
                                EuclideanMoveStrategy oldStrategy = oldConfig.getMoveStrategy() instanceof EuclideanMoveStrategy
                                        ? (EuclideanMoveStrategy) oldConfig.getMoveStrategy()
                                        : (EuclideanMoveStrategy) MoveStrategies.LOOKUP.get(EuclideanMoveStrategy.NAME).get();

                                currentMoveStrategy.set(oldStrategy);

                                VBox mollifierSettingsLayout = new VBox();

                                Spinner<Integer> radiusSpinner = new Spinner<>(0, Integer.MAX_VALUE, oldStrategy.getRadius(), 1);
                                HBox.setHgrow(radiusSpinner, Priority.ALWAYS);
                                radiusSpinner.setMaxWidth(Double.MAX_VALUE);
                                Spinner<Integer> mollifierRangeSpinner = new Spinner<>(1, Integer.MAX_VALUE, oldStrategy.getMollifierConfiguration().getRange(), 1);
                                HBox.setHgrow(mollifierRangeSpinner, Priority.ALWAYS);
                                mollifierRangeSpinner.setMaxWidth(Double.MAX_VALUE);
                                Spinner<Double> mollifierStrengthSpinner = new Spinner<>(0.0, 999999, oldStrategy.getMollifierConfiguration().getStrength(), 0.1);
                                HBox.setHgrow(mollifierStrengthSpinner, Priority.ALWAYS);
                                mollifierStrengthSpinner.setMaxWidth(Double.MAX_VALUE);

                                radiusSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new EuclideanMoveStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));
                                mollifierRangeSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new EuclideanMoveStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));
                                mollifierStrengthSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new EuclideanMoveStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));

                                mollifierSettingsLayout.getChildren().addAll(
                                        new Label("Respect-other-people radius:"),
                                        radiusSpinner,
                                        new Label("Mollifier range:"),
                                        mollifierRangeSpinner,
                                        new Label("Mollifier strength:"),
                                        mollifierStrengthSpinner
                                );

                                moveStrategiesSettings.setCenter(mollifierSettingsLayout);
                            }
                            case DijkstraMoveStrategy.NAME -> {
                                DijkstraMoveStrategy oldStrategy = oldConfig.getMoveStrategy() instanceof DijkstraMoveStrategy
                                        ? (DijkstraMoveStrategy) oldConfig.getMoveStrategy()
                                        : (DijkstraMoveStrategy) MoveStrategies.LOOKUP.get(DijkstraMoveStrategy.NAME).get();

                                currentMoveStrategy.set(oldStrategy);

                                VBox mollifierSettingsLayout = new VBox();

                                Spinner<Integer> radiusSpinner = new Spinner<>(0, Integer.MAX_VALUE, oldStrategy.getRadius(), 1);
                                HBox.setHgrow(radiusSpinner, Priority.ALWAYS);
                                radiusSpinner.setMaxWidth(Double.MAX_VALUE);
                                Spinner<Integer> mollifierRangeSpinner = new Spinner<>(1, Integer.MAX_VALUE, oldStrategy.getMollifierConfiguration().getRange(), 1);
                                HBox.setHgrow(mollifierRangeSpinner, Priority.ALWAYS);
                                mollifierRangeSpinner.setMaxWidth(Double.MAX_VALUE);
                                Spinner<Double> mollifierStrengthSpinner = new Spinner<>(0.0, 999999, oldStrategy.getMollifierConfiguration().getStrength(), 0.1);
                                HBox.setHgrow(mollifierStrengthSpinner, Priority.ALWAYS);
                                mollifierStrengthSpinner.setMaxWidth(Double.MAX_VALUE);

                                radiusSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new DijkstraMoveStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));
                                mollifierRangeSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new DijkstraMoveStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));
                                mollifierStrengthSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new DijkstraMoveStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));

                                mollifierSettingsLayout.getChildren().addAll(
                                        new Label("Respect-other-people radius:"),
                                        radiusSpinner,
                                        new Label("Mollifier range:"),
                                        mollifierRangeSpinner,
                                        new Label("Mollifier strength:"),
                                        mollifierStrengthSpinner
                                );

                                moveStrategiesSettings.setCenter(mollifierSettingsLayout);
                            }
                            case FastMarchingMethodMovementStrategy.NAME -> {
                                FastMarchingMethodMovementStrategy oldStrategy = oldConfig.getMoveStrategy() instanceof FastMarchingMethodMovementStrategy
                                        ? (FastMarchingMethodMovementStrategy) oldConfig.getMoveStrategy()
                                        : (FastMarchingMethodMovementStrategy) MoveStrategies.LOOKUP.get(FastMarchingMethodMovementStrategy.NAME).get();

                                currentMoveStrategy.set(oldStrategy);

                                VBox mollifierSettingsLayout = new VBox();

                                Spinner<Integer> radiusSpinner = new Spinner<>(0, Integer.MAX_VALUE, oldStrategy.getRadius(), 1);
                                HBox.setHgrow(radiusSpinner, Priority.ALWAYS);
                                radiusSpinner.setMaxWidth(Double.MAX_VALUE);
                                Spinner<Integer> mollifierRangeSpinner = new Spinner<>(1, Integer.MAX_VALUE, oldStrategy.getMollifierConfiguration().getRange(), 1);
                                HBox.setHgrow(mollifierRangeSpinner, Priority.ALWAYS);
                                mollifierRangeSpinner.setMaxWidth(Double.MAX_VALUE);
                                Spinner<Double> mollifierStrengthSpinner = new Spinner<>(0.0, 999999, oldStrategy.getMollifierConfiguration().getStrength(), 0.1);
                                HBox.setHgrow(mollifierStrengthSpinner, Priority.ALWAYS);
                                mollifierStrengthSpinner.setMaxWidth(Double.MAX_VALUE);

                                radiusSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new FastMarchingMethodMovementStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));
                                mollifierRangeSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new FastMarchingMethodMovementStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));
                                mollifierStrengthSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentMoveStrategy.set(new FastMarchingMethodMovementStrategy(new MollifierConfiguration(mollifierRangeSpinner.getValue(), mollifierStrengthSpinner.getValue()), radiusSpinner.getValue())));

                                mollifierSettingsLayout.getChildren().addAll(
                                        new Label("Respect-other-people radius:"),
                                        radiusSpinner,
                                        new Label("Mollifier range:"),
                                        mollifierRangeSpinner,
                                        new Label("Mollifier strength:"),
                                        mollifierStrengthSpinner
                                );

                                moveStrategiesSettings.setCenter(mollifierSettingsLayout);
                            }
                        }
                    });
                    moveStrategiesComboBox.getSelectionModel().select(oldConfig.getMoveStrategy().getName());

                    // Max spawns spinner and checkbox
                    CheckBox limitSpawns = new CheckBox("Limit spawns");
                    VBox.setMargin(limitSpawns, new Insets(10, 0, 5, 0));
                    limitSpawns.setSelected(currentMaxSpawns.get() >= 0);

                    Spinner<Integer> maxSpawnsSpinner = new Spinner<>(1, Integer.MAX_VALUE, Math.max(currentMaxSpawns.get(), 1), 1);
                    VBox.setMargin(maxSpawnsSpinner, new Insets(5));
                    maxSpawnsSpinner.getEditor().setTextFormatter(StateVizView.getIntegerNumberFormatter(Math.max(currentMaxSpawns.get(), 1)));
                    HBox.setHgrow(maxSpawnsSpinner, Priority.ALWAYS);
                    maxSpawnsSpinner.setMaxWidth(Double.MAX_VALUE);
                    maxSpawnsSpinner.setEditable(true);
                    maxSpawnsSpinner.disableProperty().bind(limitSpawns.selectedProperty().not());
                    maxSpawnsSpinner.valueProperty().addListener((observable1, oldValue1, newValue1) -> currentMaxSpawns.set(newValue1));
                    limitSpawns.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue) {
                            currentMaxSpawns.set(-1);
                        } else {
                            currentMaxSpawns.set(Math.max(maxSpawnsSpinner.getValue(), 1));
                        }
                    });

                    // SPEED GENERATOR CONTROLS

                    BorderPane speedGeneratorLayout = new BorderPane();
                    VBox.setMargin(speedGeneratorLayout, new Insets(5));

                    ComboBox<String> speedGeneratorComboBox = new ComboBox<>();
                    VBox.setMargin(speedGeneratorComboBox, new Insets(5, 0, 5, 0));
                    HBox.setHgrow(speedGeneratorComboBox, Priority.ALWAYS);
                    speedGeneratorComboBox.setMaxWidth(Double.MAX_VALUE);
                    speedGeneratorComboBox.getItems().addAll(SpeedGenerators.LOOKUP.keySet().stream()
                            .sorted(Collator.getInstance(Locale.getDefault()))
                            .collect(Collectors.toList()));

                    speedGeneratorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        switch (newValue) {
                            case FixedSpeedGenerator.NAME -> {
                                FixedSpeedGenerator oldGenerator = oldConfig.getSpeedGenerator() instanceof FixedSpeedGenerator
                                        ? (FixedSpeedGenerator) oldConfig.getSpeedGenerator()
                                        : (FixedSpeedGenerator) SpeedGenerators.LOOKUP.get(FixedSpeedGenerator.NAME).get();

                                currentSpeedGenerator.set(oldGenerator);

                                VBox vBox = new VBox();

                                Spinner<Double> fixedSpeedSpinner = new Spinner<>(0.01, 1000, oldGenerator.getSpeed(), 1);
                                fixedSpeedSpinner.setEditable(true);
                                HBox.setHgrow(fixedSpeedSpinner, Priority.ALWAYS);
                                fixedSpeedSpinner.setMaxWidth(Double.MAX_VALUE);
                                fixedSpeedSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentSpeedGenerator.set(new FixedSpeedGenerator(newValue1)));

                                vBox.getChildren().addAll(new Label("Speed (cells/timeunit):"), fixedSpeedSpinner);

                                speedGeneratorLayout.setCenter(vBox);
                            }
                            case NormSpeedGenerator.NAME -> {
                                NormSpeedGenerator oldGenerator = oldConfig.getSpeedGenerator() instanceof NormSpeedGenerator
                                        ? (NormSpeedGenerator) oldConfig.getSpeedGenerator()
                                        : (NormSpeedGenerator) SpeedGenerators.LOOKUP.get(NormSpeedGenerator.NAME).get();

                                currentSpeedGenerator.set(oldGenerator);

                                VBox vBox = new VBox();

                                Spinner<Double> meanExpectationSpinner = new Spinner<>(0.01, 1000, oldGenerator.getMeanExpectation(), 1);
                                meanExpectationSpinner.setEditable(true);
                                HBox.setHgrow(meanExpectationSpinner, Priority.ALWAYS);
                                meanExpectationSpinner.setMaxWidth(Double.MAX_VALUE);

                                Spinner<Double> standardDeviationSpinner = new Spinner<>(0.01, 1000, oldGenerator.getStandardDeviation(), 1);
                                standardDeviationSpinner.setEditable(true);
                                HBox.setHgrow(standardDeviationSpinner, Priority.ALWAYS);
                                standardDeviationSpinner.setMaxWidth(Double.MAX_VALUE);
                                SpinnerValueFactory.DoubleSpinnerValueFactory standardDeviationSpinnerValueFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory) standardDeviationSpinner.getValueFactory();
                                standardDeviationSpinnerValueFactory.maxProperty().bind(meanExpectationSpinner.valueProperty());

                                meanExpectationSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentSpeedGenerator.set(new NormSpeedGenerator(meanExpectationSpinner.getValue(), standardDeviationSpinner.getValue())));
                                standardDeviationSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                        -> currentSpeedGenerator.set(new NormSpeedGenerator(meanExpectationSpinner.getValue(), standardDeviationSpinner.getValue())));

                                vBox.getChildren().addAll(new Label("Mean speed (cells/timeunit):"), meanExpectationSpinner, new Label("Standard deviation:"), standardDeviationSpinner);

                                speedGeneratorLayout.setCenter(vBox);
                            }
                        }
                    });
                    speedGeneratorComboBox.getSelectionModel().select(oldConfig.getSpeedGenerator().getName());

                    // PATIENCE SETTINGS
                    VBox patienceSettingsLayout = new VBox();
                    {
                        NormPatienceGenerator normPatienceGenerator = (NormPatienceGenerator) currentPatienceGenerator.get();

                        Spinner<Integer> meanPatienceSpinner = new Spinner<>(0, 1000, normPatienceGenerator.getMean(), 1);
                        meanPatienceSpinner.setEditable(true);
                        HBox.setHgrow(meanPatienceSpinner, Priority.ALWAYS);
                        meanPatienceSpinner.setMaxWidth(Double.MAX_VALUE);

                        Spinner<Integer> patienceMaxDeviation = new Spinner<>(0, 1000, normPatienceGenerator.getMaxDeviation(), 1);
                        patienceMaxDeviation.setEditable(true);
                        HBox.setHgrow(patienceMaxDeviation, Priority.ALWAYS);
                        patienceMaxDeviation.setMaxWidth(Double.MAX_VALUE);
                        SpinnerValueFactory.IntegerSpinnerValueFactory standardDeviationSpinnerValueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) patienceMaxDeviation.getValueFactory();
                        standardDeviationSpinnerValueFactory.maxProperty().bind(meanPatienceSpinner.valueProperty());

                        meanPatienceSpinner.valueProperty().addListener((observable1, oldValue1, newValue1)
                                -> currentPatienceGenerator.set(new NormPatienceGenerator(meanPatienceSpinner.getValue(), patienceMaxDeviation.getValue())));
                        patienceMaxDeviation.valueProperty().addListener((observable1, oldValue1, newValue1)
                                -> currentPatienceGenerator.set(new NormPatienceGenerator(meanPatienceSpinner.getValue(), patienceMaxDeviation.getValue())));

                        patienceSettingsLayout.getChildren().addAll(
                                new Label("Mean patience:"),
                                meanPatienceSpinner,
                                new Label("Max deviation:"),
                                patienceMaxDeviation
                        );
                    }

                    Label spawnStrategiesLabel = new Label("Spawn strategy:");
                    VBox.setMargin(spawnStrategiesLabel, new Insets(10, 0, 0, 0));

                    Label moveStrategiesLabel = new Label("Move strategies:");
                    VBox.setMargin(moveStrategiesLabel, new Insets(10, 0, 0, 0));

                    Label speedGeneratorLabel = new Label("Spawned people speed settings:");
                    VBox.setMargin(speedGeneratorLabel, new Insets(10, 0, 0, 0));

                    Label patienceGeneratorLabel = new Label("People patience:");
                    VBox.setMargin(patienceGeneratorLabel, new Insets(10, 0, 0, 0));

                    // Add all to layout
                    controlsLayout.getChildren().addAll(
                            spawnStrategiesLabel,
                            spawnStrategyComboBox,
                            spawnStrategySettings,
                            new Separator(),
                            limitSpawns,
                            maxSpawnsSpinner,
                            new Separator(),
                            moveStrategiesLabel,
                            moveStrategiesComboBox,
                            moveStrategiesSettings,
                            new Separator(),
                            speedGeneratorLabel,
                            speedGeneratorComboBox,
                            speedGeneratorLayout,
                            new Separator(),
                            patienceGeneratorLabel,
                            patienceSettingsLayout
                    );
                }
                case TARGET -> {
                    TargetConfiguration oldConfig = cellDescriptor.getConfiguration() != null
                            ? (TargetConfiguration) cellDescriptor.getConfiguration()
                            : new TargetConfiguration(ConsumeStrategies.DEFAULT.get());

                    ObjectProperty<ConsumeStrategy> currentConsumeStrategy = new SimpleObjectProperty<>(oldConfig.getConsumeStrategy());
                    currentConsumeStrategy.addListener((observable, oldValue, newValue)
                            -> model.setCellDescription(
                            new CellDescriptor(
                                    cellDescriptor.getTypeID(),
                                    cellDescriptor.getLocation(),
                                    new TargetConfiguration(newValue)), false));

                    BorderPane consumeStrategySettings = new BorderPane();

                    ComboBox<String> consumeStrategyComboBox = new ComboBox<>();
                    VBox.setMargin(consumeStrategyComboBox, new Insets(5, 0, 5, 0));
                    HBox.setHgrow(consumeStrategyComboBox, Priority.ALWAYS);
                    consumeStrategyComboBox.setMaxWidth(Double.MAX_VALUE);
                    consumeStrategyComboBox.getItems().addAll(ConsumeStrategies.LOOKUP.keySet().stream()
                            .sorted(Collator.getInstance(Locale.getDefault()))
                            .collect(Collectors.toList()));
                    consumeStrategyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.equals(RemoveConsumeStrategy.NAME)) {
                            currentConsumeStrategy.set(new RemoveConsumeStrategy());

                            Label descriptionLabel = new Label("When a person reaches this target, it will be removed from the simulation world.");
                            descriptionLabel.setWrapText(true);

                            consumeStrategySettings.setCenter(descriptionLabel);
                        } else if (newValue.equals(ReviveConsumeStrategy.NAME)) {
                            currentConsumeStrategy.set(new ReviveConsumeStrategy());

                            Label reviveDescription = new Label("When a person reaches this target, it will be immediately revived (teleported) to a random source object.");
                            reviveDescription.setWrapText(true);

                            consumeStrategySettings.setCenter(reviveDescription);
                        }
                    });
                    consumeStrategyComboBox.getSelectionModel().select(oldConfig.getConsumeStrategy().getName());

                    Label consumeStrategiesLabel = new Label("Consume strategy:");
                    VBox.setMargin(consumeStrategiesLabel, new Insets(10, 0, 0, 0));

                    controlsLayout.getChildren().addAll(consumeStrategiesLabel, consumeStrategyComboBox, consumeStrategySettings);
                }
                default -> {
                    Label noticeLabel = new Label("For this cell type are no configuration options available");
                    noticeLabel.setWrapText(true);
                    controlsLayout.getChildren().add(noticeLabel);
                }
            }

            ScrollPane scrollPane = new ScrollPane(controlsLayout);
            scrollPane.setFitToWidth(true);
            controlsLayout.setOnScroll(event -> {
                double deltaY = event.getDeltaY() * 6;
                double width = scrollPane.getContent().getBoundsInLocal().getWidth();
                double vvalue = scrollPane.getVvalue();
                scrollPane.setVvalue(vvalue + -deltaY / width);
            });

            configurationSettingsLayout.setCenter(scrollPane);
        }
    }

    /**
     * Mark a cell.
     *
     * @param event    the mouse event
     * @param location that should be marked
     */
    private void markCell(MouseEvent event, Location location) {
        // Check if painting is allowed
        if (!model.getEditingEnabled()) {
            return;
        }

        // Check if location exists
        if (location.getRow() < 0 || location.getRow() >= model.getRows()
                || location.getColumn() < 0 || location.getColumn() >= model.getColumns()) {
            return;
        }

        boolean doMark = event.getButton() == MouseButton.PRIMARY;
        if (doMark) {
            controller.paintCell(location, model.getPaint());
        } else {
            controller.removePaint(location);
        }
    }

    /**
     * Select the cell at the passed location for configuration.
     *
     * @param event    the original mouse event
     * @param location of the cell to configure
     */
    private void configureCell(MouseEvent event, @Nullable Location location) {
        var cellDescriptor = model.getCellDescription(location).orElse(null);
        controller.selectForConfiguration(cellDescriptor);
    }

    /**
     * Hover over a cell.
     *
     * @param event    the mouse event
     * @param location that should be marked
     */
    private void hoverCell(MouseEvent event, @Nullable Location location) {
        if (currentlyHoveredLocation != null) {
            // Reset paint at the old hovered location
            paintCellAt(currentlyHoveredLocation.getRow(), currentlyHoveredLocation.getColumn(), model.getColorForLocation(currentlyHoveredLocation));
        }

        if (location != null) {
            // Check if location exists
            if (location.getRow() < 0 || location.getRow() >= model.getRows()
                    || location.getColumn() < 0 || location.getColumn() >= model.getColumns()) {
                return;
            }

            currentlyHoveredLocation = location;
            paintCellAt(currentlyHoveredLocation.getRow(), currentlyHoveredLocation.getColumn(), model.getColorForLocation(currentlyHoveredLocation).brighter());
        } else {
            currentlyHoveredLocation = null;
        }
    }

    /**
     * Get the location for the passed mouse event.
     *
     * @param event to get location for
     * @return location
     */
    private Location getLocationForMouseEvent(MouseEvent event) {
        int tileSize = getTileSize();

        int xPadding = (int) ((canvas.getWidth() - tileSize * model.getColumns()) / 2);
        int yPadding = (int) ((canvas.getHeight() - tileSize * model.getRows()) / 2);

        int row = (int) ((event.getY() - yPadding) / tileSize);
        int column = (int) ((event.getX() - xPadding) / tileSize);

        return new Location(row, column);
    }

    /**
     * Get the current tile size.
     *
     * @return tile size
     */
    private int getTileSize() {
        return (int) Math.floor(
                Math.min(
                        canvas.getWidth() / model.getColumns(),
                        canvas.getHeight() / model.getRows()
                )
        );
    }

    /**
     * Repaint the canvas.
     *
     * @param gc     the graphics context to use
     * @param width  of the canvas
     * @param height of the canvas
     */
    private void repaintCanvas(GraphicsContext gc, double width, double height) {
        int tileSize = getTileSize();

        int xPadding = (int) ((width - tileSize * model.getColumns()) / 2);
        int yPadding = (int) ((height - tileSize * model.getRows()) / 2);

        int y = yPadding;
        for (int row = 0; row < model.getRows(); row++) {
            int x = xPadding;
            for (int column = 0; column < model.getColumns(); column++) {
                Color color = model.getColorForLocation(new Location(row, column));

                gc.setFill(color);
                gc.fillRect(
                        x + BORDER,
                        y + BORDER,
                        tileSize - BORDER,
                        tileSize - BORDER
                );

                x += tileSize;
            }

            y += tileSize;
        }
    }

    /**
     * Paint the cell at the passed row and column on the canvas.
     *
     * @param row    to paint cell at
     * @param column to paint cell at
     * @param color  to paint cell with
     */
    private void paintCellAt(int row, int column, Color color) {
        int tileSize = getTileSize();

        int xPadding = (int) ((canvas.getWidth() - tileSize * model.getColumns()) / 2);
        int yPadding = (int) ((canvas.getHeight() - tileSize * model.getRows()) / 2);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear area first, otherwise there might be some weird artifacts left
        gc.clearRect(
                xPadding + tileSize * column + BORDER / 2,
                yPadding + tileSize * row + BORDER / 2,
                tileSize - BORDER / 2,
                tileSize - BORDER / 2
        );

        gc.setFill(color);
        gc.fillRect(
                xPadding + tileSize * column + BORDER,
                yPadding + tileSize * row + BORDER,
                tileSize - BORDER,
                tileSize - BORDER
        );
    }

    /**
     * Takes a snapshot from the charts.
     * Saves it to a file specified by the file chooser
     */
    private void saveChart(Chart chart, File file) {
        if (file != null) {
            final AnchorPane anchorPane = new AnchorPane();
            final int minWidth = 1920;
            final int minHeight = 1080;
            anchorPane.setMinSize(minWidth, minHeight);
            anchorPane.setMaxSize(minWidth, minHeight);
            anchorPane.setPrefSize(minWidth, minHeight);

            final ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(anchorPane);

            final JFXPanel fxPanel = new JFXPanel();
            fxPanel.setScene(new Scene(scrollPane));

            final JFrame frame = new JFrame();

            final Pane previousParentPane = (Pane) chart.getParent();

            frame.setSize(new Dimension(64, 64));
            frame.setVisible(false);
            frame.add(fxPanel);

            anchorPane.getChildren().clear();
            AnchorPane.setLeftAnchor(chart, 0.0);
            AnchorPane.setRightAnchor(chart, 0.0);
            AnchorPane.setTopAnchor(chart, 0.0);
            AnchorPane.setBottomAnchor(chart, 0.0);
            anchorPane.getChildren().add(chart);

            anchorPane.layout();
            chart.setAnimated(false);
            chart.applyCss();
            chart.layout();

            try {
                final SnapshotParameters snapshotParameters = new SnapshotParameters();
                snapshotParameters.setViewport(new Rectangle2D(0.0, 0.0, minWidth, minHeight));
                ImageIO.write(SwingFXUtils.fromFXImage(anchorPane.snapshot(snapshotParameters, new WritableImage(minWidth, minHeight)),
                        new BufferedImage(minWidth, minHeight, BufferedImage.TYPE_INT_ARGB)), "png", file);
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    previousParentPane.getChildren().clear();
                    // Return the node back into it's previous parent
                    AnchorPane.setLeftAnchor(chart, 0.0);
                    AnchorPane.setRightAnchor(chart, 0.0);
                    AnchorPane.setTopAnchor(chart, 0.0);
                    AnchorPane.setBottomAnchor(chart, 0.0);
                    previousParentPane.getChildren().add(chart);

                    frame.dispose();
                });
            }
            chart.setAnimated(true);
        }
    }

    /**
     * Get the view as parent used to display it.
     *
     * @return parent
     */
    public Parent asParent() {
        return borderPane;
    }

    /**
     * Get an integer number formatter for example for number spinners.
     *
     * @param defaultValue of the spinner
     * @return integer text formatter
     */
    private static TextFormatter<Integer> getIntegerNumberFormatter(int defaultValue) {
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePos = new ParsePosition(0);
                format.parse(c.getControlNewText(), parsePos);
                if (parsePos.getIndex() == 0
                        || parsePos.getIndex() < c.getControlNewText().length()) {
                    return null;
                }
            }
            return c;
        };

        return new TextFormatter<>(new IntegerStringConverter(), defaultValue, filter);
    }

    /**
     * Custom list cell to display paint descriptors.
     */
    private static class PaintDescriptorListCell extends ListCell<PaintDescriptor> {

        @Override
        public void updateItem(PaintDescriptor item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                setText(item.getName());
                setGraphic(new Rectangle(25, 25, item.getColor()));
            }
        }

    }

}
