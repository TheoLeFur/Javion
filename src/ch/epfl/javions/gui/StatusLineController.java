package ch.epfl.javions.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;


/**
 * @author Theo Le Fur
 * SCIPER 363294
 * This class controls the status line in the final graphical interface. It will be displaying the number of visible
 * aircraft as well as the number of received messages.
 */
public final class StatusLineController {

    private final BorderPane pane;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;


    /**
     * Default constructor. Takes care of creating the status line that will lie between the map and the table.
     * Creates the two properties for visible aircraft and number of received messages.
     */

    public StatusLineController() {

        this.pane = new BorderPane();
        this.aircraftCountProperty = new SimpleIntegerProperty();
        this.messageCountProperty = new SimpleLongProperty();
        this.createSceneGraph();

    }

    /**
     * Method that creates the basic scene graph
     */

    private void createSceneGraph() {
        Text leftText = new Text();
        Text rightText = new Text();
        this.pane.setLeft(leftText);
        this.pane.setRight(rightText);
        leftText.textProperty().bind(this.aircraftCountProperty().map(acp -> "VISIBLE AIRCRAFT : " + acp.doubleValue()));
        rightText.textProperty().bind(this.messageCountProperty().map(acp -> "MESSAGES RECEIVED : " + acp.doubleValue()));
    }

    /**
     * Access the main border pane
     *
     * @return the main pane.
     */
    public BorderPane pane() {
        return this.pane;
    }

    /**
     * Get access to the aircraft count property, useful for making bindings
     *
     * @return integer property containing the number of visible aircraft
     */
    public IntegerProperty aircraftCountProperty() {
        return this.aircraftCountProperty;
    }

    /**
     * Get access to the message count property, useful for making bindings
     *
     * @return long property containing the number of received messages
     */
    public LongProperty messageCountProperty() {
        return this.messageCountProperty;
    }
}
