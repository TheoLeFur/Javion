package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

public final class MapParameters {

    private IntegerProperty zoom;
    private DoubleProperty minX;
    private DoubleProperty minY;

    public MapParameters(
            int zoom,
            double minX,
            double minY
    ) {
        Preconditions.checkArgument(
                zoom >= 6 &&
                        zoom < 20
        );

        this.zoom = new SimpleIntegerProperty(zoom);
        this.minX = new SimpleDoubleProperty(minX);
        this.minY = new SimpleDoubleProperty(minY);

    }

    /**
     * Translate the corner coordinates by a vector of coordinate representation [tX, tY].
     *
     * @param tX x coordinate of the vector
     * @param tY y coordinate of the vector
     */
    public void scroll(double tX, double tY) {
        this.minX = new SimpleDoubleProperty(this.minX.doubleValue() + tX);
        this.minY = new SimpleDoubleProperty(this.minY.doubleValue() + tY);
    }

    /**
     * Change the zoom level on the map. Adapts the x and y coordinates to the zoom level.
     *
     * @param zoomIncrement positive or negative, depending on whether we want to zoom in or zoom out.
     */
    public void changeZoomLevel(int zoomIncrement) {
        int clippedZoomIncrement = Math2.clamp(6 - this.zoom.getValue(), zoomIncrement, 19 - this.zoom.getValue());
        this.zoom = new SimpleIntegerProperty(
                this.zoom.getValue() + clippedZoomIncrement
        );
        this.minX = new SimpleDoubleProperty((this.minX.getValue() * Math.pow(2, clippedZoomIncrement)));
        this.minY = new SimpleDoubleProperty((this.minY.getValue() * Math.pow(2, clippedZoomIncrement)));
    }

    public int getZoomValue() {
        return this.zoom.getValue();
    }

    public double getMinXValue() {
        return this.minX.getValue();
    }

    public double getMinYValue() {
        return this.minY.getValue();
    }

    public IntegerProperty getZoom() {
        return this.zoom;
    }

    public DoubleProperty getMinX() {
        return this.minX;
    }

    public DoubleProperty getMinY() {
        return this.minY;
    }


}
