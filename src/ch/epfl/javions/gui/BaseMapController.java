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
 * Class that manages the display of the map background and the interactions with it. Deals with various types of events
 * like scrolling and zooming.
 */
public final class BaseMapController {

    // Side length of a tile, 256 pixels
    private final static int PIXELS_IN_TILE = (int) Math.scalb(1, 8);

    private final Pane pane;
    private final Canvas canvas;
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private boolean redrawNeeded;
    private final GraphicsContext contextOfMap;
    private Point2D mousePos;
    private final LongProperty scrollDeltaT;




    /**
     * @param tileManager   Used to obtain the tiles of the map
     * @param mapParameters Portion of the map that is visible
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

        // So that we call the drawing function at construction
        this.redrawNeeded = true;
        this.scrollDeltaT = new SimpleLongProperty();

        // At each pulse, we draw the necessary images to match the desired display.
        this.canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });


        this.scrollEventHandler();
        this.dragEventHandler();

        // setting the listeners to check for when the window is modified
        this.canvas.heightProperty().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.canvas.widthProperty().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.mapParameters.getMinX().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.mapParameters.getMinY().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
        this.mapParameters.getZoom().addListener((p, oldVal, newVal) -> redrawOnNextPulse());
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
            int zoomDelta = (int) Math.signum(event.getDeltaY());
            if (zoomDelta == 0) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < scrollDeltaT.get()) return;
            scrollDeltaT.set(currentTime + 200);
            mapParameters.scroll(event.getX(), event.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(- event.getX(),- event.getY());

        });
    }

    /**
     * This method requests a new drawing of the map at the next pulse. Whenever we have to redraw the display,
     * we call this method
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Whenever redrawNeeded is true, we draw the image that has to be displayed. Else we do nothing.
     */
    private void redrawIfNeeded() {
        if(!redrawNeeded) return;
        redrawNeeded = false;
        draw();
        redrawOnNextPulse();

    }


    /**
     * Draw the relevant images that are required for a proper display.
     */
    private void draw() {

        int zoom =this.mapParameters.getZoomValue();
        double mapX = this.mapParameters.getMinXValue();
        double mapY = this.mapParameters.getMinYValue();

        for(int i = 0; i <= Math.ceil(canvas.getWidth() / PIXELS_IN_TILE); ++i) {
            for (int j = 0; j <= Math.ceil(canvas.getHeight() / PIXELS_IN_TILE); j++) {
                TileManager.TileId tileToDraw = new TileManager.TileId(zoom,
                        mapToTile(mapX) + i,
                        mapToTile(mapY) + j);
                try {
                    contextOfMap.drawImage(tileManager.imageForTileAt(tileToDraw), (tileToDraw.x() * PIXELS_IN_TILE)
                            - mapX, (tileToDraw.y() * PIXELS_IN_TILE) - mapY);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * getter for the main pane, where the map background is hosted
     * @return the main pane
     */
    public Pane pane(){
        return pane;
    }

    /**
     * Centers the map at the following position
     * @param position position at which we want to center our map on : type GeoPos
     */
    public void centerOn(GeoPos position) {
        int zoomValue = mapParameters.getZoomValue();
        int x = (int) WebMercator.x(zoomValue, position.longitude());
        int y = (int) WebMercator.y(zoomValue, position.latitude());
        mapParameters.getZoom().set(zoomValue);
        mapParameters.setMinX(x + canvas.getWidth()/2);
        mapParameters.setMinY(y + canvas.getHeight()/2);
    }

    /**
     *
     * @param mapCoord Component of the position vector
     * @return the accordingly computed tile coordinates.
     */
    private int mapToTile(double mapCoord) {
        return (int) Math.floor(mapCoord / PIXELS_IN_TILE);
    }
}
