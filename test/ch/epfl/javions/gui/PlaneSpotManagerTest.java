package ch.epfl.javions.gui;

import ch.epfl.javions.aircraft.AircraftRegistration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaneSpotManagerTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        final AircraftRegistration ag = new AircraftRegistration("D-ACNH");
        new PlaneSpotManager(Path.of("plane-cache"), "api.planespotters.net/pub/photos/reg").imageForRegistrationAt(ag);
        Platform.exit();
    }

}