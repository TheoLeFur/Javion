package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftRegistration;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

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

    private final String ICAO = "ICAO";
    private final String CALLSIGN = "CALL_SIGN";
    private final String REGISTRATION = "REGISTRATION";
    private final String MODEL = "MODEL";
    private final String TYPE = "TYPE";
    private final String DESCRIPTION = "DESCRIPTION";
    private final String LATITUDE = "LATITUDE";
    private final String LONGITUDE = "LONGITUDE";
    private final String ALTITUDE = "ALTITUDE";
    private final String VELOCITY = "VELOCITY";


    private final double PREFERRED_WIDTH_NUMERIC = 85;
    private final String TABLE_STYLE_SHEET_PATH = "/table.css";
    private final Pane pane;
    private final TableView<ObservableAircraftState> tableView;
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


        // add a listener on the set of observable states :
        this.tableView = new TableView<>();
        this.buildSceneGraph(this.tableView);

        this.observableSet.addListener(
                (SetChangeListener<ObservableAircraftState>) change -> {
                    this.tableView.getItems().add(change.getElementAdded());
                    this.tableView.getItems().remove(change.getElementRemoved());
                    this.tableView.sort();
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
        if (!Objects.isNull(cs)) cs.accept(this.selectedAircraftState.getValue());
    }

    /**
     * Builds the main scene graph.
     */
    private void buildSceneGraph(TableView<ObservableAircraftState> tv) {

        tv.getStylesheets().add(TABLE_STYLE_SHEET_PATH);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tv.setTableMenuButtonVisible(true);
        this.pane.getChildren().add(tv);

        this.selectedAircraftState.addListener(
                (p, oldVal, newVal) -> {
                    tv.getSelectionModel().select(newVal);
                    tv.scrollTo(newVal);
                }
        );

        tv.getSelectionModel()
                .selectedItemProperty()
                .addListener((p, oldVal, newVal) ->
                        this.selectedAircraftState.setValue(newVal));


        tv.setOnMouseClicked(event -> {
            int clickCount = event.getClickCount();
            MouseButton button = event.getButton();
            if (clickCount == 2 && button.equals(MouseButton.PRIMARY)) {
                System.out.println("good");
                this.setOnDoubleClick(this.cs);
            }
        });

        // Creates textual and numerical columns.
        this.createTextColumns(tv);
        this.createNumericColumns(tv);

    }

    private void createTextColumns(TableView<ObservableAircraftState> tv) {


        tv.getColumns().addAll(List.of(
                        this.createTextualColumn(
                                WIDTH.getWidth(WIDTH.ICAO),
                                ICAO,
                                f -> new ReadOnlyObjectWrapper<>(f.getValue().getIcaoAddress()).map(IcaoAddress::string)
                        ),
                        this.createTextualColumn(
                                WIDTH.getWidth(WIDTH.ID),
                                CALLSIGN,
                                f -> f.getValue().callSignProperty().map(CallSign::string))
                        ,
                        this.createTextualColumn(
                                WIDTH.getWidth(WIDTH.REGISTRATION),
                                REGISTRATION,
                                f -> new ReadOnlyObjectWrapper<>(f.getValue().getRegistration()).map(AircraftRegistration::string)
                        )
                        ,
                        this.createTextualColumn(
                                WIDTH.getWidth(WIDTH.MODEL),
                                MODEL,
                                f -> new ReadOnlyObjectWrapper<>(f.getValue().getModel())
                        )
                        ,
                        this.createTextualColumn(
                                WIDTH.getWidth(WIDTH.TYPE),
                                TYPE,
                                f -> new ReadOnlyObjectWrapper<>(f.getValue().getTypeDesignator()).map(AircraftTypeDesignator::string)
                        )
                        ,
                        this.createTextualColumn(
                                WIDTH.getWidth(WIDTH.DESCRIPTION),
                                DESCRIPTION,
                                f -> new ReadOnlyObjectWrapper<>(f.getValue().getDescription()).map(AircraftDescription::string)


                        )
                )
        );

    }


    private void createNumericColumns(TableView<ObservableAircraftState> tv) {

        tv.getColumns().addAll(List.of(
                        this.createNumericalColumn(LATITUDE,
                                f -> new SimpleDoubleProperty(f.getValue().getPosition().latitude()),
                                4,
                                Units.Angle.DEGREE
                        ),
                        this.createNumericalColumn(LONGITUDE,
                                f -> new SimpleDoubleProperty(f.getValue().getPosition().longitude()),
                                4,
                                Units.Angle.DEGREE
                        ),
                        this.createNumericalColumn(ALTITUDE,
                                f -> f.getValue().altitudeProperty(),
                                0,
                                Units.Length.METER
                        ),
                        this.createNumericalColumn(VELOCITY,
                                f -> f.getValue().velocityProperty(),
                                0,
                                Units.Speed.KILOMETER_PER_HOUR
                        )
                )
        );


    }


    /**
     * Creates a column for string type values.
     *
     * @param width preferred width of the column.
     * @return string column
     */
    private TableColumn<ObservableAircraftState, String> createTextualColumn(
            int width,
            String title,
            Function<TableColumn.CellDataFeatures<ObservableAircraftState, String>, ObservableValue<String>> map) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(map::apply);

        return column;
    }

    /**
     * Creates a numerical column, for messages holding number values.
     *
     * @return numerical column
     */

    private TableColumn<ObservableAircraftState, String> createNumericalColumn(
            String title,
            Function<TableColumn.CellDataFeatures<ObservableAircraftState, String>, ReadOnlyDoubleProperty> map,
            int decimals,
            double unit
    ) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.getStyleClass().add("numeric");
        column.setPrefWidth(PREFERRED_WIDTH_NUMERIC);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(decimals);

        column.setCellValueFactory(
                f -> {
                    ReadOnlyDoubleProperty value = map.apply(f);
                    return new ReadOnlyObjectWrapper<>(nf.format(Units.convertTo(value.getValue(), unit)));
                }
        );


        column.setComparator((s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) {
                return s1.compareTo(s2);
            } else {
                try {
                    return Double.compare(nf.parse(s1).doubleValue(), nf.parse(s2).doubleValue());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        return column;
    }


}
