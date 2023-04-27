package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 *
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class MapParameters {

    private IntegerProperty zoom;
    private DoubleProperty minX;
    private DoubleProperty minY;

    /**
     * Module which will serve to track the main map params : zoom level, and origin given y (minX, minY)
     *
     * @param zoom zoom level
     * @param minX x coordinate of upper left corner of the map
     * @param minY y coordinate of upper left corner of the map
     */

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
        minX.set(getMinXValue() + tX);
        minY.set(getMinYValue() + tY);
    }

    /**
     * Change the zoom level on the map. Adapts the x and y coordinates to the zoom level.
     *
     * @param zoomIncrement positive or negative, depending on whether we want to zoom in or zoom out.
     */
    public void changeZoomLevel(int zoomIncrement) {

        int newZoomValue = zoomIncrement + getZoomValue();
        if (newZoomValue <= 19 && newZoomValue >= 6) {
            zoom.set(newZoomValue);
            minX.set(getMinXValue() * Math.pow(2, zoomIncrement));
            minY.set(getMinYValue() * Math.pow(2, zoomIncrement));
        }
    }

    public int getZoomValue() {
        return zoom.getValue();
    }

    public double getMinXValue() {
        return minX.getValue();
    }

    public double getMinYValue() {
        return minY.getValue();
    }

    public IntegerProperty getZoom() {
        return zoom;
    }

    public DoubleProperty getMinX() {
        return minX;
    }

    public DoubleProperty getMinY() {
        return minY;
    }


}
