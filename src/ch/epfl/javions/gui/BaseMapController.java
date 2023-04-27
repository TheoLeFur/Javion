package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;

import java.awt.*;
import java.io.IOException;

/**
 * Manages the display of the map background and the interactions with it.
 *
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class BaseMapController {
    TileManager tileManager;
    MapParameters mapParameters;

    /**
     * @param tileManager   Used to obtain the tiles of the map
     * @param mapParameters Portion of the map that is visible
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
    }

    /**
     * @return the JavaFX pane that displays the map background
     */
    public Pane pane() throws IOException {
        //canvas on which the map will be drawn
        Canvas canvas = new Canvas(5, 5);
        Pane mainPane = new Pane();
        mainPane.getChildren().add(canvas);
        canvas.widthProperty().bind(mainPane.widthProperty());
        canvas.heightProperty().bind(mainPane.heightProperty());
        GraphicsContext contextOfMap = canvas.getGraphicsContext2D();


        TileManager.TileId tileId = new TileManager.TileId(
                mapParameters.getZoomValue(),
                mapToTile(mapParameters.getMinXValue()),
                mapToTile(mapParameters.getMinYValue()));
        //contextOfMap.drawImage(tileManager.imageForTileAt(tileId), );
        return null;
    }

    /**
     * @param position point on the earth's surface
     * @return the visible portion of the map such that it is centered on that point
     */
    public MapParameters centerOn(GeoPos position) {
        int zoomValue = this.mapParameters.getZoomValue()
        double x = WebMercator.x(zoomValue, position.longitude());
        double y = WebMercator.y(zoomValue, position.latitude());
        // so that (x, y) are in the center of the screen
        return new MapParameters(zoomValue, x -128, y - 128);

        return null;
    }

    private record CoordinatePair(int x, int y){}

    private int mapToTile(double mapCoord) {
        return (int) Math.floor(mapCoord / (2 << mapParameters.getZoomValue() + 1));
    }
}
