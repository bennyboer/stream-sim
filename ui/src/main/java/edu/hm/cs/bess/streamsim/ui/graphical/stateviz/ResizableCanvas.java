package edu.hm.cs.bess.streamsim.ui.graphical.stateviz;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * A resizable canvas.
 *
 * @author Benjamin Eder
 */
public class ResizableCanvas extends Canvas {

    /**
     * Called on repaint.
     */
    private final RepaintListener repaintListener;

    public ResizableCanvas(RepaintListener repaintListener) {
        this.repaintListener = repaintListener;

        widthProperty().addListener(evt -> repaint());
        heightProperty().addListener(evt -> repaint());
    }

    /**
     * Repaint the canvas.
     */
    private void repaint() {
        double width = getWidth();
        double height = getHeight();

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        repaintListener.repaint(gc, width, height);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return 0;
    }

    @Override
    public double prefHeight(double width) {
        return 0;
    }

    /**
     * Repaint listener used to update the canvas.
     */
    @FunctionalInterface
    public interface RepaintListener {

        /**
         * Repaint the canvas.
         *
         * @param gc     graphics context to use
         * @param width  of the canvas
         * @param height of the canvas
         */
        void repaint(GraphicsContext gc, double width, double height);

    }

}
