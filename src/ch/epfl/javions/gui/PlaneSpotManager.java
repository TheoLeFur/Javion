package ch.epfl.javions.gui;


import ch.epfl.javions.aircraft.AircraftRegistration;
import javafx.scene.image.Image;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;


public final class PlaneSpotManager {

    public static final class FileToHashMap {

        public static Map<String, String> jsonToMap(String fileName) {
            return null;
        }
    }

    private final Path cacheDiskPath;
    private final String serverName;
    private final int MAX_CACHE_CAPACITY = 100;
    private final Map<AircraftRegistration, Image> memoryCache;

    public PlaneSpotManager(Path cacheDiskPath, String serverName) {
        this.memoryCache = new LinkedHashMap<>(MAX_CACHE_CAPACITY, 0.75f, true);
        this.cacheDiskPath = cacheDiskPath;
        this.serverName = serverName;
    }

    public Image imageForRegistrationAt(AircraftRegistration reg) throws IOException {
        byte[] buffer;
        Image img = this.memoryCache.get(reg);

        if (img == null) {
            String path = "/" + reg.string();
            Path imgPath = Path.of(this.cacheDiskPath.toString() + path);
            if (Files.exists(imgPath))
                img = new Image(new FileInputStream(imgPath.toFile()));
            else {
                URL u = new URL("https://" + this.serverName + path);
                URLConnection c = u.openConnection();
                c.setRequestProperty("User-Agent", "Javions");

                try (InputStream i = c.getInputStream()) {
                    buffer = i.readAllBytes();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                    img = new Image(byteArrayInputStream);
                }

                Files.createDirectories(Path.of(this.cacheDiskPath.toUri()));
                try (OutputStream o = new FileOutputStream(imgPath.toString())) {
                    o.write(buffer);
                }


            }
            this.addToCacheMemory(reg, img);
        }
        return img;
    }

    private void addToCacheMemory(AircraftRegistration reg, Image image) {
        if (this.memoryCache.size() == 100) {
            AircraftRegistration firstReg = this.memoryCache.keySet().iterator().next();
            this.memoryCache.remove(firstReg);
        }
        this.memoryCache.put(reg, image);
    }

}
