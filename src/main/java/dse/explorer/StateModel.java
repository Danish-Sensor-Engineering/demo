package dse.explorer;

import java.util.Arrays;

import io.fair_acc.chartfx.utils.FXUtils;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.chart.XYChart;

public class StateModel {

    private static final int MARGINS = 25;
    private int conversion = 100;
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
    protected IntegerProperty lowerBound = new SimpleIntegerProperty();
    protected IntegerProperty upperBound = new SimpleIntegerProperty();

    // https://stackoverflow.com/a/49557072
    protected final XYChart.Series<Number, Number> numberSeries1 = new XYChart.Series<>();
    //protected final XYChart.Series<Number, Number> numberSeries2 = new XYChart.Series<>();
    protected DoubleDataSet dataSet = new DoubleDataSet("data set #1", elements);

    public void setConversion(int conversion) {
        this.conversion = conversion;
    }

    public void setAverageOver(int averageOver) {
        movingAverageCounter = 0;
        this.averageOver = averageOver;
        movingAverageArray = new int[averageOver];
    }

    public void setElements(int elements) {
        this.elements = elements;
        dataSet.resize(elements);

        /*
        for(int i=0; i <= elements; i++) {
            numberSeries1.getData().add(new XYChart.Data<>(i, null));
        }*/
    }

    public void setFrequency(int freq) {
        Platform.runLater(() -> {
            frequency.setValue(freq);
        });
    }

    public void setMeasurement(Integer measurement) {

        double measurementConverted = (double) measurement / conversion;

        Platform.runLater(() -> {

            movingAverageArray[movingAverageCounter++] = measurement;
            if(movingAverageCounter >= averageOver) {
                double avgConverted = avg / conversion;
                avg = Arrays.stream(movingAverageArray).average().orElse(measurement);
                averageValue.setValue(avg);
                minimumValue.setValue(Arrays.stream(movingAverageArray).min().orElse(measurement));
                maximumValue.setValue(Arrays.stream(movingAverageArray).max().orElse(measurement));
                movingAverageCounter = 0;

                this.measurementValue.setValue(avg);

                if(lowerBound.get() < avgConverted - (MARGINS + 10)) {
                    lowerBound.setValue(avgConverted - MARGINS);
                    System.out.println("Lowerbound adjust");
                }
                if(upperBound.get() < avgConverted + (MARGINS - 10)) {
                    upperBound.setValue(avgConverted + MARGINS);
                    System.out.println("Upperbound adjust");
                }
            }

        });

        //numberSeries1.getData().add(new XYChart.Data<>(elementCounter, avg / conversion));

        dataSet.add(elementCounter, measurementConverted);
        System.out.println(dataSet.getDataCount());
        //numberSeries1.getData().set(elementCounter, new XYChart.Data<>(elementCounter, measurementConverted));
        //if(numberSeries1.getData().size() > elements) { numberSeries1.getData().remove(0, 5); }
        //if(numberSeries1.getData().size() > elements) { numberSeries1.getData().remove(0, 5); }
        //numberSeries1.getData().add(new XYChart.Data<>(elementCounter, measurementConverted));

        if(elementCounter++ >= elements) {
            dataSet.trim();
            elementCounter = 0;
        }


        //dataSet.add(elementCounter, elementCounter, measurementConverted);
        //numberSeries1.getData().set(elementCounter, new DoubleDataSet(elementCounter,measurementConverted));
        //numberSeries1.getData().get(elementCounter).setXValue(elementCounter);
        //numberSeries1.getData().get(elementCounter).setYValue(measurementConverted);
    }

}
