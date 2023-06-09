package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * @author Theo Le Fur
 * SCIPER : 363294
 * Main application threads
 */
public class Main extends Application {

    // conversion factor from ns to ms
    private static final double NS_TO_MILLIS = 1_000_000d;

    // One second in nanoseconds
    private static final long SECOND_NS = 1_000_000_000L;

    // Minimal preferred width for the display
    private static final int MIN_WIDTH = 800;

    // Minimal preferred height for the display
    private static final int MIN_HEIGHT = 600;

    // name of the file containing the messages
    private static final String MESSAGE_FILE_NAME = "/aircraft.zip";

    // name of te dir where we store tiles queried from the open street map server
    private static final String DISK_CACHE_NAME = "tile-cache";

    // name of the server from which tiles are queried
    private static final String TILE_SERVER_NAME = "tile.openstreetmap.org";


    /**
     * Launches the main application.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL u = getClass().getResource(MESSAGE_FILE_NAME);
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase db = new AircraftDatabase(p.toString());


        Path tileCache = Path.of(DISK_CACHE_NAME);

        TileManager tm = new TileManager(tileCache, TILE_SERVER_NAME);
        AircraftStateManager asm = new AircraftStateManager(db);


        // instantiate the main components of the scene graph
        MapParameters mp = new MapParameters(8, 33530, 23070);
        StatusLineController slc = new StatusLineController();
        BaseMapController bmc = new BaseMapController(tm, mp);
        ObjectProperty<ObservableAircraftState> selectedAircraft = new SimpleObjectProperty<>();
        AircraftController ac = new AircraftController(mp, asm.states(), selectedAircraft);
        AircraftTableController tc = new AircraftTableController(
                asm.states(),
                selectedAircraft
        );

        // define the consumer in the table controller
        tc.setOnDoubleClick(c -> bmc.centerOn(c.getPosition()));

        SplitPane mainPane = new SplitPane();
        mainPane.setOrientation(Orientation.VERTICAL);
        this.createSceneGraph(mainPane, bmc, ac, tc, slc);

        // handle dimensions
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.show();

        slc.aircraftCountProperty().bind(Bindings.size(asm.states()));


        List<String> params = getParameters().getRaw();
        ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();

        // define the concurrent message thread
        Thread messageAccumulationThread = new Thread(() -> {
            if (params.isEmpty()) {
                try {
                    this.demodulateMessages(messageQueue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    this.readMessagesFromFile(params.get(0), messageQueue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        messageAccumulationThread.setDaemon(true);
        messageAccumulationThread.start();

        new AnimationTimer() {
            private long prevMethodCallTimeStamp;

            @Override
            public void handle(long now) {
                try {
                    while (!messageQueue.isEmpty()) {
                        slc.messageCountProperty().setValue(slc.messageCountProperty().getValue() + 1);
                        Message m = messageQueue.remove();
                        if (m != null) asm.updateWithMessage(m);
                        if (now - prevMethodCallTimeStamp > SECOND_NS) {
                            asm.purge();
                            this.prevMethodCallTimeStamp = now;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }


    /**
     * This method demodulates messages received from radio using an ADS-B demodulator. It places the demodulated messages
     * in a queue.
     *
     * @param messageQueue queue that stores the decoded messages.
     * @throws IOException whenever error reading the System.in stream occurs
     */
    public void demodulateMessages(ConcurrentLinkedQueue<Message> messageQueue) throws IOException {

        try (InputStream s = System.in) {
            AdsbDemodulator adm = new AdsbDemodulator(s);
            while (true) {
                RawMessage nextMessage = adm.nextMessage();
                if (nextMessage != null) {
                    Message m = MessageParser.parse(nextMessage);
                    if (m != null)
                        messageQueue.add(m);
                }
            }
        }
    }


    /**
     * Method for reading messages from file. The thread places the messages in the queue according to their timestamp, putting
     * execution to sleep if necessary.
     *
     * @param fileName     name of file
     * @param messageQueue queue that stores messages from file.
     * @throws IOException whenever error while reading the input file stream occurs
     */


    public void readMessagesFromFile(String fileName, ConcurrentLinkedQueue<Message> messageQueue) throws IOException {


        try (DataInputStream s = new DataInputStream((new BufferedInputStream(new FileInputStream(Objects.requireNonNull(getClass().getResource(fileName)).getFile()))))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            long start = System.nanoTime();

            while (true) {
                long timeStampNs = s.readLong();
                long currentTime = System.nanoTime();

                Thread.sleep((long) (
                        Math.max(0, (timeStampNs - (currentTime - start))) / NS_TO_MILLIS));

                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage rm = RawMessage.of(timeStampNs, bytes);
                Message m;
                if (rm != null) {
                    m = MessageParser.parse(rm);
                    if (m != null)
                        messageQueue.add(m);
                }
            }
        } catch (InterruptedException e) {
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


    public void createSceneGraph(
            SplitPane mainPane,
            BaseMapController bmc,
            AircraftController ac,
            AircraftTableController tc,
            StatusLineController slc) {

        StackPane aircraftPane = new StackPane(bmc.pane(), ac.pane());
        BorderPane metaPane = new BorderPane();
        metaPane.setCenter(tc.pane());
        metaPane.setTop(slc.pane());
        mainPane.getItems().add(aircraftPane);
        mainPane.getItems().add(metaPane);

    }


}
