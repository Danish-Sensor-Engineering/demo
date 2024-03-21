package dse.explorer;

import dse.libods.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainController implements TelegramListener {

    private final static Logger log = LoggerFactory.getLogger(MainController.class);


    @FXML private Spinner<Integer> skip;
    @FXML private Spinner<Integer> points;

    @FXML private ChoiceBox<String> choiceSensorType;

    @FXML private ChoiceBox<String> choiceSensorSerialPort;

    @FXML private ChoiceBox<Integer> choiceSensorBaudRate;

    @FXML private Button btnStart;

    @FXML private Button btnStop;

    @FXML private LineChart<Number, Number> dataChart;
    @FXML private NumberAxis xAxis;

    @FXML private Label lastErrorMessage;

    @FXML private Label lastDistanceResult;

    @FXML private Label averageDistance;
    @FXML private Label minimumDistance;
    @FXML private Label maximumDistance;


    private final ObservableList<XYChart.Series<Number, Number>> observableList1 = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Series<Number, Number>> observableList2 = FXCollections.observableArrayList();
    private final XYChart.Series<Number, Number> numberSeries1 = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> numberSeries2 = new XYChart.Series<>();

    // DSE Sensor Library
    private final SerialSensor serialSensor = new SerialSensor();
    private final TestSensor testSensor = new TestSensor();

    private String selectedType;
    private String selectedPort;
    private Integer selectedBaud;
    private int counter = 0;


    @FXML public void initialize() {
        log.debug("initialize()");
        choiceSensorType.getItems().addAll("16bit", "18bit");
        choiceSensorBaudRate.getItems().addAll(38400, 115200);
        choiceSensorSerialPort.getItems().add("Test");
        choiceSensorSerialPort.getItems().addAll(SerialSensor.getSerialPorts());

        skip.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                skip.getEditor().setText(oldValue);
            }
        });

         points.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                points.getEditor().setText(oldValue);
            }
        });

        try {

            numberSeries1.setName("Distance");
            observableList1.add(numberSeries1);
            dataChart.getData().addAll(observableList1);

            numberSeries2.setName("Average");
            observableList2.add(numberSeries2);
            dataChart.getData().addAll(observableList2);

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

        if( selectedPort != "Test" && (selectedPort == null || selectedType == null || selectedBaud == null) ) {
            log.warn("onButtonStart() - options missing");
            return;
        }

        btnStart.setDisable(true);
        btnStop.setDisable(false);
        lastErrorMessage.setText("");

        if(selectedPort.equals("Test")) {
            testSensor.setTelegramHandler(new TelegramHandler16Bit());
            testSensor.start();
            testSensor.addEventListener(this);
        } else {
            switch (selectedType) {
                case "16bit" -> serialSensor.setTelegramHandler(new TelegramHandler16Bit());
                case "18bit" -> serialSensor.setTelegramHandler(new TelegramHandler18Bit());
                default -> log.warn("Unknown sensor type: " + selectedType);
            }

            serialSensor.interval = skip.getValue();
            serialSensor.openPort(selectedPort, selectedBaud);
            serialSensor.addEventListener(this);
        }
    }


    @FXML private void onButtonStop() {
        log.debug("onButtonStop()");

        if(selectedPort.equals("Test")) {
            testSensor.removeEventListener(this);
            testSensor.stop();
        } else {
            serialSensor.removeEventListener(this);
            serialSensor.closePort();
        }

        btnStart.setDisable(false);
        btnStop.setDisable(true);
    }


     public void onTelegramResultEvent(TelegramResultEvent event) {

        int measurement = event.getMeasurement();
        int average = event.getAverage();
        int minimum = event.getMinimum();
        int maximum = event.getMaximum();

        Platform.runLater(() -> {
            lastDistanceResult.setText(Integer.toString(measurement));
            averageDistance.setText(String.valueOf(average));
            minimumDistance.setText(String.valueOf(minimum));
            maximumDistance.setText(String.valueOf(maximum));

            numberSeries1.getData().add(new XYChart.Data<>(counter, measurement));
            numberSeries2.getData().add(new XYChart.Data<>(counter, average));

            if(numberSeries1.getData().size() > points.getValue()) {
                numberSeries1.getData().remove(0, 2);
            }

            if(numberSeries2.getData().size() > points.getValue()) {
                numberSeries2.getData().remove(0, 2);
            }

            counter++;
            if(counter > points.getValue()) {
                lastErrorMessage.setText("");
                xAxis.autoRangingProperty().set(false);
                xAxis.setUpperBound(counter);
                counter = 0;
            }

         });

     }


    public void onTelegramErrorEvent(TelegramErrorEvent event) {
        Platform.runLater(() -> {
            lastErrorMessage.setText(event.toString());
            System.err.println(event.toString());
        });
    }

}
