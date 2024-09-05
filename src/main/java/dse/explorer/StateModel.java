package dse.explorer;

import java.util.Arrays;

import io.fair_acc.chartfx.utils.FXUtils;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.chart.XYChart;

public class StateModel {

    private static final int MARGINS = 25;
    private int elements = 5000;
    private int elementCounter;
    private double avg;

    private int averageOver = 25;
    private int movingAverageCounter;
    private int[] movingAverageArray;

    protected IntegerProperty measurementValue = new SimpleIntegerProperty();
    protected IntegerProperty averageValue = new SimpleIntegerProperty();
    protected IntegerProperty minimumValue = new SimpleIntegerProperty();
    protected IntegerProperty maximumValue = new SimpleIntegerProperty();
    protected IntegerProperty frequency = new SimpleIntegerProperty();
    protected DoubleProperty lowerBound = new SimpleDoubleProperty();
    protected DoubleProperty upperBound = new SimpleDoubleProperty();
    protected DoubleDataSet dataSet = new DoubleDataSet("Optical Displacement Sensor", elements);


    public void setAverageOver(int averageOver) {
        this.averageOver = averageOver;
        movingAverageCounter = 0;
        movingAverageArray = new int[averageOver];
    }

    public void setElements(int elements) {
        this.elements = elements;
        dataSet.clearData();
    }

    public void setFrequency(int freq) {
        Platform.runLater(() -> {
            frequency.setValue(freq);
        });
    }

    public void setMeasurement(Integer measurement) {

        if(measurement < 1000) {
            System.err.println("LOW VALUE BELOW 100");
        }

        dataSet.add(elementCounter, measurement);
        if(elementCounter++ >= elements) {
            elementCounter = 0;
            dataSet.clearData();
        }

        Platform.runLater(() -> {

            movingAverageArray[movingAverageCounter++] = measurement;
            if(movingAverageCounter >= averageOver) {
                avg = Arrays.stream(movingAverageArray).average().orElse(measurement);
                averageValue.setValue(avg);
                minimumValue.setValue(Arrays.stream(movingAverageArray).min().orElse(measurement));
                maximumValue.setValue(Arrays.stream(movingAverageArray).max().orElse(measurement));
                movingAverageCounter = 0;

                this.measurementValue.setValue(avg);

                if(lowerBound.get() < avg - (MARGINS + 10)) {
                    lowerBound.setValue(avg - MARGINS);
                    System.out.println("Lowerbound adjust");
                }
                if(upperBound.get() < avg + (MARGINS - 10)) {
                    upperBound.setValue(avg + MARGINS);
                    System.out.println("Upperbound adjust");
                }
            }

        });

    }

}
