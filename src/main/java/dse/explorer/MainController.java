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

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Flow;


public class MainController implements Flow.Subscriber<Integer> {

    private final static Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private Spinner<Integer> avgMeasurement;
    @FXML private Spinner<Integer> avgDisplay;
    @FXML private Spinner<Integer> points;

    @FXML private ChoiceBox<String> choiceSensorType;
    @FXML private ChoiceBox<String> choiceSensorSerialPort;
    @FXML private ChoiceBox<Integer> choiceSensorBaudRate;

    @FXML private Button btnStart;
    @FXML private Button btnStop;

    @FXML private LineChart<Number, Number> dataChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private Label lastErrorMessage;
    @FXML private Label lastDistanceResult;
    @FXML private Label averageDistance;
    @FXML private Label minimumDistance;
    @FXML private Label maximumDistance;
    @FXML private Label frequencyLabel;

    private final ObservableList<XYChart.Series<Number, Number>> observableList1 = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Series<Number, Number>> observableList2 = FXCollections.observableArrayList();
    private final XYChart.Series<Number, Number> numberSeries1 = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> numberSeries2 = new XYChart.Series<>();

    // DSE Sensor Library
    private SerialSensor serialSensor;
    private DemoSensor demoSensor;

    // Java Flow API
    private Flow.Subscription subscription;

    private String selectedType;
    private String selectedPort;
    private Integer selectedBaud;
    private int pointsCounter = 0;
    private float minimumForRotation = 150f;
    private float maximumForRotation = 50f;

    private int frequencyCounter = 0;
    private int averageCounter = 0;       // We want to have data for avg. before observing x data points
    private int averageOver = 15;         // Size of moving data points to calculate average on
    private int[] movingPointsArray  = new int[averageOver];
    private int movingPointsAvg = 0;
    private int movingPointsMin = 0;
    private int movingPointsMax = 0;
    private int frequency = 0;
    private long lastNanoTime;


    @FXML public void initialize() {
        log.debug("initialize()");

        choiceSensorType.getItems().addAll("16bit", "18bit");
        choiceSensorType.getSelectionModel().select(0);

        choiceSensorBaudRate.getItems().addAll(38400, 115200);
        choiceSensorBaudRate.getSelectionModel().select(0);

        choiceSensorSerialPort.getItems().add("Demo");
        choiceSensorSerialPort.getItems().addAll(SerialSensor.getSerialPorts());
        choiceSensorSerialPort.getSelectionModel().select(0);

        int idx = 0;
        for(String port : choiceSensorSerialPort.getItems()) {
            if(port.equals("ttyUSB0")) {
                choiceSensorSerialPort.getSelectionModel().select(idx);
            }
            idx++;
        }

        avgDisplay.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                avgMeasurement.getEditor().setText(oldValue);
            }
        });

        points.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                points.getEditor().setText(oldValue);
            }
        });

        try {

            numberSeries1.setName("Last Measurement in mm.");
            observableList1.add(numberSeries1);
            dataChart.getData().addAll(observableList1);

            numberSeries2.setName("Moving Average in mm.");
            observableList2.add(numberSeries2);
            dataChart.getData().addAll(observableList2);

        } catch (Exception ignored) {
        }

        // Smaller stroke
        numberSeries1.nodeProperty().get().setStyle("-fx-stroke-width: 2px;");

        // Autostart for demo, if possible
        onButtonStart();
    }


    @FXML private void onSelectModel(ActionEvent ignoredE) {
        //log.info(choiceSensorType.getSelectionModel().getSelectedItem());
        selectedType = choiceSensorType.getSelectionModel().getSelectedItem();
    }


    @FXML private void onSelectPort(ActionEvent ignoredE) {
        //log.info(choiceSensorSerialPort.getSelectionModel().getSelectedItem());
        selectedPort = choiceSensorSerialPort.getSelectionModel().getSelectedItem();
    }


    @FXML private void onSelectBaud(ActionEvent ignoredE) {
        //log.info(String.valueOf(choiceSensorBaudRate.getSelectionModel().getSelectedItem()));
        selectedBaud = choiceSensorBaudRate.getSelectionModel().getSelectedItem();
    }


    @FXML private void onButtonStart() {
        log.debug("onButtonStart()");
        reset();

        if(!Objects.equals(selectedPort, "Demo") && (selectedPort == null || selectedType == null || selectedBaud == null) ) {
            log.warn("onButtonStart() - options missing");
            lastErrorMessage.setText("Missing options");
            return;
        }

        averageOver = avgDisplay.getValue();
        movingPointsArray  = new int[averageOver];

        btnStart.setDisable(true);
        btnStop.setDisable(false);
        lastErrorMessage.setText("");

        if(selectedPort.equals("Demo")) {
            demoSensor = new DemoSensor();
            demoSensor.setTelegramHandler(new TelegramHandler16Bit());
            demoSensor.setAverageOver(avgMeasurement.getValue());
            demoSensor.start();
            demoSensor.subscribe(this);
        } else {
            serialSensor = new SerialSensor();
            serialSensor.setAverageOver(avgMeasurement.getValue());
            switch (selectedType) {
                case "16bit" -> serialSensor.setTelegramHandler(new TelegramHandler16Bit());
                case "18bit" -> serialSensor.setTelegramHandler(new TelegramHandler18Bit());
                default -> log.warn("Unknown sensor type: {}", selectedType);
            }
            serialSensor.openPort(selectedPort, selectedBaud);
            serialSensor.subscribe(this);
        }

    }


    @FXML private void onButtonStop() {
        log.debug("onButtonStop()");
        subscription.cancel();

        if(selectedPort.equals("Demo")) {
            demoSensor.stop();
            demoSensor = null;
        } else {
            serialSensor.stop();
            serialSensor = null;
        }

        btnStart.setDisable(false);
        btnStop.setDisable(true);
    }


    private void reset() {
        numberSeries1.getData().clear();
        numberSeries2.getData().clear();

        pointsCounter = 0;
        averageCounter = 0;
        frequencyCounter = 0;
    }


    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }


    @Override
    public void onNext(Integer measurement) {

        if(measurement < 99) {
            Platform.runLater(() -> lastErrorMessage.setText(TelegramError.getError(measurement)));
            subscription.request(1);
            return;
        }

        frequencyCounter++;
        long elapsedNanos = System. nanoTime() - lastNanoTime;
        if(elapsedNanos > 1000000000) {
            lastNanoTime = System.nanoTime();
            frequency = frequencyCounter;
            frequencyCounter = 0;
        }

        movingPointsArray[averageCounter++] = measurement;
        if(averageCounter >= averageOver) {
            movingPointsAvg = (int) Arrays.stream(movingPointsArray).average().orElse(measurement);
            movingPointsMin = Arrays.stream(movingPointsArray).min().orElse(measurement);
            movingPointsMax = Arrays.stream(movingPointsArray).max().orElse(measurement);
            averageCounter = 0;
        }

        float convertedMeasurement = (float) measurement / 100;
        float convertedAverage = (float) movingPointsAvg / 100;
        float convertedMinimum = (float) movingPointsMin / 100;
        float convertedMaximum = (float) movingPointsMax / 100;

        if(convertedMeasurement < minimumForRotation) minimumForRotation = convertedMeasurement;
        if(convertedMeasurement > maximumForRotation) maximumForRotation = convertedMeasurement;

        Platform.runLater(() -> {

            lastDistanceResult.setText(String.format("%.2f", convertedMeasurement));
            averageDistance.setText(String.format("%.2f", convertedAverage));
            minimumDistance.setText(String.format("%.2f", convertedMinimum));
            maximumDistance.setText(String.format("%.2f", convertedMaximum));
            frequencyLabel.setText(String.format("%d Hz", frequency));

            numberSeries1.getData().add(new XYChart.Data<>(pointsCounter, convertedMeasurement));
            numberSeries2.getData().add(new XYChart.Data<>(pointsCounter, convertedAverage));

            if(numberSeries1.getData().size() > points.getValue()) {
                numberSeries1.getData().remove(0, 5);
            }

            if(numberSeries2.getData().size() > points.getValue()) {
                numberSeries2.getData().remove(0, 5);
            }

            yAxis.setLowerBound(Math.max(minimumForRotation - 25, 0));
            yAxis.setUpperBound(maximumForRotation + 25);

            pointsCounter++;
            if(pointsCounter > points.getValue()) {
                lastErrorMessage.setText("");
                xAxis.autoRangingProperty().set(false);
                xAxis.setUpperBound(pointsCounter);
                pointsCounter = 0;
            }

            subscription.request(1);
        });

    }


    @Override
    public void onError(Throwable throwable) {
        System.err.println(throwable.toString());
        onButtonStop();
    }


    @Override
    public void onComplete() {
        System.out.println("Done.");
    }

}
