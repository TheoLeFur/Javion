package ch.epfl.javions.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class TestTileManager extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new TileManager(Path.of("tile-cache"),
                "tile.openstreetmap.org")
                .imageForTileAt(new TileManager.TileId(17, 67927, 46357));

        Platform.exit();
    }

    @Test
    void testImageForFileAt() throws IOException {

        TileManager manager = new TileManager(
                Path.of("title-cache"),
                "tile.openstreetmap.org"
        );

        int max_capacity = 100;

        for (int i = 0 ; i < max_capacity + 10 ; i++){
            manager.imageForTileAt(new TileManager.TileId(10, 1, i));
        }

        Map<TileManager.TileId, Image> memoryCache = manager.getMemoryCache();
        System.out.println(memoryCache.size());

        assertNull(memoryCache.get(new TileManager.TileId(10, 1, 1)));

    }


}