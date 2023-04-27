package ch.epfl.javions.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.nio.file.Path;

public final class TestTileManager extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new TileManager(Path.of("tile-cache"),
                "tile.openstreetmap.org")
                .imageForTileAt(new TileManager.TileId(17, 67822, 46357));

        for(int i = 0; i < 5; ++i) {
            new TileManager(Path.of("tile-cache"),
                    "tile.openstreetmap.org")
                    .imageForTileAt(new TileManager.TileId(17, 67822, i));
        }

        Platform.exit();
    }
}