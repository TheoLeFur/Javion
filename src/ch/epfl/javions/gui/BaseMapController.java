package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;

import javax.management.ImmutableDescriptor;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.EventHandler;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Manages the display of the map background and the interactions with it.
 *
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class BaseMapController {
    TileManager tileManager;
    MapParameters mapParameters;
    private boolean redrawNeeded;
    Canvas canvas;
    Pane mainPane;
    GraphicsContext contextOfMap;

    int pixelsInATile = 1 << 8;


    /**
     * @param tileManager Used to obtain the tiles of the map
     * @param mapParameters Portion of the map that is visible
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters){
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
        //canvas on which the map will be drawn
        canvas = new Canvas();
        mainPane = new Pane();
        mainPane.getChildren().add(canvas);
        canvas.widthProperty().bind(mainPane.widthProperty());
        canvas.heightProperty().bind(mainPane.heightProperty());
        contextOfMap = canvas.getGraphicsContext2D();
        //draw for the first time
        redrawNeeded = true;

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.heightProperty().addListener((p, oldVal, newVal) -> {
            if(!oldVal.equals(newVal))
                redrawOnNextPulse();
        });

        canvas.widthProperty().addListener((p, oldVal, newVal) -> {
            if(!oldVal.equals(newVal))
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

        for(int i = 0; i <= Math.ceil(canvas.getWidth() / pixelsInATile); ++i) {
            for (int j = 0; j <= Math.ceil(canvas.getHeight() / pixelsInATile); j++) {
                TileManager.TileId tileToDraw = new TileManager.TileId(zoom,
                        mapToTile(mapX) + i,
                        mapToTile(mapY) + j);

                try {
                    contextOfMap.drawImage(tileManager.imageForTileAt(tileToDraw), (tileToDraw.X() * pixelsInATile) - mapX, (tileToDraw.Y() * pixelsInATile) - mapY);
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
     *
     * @param position point on the earth's surface
     * @return the visible portion of the map such that it is centered on that point
     */
    public MapParameters centerOn(GeoPos position) {
        return null;
    }

    /**
     *
     * @param mapCoord x or y coordinate of the point on the map
     * @return corresponding tile coordinate
     */
    private int mapToTile(double mapCoord) {
        return (int)Math.floor(mapCoord / pixelsInATile);
    }
}
