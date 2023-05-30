package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import java.io.IOException;

/**
 * @author Theo Le Fur
 * SCIPER : 363294
 * Class that manages the display of the map background and the interactions with it. Deals with various types of events
 * like scrolling and zooming.
 */
public final class BaseMapController {

    // Side length of a tile
    private final static int PIXELS_IN_TILE = (int) Math.scalb(1, 8);

    // time we wait during a mouse drag
    private final int SCROLL_DELTA_T = 200;
    private final Pane pane;
    private final Canvas canvas;
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private Point2D mousePos;
    private final LongProperty scrollDeltaT;
    private boolean redrawNeeded;
    private final GraphicsContext contextOfMap;


    /**
     * Instantiates a map controller. Draws the background map and handles various control events like mouse drag or scrolling.
     *
     * @param tileManager   manages the access to the tiles required for proper display
     * @param mapParameters params of the map that is visible
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {

        this.tileManager = tileManager;
        this.mapParameters = mapParameters;

        // Create the scene graph.
        this.pane = new Pane();
        this.canvas = new Canvas();
        this.pane.getChildren().add(this.canvas);

        // Make the canvas follow the dimensions of the pane
        this.canvas.heightProperty().bind(this.pane.heightProperty());
        this.canvas.widthProperty().bind(this.pane.widthProperty());

        // Get the graphic context of the map, that allows us to draw images subsequently
        this.contextOfMap = this.canvas.getGraphicsContext2D();

        // So that we call the draw function at construction
        this.redrawNeeded = true;
        this.scrollDeltaT = new SimpleLongProperty();

        // At each pulse, we draw the necessary images to match the desired display.
        this.canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        this.canvas.heightProperty().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.canvas.widthProperty().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.mapParameters.minXProperty().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.mapParameters.minYProperty().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.mapParameters.zoomProperty().addListener((p, oldVal, newVal) -> redrawOnNextPulse());


        this.scrollEventHandler();
        this.dragEventHandler();

    }


    /**
     * Handles dragging events, that is displacement on the map with the mouse/the touch bar.
     */
    private void dragEventHandler() {

        this.pane.setOnMousePressed(
                event -> this.mousePos = new Point2D(event.getX(), event.getY())
        );
        this.pane.setOnMouseDragged(event -> {
            Point2D newPoint = this.mousePos.subtract(new Point2D(event.getX(), event.getY()));
            this.mapParameters.scroll(newPoint.getX(), newPoint.getY());
            this.redrawOnNextPulse();
            this.mousePos = new Point2D(event.getX(), event.getY());
        });
        this.pane.setOnMouseReleased(event -> this.mousePos = null);
    }


    /**
     * Handles scrolling events, that is zooming in and out of the map.
     */
    private void scrollEventHandler() {

        this.pane.setOnScroll(event -> {
            int dZoom = (int) Math.signum(event.getDeltaY());
            if (dZoom == 0) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < this.scrollDeltaT.get()) return;
            this.scrollDeltaT.set(currentTime + SCROLL_DELTA_T);
            this.mapParameters.scroll(event.getX(), event.getY());
            this.mapParameters.changeZoomLevel(dZoom);
            this.mapParameters.scroll(-event.getX(), -event.getY());

        });
    }

    /**
     * This method requests a new drawing of the map at the next pulse. Whenever we have to redraw the display,
     * we call this method.
     */
    private void redrawOnNextPulse() {
        this.redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Whenever redrawNeeded is true, we draw the image that has to be displayed. Else we do nothing.
     */
    private void redrawIfNeeded() {
        if (!this.redrawNeeded) return;
        this.redrawNeeded = false;
        draw();
    }


    /**
     * Draw the relevant images that are required for a proper display.
     */
    private void draw() {

        int zoom = this.mapParameters.getZoomValue();
        double minXValue = this.mapParameters.getMinXValue();
        double minYValue = this.mapParameters.getMinYValue();

        for (int i = 0; i <= Math.ceil(this.canvas.getWidth() / PIXELS_IN_TILE); i+=1) {
            for (int j = 0; j <= Math.ceil(this.canvas.getHeight() / PIXELS_IN_TILE); j+=1) {
                TileManager.TileId tileToDraw = new TileManager.TileId(zoom,
                        this.tileCoords(minXValue) + i,
                        this.tileCoords(minYValue) + j);
                try {
                    this.contextOfMap.drawImage(this.tileManager.imageForTileAt(tileToDraw), tileToDraw.x() * PIXELS_IN_TILE
                            - minXValue, tileToDraw.y() * PIXELS_IN_TILE - minYValue);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * getter for the main pane, where the map background is hosted
     *
     * @return the main pane
     */
    public Pane pane() {
        return this.pane;
    }

    /**
     * Centers the map at the following position
     *
     * @param position position at which we want to center our map on : type GeoPos
     */
    public void centerOn(GeoPos position) {
        int zoomValue = this.mapParameters.getZoomValue();
        this.mapParameters.zoomProperty().set(zoomValue);
        this.mapParameters.setMinX(
                (int) WebMercator.x(zoomValue, position.longitude())
                        - this.canvas.getWidth() / 2);
        this.mapParameters.setMinY(
                (int) WebMercator.y(zoomValue, position.latitude())
                        - this.canvas.getHeight() / 2);
    }

    /**
     * @param mapCoord Component of the position vector
     * @return the accordingly computed tile coordinates.
     */
    private int tileCoords(double mapCoord) {
        return (int) Math.floor(mapCoord / PIXELS_IN_TILE);
    }
}
