package ch.epfl.javions;


import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import ch.epfl.javions.gui.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Main extends Application {

    private final long SECOND_NS = 1_000_000_000L;


    /**
     * Launches the javaFX application
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase db = new AircraftDatabase(p.toString());

        Path tileCache = Path.of("tile-cache");

        TileManager tm = new TileManager(tileCache, "tile.openstreetmap.org");
        AircraftStateManager asm = new AircraftStateManager(db);
        MapParameters mp = new MapParameters(8, 33530, 23070);

        StatusLineController slc = new StatusLineController();
        BaseMapController bmc = new BaseMapController(tm, mp);

        ObjectProperty<ObservableAircraftState> selectedAircraft = new SimpleObjectProperty<>();
        AircraftController ac = new AircraftController(mp, asm.states(), selectedAircraft);
        TableController tc = new TableController((asm.states()), selectedAircraft, c -> bmc.centerOn(c.getPosition()));


        SplitPane mainPane = new SplitPane();
        mainPane.setOrientation(Orientation.VERTICAL);
        this.createSceneGraph(mainPane, bmc, ac, tc, slc);

        primaryStage.setScene(new Scene(mainPane));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        slc.aircraftCountProperty().bind(Bindings.size(asm.states()));


        List<String> params = getParameters().getRaw();
        ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();


        Thread messageAccumulationThread = new Thread(() -> {
            if (params.isEmpty()) {
                this.demodulateMessages(messageQueue);
            } else {
                this.readMessagesFromFile(params.get(0), messageQueue);
            }

        });
        messageAccumulationThread.setDaemon(true);
        messageAccumulationThread.start();


        new AnimationTimer() {

            private long prevMethodCallTimeStamp;

            @Override
            public void handle(long now) {

                try {
                    for (int i = 0; i < 10; i++) {
                        if (!messageQueue.isEmpty()) {
                            slc.messageCountProperty().setValue(slc.messageCountProperty().getValue() + 1);
                            Message m = messageQueue.remove();
                            if (!Objects.isNull(m)) asm.updateWithMessage(m);
                            if (now - prevMethodCallTimeStamp > SECOND_NS) {
                                asm.purge();
                                this.prevMethodCallTimeStamp = now;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

    }


    /**
     * This method demodulates messages received from antenna using an ADSB-Demodulator. It placed the demodulated messages
     * in a queue
     *
     * @param messageQueue queue that stores the decoded messages.
     */
    private void demodulateMessages(ConcurrentLinkedQueue<Message> messageQueue) {

        try (InputStream s = System.in) {
            AdsbDemodulator adm = new AdsbDemodulator(s);
            while (true) {
                RawMessage nextMessage = adm.nextMessage();
                assert nextMessage != null;
                Message m = MessageParser.parse(nextMessage);
                messageQueue.add(m);
            }
        } catch (EOFException e) {
            //ignore
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Method for reading messages from file. The thread places the messages in the queue according to their timestamp, putting
     * execution to sleep if necessary.
     *
     * @param fileName     name of file
     * @param messageQueue queue that stores messages from file.
     */


    private void readMessagesFromFile(String fileName, ConcurrentLinkedQueue<Message> messageQueue) {


        try (DataInputStream s = new DataInputStream((new BufferedInputStream(new FileInputStream(Objects.requireNonNull(getClass().getResource(fileName)).getFile()))))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            long start = System.nanoTime();

            while (true) {

                long timeStampNs = s.readLong();
                long currentTime = System.nanoTime();
                Thread.sleep((long) (Math.max(0, (timeStampNs - (currentTime - start))) / 1000000d));
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage rm = RawMessage.of(timeStampNs, bytes);
                Message m;
                if (rm != null) {
                    m = MessageParser.parse(rm);
                    if (m != null) {
                        messageQueue.add(m);

                    }
                }

            }


        } catch (
                EOFException e) {
            // do nothing
        } catch (
                IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Creates the final scene graph
     *
     * @param mainPane main  pane
     * @param bmc      base map controller
     * @param ac       aircraft controller
     * @param tc       table controller
     * @param slc      status line controller
     */

    private void createSceneGraph(SplitPane mainPane, BaseMapController bmc, AircraftController ac, TableController tc, StatusLineController slc) {

        StackPane aircraftPane = new StackPane(bmc.pane(), ac.pane());
        BorderPane metaPane = new BorderPane();
        metaPane.setCenter(tc.pane());
        metaPane.setTop(slc.pane());
        mainPane.getItems().add(aircraftPane);
        mainPane.getItems().add(metaPane);

    }


}
