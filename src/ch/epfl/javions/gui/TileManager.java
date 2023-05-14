package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class that manages the tile. As we zoom more into the OSM images, the number of pixels necessary to represent the image exponentially increases.
 * Instead, we cut the image into tiles of 256 x 256 pixels, that are accessed whenever we need them.
 */
public final class TileManager {
    /**
     * Record storing the identity of an OSM tile
     *
     * @param zoomLevel zoom level of the image
     * @param x         index of the tile, representing the x coordinate of the top left corner
     * @param y         index of the tile, representing the y coordinate of the top left corner
     */
    public record TileId(int zoomLevel, int x, int y) {

        /**
         * Asserts whether the tile parameters passed in arguments are valid args
         *
         * @param zoom oom level
         * @param x    x index of the tile
         * @param y    y index of the tile
         * @return true if the params are valid
         */

        public static boolean isValid(int zoom, int x, int y) {

            int maxNumberOfTiles = 2 << (zoom + 1);
            return 0 <= zoom && zoom <= 19
                    && x >= 0 && x <= maxNumberOfTiles
                    && y >= 0 && y <= maxNumberOfTiles;
        }

    }

    // path for the cache-disk
    private final Path cacheDiskPath;
    // url for the server name
    private final String tileServerName;
    // max number of elements in the memory cache
    private final int maxMemoryCacheCapacity = 100;

    // memory cache, instantiated as a LinkedHashMap
    private final Map<TileId, Image> memoryCache = new LinkedHashMap<>(maxMemoryCacheCapacity, 0.75f, true);


    /**
     * Constructor for tile manager.
     *
     * @param cacheDiskPath  path of the disk cache
     * @param tileServerName url of the server, where we request the tiles data whenever it is not yet downloaded.
     */

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
        image = this.memoryCache.get(tileId);
        if (Objects.isNull(image)) {
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

            this.addToCacheMemory(tileId, image);
        }
        return image;
    }

    /**
     * Adds an image to the cache memory. Whenever the latter's size exceed 100, we delete
     * the exceeding elements according to reverse access ordering.
     *
     * @param id    id of tile
     * @param image image of tile.
     */
    private void addToCacheMemory(TileId id, Image image) {
        if (this.memoryCache.size() == 100) {
            TileId firstId = this.memoryCache.keySet().iterator().next();
            this.memoryCache.remove(firstId);
        }

        this.memoryCache.put(id, image);
    }

    /**
     * Getter for memoryCache, used in testing
     *
     * @return Memory Cache containing the 100 most recently accessed images by the program.
     */
    public Map<TileId, Image> getMemoryCache() {
        return this.memoryCache;
    }
}