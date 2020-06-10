package edu.hm.cs.bess.streamsim.ui.graphical.dialog.create;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import jfxtras.styles.jmetro.FlatDialog;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

/**
 * Dialog creating a new simulation scenario.
 *
 * @author Benjamin Eder
 */
public class CreateDialog extends FlatDialog<CreateDialog.CreateDialogResult> {

    /**
     * Property of the currently set width.
     */
    private final IntegerProperty width = new SimpleIntegerProperty();

    /**
     * Property of the currently set height.
     */
    private final IntegerProperty height = new SimpleIntegerProperty();

    /**
     * The dialogs root view.
     */
    private GridPane view;

    public CreateDialog(Stage owner, Style style) {
        setTitle("Create new scenario");

        if (owner != null) {
            initOwner(owner);
        }

        // Init theme
        JMetro jMetro = new JMetro(style);
        jMetro.setScene(getDialogPane().getScene());

        setResultConverter((dialogButton) -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.FINISH) {
                return new CreateDialogResult(width.get(), height.get());
            }

            return null;
        });

        init();
    }

    /**
     * Initialize the dialog.
     */
    private void init() {
        view = new GridPane();
        view.setHgap(5);
        view.setVgap(5);

        getDialogPane().setContent(view);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.FINISH);

        buildSizeSliders();
    }

    /**
     * Build sliders changing the scenario size.
     */
    private void buildSizeSliders() {
        Label widthLabel = new Label("Width");
        Spinner<Integer> widthSpinner = new Spinner<>(1, 200, 20, 1);
        widthSpinner.getEditor().setTextFormatter(CreateDialog.getIntegerNumberFormatter());
        widthSpinner.setEditable(true);
        width.bind(widthSpinner.valueProperty());

        Label heightLabel = new Label("Height");
        Spinner<Integer> heightSpinner = new Spinner<>(1, 200, 20, 1);
        heightSpinner.getEditor().setTextFormatter(CreateDialog.getIntegerNumberFormatter());
        heightSpinner.setEditable(true);
        height.bind(heightSpinner.valueProperty());

        view.add(widthLabel, 0, 0);
        view.add(widthSpinner, 1, 0);
        view.add(heightLabel, 0, 1);
        view.add(heightSpinner, 1, 1);
    }

    /**
     * Get integer number formatter for text.
     *
     * @return formatter
     */
    private static TextFormatter<Integer> getIntegerNumberFormatter() {
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

        return new TextFormatter<>(new IntegerStringConverter(), 20, filter);
    }

    /**
     * Result of the dialog.
     */
    public static class CreateDialogResult {

        /**
         * Width of the grid.
         */
        private final int width;

        /**
         * Height of the grid.
         */
        private final int height;

        public CreateDialogResult(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

    }

}
