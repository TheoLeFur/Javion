package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TableControllerTest extends Application {

    public static final String message_dir = "/Users/theolefur/Javion/resources/messages_20230318_0915.bin";
    public static final String aircraft_dir = "/Users/theolefur/Javion/resources/aircraft.zip";

    public static void main(String[] args) {
        launch(args);
    }

    static List<RawMessage> readAllMessages(String fileName) throws IOException {
        List<RawMessage> rm = new ArrayList<>();
        AircraftStateManager stateManager = new AircraftStateManager(new AircraftDatabase(aircraft_dir));
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                rm.add(rawMessage);
                Message m = MessageParser.parse(rawMessage);
            }
        } catch (EOFException e) { /* nothing to do */ } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rm;

    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Path tileCache = Path.of("tile-cache");

        URL dbUrl = getClass().getResource("/aircraft.zip");
        assert dbUrl != null;
        String f = Path.of(dbUrl.toURI()).toString();
        var db = new AircraftDatabase(f);

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> selectedAircraft = new SimpleObjectProperty<>();
        TableController tab = new TableController(asm.states(), selectedAircraft, null);
        primaryStage.setScene(new Scene(tab.pane()));
        primaryStage.show();
        var mi = readAllMessages(message_dir).iterator();

        new AnimationTimer() {
            @Override
            public void handle(long now) {

                try {
                    for (int i = 0; i < 10; i++) {
                        if (mi.hasNext()) {
                            Message m = MessageParser.parse(mi.next());
                            if (m != null) asm.updateWithMessage(m);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

            }
        }.start();

    }
}