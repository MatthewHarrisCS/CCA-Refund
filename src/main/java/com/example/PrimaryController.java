package com.example;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.text.DecimalFormat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class PrimaryController implements Initializable{

    @FXML private TextField minRef;
    @FXML private TextField procFee;
    @FXML private TextField redRate;
    @FXML private TextField fullRef;
    @FXML private Label copyStatus;
    @FXML private ToggleGroup monthOrYear;
    @FXML private TableView<Data> table;
    @FXML private TableColumn<Data, Integer> yr;
    @FXML private TableColumn<Data, Integer> month;
    @FXML private TableColumn<Data, Double> rla;
    private DecimalFormat decimal = new DecimalFormat("0.000");

    ObservableList<Data> list = FXCollections.observableArrayList();

    // Initialize(): initialize the table
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Code to build the columns of the table
        yr.setCellValueFactory(new PropertyValueFactory<Data, Integer>("yr"));
        month.setCellValueFactory(new PropertyValueFactory<Data, Integer>("month"));
        rla.setCellValueFactory(new PropertyValueFactory<Data, Double>("rla"));

        // Calls function to calculate the rows of the table
        updateRla();

        // Connects the calculated rows to the table in the UI
        table.setItems(list);
    }

    // onTab(): recalcuates the RLAs whenever the user tabs
    @FXML
    private void onTab(KeyEvent event) {
        if(event.getCode().equals(KeyCode.TAB)) {
            updateVals();
        }
    }

    // updateRla(): function to calculate the Refund Liability Assumptions
    //              for the table over the next 100 months
    private void updateRla() {

        // Clears the current data (if recalculating RLAs)
        list.clear();

        Double start;
        Integer addYr;
        Integer addMonth;
        Integer decrement;
        Double addRLA;
        Double currRef;
        Double currFee;
        Double currRed;
        Double override;
        Double prevStart;
        Double prevRLA;
        boolean isMonthly;

        // Setting variables to the values input by the user
        currRef = Double.parseDouble(minRef.getText());
        currFee = Double.parseDouble(procFee.getText());
        currRed = Double.parseDouble(redRate.getText());
        override = Double.parseDouble(fullRef.getText());

        // Setting the initial values for Start% and RLA to 100%
        prevStart = 100.0;
        prevRLA = 100.0;

        // Getting the radio button value to determine month or year
        RadioButton selected = (RadioButton) monthOrYear.getSelectedToggle();
        isMonthly = ("Monthly".equals(selected.getText()));

            // Iterates through the first 100 months (9 years 4 months)
            for (Integer j = 0; j < 100; j++) {

                // First calculates the next month and year
                addMonth = j + 1;
                addYr = (j / 12) + 1;

                // Sets the decrement value to month or year
                if(isMonthly) {
                    decrement = addMonth;
                } else {
                    decrement = addYr;
                }

                // Starting % calculated by finding the maximum value between
                // the current minimum refund % and the calculated minimum
                // (100% - processing fee - reduction % over X months/years)
                start = Double.max(currRef, 100-currFee-(decrement*currRed));

                // Refund Liability Assumption calculated by finding the
                // value (if non-zero, otherwize returns 0%) of the previous
                // RLA (Previous RLA - diff between previous/current Start% 
                // * 1 / (1 - minimum refund %))
                addRLA = Double.max(0, prevRLA-((prevStart - start)*(1/(1-(currRef*0.01)))));
                
                // Stores the current Start% and RLA for calculating the next month
                prevStart = start;
                prevRLA = addRLA;

                if(override > 0) {
                    addRLA = 100.0;
                    override--;
                }

                // Adds current entry to the table 
                list.add(new Data(addYr, addMonth, decimal.format(addRLA)));
            }

        // When finished, set copy status to notify of new calculations
        copyStatus.setText("");
    }


    // checkVal(TextField source): function to correct bad values
    //                           : passed through the textfields
    private void checkVal(TextField source) {

        double newVal;

        // Parses the value from the textfield; If the value is 
        // not a valid number, sets it to 0 and breaks
        try {
            newVal = Double.parseDouble(source.getText());
        }
        catch(NumberFormatException e) {
            newVal = 0.0;
            source.setText(Integer.toString(0));
            return;
        }

        // Keeps the value within the range of 0-100
        if (newVal > 100) {
            source.setText(Integer.toString(100));
        } else if (newVal < 0) {
            source.setText(Integer.toString(0));
        }
    }


    // updateVals(): function to check the new textfield values
    //             : and update the refund liability assumptions
    @FXML 
    private void updateVals() {

        // Gets the new values from their respective textbox
        checkVal(minRef);
        checkVal(procFee);
        checkVal(redRate);
        checkVal(fullRef);

        // Updates the table with new RLA values
        updateRla();
    }

    // copyRla(); copies the computed RLA values into the clipboard
    @FXML private void copyRla() {

        updateVals();

        ArrayList<String> rlaList = new ArrayList<String>();
        String rlaStr = "";
        Clipboard clip = Clipboard.getSystemClipboard();
        ClipboardContent x = new ClipboardContent();

        // Gets each of the RLA values from the table column
        list.forEach((i) -> rlaList.add(Double.toString(i.getRla())));
        
        // Adds each of the RLA values to a string with linebreaks
        for (int i = 0; i < rlaList.size(); i++) {
            rlaStr = rlaStr.concat(rlaList.get(i)).concat("\n");
        }
        
        // Adds the RLA value string to the computer's clipboard
        x.putString(rlaStr);
        clip.setContent(x);
        copyStatus.setText("Copied to clipboard");
    }
}