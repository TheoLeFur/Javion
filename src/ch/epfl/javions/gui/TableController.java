package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftRegistration;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * This class takes care of the display of the table of states of the aircraft.
 * It comes in conjunction with the map, whose logic is implemented in the AircraftController class.
 */
public final class TableController {

    private enum WIDTH {
        ICAO,
        ID,
        REGISTRATION,
        MODEL,
        TYPE,
        DESCRIPTION;


        private static int width(WIDTH width) {
            return switch (width) {
                case ICAO -> 60;
                case ID -> 70;
                case REGISTRATION -> 90;
                case MODEL -> 230;
                case TYPE -> 50;
                case DESCRIPTION -> 70;
            };
        }

        private static int getWidth(WIDTH width) {
            return width(width);
        }
    }

    private enum DECIMALS {
        LONGITUDE,
        LATITUDE,
        ALTITUDE,
        VELOCITY;

        private static int decimals(DECIMALS d) {
            return switch (d) {
                case LONGITUDE -> 4;
                case LATITUDE -> 4;
                case ALTITUDE -> 0;
                case VELOCITY -> 0;
            };
        }

        private static int getDecimals(DECIMALS d) {
            return decimals(d);
        }


    }


    private final double PREFERRED_WIDTH_NUMERIC = 85;
    private final String TABLE_STYLE_SHEET_PATH = "/table.css";
    private final Pane pane;
    private TableView<ObservableAircraftState> tableView;
    private final ObservableSet<ObservableAircraftState> observableSet;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftState;
    private Consumer<ObservableAircraftState> cs;

    /**
     * Instantiates a table controller.
     *
     * @param obsSet set of observable states passed into construction.
     */
    public TableController(ObservableSet<ObservableAircraftState> obsSet) {

        this.observableSet = obsSet;
        this.selectedAircraftState = new SimpleObjectProperty<>();

        this.pane = new Pane();
        TableView<ObservableAircraftState> tableView = buildSceneGraph();

        // add a listener on the set of observable states :

        this.observableSet.addListener(
                (SetChangeListener<ObservableAircraftState>) change -> {
                    tableView.getItems().add(change.getElementAdded());
                    tableView.getItems().remove(change.getElementRemoved());
                    tableView.sort();
                }
        );

    }

    /**
     * Access the main pane, in which we organise the display
     *
     * @return pane holding the table.
     */

    public Pane pane() {
        return this.pane;
    }

    /**
     * @param cs
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> cs) {
        cs.accept(this.selectedAircraftState.getValue());
    }

    /**
     * Builds the main scene graph.
     */
    private TableView<ObservableAircraftState> buildSceneGraph() {

        this.tableView = new javafx.scene.control.TableView<>();
        tableView.getStylesheets().add(TABLE_STYLE_SHEET_PATH);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setTableMenuButtonVisible(true);

        this.selectedAircraftState.addListener(
                (p, oldVal, newVal) -> {
                    tableView.getSelectionModel().select(newVal);
                    tableView.scrollTo(newVal);
                }
        );

        tableView.getSelectionModel()
                .selectedItemProperty()
                .addListener((p, oldVal, newVal) ->
                        this.selectedAircraftState.setValue(newVal));


        tableView.setOnMouseClicked(event -> {
            int clickCount = event.getClickCount();
            MouseButton button = event.getButton();
            if (clickCount == 2 && button.equals(MouseButton.PRIMARY)) {
                this.setOnDoubleClick(this.cs);
            }
        });

        this.createTextColumns();

        return tableView;

    }

    private void createTextColumns() {


        this.tableView.getColumns()
                .add(this.createTextualColumn(WIDTH.getWidth(WIDTH.ICAO),
                                f -> {
                                    ObservableValue<IcaoAddress> addressObservableValue = new ReadOnlyObjectWrapper<>(f.getValue().getIcaoAddress());
                                    return addressObservableValue.map(IcaoAddress::string);
                                }
                        )
                );

        this.tableView.getColumns()
                .add(this.createTextualColumn(WIDTH.getWidth(WIDTH.ID),
                                f -> f.getValue().callSignProperty().map(CallSign::string)
                        )
                );

        this.tableView.getColumns()
                .add(this.createTextualColumn(WIDTH.getWidth(WIDTH.REGISTRATION),
                        f -> {
                            ObservableValue<AircraftRegistration> registrationObservableValue = new ReadOnlyObjectWrapper<>(f.getValue().getRegistration());
                            return registrationObservableValue.map(AircraftRegistration::string);
                        }));

        this.tableView.getColumns()
                .add(this.createTextualColumn(WIDTH.getWidth(WIDTH.MODEL),
                        f -> new ReadOnlyObjectWrapper<>(f.getValue().getModel()))
                );

        this.tableView.getColumns()
                .add(this.createTextualColumn(WIDTH.getWidth(WIDTH.TYPE),
                        f -> {
                            ObservableValue<AircraftTypeDesignator> typeDesignatorObservableValue = new ReadOnlyObjectWrapper<>(f.getValue().getTypeDesignator());
                            return typeDesignatorObservableValue.map(AircraftTypeDesignator::string);
                        }));

        this.tableView.getColumns()
                .add(this.createTextualColumn(WIDTH.getWidth(WIDTH.DESCRIPTION),
                                f -> {
                                    ObservableValue<AircraftDescription> descriptionObservableValue = new ReadOnlyObjectWrapper<>(f.getValue().getDescription());
                                    return descriptionObservableValue.map(AircraftDescription::string);
                                }
                        )
                );

    }

    private void createNumericColumns() {


    }


    /**
     * Creates a column for string type values.
     *
     * @param width preferred width of the column.
     * @param value lambda associating the ObservableAircraftState with the String value representing it in the table.
     * @return string column
     */
    private TableColumn<ObservableAircraftState, String> createTextualColumn(
            int width,
            Callback<TableColumn.CellDataFeatures<ObservableAircraftState, String>, ObservableValue<String>> value) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>();
        column.setPrefWidth(width);
        column.setCellValueFactory(value);

        return column;
    }

    /**
     * Creates a numerical column, for messages holding number values.
     *
     * @param value          lambda associating state to number representing it in the table
     * @param decimalNumbers number of decimal numbers allowed in the numerical representation
     * @return numerical column
     */

    private TableColumn<ObservableAircraftState, String> createNumericalColumn(
            Callback<TableColumn.CellDataFeatures<ObservableAircraftState, String>, ObservableValue<String>> value,
            int decimalNumbers) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>();
        column.getStyleClass().add("numeric");
        column.setPrefWidth(PREFERRED_WIDTH_NUMERIC);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(decimalNumbers);

        column.setCellValueFactory(value);

        column.setComparator(
                (s1, s2) -> {
                    if (s1.isEmpty() || s2.isEmpty()) {
                        return s1.compareTo(s2);
                    } else {
                        try {
                            return Double.compare(nf.parse(s1).doubleValue(), nf.parse(s2).doubleValue());
                        } catch (ParseException e) {
                            System.out.println("String cannot be parsed");
                            throw new RuntimeException(e);
                        }
                    }
                }
        );


        return column;
    }


}
