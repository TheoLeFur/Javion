package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * @author Theo Le Fur
 * SCIPER = 363294
 * This class takes care of the display of the table of states of the aircraft.
 * It comes in conjunction with the map, whose logic is implemented in the AircraftController class.
 */


public final class AircraftTableController {


    /**
     * Stores the widths of various columns.
     */

    private enum WIDTH {
        ICAO, ID, REGISTRATION, MODEL, TYPE, DESCRIPTION, NUMERIC;

        /**
         * get width from enum instance
         *
         * @param width enum instance
         * @return width of column of the same name
         */
        private static int getWidth(WIDTH width) {
            return switch (width) {
                case ICAO -> 60;
                case ID, DESCRIPTION -> 70;
                case REGISTRATION -> 90;
                case MODEL -> 230;
                case TYPE -> 50;
                case NUMERIC -> 85;
            };
        }
    }


    private final String TABLE_STYLE_SHEET_PATH = "/table.css";
    private final boolean TABLE_MENU_BUTTON_VISIBLE = true;
    private final BorderPane pane;
    private final TableView<ObservableAircraftState> tableView;
    private final ObservableSet<ObservableAircraftState> observableSet;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private Consumer<ObservableAircraftState> cs;

    /**
     * Instantiates a table controller
     * @param obsSet set of observable aircraft
     * @param selectedAircraft selected aircraft
     * @param cs consumer accepting the value of the selected aircraft when double-clicked on
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> obsSet, ObjectProperty<ObservableAircraftState> selectedAircraft) {

        this.observableSet = obsSet;
        this.selectedAircraft = selectedAircraft;

        this.pane = new BorderPane();


        // add a listener on the set of observable states :

        this.tableView = new TableView<>();
        this.createSceneGraph(this.tableView);

        this.observableSet.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            this.tableView.getItems().add(change.getElementAdded());
            this.tableView.getItems().remove(change.getElementRemoved());
            if (change.wasAdded()) this.tableView.sort();
        });
    }

    /**
     * Access the main pane, in which we organise the display
     *
     * @return pane holding the table.
     */

    public BorderPane pane() {
        return this.pane;
    }

    /**
     * @param cs consumer value.
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> cs) {
        if (cs != null) this.cs = cs;
    }

    /**
     * Builds the main scene graph
     *
     * @param tv table view
     */
    private void createSceneGraph(TableView<ObservableAircraftState> tv) {

        tv.getStylesheets().add(TABLE_STYLE_SHEET_PATH);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tv.setTableMenuButtonVisible(TABLE_MENU_BUTTON_VISIBLE);
        this.pane.setCenter(tv);

        this.selectedAircraft.addListener((p, oldVal, newVal) -> {
            tv.getSelectionModel().select(newVal);
            tv.scrollTo(newVal);
        });

        tv.getSelectionModel().selectedItemProperty().addListener(
                (p, oldVal, newVal) -> this.selectedAircraft.setValue(newVal));

        tv.setOnMouseClicked(event -> {
            int clickCount = event.getClickCount();
            MouseButton button = event.getButton();
            if (clickCount == 2 && button.equals(MouseButton.PRIMARY)) {
                this.setOnDoubleClick(this.cs);
                this.cs.accept(this.selectedAircraft.getValue());
            }
        });
        // Creates textual and numerical columns.
        this.createColumns(tv);
    }

    private void createColumns(TableView<ObservableAircraftState> tv) {
        this.createTextColumns(tv);
        this.createNumericColumns(tv);
    }

    /**
     * Handles teh creation of all the textual values
     *
     * @param tv table view
     */
    private void createTextColumns(TableView<ObservableAircraftState> tv) {
        tv.getColumns().addAll(List.of(
                this.createTextualColumn(WIDTH.getWidth(WIDTH.ICAO), "ICAO",
                        f -> new ReadOnlyObjectWrapper<>(f.getValue()).map(e -> e.getIcaoAddress().string())),
                this.createTextualColumn(WIDTH.getWidth(WIDTH.ID), "CALL SIGN",
                        f -> f.getValue().callSignProperty().map(CallSign::string)),
                this.createTextualColumn(WIDTH.getWidth(WIDTH.REGISTRATION), "REGISTRATION",
                        f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData()).map(e -> e.registration().string())),
                this.createTextualColumn(WIDTH.getWidth(WIDTH.MODEL), "MODEL",
                        f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData()).map(AircraftData::model)),
                this.createTextualColumn(WIDTH.getWidth(WIDTH.TYPE), "TYPE",
                        f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData()).map(e -> e.typeDesignator().string())),
                this.createTextualColumn(WIDTH.getWidth(WIDTH.DESCRIPTION), "DESCRIPTION",
                        f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData()).map(e -> e.description().string())
                )));

    }

    /**
     * Handles the creation of all the numerical columns in the table
     *
     * @param tv table view
     */
    private void createNumericColumns(TableView<ObservableAircraftState> tv) {
        tv.getColumns().addAll(List.of(
                this.createNumericalColumn(
                        "LATITUDE",
                        f -> f.getValue().positionProperty().map(GeoPos::latitude),
                        4, Units.Angle.DEGREE),
                this.createNumericalColumn("LONGITUDE", f -> f.getValue().positionProperty().map(GeoPos::longitude),
                        4, Units.Angle.DEGREE),
                this.createNumericalColumn("ALTITUDE",
                        f -> f.getValue().altitudeProperty().map(Number::doubleValue), 0, Units.Length.METER),
                this.createNumericalColumn("VELOCITY",
                        f -> f.getValue().velocityProperty().map(Number::doubleValue), 0, Units.Speed.KILOMETER_PER_HOUR)));


    }


    /**
     * Creates a textual column.
     *
     * @param width width, depending on the size of the string the property accounts for
     * @param title title of the column
     * @param map   function extracting values of the property
     * @return string column
     */
    private TableColumn<ObservableAircraftState, String> createTextualColumn(
            int width,
            String title,
            Function<TableColumn.CellDataFeatures<ObservableAircraftState, String>,
                    ObservableValue<String>> map) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(map::apply);
        return column;
    }

    /**
     * Creates numerical column for the table view
     *
     * @param title    title
     * @param map      map extracting necessary value from properties
     * @param decimals number of decimals desired in the decimal representation
     * @param unit     unit of the extracted value
     * @return numerical column
     */

    private TableColumn<ObservableAircraftState, String> createNumericalColumn(
            String title,
            Function<TableColumn.CellDataFeatures<ObservableAircraftState, String>,
                    ObservableValue<Double>> map,
            int decimals,
            double unit) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.getStyleClass().add("numeric");
        column.setPrefWidth(WIDTH.getWidth(WIDTH.NUMERIC));

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(decimals);

        column.setCellValueFactory(f -> map.apply(f).map(v -> nf.format(Units.convertTo(v, unit))));

        column.setComparator((s1, s2) -> {
            try {
                return (s1.isEmpty() || s2.isEmpty())
                        ? s1.compareTo(s2)
                        : Double.compare(nf.parse(s1).doubleValue(), nf.parse(s2).doubleValue());
            } catch (ParseException e) {
                System.out.println("Error : string cannot be parsed");
                throw new RuntimeException(e);
            }
        });
        return column;

    }


}
