package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class TileManager {

    public record TileId(int zoomLevel, int x, int y) {

        public static boolean isValid(int zoom, int x, int y) {

            int maxNumberOfTiles = 2 << (zoom + 1);
            return 0 <= zoom && zoom <= 19
                    && x >= 0 && x <= maxNumberOfTiles
                    && y >= 0 && y <= maxNumberOfTiles;
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
            String path = "/" + tileId.zoomLevel() + "/" + tileId.x() + "/" + tileId.y() + ".png";
            Path imgPath = Path.of(this.cacheDiskPath.toString() + path);
            if (Files.exists(imgPath)) {
                image = new Image(new FileInputStream(imgPath.toFile()));

            } else {
                URL u = new URL("https://" + tileServerName + path);
                URLConnection c = u.openConnection();
                c.setRequestProperty("User-Agent", "Javions");

                try (InputStream i = c.getInputStream()) {
                    byteBuffer = i.readAllBytes();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
                    image = new Image(byteArrayInputStream);
                }

                Files.createDirectories(Path.of(this.cacheDiskPath + "/" +
                        tileId.zoomLevel() + "/" + tileId.x()));

                try (OutputStream o = new FileOutputStream(imgPath.toString())) {
                    o.write(byteBuffer);
                }
            }
            this.memoryCache.put(tileId, image);
            this.cropMemCache();
        }
        return image;
    }

    /**
     * Whenever the memory cache map has more than 100 elements, we remove the least recently accessed
     * element.
     */
    private void cropMemCache(){
        if (this.memoryCache.size() > 100){
            TileId firstId = this.memoryCache.keySet().iterator().next();
            this.memoryCache.remove(firstId);

        }
    }

    /**
     * Getter for memoryCache, used in testing
     * @return Memory Cache containing the 100 most recently accessed images by the program.
     */
    public Map<TileId, Image> getMemoryCache(){
        return this.memoryCache;
    }
}