package edu.hm.cs.bess.streamsim.ui.graphical.timeline;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.converter.IntegerStringConverter;
import jfxtras.styles.jmetro.MDL2IconFont;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Random;

/**
 * View of the timeline.
 *
 * @author Benjamin Eder
 */
public class TimeLineView {

    /**
     * Model of the timeline.
     */
    private final TimeLineModel model;

    /**
     * Controller of the timeline.
     */
    private final TimeLineController controller;

    /**
     * Root pane of the view.
     */
    private BorderPane view;

    /**
     * The play button.
     */
    private Button playButton;

    /**
     * The reset button.
     */
    private Button resetButton;

    public TimeLineView(TimeLineController controller, TimeLineModel model) {
        this.model = model;
        this.controller = controller;

        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        view = new BorderPane();

        HBox timeLayout = new HBox();
        timeLayout.setAlignment(Pos.CENTER);
        timeLayout.setPadding(new Insets(10));
        timeLayout.setSpacing(10);

        Label timeLabel = new Label();
        model.animationTimeProperty().addListener((observable, oldValue, newValue) -> timeLabel.setText(String.format("%.3f", newValue.doubleValue())));
        timeLabel.setText(String.format("%.3f", model.getAnimationTime()));

        timeLabel.setFont(Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, 16));

        timeLayout.getChildren().addAll(new Label("Elapsed simulation time: "), timeLabel, new Label("time units"));

        view.setTop(timeLayout);

        HBox controls = new HBox();
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));
        controls.setSpacing(10);

        MDL2IconFont playIcon = new MDL2IconFont("\uE768");
        MDL2IconFont pauseIcon = new MDL2IconFont("\uE769");

        playButton = new Button("Play", playIcon);
        playButton.setMaxHeight(40);
        playButton.setMinHeight(40);
        playButton.setDefaultButton(true);
        playButton.setOnAction((event) -> {
            controller.togglePlayPause();
        });

        resetButton = new Button("Reset", new MDL2IconFont("\uEB9E"));
        resetButton.setMaxHeight(40);
        resetButton.setMinHeight(40);
        resetButton.disableProperty().bind(model.playingProperty());
        resetButton.setOnAction(event -> {
            controller.reset();
        });

        TextField seedInput = new TextField();
        NumberFormat format = NumberFormat.getIntegerInstance();
        seedInput.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, c -> {
            if (c.isContentChange()) {
                ParsePosition parsePos = new ParsePosition(0);
                format.parse(c.getControlNewText(), parsePos);
                if (parsePos.getIndex() == 0
                        || parsePos.getIndex() < c.getControlNewText().length()) {
                    return null;
                }
            }
            return c;
        }));
        seedInput.disableProperty().bind(model.playingProperty());
        seedInput.textProperty().addListener((observable, oldValue, newValue) -> model.setSeed(Long.parseLong(newValue)));
        model.seedProperty().addListener((observable, oldValue, newValue) -> seedInput.setText(newValue.toString()));

        Button reseedBtn = new Button("Reseed", new MDL2IconFont("\uE72C"));
        reseedBtn.disableProperty().bind(model.playingProperty());
        Random rng = new Random();
        reseedBtn.setOnAction(event -> seedInput.setText(String.valueOf(rng.nextInt(999999999))));
        seedInput.setText(String.valueOf(rng.nextInt(999999999)));

        controls.getChildren().addAll(playButton, resetButton, seedInput, reseedBtn);

        model.playingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                playButton.setText("Pause");
                playButton.setGraphic(pauseIcon);
            } else {
                playButton.setText("Play");
                playButton.setGraphic(playIcon);
            }
        });

        view.setCenter(controls);

        HBox speedLayout = new HBox();
        speedLayout.setAlignment(Pos.CENTER);
        speedLayout.setSpacing(5);

        Slider speedSlider = new Slider();
        speedSlider.setPrefWidth(300);
        speedSlider.setMin(0);
        speedSlider.setMax(1000);
        speedSlider.valueProperty().bindBidirectional(model.speedProperty());
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(50);
        speedSlider.setMinorTickCount(1);
        speedSlider.setBlockIncrement(1);
        HBox.setMargin(speedSlider, new Insets(5));

        Button speedPlus = new Button("", new MDL2IconFont("\uE948"));
        speedPlus.setOnAction(event -> speedSlider.increment());
        Button speedMinus = new Button("", new MDL2IconFont("\uE949"));
        speedMinus.setOnAction(event -> speedSlider.decrement());

        speedLayout.getChildren().addAll(new Label("Speed (ms / timeunit):"), speedSlider, speedPlus, speedMinus);

        view.setBottom(speedLayout);
    }

    /**
     * Get the view as parent to display it.
     *
     * @return parent
     */
    public Parent asParent() {
        return view;
    }

}
