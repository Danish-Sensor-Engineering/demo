package dse.explorer;

import dse.libsensor.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainController implements TelegramListener {

    private final static Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private Spinner<Integer> spinnerAvg;

    @FXML private ChoiceBox<String> choiceSensorType;

    @FXML private ChoiceBox<String> choiceSensorSerialPort;

    @FXML private ChoiceBox<Integer> choiceSensorBaudRate;

    @FXML private Button btnStart;

    @FXML private Button btnStop;

    @FXML private LineChart<Integer, Integer> dataChart;

    @FXML private Label lastErrorMessage;

    @FXML private Label lastDistanceResult;

    private final ObservableList<XYChart.Series<Integer, Integer>> observableList = FXCollections.observableArrayList();
    private final XYChart.Series<Integer, Integer> numberSeries = new XYChart.Series<>();

    // DSE Sensor Library
    private final SerialSensor sensor = new SerialSensor();
    private final int maxMeasurementElements = 100;
    private Integer measurementCounter = 0;

    private String selectedType;
    private String selectedPort;
    private Integer selectedBaud;


    @FXML public void initialize() {
        log.debug("initialize()");
        choiceSensorType.getItems().addAll("16bit", "18bit");
        choiceSensorBaudRate.getItems().addAll(38400, 115200);
        choiceSensorSerialPort.getItems().addAll(SerialSensor.getSerialPorts());

        spinnerAvg.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                spinnerAvg.getEditor().setText(oldValue);
            }
        });

        try {
            numberSeries.setName("Measurements");
            dataChart.setData(observableList);
            observableList.add(numberSeries);
        } catch (Exception ignored) {
        }
    }


    @FXML private void onSelectModel(ActionEvent e) {
        log.info(choiceSensorType.getSelectionModel().getSelectedItem());
        selectedType = choiceSensorType.getSelectionModel().getSelectedItem();
    }

    @FXML private void onSelectPort(ActionEvent e) {
        log.info(choiceSensorSerialPort.getSelectionModel().getSelectedItem());
        selectedPort = choiceSensorSerialPort.getSelectionModel().getSelectedItem();
    }

    @FXML private void onSelectBaud(ActionEvent e) {
        log.info(String.valueOf(choiceSensorBaudRate.getSelectionModel().getSelectedItem()));
        selectedBaud = choiceSensorBaudRate.getSelectionModel().getSelectedItem();
    }


    @FXML private void onButtonStart() {
        log.debug("onButtonStart()");

        if(selectedType == null || selectedPort == null || selectedBaud == null) {
            log.warn("onButtonStart() - options missing");
            return;
        }

        btnStart.setDisable(true);
        btnStop.setDisable(false);

        switch (selectedType) {
            case "16bit" -> sensor.setTelegramHandler(new TelegramHandler16Bit());
            case "18bit" -> sensor.setTelegramHandler(new TelegramHandler18Bit());
            default -> log.warn("Unknown sensor type: " + selectedType);
        }

        sensor.doAverageOver = spinnerAvg.getValue();
        sensor.openPort(selectedPort, selectedBaud);

        sensor.addEventListener(this);
    }


    @FXML private void onButtonStop() {
        log.debug("onButtonStop()");

        sensor.removeEventListener(this);
        sensor.closePort();

        btnStart.setDisable(false);
        btnStop.setDisable(true);
    }


     public void onTelegramResultEvent(TelegramResultEvent event) {

        int measurement = event.getMeasurement();
         Platform.runLater(() -> {
             lastDistanceResult.setText(Integer.toString(measurement));
             numberSeries.getData().add(new XYChart.Data<>(++measurementCounter, measurement));

             // Remove old elements from beginning of series
             int numberOfElements = numberSeries.getData().size();
             if(numberOfElements > maxMeasurementElements + 10) {
                 numberSeries.getData().remove(0, numberOfElements - maxMeasurementElements);
             }

             lastErrorMessage.setText("");
         });

     }


    public void onTelegramErrorEvent(TelegramErrorEvent event) {
        Platform.runLater(() -> {
            lastErrorMessage.setText(event.toString());
        });
    }

}
