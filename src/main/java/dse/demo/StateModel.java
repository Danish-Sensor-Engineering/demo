package dse.demo;

import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class StateModel {

    private int elements = 5000;
    private int elementCounter;

    protected IntegerProperty measurementValue = new SimpleIntegerProperty();
    protected IntegerProperty frequency = new SimpleIntegerProperty();
    protected DoubleDataSet dataSet = new DoubleDataSet("Sensor Data", elements);


    public void setElements(int elements) {
        this.elements = elements;
        dataSet.clearData();
    }

    public void setFrequency(int freq) {
        Platform.runLater(() -> frequency.setValue(freq));
    }

    public void setMeasurement(Integer measurement) {

        dataSet.add(elementCounter, measurement);

        if(elementCounter++ >= elements) {
            elementCounter = 0;
            dataSet.clearData();
        }

        Platform.runLater(() -> {
            this.measurementValue.setValue(measurement);
            /*if(movingAverageCounter++ >= averageOver) {
                movingAverageCounter = 0;
                double[] ds = dataSet.getValues(1);
                this.averageValue.setValue(Arrays.stream(ds).filter(Objects::nonNull).filter(dv -> dv > 0).average().orElse(measurement));
            }*/

        });

    }

}
