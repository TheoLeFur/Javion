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
 * Manages the display of the map background and the interactions with it.
 *
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class BaseMapController {
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private boolean redrawNeeded;
    private final Canvas canvas;
    private final Pane mainPane;
    private final GraphicsContext contextOfMap;
    private Point2D cursorPosition;

    final static int PIXELS_IN_TILE = 1 << 8;


    /**
     * @param tileManager Used to obtain the tiles of the map
     * @param mapParameters Portion of the map that is visible
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters){
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;

        canvas = new Canvas();
        mainPane = new Pane();
        mainPane.getChildren().add(canvas);
        canvas.widthProperty().bind(mainPane.widthProperty());
        canvas.heightProperty().bind(mainPane.heightProperty());
        contextOfMap = canvas.getGraphicsContext2D();
        //draw for the first time
        redrawNeeded = true;


        //zoom in/out with scroll wheel
        LongProperty minScrollTime = new SimpleLongProperty();
        mainPane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            mapParameters.scroll(e.getX(), e.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(- e.getX(),- e.getY());

        });

        mapParameters.getMinX().addListener((p, oldVal, newVal) -> {
            redrawOnNextPulse();
        });

        mapParameters.getMinY().addListener((p, oldVal, newVal) -> {
            redrawOnNextPulse();
        });

        mapParameters.getZoom().addListener((p, oldVal, newVal) -> {
            redrawOnNextPulse();
        });

        //dragging lambdas
        mainPane.setOnMousePressed(e -> {
             cursorPosition = new Point2D(e.getX(), e.getY());
        });
        mainPane.setOnMouseDragged(e -> {
            Point2D midWayPoint = cursorPosition.subtract(new Point2D(e.getX(), e.getY()));
            mapParameters.scroll(midWayPoint.getX(), midWayPoint.getY());
            redrawOnNextPulse();
            cursorPosition = new Point2D(e.getX(), e.getY());
        });
        mainPane.setOnMouseReleased(e -> {
            cursorPosition = null;
        });

        //making it so every pulse, the image is redrawn
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        //setting the listeners to check for when the window is modified
        canvas.heightProperty().addListener((p, oldVal, newVal) -> {
            redrawOnNextPulse();
        });
        canvas.widthProperty().addListener((p, oldVal, newVal) -> {
            redrawOnNextPulse();
        });
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void redrawIfNeeded() {
        if(!redrawNeeded)
            return;

        drawImages();
        redrawOnNextPulse();
        redrawNeeded = false;
    }

    private void drawImages() {
        int zoom = mapParameters.getZoomValue();
        double mapX = mapParameters.getMinXValue();
        double mapY = mapParameters.getMinYValue();

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
     *
     * @return the JavaFX pane that displays the map background
     */
    public Pane pane(){
        return mainPane;
    }

    /**
     * @param position point on the earth's surface
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
     * @param mapCoord x or y coordinate of the point on the map
     * @return corresponding tile coordinate
     */
    private int mapToTile(double mapCoord) {
        return (int)Math.floor(mapCoord / PIXELS_IN_TILE);
    }
}
