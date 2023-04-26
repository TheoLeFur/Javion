package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.font.ImageGraphicAttribute;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TileManager {

    public record TileId(int zoomLevel, int X, int Y) {

        public static boolean isValid(int zoom, int X, int Y) {
            int x = X * 256 + 1;
            int y = Y * 256 + 1;
            return (0 <= zoom && zoom <= 19);
        }
    }

    private final Path cacheDiskPath;
    private final String tileServerName;
    private final int maxMemoryCacheCapacity = 100;

    private final Map<TileId, Image> memoryCache = new LinkedHashMap<>(maxMemoryCacheCapacity, 1, true);


    public TileManager(Path cacheDiskPath, String tileServerName) {
        this.cacheDiskPath = cacheDiskPath;
        this.tileServerName = tileServerName;
    }

    /**
     * Create a JavaFX Image from tile information : zoom level, X position and Y position. Request the data from server,
     * add the image to the cache memory if required and store the data a steamed passed into an Image object.
     *
     * @param tileId record accounting for zoom level, X and Y positions.
     * @return An JavaFX image object
     * @throws IOException if error while reading the stream.
     */

    public Image imageForTileAt(TileId tileId) throws IOException {

        byte[] byteBuffer;
        Image image;

        if (this.memoryCache.get(tileId) != null) {
            image = this.memoryCache.get(tileId);
        } else {

            Path imgPath = Path.of(this.buildImageDir(this.cacheDiskPath.toString(), tileId));
            if (Files.exists(imgPath)) {
                image = new Image(imgPath.toString());

            } else {
                String serverPath = "https://" +  this.buildImageDir(this.tileServerName, tileId);
                URL requestUrl = new URL(serverPath);
                URLConnection c = requestUrl.openConnection();
                System.out.println("1");
                c.setRequestProperty("User-Agent", "Javions");
                try (InputStream i = c.getInputStream()) {
                    System.out.println("2");
                    byteBuffer = i.readAllBytes();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
                    image = new Image(byteArrayInputStream);
                }
                System.out.println("3");
                Files.createDirectories(imgPath);

                try (OutputStream o = new FileOutputStream(imgPath.toString())) {
                    o.write(byteBuffer);
                }
            }
            // If maximal capacity is exceeded, the image accessed the furthest
            // amount of time from now will be replaced
            this.memoryCache.put(tileId, image);
        }
        return image;
    }

    /**
     * Build a directory for storing he images following the logic : src/zoomLevel/X/Y.png
     *
     * @param src source folder
     * @param id  id of image
     * @return directory for image.
     */
    private String buildImageDir(String src, TileId id) {
        return src + "/" +
                id.zoomLevel() + "/" +
                id.X() + "/" +
                id.Y() + ".png";
    }
}