package ch.epfl.javions.gui;
/*
 */

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Objects;


/**
 * Defines the pane that will display the map as well as methods for
 * the user to interact with it.
 *
 */
public final class BaseMapController {

    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private final Point2D point;
    private final Pane pane;
    private final Canvas canvas;
    private final GraphicsContext graphicContext;
    private boolean redrawNeeded;

    /**
     * Public Constructor.
     *
     * @param tileManager   The TileManager instance that will be used to recover the tiles.
     * @param mapParameters The MapParameters instance that will be used to handle the position
     *                      of the screen on the map.
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
        canvas = new Canvas();
        pane = new Pane(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        point = new Point2D.Double();
        graphicContext = canvas.getGraphicsContext2D();


        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.widthProperty().addListener(v -> redrawOnNextPulse());
        canvas.heightProperty().addListener(v -> redrawOnNextPulse());


        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            if (!(6 <= mapParameters.getZoomValue() + zoomDelta && mapParameters.getZoomValue() + zoomDelta <= 19)) return;

            double [] absolutePosOfCursor = new double[]{e.getX() + mapParameters.getMinXValue(),        // Mouse position.
                    e.getY() + mapParameters.getMinYValue()};

            double[] newCoordinates = new double[]{absolutePosOfCursor[0] * Math.pow(2, zoomDelta),  // Where the screen should be positioned regarding
                    absolutePosOfCursor[1] * Math.pow(2, zoomDelta)};                                // the new zoom level and the position of the mouse.

            mapParameters.changeZoomLevel(zoomDelta);

            double[] newWrongCoordinates = new double[]{e.getX() + mapParameters.getMinXValue(),          // The coordinates of the screen after the zoom update (they need to be changed).
                    e.getY() + mapParameters.getMinYValue()};

            // Rectification of the position of the screen.
            mapParameters.scroll(newCoordinates[0] - newWrongCoordinates[0], newCoordinates[1] - newWrongCoordinates[1]);

            redrawOnNextPulse();
        });


        pane.setOnMousePressed(e -> {
            point.setLocation(e.getX(), e.getY());  // Used for the MouseDrag event to work.
            redrawOnNextPulse();
        });


        pane.setOnMouseDragged(e -> {

            mapParameters.scroll(point.getX() - e.getX(), point.getY() - e.getY());
            point.setLocation(e.getX(), e.getY());

            redrawOnNextPulse();
        });

    }


    /**
     * Pane getter.
     *
     * @return The pane where the map is displayed.
     */
    public Pane pane() {
        return pane;
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        int[] visibleLengthOfTiles = new int[]{(int) Math.ceil(canvas.getWidth() / 256),   // visibleLengthOfTiles = {visibleLengthOfTilesX, visibleLengthOfTilesY}
                (int) Math.ceil(canvas.getHeight() / 256)};

        int[] indexOfFirstTiles = new int[]{(int) (mapParameters.getMinXValue() / 256),         // indexOfFirstTiles = {indexOfFirstTilesX, indexOfFirstTilesY}
                (int) (mapParameters.getMinYValue() / 256)};

        double[] offset = new double[]{indexOfFirstTiles[0] * 256 - mapParameters.getMinXValue(),     // Offset vector : offset = {xOffset, yOffset}
                indexOfFirstTiles[1]*256 - mapParameters.getMinYValue()};

        while (visibleLengthOfTiles[0]*256 + offset[0] < canvas.getWidth()) {                       // Make sure the screen is completely covered.
            visibleLengthOfTiles[0]++;
        }
        while (visibleLengthOfTiles[1]*256 + offset[1] < canvas.getHeight()) {
            visibleLengthOfTiles[1]++;
        }

        int[] indexOfLastTiles = new int[]{indexOfFirstTiles[0] + visibleLengthOfTiles[0],     // indexOfLastTiles = {indexOfLastTileX, indexOfLastTileY}
                indexOfFirstTiles[1] + visibleLengthOfTiles[1]};

        graphicContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());          // Erases the previous drawing.

        for (int i = indexOfFirstTiles[0]; i < indexOfLastTiles[0]; ++i) {
            for (int j = indexOfFirstTiles[1]; j < indexOfLastTiles[1]; ++j) {
                try {
                    TileManager.TileId tile = new TileManager.TileId(mapParameters.getZoomValue(), i, j);

                    graphicContext.drawImage(tileManager.imageForTileAt(tile),
                            (i - indexOfFirstTiles[0])*256 + offset[0],
                            (j - indexOfFirstTiles[1])*256 + offset[1]);

                } catch (IOException | IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }


    /**
     * Method centering the screen on a certain point on the map.
     *
     * @param point The point on which the screen will be centered.
     */
    public void centerOn(GeoPos point) {
        Preconditions.checkArgument(GeoPos.isValidLatitudeT32(point.latitudeT32()));
        double newXCoordinate = WebMercator.x(mapParameters.getZoomValue(), point.longitude()) + canvas.getWidth() / 2;      // Check this ???
        double newYCoordinate = WebMercator.y(mapParameters.getZoomValue(), point.latitude()) + canvas.getHeight() / 2;

        mapParameters.scroll(newXCoordinate - mapParameters.getMinXValue(), newYCoordinate - mapParameters.getMinYValue());
    }
}