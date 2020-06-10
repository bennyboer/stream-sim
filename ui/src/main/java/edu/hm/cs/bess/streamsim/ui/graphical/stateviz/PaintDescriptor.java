package edu.hm.cs.bess.streamsim.ui.graphical.stateviz;

import javafx.scene.paint.Color;

/**
 * Description of an available paint.
 *
 * @author Benjamin Eder
 */
public class PaintDescriptor {

    /**
     * The ID of the cell/paint.
     */
    private final int typeID;

    /**
     * Name of the paint.
     */
    private final String name;

    /**
     * Color of the paint.
     */
    private final Color color;

    /**
     * Whether the user is able to use that paint.
     */
    private final boolean canPaint;

    public PaintDescriptor(int typeID, String name, Color color) {
        this(typeID, name, color, true);
    }

    public PaintDescriptor(int typeID, String name, Color color, boolean canPaint) {
        this.typeID = typeID;
        this.name = name;
        this.color = color;
        this.canPaint = canPaint;
    }

    public int getTypeID() {
        return typeID;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public boolean canPaint() {
        return canPaint;
    }

}
