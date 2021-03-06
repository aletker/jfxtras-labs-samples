package jfxtras.labs.samples;

import fxsampler.SampleBase;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.util.StringConverter;
import jfxtras.labs.internal.scene.control.skin.CalendarPickerControlSkin;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.CalendarPicker;
import jfxtras.labs.scene.layout.HBox;
import sun.java2d.SurfaceData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Observable;

public class CalendarPickerSample1 extends SampleBase
{
    public CalendarPickerSample1() {
        calendarPicker = new CalendarPicker();
    }
    final CalendarPicker calendarPicker;

    @Override
    public String getSampleName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getSampleDescription() {
        return "Basic CalendarPicker usage";
    }

    @Override
    public Node getPanel(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 30, 30, 30));

        root.getChildren().addAll(calendarPicker);

        return root;
    }

    @Override
    public Node getControlPanel() {
        // the result
        GridPane lGridPane = new GridPane();
        lGridPane.setVgap(2.0);
        lGridPane.setHgap(2.0);

        // setup the grid so all the labels will not grow, but the rest will
        ColumnConstraints lColumnConstraintsAlwaysGrow = new ColumnConstraints();
        lColumnConstraintsAlwaysGrow.setHgrow(Priority.ALWAYS);
        ColumnConstraints lColumnConstraintsNeverGrow = new ColumnConstraints();
        lColumnConstraintsNeverGrow.setHgrow(Priority.NEVER);
        lGridPane.getColumnConstraints().addAll(lColumnConstraintsNeverGrow, lColumnConstraintsAlwaysGrow);
        int lRowIdx = 0;

        // Mode
        {
            lGridPane.add(new Label("Mode"), new GridPane.C().row(lRowIdx).col(0));
            ChoiceBox<CalendarPicker.Mode> lChoiceBox = new ChoiceBox(FXCollections.observableArrayList(CalendarPicker.Mode.values()));
            lGridPane.add(lChoiceBox, new GridPane.C().row(lRowIdx).col(1));
            lChoiceBox.valueProperty().bindBidirectional(calendarPicker.modeProperty());
        }
        lRowIdx++;

        // Locale
        {
            lGridPane.add(new Label("Locale"), new GridPane.C().row(lRowIdx).col(0));
            ObservableList<Locale> lLocales = FXCollections.observableArrayList(Locale.getAvailableLocales());
            FXCollections.sort(lLocales,  (o1, o2) -> { return o1.toString().compareTo(o2.toString()); } );
            ComboBox<Locale> lComboBox = new ComboBox( lLocales );
            lComboBox.converterProperty().set(new StringConverter<Locale>() {
                @Override
                public String toString(Locale locale) {
                    return locale == null ? "-"  : locale.toString();
                }

                @Override
                public Locale fromString(String s) {
                    if ("-".equals(s)) return null;
                    return new Locale(s);
                }
            });
            lComboBox.setEditable(true);
            lGridPane.add(lComboBox, new GridPane.C().row(lRowIdx).col(1));
            lComboBox.valueProperty().bindBidirectional(calendarPicker.localeProperty());
        }
        lRowIdx++;

        // nullAllowed
        {
            Label lLabel = new Label("Null allowed");
            lLabel.setTooltip(new Tooltip("Is the control allowed to hold null (or have no calendar deselected)"));
            lGridPane.add(lLabel, new GridPane.C().row(lRowIdx).col(0));
            CheckBox lCheckBox = new CheckBox();
            lGridPane.add(lCheckBox, new GridPane.C().row(lRowIdx).col(1));
            lCheckBox.selectedProperty().bindBidirectional(calendarPicker.allowNullProperty());
        }
        lRowIdx++;

        // showTime
        {
            Label lLabel = new Label("Show time");
            lLabel.setTooltip(new Tooltip("Only in SINGLE mode"));
            lGridPane.add(lLabel, new GridPane.C().row(lRowIdx).col(0));
            CheckBox lCheckBox = new CheckBox();
            lGridPane.add(lCheckBox, new GridPane.C().row(lRowIdx).col(1));
            lCheckBox.selectedProperty().bindBidirectional(calendarPicker.showTimeProperty());
        }
        lRowIdx++;

        // showWeeknumbers
        {
            Label lLabel = new Label("Show weeknumbers");
            //lLabel.setTooltip(new Tooltip("Only in SINGLE mode"));
            lGridPane.add(lLabel, new GridPane.C().row(lRowIdx).col(0));
            final CheckBox lCheckBox = new CheckBox();
            lGridPane.add(lCheckBox, new GridPane.C().row(lRowIdx).col(1));
            lCheckBox.selectedProperty().addListener( (observable) -> {
                calendarPicker.setStyle( lCheckBox.isSelected() ? "-fxx-show-weeknumbers:YES;" : "-fxx-show-weeknumbers:NO;");
            });
            lCheckBox.setSelected(true);
        }
        lRowIdx++;

        // calendar
        {
            Label lLabel = new Label("Value");
            lLabel.setTooltip(new Tooltip("The currently selected value"));
            lGridPane.add(lLabel, new GridPane.C().row(lRowIdx).col(0));
            final CalendarTextField lCalendarTextField = new CalendarTextField();
            lCalendarTextField.setDisable(true);
            lGridPane.add(lCalendarTextField, new GridPane.C().row(lRowIdx).col(1));
            lCalendarTextField.calendarProperty().bindBidirectional(calendarPicker.calendarProperty());
            calendarPicker.showTimeProperty().addListener( (observable) -> {
                lCalendarTextField.setDateFormat( calendarPicker.getShowTime() ? SimpleDateFormat.getDateTimeInstance() : SimpleDateFormat.getDateInstance() );
            });
            lCalendarTextField.setDateFormat( calendarPicker.getShowTime() ? SimpleDateFormat.getDateTimeInstance() : SimpleDateFormat.getDateInstance() );
        }
        lRowIdx++;

        // calendars
        {
            Label lLabel = new Label("Values");
            lLabel.setTooltip(new Tooltip("All selected values"));
            lGridPane.add(lLabel, new GridPane.C().row(lRowIdx).col(0));
            final ListView lListView = new ListView();
            lListView.setItems(calendarPicker.calendars());
            lListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<java.util.Calendar>() {
                @Override
                public String toString(java.util.Calendar o) {
                    DateFormat lDateFormat = calendarPicker.getShowTime() ? SimpleDateFormat.getDateTimeInstance() : SimpleDateFormat.getDateInstance();
                    return o == null ? "" : lDateFormat.format(o.getTime());
                }

                @Override
                public java.util.Calendar fromString(String s) {
                    return null;  //never used
                }
            }));
            lGridPane.add(lListView, new GridPane.C().row(lRowIdx).col(1));
        }
        lRowIdx++;

        // done
        return lGridPane;
    }

    @Override
    public String getJavaDocURL() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void main(String[] args) {
        launch(args);
    }
}