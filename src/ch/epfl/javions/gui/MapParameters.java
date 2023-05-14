package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * This class describes the params. of the visible portion of the map we access while interacting with the application.
 * It is characterized by a system if local coordinates, as well as a zoom level.
 */
public final class MapParameters {

    private final IntegerProperty zoom;
    private final DoubleProperty minX;
    private final DoubleProperty minY;

    /**
     * Module which will serve to track the main map params : zoom level, and origin given y (minX, minY)
     *
     * @param zoom zoom level
     * @param minX x coordinate of upper left corner of the map
     * @param minY y coordinate of upper left corner of the map
     */

    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(zoom >= 6 && zoom <= 19);

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


    /**
     * Getter for zoom value
     *
     * @return zoom factor
     */
    public int getZoomValue() {
        return zoom.getValue();
    }

    /**
     * Getter for x coordinate of the left right corner of the tile displayed
     *
     * @return value of minX
     */
    public double getMinXValue() {
        return minX.getValue();
    }

    /**
     * Getter for y coordinate of the left right corner of the tile displayed
     *
     * @return value of minY
     */
    public double getMinYValue() {
        return minY.getValue();
    }

    /**
     * Getter for the zoom property
     *
     * @return zoom property
     */

    public IntegerProperty zoomProperty() {
        return zoom;
    }

    /**
     * Getter for the property holding the x value of the left right corner of the tile displayed
     *
     * @return minX property
     */

    public DoubleProperty minXProperty() {
        return minX;
    }

    /**
     * Getter for the property holding the y value of the left right corner of the tile displayed
     *
     * @return minY  property
     */

    public DoubleProperty minYProperty() {
        return minY;
    }

    /**
     * Set the value of the zoom property
     *
     * @param zoom new zoom value
     */

    public void setZoom(int zoom) {
        this.zoom.set(zoom);
    }

    /**
     * Set the value of the minY property
     *
     * @param minY new zoom value
     */

    public void setMinY(double minY) {
        this.minY.set(minY);
    }

    /**
     * Set the value of the minX property
     *
     * @param minX new zoom value
     */

    public void setMinX(double minX) {
        this.minX.set(minX);
    }
}
